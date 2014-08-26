package it.pointec.adempiere;

import it.adempiere.pointec.util.Util;
import it.pointec.adempiere.bankstatement.IWBank;
import it.pointec.adempiere.bankstatement.I_BankStatement;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Adempiere a = new Adempiere();
		a.inizializza();
		
		// IWBank
		I_BankStatement i = new I_BankStatement(new IWBank());
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
	}

}
