package it.pointec.adempiere.archive;

import it.pointec.adempiere.Adempiere;

public class CloseEsercizi {

	public static void main(String[] args) {
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		Esercizi e = new Esercizi();
		e.CloseYear(2015);

	}

}
