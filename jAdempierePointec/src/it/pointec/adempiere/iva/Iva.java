package it.pointec.adempiere.iva;

import java.sql.PreparedStatement;

import org.compiere.util.DB;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class Iva {

	public static void main(String[] args) {
		
		Adempiere a = new Adempiere();
		a.inizializza();

	}
	
	public static void elabora() {
		
		updateVatLedgerDate();
		Util.printErrorAndExit();
		
	}
	
	private static void updateVatLedgerDate() {
		
		try {
			
			String sql = "update c_invoice set VATLEDGERDATE = DATEACCT where DOCSTATUS = 'CO' and VATLEDGERNO is null and ad_cliente_id = ? and ad_org_id = ?";
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}

}
