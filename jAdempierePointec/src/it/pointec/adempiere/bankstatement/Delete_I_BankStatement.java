package it.pointec.adempiere.bankstatement;

import org.compiere.util.DB;

import it.pointec.adempiere.Adempiere;

public class Delete_I_BankStatement {

	public static void main(String[] args) {
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		String sql = "delete from i_bankstatement";
		
		DB.executeUpdate(sql, null);
		
	}

}
