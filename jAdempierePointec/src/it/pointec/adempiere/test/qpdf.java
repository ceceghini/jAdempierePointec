package it.pointec.adempiere.test;

import java.io.IOException;

import com.lowagie.text.pdf.PdfReader;

import it.pointec.adempiere.util.Util;

public class qpdf {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String source = "/tmp/a";
		String nomeFileSource = "prova.pdf";
		
		PdfReader pdf = new PdfReader(source + "/" + nomeFileSource);
		if (pdf.isEncrypted()) {
			Util.moveFile(source, "/tmp", nomeFileSource, "tmp.pdf");
			String c = "qpdf --decrypt /tmp/tmp.pdf "+source + "/" + nomeFileSource+"";
			System.out.println(c);
			//String c = "qpdf --decrypt /tmp/a/prova.pdf /tmp/a/porvab.pdf";
			Process child = Runtime.getRuntime().exec(c);
			System.out.println("Decriptato");
		}
		
	}

}
