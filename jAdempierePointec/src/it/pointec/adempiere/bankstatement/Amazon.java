package it.pointec.adempiere.bankstatement;

import java.io.FileReader;
import java.sql.Date;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

public class Amazon implements I_I_BankStatement_Source {

	private final String _subpath = "amazon";
	private final String _name = "AMAZON";
	private final int _c_bankaccount_id = 1000001;
	
	@Override
	public int get_c_bankaccount_id() {
		return _c_bankaccount_id;
	}

	@Override
	public int get_c_charge_id() {
		return 1000001;
	}

	@Override
	public boolean has_c_charge_id() {
		return true;
	}

	@Override
	public String get_name() {
		return _name;
	}

	@Override
	public boolean is_from_file() {
		return true;
	}

	@Override
	public String get_subpath() {
		return _subpath;
	}

	@Override
	public String insertIntoAdempiere(String file, I_BankStatement bs) throws Exception {
		
		CSVReader reader = new CSVReader(new FileReader(file), '\t');
		String [] data;
		I_BankStatement_Line line;
		AmazonOrder o;
		HashMap<String, AmazonOrder> h = new HashMap<String, AmazonOrder>();
		
		Date first = null;
		Date last = null;
		
		while ((data = reader.readNext()) != null) {
			
			if (data.length==9 && data[0].compareTo("Data")!=0) {
				
				if (data[1].compareTo("")!=0) {
					
					if (h.containsKey(data[1])) {
						o = h.get(data[1]);
						o.elaboraRiga(data);
					}
					else {
						o = new AmazonOrder(data);
						h.put(data[1], o);
					}
					
					// Date
					if (first==null)
						first=o.get_Date();
					else {
						if (first.compareTo(o.get_Date())>0)
							first=o.get_Date();
					}
					
					if (last==null)
						last=o.get_Date();
					else {
						if (last.compareTo(o.get_Date())<0)
							last=o.get_Date();
					}
					
				}
				
			}
			
		}
		
		reader.close();
		
		String bs_name = _name + "[" + first + " - " + last + "]";
		
		// Loop tra gli ordini amazon appena elaborati
		for(String k : h.keySet()){
			
			o = h.get(k);
			
			line = new I_BankStatement_Line();
			line.set_date(o.get_Date());
			line.set_description(o.get_order());
			line.set_charge_amount(o.getCharge_amt());
			line.set_gross_amount(o.getGross_amt());
			line.set_trxid(o.get_order());
			
			bs.insertLineIntoAdempiere(line, bs_name);
			
		}
		
		return bs_name;
		
	}

}
