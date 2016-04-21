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
	private boolean override;
	private String year;
	
	public PrintInvoice() {
		
		client = MClient.get(Env.getCtx());
		format = MPrintFormat.get (Env.getCtx(), 1000010, false);
		process = MProcess.get (Env.getCtx(), format.getJasperProcess_ID());
		mText = new MMailText(Env.getCtx(), Ini.getInt("r_mailtext_id"), null);
		
		override = Ini.getBoolean("generate_invoice_override");
		year = Ini.getString("generate_invoice_year");
		
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
		p.sendInvoiceEmail();
				
		Util.printErrorAndExit();

	}
	
	/**
	 * Elaborazione fatture	
	 */
	private void sendInvoiceEmail() {
		
		try {
			
			// Invio fatture per email
			
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id, u.EMAIL from c_invoice i join c_bpartner b on i.C_BPARTNER_ID = b.C_BPARTNER_ID join ad_user u on b.C_BPARTNER_ID = u.C_BPARTNER_ID where i.ad_client_id = ? and i.C_DOCTYPE_ID = ? and i.ISPRINTED='N'", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("doc_type_id_invoice"));
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				MInvoice i = new MInvoice(Env.getCtx(), rs.getInt(1), null);
				
				sendEmail(i, rs.getString(2));
								
				
			}
			
			// Verifica ultima fattura inserita a sistema
			stmt = DB.prepareStatement("select max(i.DATEINVOICED) from c_invoice i where i.ad_client_id = ? and i.C_DOCTYPE_ID = ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("doc_type_id_invoice"));
			
			rs = stmt.executeQuery();
			rs.next();
			
			long last = rs.getDate(1).getTime();
			java.util.Date now = new java.util.Date();
			
			if((now.getTime() - last) > 432000000) {
				
				Util.addError("ATTENZIONE !!! Il caricamento delle fatture è fermo da più di 5 giorni");
				
			}
						
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Elaborazione fatture	
	 */
	public void sendInvoiceEmailSingle() {
		
		try {
			
			// Invio fatture per email
			
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id, 'ceceghini@gmail.com' as email from c_invoice i join c_bpartner b on i.C_BPARTNER_ID = b.C_BPARTNER_ID join ad_user u on b.C_BPARTNER_ID = u.C_BPARTNER_ID where i.ad_client_id = ? and i.documentno = ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setString(2, Ini.getString("send_single_invoice_documentno"));
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				MInvoice i = new MInvoice(Env.getCtx(), rs.getInt(1), null);
				
				sendEmail(i, rs.getString(2));
								
				
			}
						
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Elaborazione fatture	
	 */
	public void generateInvoice(String type) {
		
		try {
			
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id from c_invoice i where i.ad_client_id = ? and i.C_DOCTYPE_ID = ? and to_char(vatledgerdate, 'yyyy') = ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt(type));
			stmt.setString(3, year);
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				MInvoice i = new MInvoice(Env.getCtx(), rs.getInt(1), null);
				GenerateInvoice(i);
				
				
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
	private void sendEmail(MInvoice i, String to_email) {
		
		File f_invoice = createPdf(i);
		if (f_invoice != null)
		{
			String subject = mText.getMailHeader() + " - " + i.getDocumentNo();
			EMail email = client.createEMail(to_email, subject, null);
			
			String message = mText.getMailText(true);
			
			email.setMessageHTML(subject, message);
			
			email.addAttachment(f_invoice);
			
			String msg = email.send();
			
			if (msg.compareTo("OK")==0) {
				
				System.out.println("Fattura inviata correttamente [" +i.getDocumentNo()+ "] ["+to_email+"]");
				
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
	
	public File createPdf(MInvoice i) {
		
		MPInstance pInstance = new MPInstance (process, i.getC_Invoice_ID());
		ProcessInfo pi = new ProcessInfo (process.getName(), process.getAD_Process_ID(), MInvoice.Table_ID, i.getC_Invoice_ID());
		pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
		pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
		pi.setClassName(process.getClassname());
		pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
		
		pi.setPrintPreview (false);
		pi.setIsBatch(true);
		
		Trx trx = Trx.get(Trx.createTrxName("WebPrc"), true);
		
		//boolean processOK = false;
		
		try
		{				
			process.processIt(pi, trx);			
			trx.commit();
			trx.close();
			return pi.getPDFReport();
		}
		catch (Throwable t)
		{
			System.out.println(t.getMessage());
			trx.rollback();
			trx.close();
			return null;
		}
		
	}
	
	private void GenerateInvoice(MInvoice i) {
		
		File f_source = createPdf(i);
		
		if (f_source == null) {
			System.out.println("File non rinominato");
			return;
		}
		
		String dest = Util.getArchivio(i.getC_DocType_ID(), i.get_ValueAsString("VATLEDGERDATE").substring(0,  4));
		String nomeFileDest = i.getDocumentNo() + ".pdf";
		
		File f_dest = new File(dest + "/" + nomeFileDest);
		
		if (!f_dest.exists() || override) {
			f_source.renameTo(f_dest);
			System.out.println("Fattura ["+ i.getDocumentNo() +"] generata. ["+dest+"]");
		}
		
		//System.out.println(i.getDocumentNo());
		//System.out.println(dest);
		
	}

}
