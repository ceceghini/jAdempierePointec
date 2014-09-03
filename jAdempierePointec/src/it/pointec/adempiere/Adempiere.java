package it.pointec.adempiere;

import java.sql.SQLException;

import it.pointec.adempiere.util.Ini;

import org.compiere.util.CLogMgt;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class Adempiere {
	
	public void inizializza() {
		
		// Caricamento property
		Ini.loadPropery();
		
		// Inizializzazione adempiere
		org.compiere.util.Ini.setClient (false);		//	init logging in Ini
		org.compiere.util.Ini.loadProperties(Ini.getString("adempiere_properties"));
		//  System properties
		CLogMgt.setLevel(org.compiere.util.Ini.getProperty(org.compiere.util.Ini.P_TRACELEVEL));
		org.compiere.Adempiere.startupEnvironment(false);
		
		Env.getCtx().setProperty("#AD_Client_ID", Integer.toString(Ini.getInt("ad_client_id")));
		Env.getCtx().setProperty("#AD_Org_ID", Integer.toString(Ini.getInt("ad_org_id")));
		
	}
	
	protected void finalize () throws SQLException {
		DB.closeTarget();
	}
	
	
	
}
