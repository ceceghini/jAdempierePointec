package it.pointec.adempiere.vendite;

import org.compiere.util.DB;

import it.pointec.adempiere.Adempiere;

public class Delete_I_BPartner {

	public static void main(String[] args) {
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		String sql = "delete from i_bpartner";
		
		DB.executeUpdate(sql, null);
		
	}

}
