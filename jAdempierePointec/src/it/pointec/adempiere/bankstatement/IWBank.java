package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.io.FileReader;
import java.math.BigDecimal;

import au.com.bytecode.opencsv.CSVReader;

public class IWBank extends I_BankStatement implements I_Source {

	private final String _subpath = "iwbank";
	private final String _name = "IWBANK [11768188]";
	private final int _c_bankaccount_id = 999922;
	private final String _extension = "csv";
		
	@Override
	public void insertIntoAdempiere(String file) throws Exception {
		
		CSVReader reader = new CSVReader(new FileReader(file), ';');
		String [] data;
		I_BankStatement_Line line;
		BigDecimal gross_amt;
			    
	    while ((data = reader.readNext()) != null) {
	    	
	    	if (data.length==8) {
	    		
	    		if ( Util.isNumeric(data[0]) ) {
	    			
	    			line = new I_BankStatement_Line();
	    			
	    			line.set_date(Util.getDate(data[1], "dd/MM/yyyy"));
	    			
	    			gross_amt = Util.getImporto(data[5]);
	    			if (data[4].compareTo("-")==0)
	    				gross_amt = gross_amt.multiply(new BigDecimal(-1));
	    			
	    			line.set_gross_amount(gross_amt);
	    			line.set_description(data[3]);
					
	    			super.insertLineIntoAdempiere(line);
						
	    		}
	    		
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
