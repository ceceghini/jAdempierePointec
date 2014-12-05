package it.pointec.adempiere;

import it.pointec.adempiere.util.Extractor;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.compiere.model.MBankStatement;
import org.compiere.model.MInvoice;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.process.ImportInvoice;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;


public class test {

	public static void main(String[] args) throws SQLException, ParseException {
		// TODO Auto-generated method stub

		File f = new File("/tmp/prova.pdf");
		String parsedText = Util.parsePdf(f);
		
		System.out.println(parsedText);
		
		Extractor ex = new Extractor(parsedText);
		
		// POREFERENCE
		ex.set_pattern("Nr. fattura: (.*)\nData:");
		System.out.println(ex.group(1));
		
		// DATEINVOICED
		ex.set_pattern("Data: (.*)\nCod.");
		System.out.println(Util.getDate(ex.group(1), "dd/MM/yyyy"));
		
		
		// GRANDTOTAL
		ex.set_pattern("Cod. IVA Aliquota Imponibile Imposta Totale\n22 22% € 300.00 € 66.00 € (.*)\nPagina");
		
		System.out.println(ex.group(1));
		
		
	}

}
