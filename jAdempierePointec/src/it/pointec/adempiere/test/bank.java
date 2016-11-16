package it.pointec.adempiere.test;




import java.io.File;
import java.text.ParseException;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class bank {
	
	
	public static void main(String[] args) throws ParseException {
		
		Adempiere i = new Adempiere();
		i.inizializza();
		
		String source = "/opt/owncloud/pointec/files/da elaborare/estratti conto/crgiovo/ListaMovimentiCsv07_11_2016_12_25_21.csv";
		
		File f_source = new File(source);
		
		String dest = Util.getArchivio("estratti conto", "2016") + "/crgiovo";
		
		Util.debug("prova");
		
		//Util.moveFile(f_source.getParent(), dest, f_source.getName(), "CRGIOVO [37031] [2016-09]" + "." + "csv");

	}

}
