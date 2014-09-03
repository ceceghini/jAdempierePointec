package it.pointec.adempiere.vendite;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Util;

public class ImportFattureVendita {
	
	
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		elabora();
		
	}

	public static void elabora() {
		
		// Fatture
		System.out.println("INIZIO ELABORAZIONE INVOICE");
		I_Invoice o = new I_Invoice("invoice");
		
		o.initialCheck();
		Util.printErrorAndExit();
		
		o.downloadOrder();
		Util.printErrorAndExit();
		
		o.Check();
		Util.printErrorAndExit();
		
		o.importAndProcess();
		
		// Note di credito
		System.out.println("INIZIO ELABORAZIONE CREDITMEMO");
		o = new I_Invoice("creditmemo");
		
		o.initialCheck();
		Util.printErrorAndExit();
		
		o.downloadOrder();
		Util.printErrorAndExit();
		
		o.Check();
		Util.printErrorAndExit();
		
		o.importAndProcess();
		
	}

}
