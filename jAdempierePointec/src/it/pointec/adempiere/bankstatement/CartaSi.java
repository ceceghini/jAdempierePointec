package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.io.FileInputStream;
import java.math.BigDecimal;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class CartaSi extends I_BankStatement implements I_Source {

	private final String _subpath = "cartasi";
	private final String _name = "CARTASI [4751]";
	private final int _c_bankaccount_id = 1000007;
	private final String _extension = "pdf";
	
	@Override
	public void insertIntoAdempiere(String file) throws Exception {
		
		I_BankStatement_Line line;
		
		PDFTextStripper pdfStripper = new PDFTextStripper();
	    
		PDFParser parser = new PDFParser(new FileInputStream(file));
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        //pdfStripper = new PDFTextStripper();
        PDDocument pdDoc = new PDDocument(cosDoc);
        String parsedText = pdfStripper.getText(pdDoc);
        
        int n1 = parsedText.indexOf("Data Descrizione Importo in Euro Importo in altre valute Cambio");
        int n2 = parsedText.indexOf("\nTOTALE SPESE ");
        
        cosDoc.close();
        pdDoc.close();
        
        String[] lines = parsedText.substring(n1+64, n2).split("\n");
        String[] items;
		        
        for (String riga:lines) {
        	
        	Util.debug("Elaborazione riga: " + riga);
        	
        	items = riga.split(" ");
        	
        	line = new I_BankStatement_Line();
        	
        	line.set_date(Util.getDate(items[0], "dd/MM/yy"));
			line.set_gross_amount(Util.getImporto(items[items.length-1]).multiply(new BigDecimal(-1)));
			line.set_description(riga);
			
			super.insertLineIntoAdempiere(line);
        	
        }
		
	}

	@Override
	public int get_c_bankaccount_id() {
		return _c_bankaccount_id;
	}

	@Override
	public String get_subpath() {
		return _subpath;
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
	public String get_extension() {
		return _extension;
	}

	@Override
	public String get_dateformat() {
		return "yyyy-MM";
	}
	
}
