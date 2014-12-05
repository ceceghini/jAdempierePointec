package it.pointec.adempiere.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.compiere.model.MPayment;
import org.compiere.util.DB;
import org.compiere.util.Env;

import it.pointec.adempiere.Adempiere;

public class ReopenMultiplePayment {

	public static void main(String[] args) throws SQLException {
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		String sql = "select c_payment_id from c_payment where c_bpartner_id = 1000860";
		PreparedStatement stmt = DB.prepareStatement(sql, null);
		
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			
			MPayment p = new MPayment(Env.getCtx(), rs.getInt(1), null);
			p.processIt("RE");
			
			p.save();
			
		}
		
	}
	
}
