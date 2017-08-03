package it.pointec.adempiere;

import it.pointec.adempiere.util.Util;

public class Import_Acquisti {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		Import_Vendite.processInvoice();
		
		Util.printErrorAndExit();
		
	}

}
