package it.pointec.adempiere.vendite;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MClient;
import org.compiere.model.MMailText;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.print.MPrintFormat;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.f3p.adempiere.model.override.MInvoice;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

/**
 * Elaborazione delle fatture importate in adempiere, generazione del pdf ed invio dello stesso al cliente
 * @author cesare
 *
 */
public class PrintInvoice {
	
	private MPrintFormat format;
	private MProcess process;
	private MClient client;
	private MMailText mText;
	
	private PrintInvoice() {
		
		client = MClient.get(Env.getCtx());
		format = MPrintFormat.get (Env.getCtx(), 1000010, false);
		process = MProcess.get (Env.getCtx(), format.getJasperProcess_ID());
		mText = new MMailText(Env.getCtx(), Ini.getInt("r_mailtext_id"), null);
		
	}

	/**
	 * Metodo statico per l'esecuzione
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		PrintInvoice p = new PrintInvoice();
		p.process();
		
		Util.printErrorAndExit();

	}
	
	/**
	 * Elaborazione fatture	
	 */
	private void process() {
		
		try {
			
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id, u.EMAIL from c_invoice i join c_bpartner b on i.C_BPARTNER_ID = b.C_BPARTNER_ID join ad_user u on b.C_BPARTNER_ID = u.C_BPARTNER_ID where i.ad_client_id = ? and i.C_DOCTYPE_ID = ? and i.ISPRINTED='N'", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("doc_type_id_invoice"));
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				//System.out.println(rs.getString(1));
				
				sendEmail(rs.getInt(1), rs.getString(2));
				//sendEmail(rs.getInt(1), "anpiffer@gmail.com");
				
			}
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Invio email al cliente
	 * @param C_Invoice_ID	ID della fattura da elaborare
	 * @param to_email	Email a cui inviare la fattura
	 */
	private void sendEmail(int C_Invoice_ID, String to_email) {
		
		MInvoice i = new MInvoice(Env.getCtx(), C_Invoice_ID, null);
		
		MPInstance pInstance = new MPInstance (process, C_Invoice_ID);
		ProcessInfo pi = new ProcessInfo (process.getName(), process.getAD_Process_ID(), MInvoice.Table_ID, C_Invoice_ID);
		pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
		pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
		pi.setClassName(process.getClassname());
		pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
		
		pi.setPrintPreview (false);
		pi.setIsBatch(true);
		
		Trx trx = Trx.get(Trx.createTrxName("WebPrc"), true);
		
		boolean processOK = false;
		
		try
		{				
			processOK = process.processIt(pi, trx);			
			trx.commit();
			trx.close();
		}
		catch (Throwable t)
		{
			trx.rollback();
			trx.close();
		}
		
		File f_invoice;
		if(processOK)
		{
			f_invoice=pi.getPDFReport();
			
			String subject = mText.getMailHeader() + " - " + i.getDocumentNo();
			EMail email = client.createEMail(to_email, subject, null);
			
			String message = mText.getMailText(true);
			
			email.setMessageHTML(subject, message);
			
			email.addAttachment(f_invoice);
			
			String msg = email.send();
			
			if (msg.compareTo("OK")==0) {
				i.setIsPrinted(true);
				i.save();
			}
			
			//MUserMail um = new MUserMail(mText, getAD_User_ID(), email);
			
		}
		else
		{
			Util.addError("Errore nella generazione della fattura\n");
			return;
		}
		
		
		
	}

}
