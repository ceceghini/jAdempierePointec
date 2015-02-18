package it.pointec.adempiere.bankstatement;

import java.io.FileReader;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

public class Amazon extends I_BankStatement implements I_Source {

	private final String _subpath = "amazon";
	private final String _name = "AMAZON";
	private final int _c_bankaccount_id = 1000001;
	private final int _c_charge_id = 1000001;
	private final String _extension = ".csv";
	
	@Override
	public int get_c_bankaccount_id() {
		return _c_bankaccount_id;
	}

	@Override
	public int get_c_charge_id() {
		return _c_charge_id;
	}

	@Override
	public String get_subpath() {
		return _subpath;
	}
	
	@Override
	public void insertIntoAdempiere(String file) throws Exception {
		
		CSVReader reader = new CSVReader(new FileReader(file), '\t');
		String [] data;
		AmazonOrder o;
		HashMap<String, AmazonOrder> h = new HashMap<String, AmazonOrder>();
		
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
					
				}
				
			}
			
		}
		
		reader.close();
		
		I_BankStatement_Line line;
		
		// Loop tra gli ordini amazon appena elaborati
		for(String k : h.keySet()){
			
			o = h.get(k);
			
			line = new I_BankStatement_Line();
			line.set_date(o.get_Date());
			line.set_description(o.get_order());
			line.set_charge_amount(o.getCharge_amt());
			line.set_gross_amount(o.getGross_amt());
			line.set_trxid(o.get_order());
			
			super.insertLineIntoAdempiere(line);
			
		}
		
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
