package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.compiere.util.DB;

import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.CallerServices;
import com.paypal.soap.api.PaymentTransactionSearchResultType;
import com.paypal.soap.api.TransactionSearchRequestType;
import com.paypal.soap.api.TransactionSearchResponseType;

public class Paypal extends I_BankStatement implements I_Source {

	private final String _name = "PAYPAL";
	private final int _c_bankaccount_id = 999924;
	private final int _c_charge_id = 1000000;
	
	private final String _setAPIUsername = "paypal_api1.pointec.it";
	private final String _setAPIPassword = "RUTSS34WBUDEMCVG";
	private final String _setSignature = "AFcWxV21C7fd0v3bYYYRCpSSRl31A-tsJFJFXFyYS8Lz-7yTxCNY01Yx";
	
	@Override
	public int get_c_bankaccount_id() {
		return _c_bankaccount_id;
	}

	@Override
	public int get_c_charge_id() {
		return _c_charge_id;
	}

	@Override
	public void insertIntoAdempiere(String file) throws Exception {
		
		PreparedStatement stmt = DB.prepareStatement("select max(EFTSTATEMENTLINEDATE) as maxd from c_bankstatementline l join c_bankstatement b on l.C_BANKSTATEMENT_ID = b.C_BANKSTATEMENT_ID and b.c_bankaccount_id = ?", null);
		stmt.setInt(1, _c_bankaccount_id);
		
		ResultSet rs = stmt.executeQuery();
		
		Calendar last = Calendar.getInstance();
		rs.next();
		
		if (rs.getString(1)!=null)
			last.setTime(rs.getDate(1));
		else {
			last.set(Calendar.YEAR, 2013);
			last.set(Calendar.MONTH, Calendar.DECEMBER);
			last.set(Calendar.DAY_OF_MONTH, 31);
		}
		
		Calendar cur = Calendar.getInstance();
		int curMonth = cur.get(Calendar.MONTH);
		
		int lastMonth = last.get(Calendar.MONTH);
		if (lastMonth==11 && curMonth!=11)
			lastMonth=-1;
		
		if (lastMonth==10 && curMonth==0)
			curMonth=12;
		
		if(curMonth-lastMonth<=1)
			return;
		
		Calendar d = Calendar.getInstance();
		d.setTime(last.getTime());
		d.add(Calendar.DAY_OF_MONTH, 1);
		d.set(Calendar.HOUR_OF_DAY, 0);
		d.set(Calendar.MINUTE, 0);
		d.set(Calendar.SECOND, 0);
		d.set(Calendar.MILLISECOND, 0);
		
		Calendar lastDay = Calendar.getInstance();
		lastDay.setTime(d.getTime());
		lastDay.add(Calendar.MONTH, 1);
		lastDay.add(Calendar.DATE, -1);
		
		int lastDay2 = (lastDay.get(Calendar.YEAR) * 10000) + (lastDay.get(Calendar.MONTH) * 100) + (lastDay.get(Calendar.DAY_OF_MONTH));
		int curDay2 = (d.get(Calendar.YEAR) * 10000) + (d.get(Calendar.MONTH) * 100) + (d.get(Calendar.DAY_OF_MONTH));
		
		while (curDay2 <= lastDay2) {
			
			getPaypalAndInsert(d);
			
			d.add(Calendar.DATE, 1);
			
			curDay2 = (d.get(Calendar.YEAR) * 10000) + (d.get(Calendar.MONTH) * 100) + (d.get(Calendar.DAY_OF_MONTH));
		}
		
		stmt.close();
		
	}
	
	private void getPaypalAndInsert(Calendar d) throws PayPalException, SQLException {
		
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
		Util.debug("Elaborazione paypal data: " + formatter.format(d.getTime()) + "]");
		
		CallerServices caller = new CallerServices();
		APIProfile profile = ProfileFactory.createSignatureAPIProfile();
		
		profile.setAPIUsername(_setAPIUsername);
		profile.setAPIPassword(_setAPIPassword);
    	profile.setSignature(_setSignature);
    	profile.setEnvironment("live");
    	caller.setAPIProfile(profile);
    	
    	TransactionSearchRequestType request = new TransactionSearchRequestType();
    	
    	request.setStartDate(d);
    	Calendar end = Calendar.getInstance();
    	end.setTime(d.getTime());
    	end.add(Calendar.DATE, 1);
    	request.setEndDate(end);
		
    	TransactionSearchResponseType response =
				(TransactionSearchResponseType) caller.call("TransactionSearch", request);
		
    	
		PaymentTransactionSearchResultType[] ts = response.getPaymentTransactions();
		
		if (ts != null)
		{
			
			// Display the results of the first transaction returned
			for (int i = 0; i < ts.length; i++)
			{
				
				insertPaypal(ts[i]);
				
				
			}
		}
		else
		{
			System.out.println("Found 0 transaction [" + d.get(Calendar.DAY_OF_MONTH) + "/" + d.get(Calendar.MONTH) + "/" + d.get(Calendar.YEAR)+"]");
		}
		
		Util.debug("Fine elaborazione paypal data: " + formatter.format(d.getTime()) + "]");
		
	}
	
	private void insertPaypal(PaymentTransactionSearchResultType ts) throws SQLException {
		
		if (!(ts.getStatus().compareTo("Completed")==0 || ts.getStatus().compareTo("Cleared")==0 || ts.getStatus().compareTo("Refunded")==0))
			return;
		
		if (!(ts.getType().compareTo("Payment")==0 || ts.getType().compareTo("Recurring Payment")==0 || ts.getType().compareTo("Refound")==0 || ts.getType().compareTo("Purchase")==0)) 
			return;
		
		if (ts.getGrossAmount()==null)
			return;
		
		Util.debug("Elaborazione riga [" + ts.getTransactionID() + "]");
		
		I_BankStatement_Line line = new I_BankStatement_Line();
		
		line.set_date(new Date(ts.getTimestamp().getTimeInMillis()));
		line.set_trxid(ts.getTransactionID());
		line.set_gross_amount(new BigDecimal(ts.getGrossAmount().get_value()));
		line.set_charge_amount(new BigDecimal(ts.getFeeAmount().get_value()));
		line.set_description(ts.getPayerDisplayName() + " - " + ts.getStatus() + " - " + ts.getType() + " - " + ts.getPayer());
		
		super.insertLineIntoAdempiere(line);
		
		Util.debug("Fine elaborazione riga [" + ts.getTransactionID() + "]");
		
	}

	public void importIntoAdempiere() {
		
		try {
			
			// Verifica se ci sono dati nella tabella di importazione
			int n = DB.getSQLValue(null, "select count(*) n from i_bankstatement");
			
			if (n>0) {
				Util.addError("Record esistenti nella tabella i_bankstatement");
				return;
			}
			
			super.initialize();
			insertIntoAdempiere("");
				
			Util.printErrorAndExit();
				
			super.process();
				
			Util.printErrorAndExit();
				
			

		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}

	@Override
	public String get_subpath() {
		return null;
	}

	@Override
	public String get_extension() {
		return null;
	}

	@Override
	public String get_name() {
		return _name;
	}

	@Override
	public String get_dateformat() {
		return "yyyy-MM";
	}
	
}
