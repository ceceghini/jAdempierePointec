package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.compiere.util.DB;

import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.CallerServices;
import com.paypal.soap.api.PaymentTransactionSearchResultType;
import com.paypal.soap.api.TransactionSearchRequestType;
import com.paypal.soap.api.TransactionSearchResponseType;

public class Paypal_Base extends I_BankStatement implements I_Source {

	private String _name;
	private int _c_bankaccount_id;
	private int _c_charge_id;
	
	private String _setAPIUsername;
	private String _setAPIPassword;
	private String _setSignature;
	
	protected Paypal_Base(String name, int c_bankaccount_id, int c_charge_id, String setAPIUsername, String setAPIPassword, String setSignature) {
	
		_name = name;
		_c_bankaccount_id = c_bankaccount_id;
		_c_charge_id = c_charge_id;
		_setAPIUsername = setAPIUsername;
		_setAPIPassword = setAPIPassword;
		_setSignature = setSignature;
	
	}
		
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
		
		I_BankStatement_Line line;
						
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
				
				if (ts[i].getStatus().compareTo("Canceled")!=0 && ts[i].getType().compareTo("Fee Reversal")!=0 && ts[i].getType().compareTo("Bill")!=0 && ts[i].getStatus().compareTo("Removed")!=0 && ts[i].getType().compareTo("Authorization")!=0) {
				
					line = new I_BankStatement_Line();
					
					line.set_date(new Date(ts[i].getTimestamp().getTimeInMillis()));
					line.set_trxid(ts[i].getTransactionID());
					line.set_gross_amount(new BigDecimal(ts[i].getGrossAmount().get_value()));
					line.set_charge_amount(new BigDecimal(ts[i].getFeeAmount().get_value()));
					line.set_description(ts[i].getPayerDisplayName() + " - " + ts[i].getStatus() + " - " + ts[i].getType() + " - " + ts[i].getPayer());
					
					super.insertLineIntoAdempiere(line);
					
				}
				
				
			}
		}
		else
		{
			System.out.println("Found 0 transaction [" + d.get(Calendar.DAY_OF_MONTH) + "/" + d.get(Calendar.MONTH) + "/" + d.get(Calendar.YEAR)+"]");
		}
		
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
