package it.pointec.adempiere.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.compiere.model.MBankStatement;
import org.compiere.util.DB;
import org.compiere.util.Env;

import it.pointec.adempiere.Adempiere;

public class ReopenMultipleBankStatement {

	public static void main(String[] args) throws SQLException {
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		String sql = "select C_BANKSTATEMENT_ID from C_BANKSTATEMENT where docstatus <> 'CO' and C_BANKSTATEMENT_ID in( select distinct C_BANKSTATEMENTLINE.C_BANKSTATEMENT_ID from C_BANKSTATEMENTLINE where c_payment_id is null and C_CHARGE_ID is null and ad_client_id = 1000002)";
		PreparedStatement stmt = DB.prepareStatement(sql, null);
		
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			
			MBankStatement p = new MBankStatement(Env.getCtx(), rs.getInt(1), null);
			p.processIt("RE");
			
			p.save();
			
		}
		
	}
	
}
