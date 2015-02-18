package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import au.com.bytecode.opencsv.CSVReader;

public class Sda extends I_BankStatement implements I_Source {

	private final String _subpath = "sda";
	private final String _name = "SDA";
	private final int _c_bankaccount_id = 999926;
	private final String _extension = "csv";
	
	@Override
	public int get_c_bankaccount_id() {
		return _c_bankaccount_id;
	}

	@Override
	public int get_c_charge_id() {
		return 0;
	}

	@Override
	public String get_name() {
		return _name;
	}

	@Override
	public String get_subpath() {
		return _subpath;
	}

	@Override
	public void insertIntoAdempiere(String file) throws Exception {
		
		CSVReader reader = new CSVReader(new FileReader(file), ';');
		String[] data;
		I_BankStatement_Line line;
		ArrayList<I_BankStatement_Line> lines = new ArrayList<I_BankStatement_Line>();
				
		while ((data = reader.readNext()) != null) {
			
			if (data.length == 8) {
				if (data[6].compareTo("CON")==0 || data[6].compareTo("ABS")==0) {
				
					line = new I_BankStatement_Line();
					
					line.set_date(Util.getDate(data[3], "dd/MM/yyyy"));
					line.set_gross_amount(Util.getImporto(data[5]));
					line.set_description("LDV ["+ data[0] +"] ORDINE ["+ data[1] +"] NOMINATIVO ["+ data[4] +"]");
					lines.add(line);
					
				}
			}
		}
		
		reader.close();
		
		Iterator<I_BankStatement_Line> it = lines.iterator();
		while (it.hasNext()) {
			
			line = it.next();
			
			super.insertLineIntoAdempiere(line);
			
		}
		
	}

	@Override
	public String get_extension() {
		return _extension;
	}

	@Override
	public String get_dateformat() {
		return "yyyy-MM-dd";
	}	

}
