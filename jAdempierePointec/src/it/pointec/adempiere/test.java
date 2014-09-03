package it.pointec.adempiere;

import it.pointec.adempiere.bankstatement.IWBank;
import it.pointec.adempiere.bankstatement.I_BankStatement;
import it.pointec.adempiere.util.Util;

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
