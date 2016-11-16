package it.pointec.adempiere.vendite;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Util;

public class GenerateInvoice {

	/**
	 * Metodo statico per l'esecuzione
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		PrintInvoice p = new PrintInvoice();
	
		p.generateInvoice("doc_type_id_invoice");
		p.generateInvoice("doc_type_id_creditmemo");
		p.generateInvoice("doc_type_id_invoice_std");
				
		Util.printErrorAndExit();

	}
	
}
