package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.io.FileReader;
import java.math.BigDecimal;

import au.com.bytecode.opencsv.CSVReader;

public class CRGiovo extends I_BankStatement implements I_Source {

	private final String _subpath = "crgiovo";
	private final String _name = "CRGIOVO [37031]";
	private final int _c_bankaccount_id = 999917;
	private final String _extension = "csv";
		
	@Override
	public void insertIntoAdempiere(String file) throws Exception {
		
		CSVReader reader = new CSVReader(new FileReader(file), ';');
		String [] data;
		I_BankStatement_Line line;
		BigDecimal gross_amt;
				
		while ((data = reader.readNext()) != null) {
	    	
    		if (data[0].compareTo("DATA")!=0 && data[1].compareTo("")!=0) {
    			
    			line = new I_BankStatement_Line();
    			
    			line.set_date(Util.getDate(data[0], "dd/MM/yyyy"));
    			
    			if (data[2].compareTo("")==0) {
    				gross_amt = Util.getImporto(data[3]);
    			}
    			else
    				gross_amt = Util.getImporto(data[2]).multiply(new BigDecimal(-1));
    			
    			line.set_gross_amount(gross_amt);
    			line.set_description(data[5]);
    			
    			super.insertLineIntoAdempiere(line);
    			
    		}	    	

	      }
	      reader.close();
		
	}

	@Override
	public int get_c_bankaccount_id() {
		return _c_bankaccount_id;
	}

	@Override
	public String get_subpath() {
		return _subpath;
	}
	
	@Override
	public int get_c_charge_id() {
		return 0;
	}
	
	@Override
	public String get_extension() {
		return _extension;
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
