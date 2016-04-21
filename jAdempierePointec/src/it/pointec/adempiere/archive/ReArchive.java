package it.pointec.adempiere.archive;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.util.DB;
import org.compiere.util.Env;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;



public class ReArchive {
	
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		elabora();

	}
	
	public static void elabora() {
		
		// Elaborazione fatture inserite
		process();
		Util.printErrorAndExit();
		
		
	}
	
	private static void process() {
		
		try {
			
			PreparedStatement stmt = DB.prepareStatement("select i.c_invoice_id from c_invoice i join C_DOCTYPE d on i.C_DOCTYPE_ID = d.C_DOCTYPE_ID where i.DOCSTATUS = 'CO' and i.ad_client_id = ? and i.ad_org_id = ? and i.description like 'archiviati%' and to_char(i.vatledgerdate, 'yyyy') = ? and d.DOCBASETYPE in ('API', 'APC') and i.vatledgerno is not null", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setString(3, Ini.getString("generate_invoice_year"));
						
			ResultSet rs = stmt.executeQuery();
			
			String source;
			String dest;
			MInvoice i;
			MBPartner b;
			String nomeFileSource;
			String nomeFileDest;
			String description;
						
			while (rs.next()) {
			
				i = MInvoice.get(Env.getCtx(), rs.getInt(1));
				
				b = MBPartner.get(Env.getCtx(), i.getC_BPartner_ID());
				
				nomeFileDest = "[" + i.get_ValueAsString("vatledgerno")+"]-";
				nomeFileDest += "[" + i.get_ValueAsString("vatledgerdate").substring(0, 10)+"]---";
				nomeFileDest += "[" + b.get_ValueAsString("name").replaceAll("[^a-zA-Z0-9]", "_") + "]-";
				nomeFileDest += "[" + i.get_ValueAsString("dateinvoiced").substring(0, 10)+"]-";
				nomeFileDest += "[" + i.get_ValueAsString("poreference").replaceAll("[^a-zA-Z0-9]", "_")+"]";
				nomeFileDest += ".pdf";
				
				description = "archiviati\n"+nomeFileDest;
				
				source = Util.getArchivio(i.getC_DocType_ID(), i.get_ValueAsString("VATLEDGERDATE").substring(0,  4));
				nomeFileSource = i.getDescription().replace("archiviati\n", "");
				dest = Util.getDaStampare(i.getC_DocType_ID(), i.get_ValueAsString("VATLEDGERDATE").substring(0,  4));
				
				if (description.compareTo(i.getDescription()) != 0) {
				
					System.out.println ("------------------------------------------------------------------");
					System.out.println ("FATTURA [" + i.getDocumentNo() + "]");
					System.out.println ("Descrizione sulla fattura: ");
					System.out.println (i.getDescription());
					System.out.println ("Nuova descrizione: ");
					System.out.println (description);
					
					Util.moveFile(source, source, nomeFileSource, nomeFileDest);
					
					i.setDescription(description);
					i.save();
				}
				
				File f = new File(dest+"/"+nomeFileDest);
				if (!f.exists()) {
					Archive.createPdf(source+"/"+nomeFileDest, dest+"/"+nomeFileDest, i);
					System.out.println ("FATTURA [" + i.getDocumentNo() + "] PDF per la stampa generato");
				}
				
			}	
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	
	
}
