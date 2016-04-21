package it.pointec.adempiere.acquisti;

import java.io.File;

import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class CheckFattureAcquisto {

	public static void main(String[] args) {
		
		Ini.loadPropery();
		
		elaboraDirectory(1000005);	// Fatture acquisti
		
		elaboraDirectory(999775);	// Fatture intra

	}
	
	private static void elaboraDirectory(int c_doctype_id) {
		
		File folder = new File(Util.getDaElaborare(c_doctype_id));
		
		File[] listOfFiles = folder.listFiles();
		
		for (File d : listOfFiles) {
			
			elaboraFornitore(d);
				
			
		}
		
	}
	
	private static void elaboraFornitore(File d) {
		
		File[] listOfFiles = d.listFiles();
				
		for (File f : listOfFiles) {
					
			System.out.println(f.getPath());
				
		}
		
	}

}
