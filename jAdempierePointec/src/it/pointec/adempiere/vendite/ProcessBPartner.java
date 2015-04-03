package it.pointec.adempiere.vendite;

import it.pointec.adempiere.model.XMLBPartner;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.process.ImportBPartner;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

/**
 * Elaborazione dei bpartner provenienti dal file xml, inserimento nella tabella i_bpartner
 * e successivo import nella tabella c_bpartner
 * @author cesare
 *
 */
public class ProcessBPartner {

	private PreparedStatement _stmt;
	private Hashtable<String, XMLBPartner> _bpartners = new Hashtable<String, XMLBPartner>();	
	
	public ProcessBPartner() {
		
		try {
			_stmt = DB.prepareStatement("insert into I_BPARTNER (ad_org_id, ad_client_id, i_bpartner_id, value, name, taxid, c_bp_group_id, address1, postal, city, regionname, countrycode, contactname, phone, email, iscustomer, isvendor, ad_language, fiscalcode) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", null);
			_stmt.setInt(1, Ini.getInt("ad_org_id"));
			_stmt.setInt(2, Ini.getInt("ad_client_id"));
			_stmt.setInt(7, Ini.getInt("c_bp_group_id"));
			_stmt.setString(12, "IT");
			_stmt.setString(16, "Y");
			_stmt.setString(17, "N");
			_stmt.setString(18, "it_IT");
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	protected void finalize () throws SQLException {
		_stmt.close();
	}
	
	/**
	 * Chiamata al processo adempiere per importare i dati nella tabella c_bpartner
	 */
	public void process() {
		
		try {
			
			String trxName = "processBPartner";
			
			int AD_Process_ID =  MProcess.getProcess_ID("Import_BPartner", trxName);
			ProcessInfo pi = new ProcessInfo ("Import_BPartner",AD_Process_ID);
			
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
	        instance.saveEx();
	        
	        pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
	        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
	
	        MPInstancePara para20 = new MPInstancePara(instance, 20);
	        para20.setParameter("AD_Client_ID", Ini.getInt("ad_client_id"));
	        para20.saveEx();
	        
	        ImportBPartner process = new ImportBPartner();
	        
	        process.startProcess(Env.getCtx(), pi, Trx.get(trxName, false));     
	
	        String sql = "select i_bpartner_id, i_errormsg, value  from i_bpartner where i_isimported <> 'Y'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			
	        while (rs.next ()) {
				
				Util.addError("BPARTNER NON IMPORTATO--> i_bpartner_id: ["+rs.getString(1)+"] - value: ["+rs.getString(3)+"] - i_errormsg: ["+rs.getString(2)+"]\n");
				
			}
	        
	        pstmt.close();
	        rs.close();
			
			DB.executeUpdate("delete from I_BPARTNER where i_isimported = 'Y'", null);
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Inserimento dei bparntner nelle tabella i_bpartner
	 */
	public void importIntoAdempiere() {
		
		XMLBPartner b;
		int id;
		String contactName;
		
		try {
		
			for(String k : _bpartners.keySet()){
				
				b = _bpartners.get(k);
				
				// Inserimento prodotti nella tabella di import
				id = Util.getNextSequence("i_bpartner");
				_stmt.setInt(3, id);
				
				// Value
				if (b.getTaxcode()!=null)
					_stmt.setString(4, b.getTaxcode());
				else if (b.getVatnumber()!=null)
					_stmt.setString(4, b.getVatnumber());
				else
					_stmt.setString(4, k);
					
				
				// Name
				if (b.getCompany()=="")
					_stmt.setString(5, Util.trunc(b.getFirstname()+" "+b.getLastname(), 60));
				else 
					_stmt.setString(5, Util.trunc(b.getCompany(), 60));
				
				// Taxid
				_stmt.setString(6, b.getVatnumber());
				
				_stmt.setString(8, Util.trunc(b.getStreet(), 60));
				_stmt.setString(9, b.getPostcode());
				_stmt.setString(10, b.getCity());
				_stmt.setString(11, b.getRegion_code());
				contactName = b.getFirstname() + " " + b.getLastname();
				if (contactName.length()>60)
					contactName = contactName.substring(0, 60);
				_stmt.setString(13, contactName);
				_stmt.setString(14, b.getTelephone());
				_stmt.setString(15, b.getEmail());
				
				_stmt.setString(19, b.getTaxcode());
				
				_stmt.execute();
					
				Util.increaseSequence("i_bpartner");
				
			}
		
			// Eliminazione BP con email doppia
			StringBuffer sql;
			//sql = new StringBuffer ("delete from I_BPARTNER a "
			//		+ "where value like 'ID_%'"
			//		+ "and exists (select 1 from I_BPARTNER b where a.email = b.email and a.i_bpartner_id <> b.i_bpartner_id)");
			//DB.executeUpdateEx(sql.toString(), null);
			
			// Aggiorno il campo value sul db sulla base del codice fiscale
			//sql = new StringBuffer ("update I_BPARTNER a "
			//		+ "set value = (select value from C_BPARTNER b where a.fiscalcode = b.fiscalcode and a.value <> b.value)"
			//		+ "where exists (select value from C_BPARTNER b where a.fiscalcode = b.fiscalcode and a.value <> b.value)");
			
			// Eliminazione BP esistenti in anagrafica con codice fiscale uguale
			sql = new StringBuffer("delete from I_BPARTNER a where exists (select value from C_BPARTNER b where a.fiscalcode = b.fiscalcode)");
			DB.executeUpdateEx(sql.toString(), null);
			
			// Cancellazione bpartner senza cf con piva uguale
			//sql = new StringBuffer ("delete from i_bpartner a "
			//		+ "where exists (select 1 from i_bpartner b where a.value <> b.value and a.taxid = b.taxid) "
			//		+ "and fiscalcode is null");
			
			// Eliminazione bpartner con partita iva uguale
			sql = new StringBuffer("delete from I_BPARTNER a where exists (select value from C_BPARTNER b where a.taxid = b.taxid)");
			DB.executeUpdateEx(sql.toString(), null);
			
			//sql = new StringBuffer ("delete from i_bpartner a "
			//		+ "where exists (select 1 from i_bpartner b where a.value <> b.value and a.name = b.name and a.CONTACTNAME = b.CONTACTNAME) "
			//		+ "and value like 'ID_%'");
			//DB.executeUpdateEx(sql.toString(), null);
			
			// Cancellazione BP senza piva con CF dupplicato
			//sql = new StringBuffer ("delete from  I_BPARTNER a "
			//		+ "where exists (select value from I_BPARTNER b where a.value = b.value and a.i_bpartner_id <> b.i_bpartner_id) "
			//		+ "  and taxid is null");	
			DB.executeUpdateEx(sql.toString(), null);
			
			//sql = new StringBuffer("update i_bpartner i "
			//		+ "set i.value = (select value from C_BPARTNER b where i.name = b.name) "
			//		+ "where i.taxid is null "
			//		+ " and i.fiscalcode is null "
			//		+ " and i.value like 'ID_%'"
			//		+ " and exists (select 1 from C_BPARTNER b where i.name = b.name)");

			// Eliminazione BP senza partita iva e codice fiscale ma con nome uguale
			sql = new StringBuffer("delete from I_BPARTNER i where i.taxid is null and i.fiscalcode is null and exists (select 1 from C_BPARTNER b where i.name = b.name)");
			DB.executeUpdateEx(sql.toString(), null);
			
			// Aggiornamento del bpartner id (stessa query dell'import bpartner)
			sql = new StringBuffer ("UPDATE I_BPartner i "
					+ "SET C_BPartner_ID=(SELECT C_BPartner_ID FROM C_BPartner p"
					+ " WHERE lower(i.Value)=lower(p.Value) AND p.AD_Client_ID=i.AD_Client_ID) "
					+ "WHERE C_BPartner_ID IS NULL AND Value IS NOT NULL"
					+ " AND I_IsImported='N'");
			
			// Eliminazione BP con value già esistente
			sql = new StringBuffer("delete from I_BPARTNER i where exists (select 1 from C_BPARTNER b where i.value = b.value)");
			DB.executeUpdateEx(sql.toString(), null);
			
			//sql = new StringBuffer ("UPDATE I_BPartner i "
			//		+ "SET C_Country_ID=(SELECT C_Country_ID FROM C_Country c"
			//		+ " WHERE lower(i.CountryCode)=lower(c.CountryCode) AND c.AD_Client_ID IN (0, i.AD_Client_ID)) "
			//		+ "WHERE C_Country_ID IS NULL"
			//		+ " AND I_IsImported<>'Y'");
			//DB.executeUpdateEx(sql.toString(), null);
			
			//sql = new StringBuffer ("UPDATE I_BPartner i "
			//		+ "Set C_Region_ID=(SELECT C_Region_ID FROM C_Region r"
			//		+ " WHERE lower(r.Name)=lower(i.RegionName) AND r.C_Country_ID=i.C_Country_ID"
			//		+ " AND r.AD_Client_ID IN (0, i.AD_Client_ID)) "
			//		+ "WHERE C_Region_ID IS NULL"
			//		+ " AND I_IsImported<>'Y'");
			//DB.executeUpdateEx(sql.toString(), null);
			
			// Aggiornamento della bplocation (se c'è già ma l'indirizzo è differente si prende quello in adempiere)
			//sql = new StringBuffer ("UPDATE I_BPartner i "
			//		+ "SET C_BPartner_Location_ID=(SELECT C_BPartner_Location_ID"
			//		+ " FROM C_BPartner_Location bpl INNER JOIN C_Location l ON (bpl.C_Location_ID=l.C_Location_ID)"
			//		+ " WHERE i.C_BPartner_ID=bpl.C_BPartner_ID AND bpl.AD_Client_ID=i.AD_Client_ID"
			//		+ " AND (lower(i.City)=lower(l.City) OR (i.City IS NULL AND l.City IS NULL))"
			//		+ " AND (i.Postal=l.Postal OR (i.Postal IS NULL AND l.Postal IS NULL))"
			//		+ " AND i.C_Region_ID=l.C_Region_ID AND i.C_Country_ID=l.C_Country_ID) "
			//		+ "WHERE C_BPartner_ID IS NOT NULL AND C_BPartner_Location_ID IS NULL"
			//		+ " AND I_IsImported='N'");
			//DB.executeUpdateEx(sql.toString(), null);
			
			// Aggiornamento del contact name se esiste già in adempiere
			//sql = new StringBuffer("update I_BPARTNER i "
			//		+ "set (i.AD_USER_ID, i.EMAIL) = (select u.AD_USER_ID, u.EMAIL from AD_USER u where i.C_BPARTNER_ID = u.C_BPARTNER_ID) "
			//		+ "where exists (select 1 from AD_USER u where i.C_BPARTNER_ID = u.C_BPARTNER_ID)");
			//DB.executeUpdateEx(sql.toString(), null);
			
			// Aggiornamento bp senza codice fiscale e partita iva sulla base solamente del nome
			/*sql = new StringBuffer("update i_bpartner i "
					+ "set i.value = (select value from C_BPARTNER b where i.name = b.name and b.taxid is null and b.fiscalcode is null) "
					+ "where i.taxid is null "
					+ " and i.fiscalcode is null "
					+ " and i.value like 'ID_BA%'"
					+ " and exists (select 1 from C_BPARTNER b where i.name = b.name and b.taxid is null and b.fiscalcode is null)");*/
						
			
		}
		catch (Exception e) {
			Util.addError(e);
		}		
		
	}
	
	/**
	 * Aggiunta di un bpartner alla lista dei bpartners da importare
	 * @param p
	 */
	public void addBPartner(XMLBPartner b) {
		
		if (!_bpartners.containsKey(b.getValue())) {
			_bpartners.put(b.getValue(), b);
		}
		
	}
	
	
	
}
