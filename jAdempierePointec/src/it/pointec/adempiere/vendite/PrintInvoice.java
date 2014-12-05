package it.pointec.adempiere.vendite;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MClient;
import org.compiere.model.MQuery;
import org.compiere.model.PrintInfo;
import org.compiere.model.X_C_Invoice;
import org.compiere.print.MPrintFormat;
import org.compiere.print.ReportEngine;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Language;

import com.f3p.adempiere.model.override.MInvoice;
import com.sfcommerce.jpaymentcomponent.ssl.Client;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class PrintInvoice {
	
	MClient _client = MClient.get(Env.getCtx());
	String documentDir = _client.getDocumentDir();

	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		elabora();

	}
	
	public static void elabora() {
		
		PrintInvoice p = new PrintInvoice();
		p.process();
		
		Util.printErrorAndExit();
		
		
	}
	
	private void process() {
		
		System.out.println(_client.get_TableName());
		
		return;
		
		/*try {
			
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id, b.C_BPARTNER_ID, u.AD_USER_ID, u.EMAIL from c_invoice i join c_bpartner b on i.C_BPARTNER_ID = b.C_BPARTNER_ID join ad_user u on b.C_BPARTNER_ID = u.C_BPARTNER_ID where i.ad_client_id = ? and i.C_DOCTYPE_ID = ? and i.DOCUMENTNO = '14-00060'", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, 1000002);
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				System.out.println(rs.getString(1));
				
				sendEmail(rs.getInt(1), rs.getString(4));
				
			}
			
		}
		catch (Exception e) {
			Util.addError(e);
		}*/
		
	}
	
	private void sendEmail(int C_Invoice_ID, String to_email) {
		
	//old_C_Invoice_ID = C_Invoice_ID;
		// Set Language when enabled
		Language language = Language.getLanguage("en_US");	// Base Language
		int AD_PrintFormat_ID = 1000010;
		int copies = 1;
		//int AD_User_ID = rs.getInt(6);
		//MUser to = new MUser (getCtx(), AD_User_ID, get_TrxName());
		//String DocumentNo = rs.getString(7);
		//C_BPartner_ID = rs.getInt(8);
		//
		String documentDir = "/tmp";
		
		// Get Format & Data
		MPrintFormat format = MPrintFormat.get (Env.getCtx(), AD_PrintFormat_ID, false);
		
		format.setLanguage(language);
		format.setTranslationLanguage(language);
		
		// query
		MQuery query = new MQuery("C_Invoice_Header_v");
		//query.addRestriction("C_Invoice_ID", MQuery.EQUAL, new Integer(C_Invoice_ID));
		//System.out.println(query.getWhereClause());
		// Engine
		PrintInfo info = new PrintInfo(
		"DocumentNo",
		X_C_Invoice.Table_ID,
		C_Invoice_ID,
		1001601);
		info.setCopies(copies);
		ReportEngine re = new ReportEngine(Env.getCtx(), format, query, info);
		
		String subject = "prova";
		EMail email = _client.createEMail("ceceghini@gmail.com", subject, null);
		
		String message = "prova";
		
		email.setSubject (subject);
		email.setMessageText (message);
		//
		
		File invoice = invoice = new File(MInvoice.getPDFFileName(documentDir, C_Invoice_ID));
		File attachment = re.getPDF(invoice);
		
		email.addAttachment(attachment);
		//
		String msg = email.send();
		
		System.out.println(msg); 
		
	}

}
