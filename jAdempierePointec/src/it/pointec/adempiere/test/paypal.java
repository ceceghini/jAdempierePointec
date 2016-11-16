package it.pointec.adempiere.test;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.CallerServices;
import com.paypal.soap.api.PaymentTransactionSearchResultType;
import com.paypal.soap.api.TransactionSearchRequestType;
import com.paypal.soap.api.TransactionSearchResponseType;

import it.pointec.adempiere.util.Util;

public class paypal {
	
	private final String _setAPIUsername = "paypal_api1.pointec.it";
	private final String _setAPIPassword = "RUTSS34WBUDEMCVG";
	private final String _setSignature = "AFcWxV21C7fd0v3bYYYRCpSSRl31A-tsJFJFXFyYS8Lz-7yTxCNY01Yx";

	public static void main(String[] args) throws PayPalException, SQLException {
		// TODO Auto-generated method stub

		Calendar last = Calendar.getInstance();
		last.set(Calendar.YEAR, 2016);
		last.set(Calendar.MONTH, Calendar.SEPTEMBER);
		last.set(Calendar.DAY_OF_MONTH, 0);
		
		paypal p = new paypal();
		
		p.getPaypalAndInsert(last);				// 01
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 02
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 03
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 04
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 05
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 06
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 07
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 08
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 09
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 10
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 11
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 12
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 13
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 14
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 15
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 16
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 17
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 18
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 19
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 20
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 21
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 22
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 23
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 24
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 25
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 26
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 27
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 28
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 29
		last.add(Calendar.DAY_OF_MONTH, 1);
		p.getPaypalAndInsert(last);				// 30
		
	}
	
	public void getPaypalAndInsert(Calendar d) throws PayPalException, SQLException {
		
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
				
				this.insertPaypal(ts[i]);
				
				
			}
		}
		else
		{
			System.out.println("Found 0 transaction [" + d.get(Calendar.DAY_OF_MONTH) + "/" + d.get(Calendar.MONTH) + "/" + d.get(Calendar.YEAR)+"]");
		}
		
		Util.debug("Fine elaborazione paypal data: " + formatter.format(d.getTime()) + "]");
		
	}
	
	private void insertPaypal(PaymentTransactionSearchResultType ts) {
		
		if (ts.getTransactionID().compareTo("3RY406815R367714P")==0) {
			System.out.println(ts.getStatus());
			System.out.println(ts.getType());
		}
		else
			return;
		
		if (!(ts.getStatus().compareTo("Completed")==0 || ts.getStatus().compareTo("Cleared")==0 || ts.getStatus().compareTo("Refunded")==0))
			return;
		
		if (!(ts.getType().compareTo("Payment")==0 || ts.getType().compareTo("Recurring Payment")==0 || ts.getType().compareTo("Refund")==0 || ts.getType().compareTo("Purchase")==0))
			return;
		
		System.out.println( 'a');
		
		
		/*if (ts.getGrossAmount()==null)
			return;*/
		
		//System.out.println(ts.getTransactionID());
		
	}

}
