package it.pointec.adempiere.test;

import java.util.Calendar;

import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.CallerServices;
import com.paypal.soap.api.PaymentTransactionSearchResultType;
import com.paypal.soap.api.TransactionSearchRequestType;
import com.paypal.soap.api.TransactionSearchResponseType;

public class bank {
	
	private final static String _setAPIUsername = "paypal_api1.pointec.it";
	private final static String _setAPIPassword = "RUTSS34WBUDEMCVG";
	private final static String _setSignature = "AFcWxV21C7fd0v3bYYYRCpSSRl31A-tsJFJFXFyYS8Lz-7yTxCNY01Yx";

	public static void main(String[] args) throws PayPalException {
		
		CallerServices caller = new CallerServices();
		APIProfile profile = ProfileFactory.createSignatureAPIProfile();
		
		profile.setAPIUsername(_setAPIUsername);
		profile.setAPIPassword(_setAPIPassword);
    	profile.setSignature(_setSignature);
    	profile.setEnvironment("live");
    	caller.setAPIProfile(profile);
    	
    	TransactionSearchRequestType request = new TransactionSearchRequestType();
    	
    	Calendar d = Calendar.getInstance();
    	d.set(Calendar.YEAR, 2016);
		d.set(Calendar.MONTH, Calendar.MARCH);
		d.set(Calendar.DAY_OF_MONTH, 9);
		d.set(Calendar.HOUR_OF_DAY, 0);
		d.set(Calendar.MINUTE, 0);
		d.set(Calendar.SECOND, 0);
		d.set(Calendar.MILLISECOND, 0);
    	
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
			for (int i = 0; i < ts.length; i++)
			{
				if (ts[i].getGrossAmount() != null) {
				System.out.println(ts[i].getTransactionID());
				System.out.println(ts[i].getTimestamp().getTime());
				System.out.println(ts[i].getPayerDisplayName());
				System.out.println(ts[i].getPayer());
				System.out.println(ts[i].getType());
				System.out.println(ts[i].getGrossAmount().get_value());
				System.out.println("");
				}
			}
		}

	}

}
