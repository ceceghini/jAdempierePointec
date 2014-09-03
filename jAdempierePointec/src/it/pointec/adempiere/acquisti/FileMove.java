package it.pointec.adempiere.acquisti;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MInvoice;
import org.compiere.util.DB;
import org.compiere.util.Env;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class FileMove {

	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		elabora();

	}
	
	public static void elabora() {
		
		// Elaborazione fatture inserite
		setPoReference();
		Util.printErrorAndExit();
		
	}
	
	/***
	 * Elaborazione delle fatture completate ed appena inserite
	 * La procedure sposta i file nella cartella _daarchiviare
	 */
	private static void setPoReference() {
		
		try {
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id from c_invoice where DOCSTATUS = 'CO' and ad_client_id = ? and ad_org_id = ? and C_DOCTYPE_ID in (?, ?, ?) and description not like 'daarchiviare%'", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setInt(3, Ini.getInt("doc_type_id_invoice_acq"));
			stmt.setInt(4, Ini.getInt("doc_type_id_invoice_intra"));
			stmt.setInt(5, Ini.getInt("doc_type_id_creditmemo_acq"));
			
			ResultSet rs = stmt.executeQuery();
			MInvoice i;
			String nomeFile;
			
			String fromPath = Ini.getString("fattureacquisto_start");
			String source;
			String dest;
			String[] a;
						
			while (rs.next()) {
				
				i = MInvoice.get(Env.getCtx(), rs.getInt(1));
				
				if (i.getPOReference()==null) {
					a = i.getDescription().split("#");
					nomeFile = a[0];
					i.setPOReference(a[1]);
				}
				else {
					nomeFile = i.getDescription();
				}
				
				if (!nomeFile.contains("/"))
					nomeFile = "altri/" + nomeFile;
				
				source = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID());
				dest = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID()) + "_daarchiviare";
								
				if (Util.moveFile(source, dest, nomeFile)) {
					
					i.setDescription("daarchiviare\n"+nomeFile);
					
					System.out.println(i.getDocumentNo() + " nuovo stato [daarchiviare]");
					
					i.save();
					
				}
				
			}
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
}
