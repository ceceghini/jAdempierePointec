package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AmazonOrder {

	private String _order;
	private Date _date;
	private BigDecimal gross_amt = new BigDecimal(0);
	private BigDecimal charge_amt = new BigDecimal(0);
	
	public AmazonOrder(String[] data) throws ParseException {
		_order = data[1];
		
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ITALIAN);
		_date = new Date(dateFormat.parse(data[0]).getTime());
		
		elaboraRiga(data);
		
	}
	
	public void elaboraRiga(String[] data) {
		
		//float imp = Float.parseFloat(data[6].replace("€ ", "").replace(",", "."));
		BigDecimal imp = new BigDecimal(data[6].replace("€", "").replace(",", "."));
		
		if (data[4].compareTo("Costo prodotti")==0 || data[4].compareTo("Altre transazioni")==0) {
			gross_amt = gross_amt.add(imp);
			return;
		}
		
		if (data[4].compareTo("Commissioni Amazon")==0 || data[4].compareTo("Trattenuta spedizione")==0) {
			charge_amt = charge_amt.add(imp);
			return;
		}
		
		Util.addError("Dettaglio pagamento non riconosciuto: ["+data[4]+"]");
		
	}

	public String get_order() {
		return _order;
	}

	public Date get_Date() {
		return _date;
	}

	public BigDecimal getGross_amt() {
		return gross_amt;
	}

	public BigDecimal getCharge_amt() {
		return charge_amt;
	}
	
	public BigDecimal getNet_amt() {
		return gross_amt.subtract(charge_amt);
	}
	
}
