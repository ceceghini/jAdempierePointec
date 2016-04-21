package it.pointec.adempiere.vendite;

import it.pointec.adempiere.Adempiere;

import org.compiere.util.DB;

public class Delete_I_Invoice {

	public static void main(String[] args) {
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		String sql = "delete from i_invoice";
		
		DB.executeUpdate(sql, null);
	}

}
