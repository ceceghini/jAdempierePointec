package it.pointec.adempiere.test;

import java.io.File;

import it.pointec.adempiere.util.Extractor;
import it.pointec.adempiere.util.Util;

public class ParsePdf {

	public static void main(String[] args) {
		
		File f = new File("/home/ceceghini/Downloads/prova.pdf");
		String parsedText = Util.parsePdf(f);
		
		System.out.println(parsedText);
		
		Extractor ex = new Extractor(parsedText);
		ex.set_pattern("Partita IVA Numero doc.\n(.*)\nData");
		
		System.out.println(ex.group(1));
		System.out.println(ex.group(2));

		ex.set_pattern("Aliquota IVA 22%\n€ (.*)\nUnicredit");
		
		System.out.println(ex.group(1));
		
		
	}

}
