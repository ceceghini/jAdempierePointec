package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.io.FileReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;

import au.com.bytecode.opencsv.CSVReader;

public class Sda implements I_I_BankStatement_Source {

	private final String _subpath = "sda";
	private final String _name = "SDA";
	private final int _c_bankaccount_id = 999926;
	
	@Override
	public int get_c_bankaccount_id() {
		return _c_bankaccount_id;
	}

	@Override
	public int get_c_charge_id() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean has_c_charge_id() {
		// TODO Auto-generated method stub
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

	@Override
	public String get_subpath() {
		return _subpath;
	}

	@Override
	public String insertIntoAdempiere(String file, I_BankStatement bs) throws Exception {
		
		CSVReader reader = new CSVReader(new FileReader(file), ';');
		String[] data;
		I_BankStatement_Line line;
		ArrayList<I_BankStatement_Line> lines = new ArrayList<I_BankStatement_Line>();
		
		Date first = null;
		
		while ((data = reader.readNext()) != null) {
			
			if (data[0].compareTo("LDV")!=0 && data[5].compareTo("Assegno bancario intestato al mittente")!=0) {
			
				line = new I_BankStatement_Line();
				
				line.set_date(Util.getDate(data[2], "yyyyMMdd"));
				line.set_gross_amount(Util.getImporto(data[4]));
				line.set_description(data[3] + " - " + data[5]);
				lines.add(line);
				
				// Date
				if (first==null)
					first=line.get_date();
				
			}
			
		}
		
		reader.close();
		
		String bs_name = _name + " [" + first + "]";
		
		Iterator<I_BankStatement_Line> it = lines.iterator();
		while (it.hasNext()) {
			
			line = it.next();
			
			bs.insertLineIntoAdempiere(line, bs_name);
			
		}
		
		return bs_name;
		
	}

}
