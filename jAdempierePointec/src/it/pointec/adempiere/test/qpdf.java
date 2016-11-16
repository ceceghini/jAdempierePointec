package it.pointec.adempiere.test;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;

import it.pointec.adempiere.util.Extractor;
import it.pointec.adempiere.util.Util;

public class qpdf {

	public static void main(String[] args) throws IOException, DocumentException, COSVisitorException {
	
		File f = new File("/home/ceceghini/Downloads/fattura_687252_16.pdf");
		
		String parsedText = Util.parsePdf(f);
		
		System.out.println(parsedText);
		
		Extractor ex = new Extractor(parsedText);
		
		ex.set_pattern("FATTURA DI VENDITA N. (.*) DEL (.*) PAGINA");
		System.out.println(ex.group(1));
		
		System.out.println(ex.group(2));
		
		ex.set_pattern("TOTALE\n(.*) (.*) (.*) (.*) (.*) (.*) (.*) (.*)\n");
		System.out.println(ex.group(8));
	
	}

}
