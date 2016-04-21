package it.pointec.adempiere.test;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Util;
import it.pointec.adempiere.vendite.PrintInvoice;

public class SendSingleInvoice {

	/**
	 * Metodo statico per l'esecuzione
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		PrintInvoice p = new PrintInvoice();
		p.sendInvoiceEmailSingle();
				
		Util.printErrorAndExit();

	}
	
}
