package it.pointec.adempiere.bankstatement;

import it.adempiere.pointec.util.Util;

import java.io.FileReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import au.com.bytecode.opencsv.CSVReader;

public class CRGiovo implements I_I_BankStatement_Source {

	private final String _subpath = "crgiovo";
	private final String _name = "CRGIOVO [37031]";
	private final int _c_bankaccount_id = 999917;
		
	@Override
	public String insertIntoAdempiere(String file, I_BankStatement bs) throws Exception {
		
		CSVReader reader = new CSVReader(new FileReader(file), ';');
		String [] data;
		I_BankStatement_Line line;
		BigDecimal gross_amt;
		String bs_name = null;
		
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
    			
    			if (bs_name==null) {
    				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
    				bs_name = _name + " [" + dateFormat.format(line.get_date()) + "]";
    			}
    			
    			bs.insertLineIntoAdempiere(line, bs_name);
    			
    		}	    	

	      }
	      reader.close( );
	      return bs_name;
		
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
	public boolean has_c_charge_id() {
		return false;
	}

	@Override
	public String get_name() {
		return _name;
	}

	@Override
	public boolean is_from_file() {
		return true;
	}

}
