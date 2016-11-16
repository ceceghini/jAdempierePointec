package it.pointec.adempiere.test;

import java.io.File;

import it.pointec.adempiere.util.Extractor;
import it.pointec.adempiere.util.Util;

public class ParsePdf {

	public static void main(String[] args) {
		
		File f = new File("/home/ceceghini/Downloads/SMBIT201610000984.pdf");
		String parsedText = Util.parsePdf(f);
		
		System.out.println(parsedText);
		
		return;
		
		/*Extractor ex = new Extractor(parsedText);
		ex.set_pattern("quietanzata: (.*)\nData: (.*)\nOrdine:");
		
		System.out.println(ex.group(1));
		System.out.println(ex.group(2));

		ex.set_pattern("TOTALE IVA INCLUSA (.*)");
		
		System.out.println(ex.group(1));*/
		
		
	}

}
