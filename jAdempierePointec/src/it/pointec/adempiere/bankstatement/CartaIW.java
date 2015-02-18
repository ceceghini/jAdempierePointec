package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.util.Util;

import java.io.FileInputStream;
import java.math.BigDecimal;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class CartaIW extends I_BankStatement implements I_Source {

	private final String _subpath = "cartaiw";
	private final String _name = "CARTAIW [5672]";
	private final int _c_bankaccount_id = 1000000;
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
        
        int n1 = parsedText.indexOf("Data operazione Data registrazione Descrizione Importo in Euro Importo valuta originale");
        int n2 = parsedText.indexOf("\nDescrizione Importo in Euro");
        
        cosDoc.close();
        pdDoc.close();
        
        String[] lines = parsedText.substring(n1+88, n2).split("\n");
        String[] items;
		        
        for (String riga:lines) {
        	
        	items = riga.split(" ");
        	
        	line = new I_BankStatement_Line();
        	
        	line.set_date(Util.getDate(items[1], "dd/MM/yyyy"));
			line.set_gross_amount(Util.getImporto(items[items.length-1]).multiply(new BigDecimal(-1)));
			line.set_description(riga.substring(22, riga.length()).replaceAll(items[items.length-1], "").trim());
			
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
