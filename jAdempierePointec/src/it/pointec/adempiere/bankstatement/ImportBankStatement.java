package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Util;

public class ImportBankStatement {
	
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		ImportBankStatement b = new ImportBankStatement();
		
		b.Import();
		
		Riconcilia.elabora();
		Util.printErrorAndExit();
				
	}
	
	/***
	 * Import degli estratti conti bancari
	 */
	private void Import() {
		
		I_BankStatement i;
		// CRGiovo
		i = new I_BankStatement(new CRGiovo());
		
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// IWBank
		i = new I_BankStatement(new IWBank());
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// CartaIW
		i = new I_BankStatement(new CartaIW());
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// Paypal
		i = new I_BankStatement(new Paypal());
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// Sda
		i = new I_BankStatement(new Sda());
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// Amazon
		i = new I_BankStatement(new Amazon());
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
	}
	
}
