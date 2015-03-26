package it.pointec.adempiere.archive;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;



public class Archive {

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
		
		process();
		Util.printErrorAndExit();
		
		finalCheck();
		Util.printErrorAndExit();
		
	}
	
	/***
	 * Elaborazione delle fatture completate ed appena inserite
	 * La procedure sposta i file nella cartella _daarchiviare
	 */
	private static void setPoReference() {
		
		try {
			PreparedStatement stmt = DB.prepareStatement("select i.c_invoice_id from c_invoice i join C_DOCTYPE d on i.C_DOCTYPE_ID = d.C_DOCTYPE_ID where i.DOCSTATUS = 'CO' and i.ad_client_id = ? and i.ad_org_id = ? and i.description not like 'daarchiviare%' and i.description not like 'archiviati%' and d.DOCBASETYPE in ('APC', 'API') and i.vatledgerno is not null", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			
			ResultSet rs = stmt.executeQuery();
			MInvoice i;
			String nomeFile;
			
			//String fromPath = Ini.getString("fattureacquisto_start");
			String source;
			String dest;
			String[] a;
						
			while (rs.next()) {
				
				i = MInvoice.get(Env.getCtx(), rs.getInt(1));
				
				source = Util.getDaElaborare(i.getC_DocType_ID());
				dest = Util.getDaArchiviare(i.getC_DocType_ID());
				
				if (i.getPOReference()==null) {
					a = i.getDescription().split("#");
					nomeFile = a[0];
					i.setPOReference(a[1]);
				}
				else {
					nomeFile = i.getDescription();
				}				
				
				if (Util.moveFile(source, dest, nomeFile, nomeFile)) {
					
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
	
	private static void process() {
		
		try {
			
			PreparedStatement stmt = DB.prepareStatement("select max (p.ENDDATE) from c_calendar c join c_year y on c.C_CALENDAR_ID = y.C_CALENDAR_ID join c_period p on y.C_YEAR_ID = p.C_YEAR_ID join C_PERIODCONTROL pc on p.C_PERIOD_ID = pc.C_PERIOD_ID where c.AD_CLIENT_ID = ? and pc.PERIODSTATUS = 'C' and p.STARTDATE < sysdate", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
						
			ResultSet rs = stmt.executeQuery();
			
			rs.next();
			
			Date date = rs.getDate(1);
			
			rs.close();
			stmt.close();
			
			stmt = DB.prepareStatement("select i.c_invoice_id from c_invoice i join C_DOCTYPE d on i.C_DOCTYPE_ID = d.C_DOCTYPE_ID where i.DOCSTATUS = 'CO' and i.ad_client_id = ? and i.ad_org_id = ? and i.description like 'daarchiviare%' and i.vatledgerdate <= ? and d.DOCBASETYPE in ('API', 'APC') and i.vatledgerno is not null", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setDate(3, date);
			
			//System.out.println(date.toString());
						
			rs = stmt.executeQuery();
			
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
				
				nomeFileSource = i.getDescription().split("\n")[1];
				
				source = Util.getDaArchiviare(i.getC_DocType_ID());
				dest = Util.getArchivio(i.getC_DocType_ID(), i.get_ValueAsString("VATLEDGERDATE").substring(0,  4));
				
				//nomeFileDest = i.get_ValueAsString("vatledgerdate").substring(0, 4)+"/";
				nomeFileDest = "[" + i.get_ValueAsString("vatledgerno")+"]-";
				nomeFileDest += "[" + i.get_ValueAsString("vatledgerdate").substring(0, 10)+"]---";
				nomeFileDest += "[" + b.get_ValueAsString("name").replaceAll("[^a-zA-Z0-9]", "_") + "]-";
				nomeFileDest += "[" + i.get_ValueAsString("dateinvoiced").substring(0, 10)+"]-";
				nomeFileDest += "[" + i.get_ValueAsString("poreference").replaceAll("[^a-zA-Z0-9]", "_")+"]";
				nomeFileDest += ".pdf";
				
				//System.out.println(source);
				//System.out.println(dest);
				
				if (Util.moveFile(source, dest, nomeFileSource, nomeFileDest)) {
					
					description = "archiviati\n"+nomeFileDest;
					
					i.setDescription(description);
					
					System.out.println(i.getDocumentNo() + " nuovo stato [archiviato]");
					
					// Timbro
					source = dest;
					dest = Util.getDaStampare(i.getC_DocType_ID(), i.get_ValueAsString("VATLEDGERDATE").substring(0,  4));
					
					createPdf(source+"/"+nomeFileDest, dest+"/"+nomeFileDest, i);
				
					i.save();
					
					
					
				}
				else {
					
					Util.addError("File [" + nomeFileSource + "] NON SPOSTATO\n");
					
				}
								
			}			
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	private static void finalCheck() {
		
		try {
			
			
			
			PreparedStatement stmt = DB.prepareStatement("select i.c_invoice_id from c_invoice i join C_DOCTYPE d on i.C_DOCTYPE_ID = d.C_DOCTYPE_ID where i.DOCSTATUS = 'CO' and i.ad_client_id = ? and i.ad_org_id = ? and nvl(i.description, '#') not like 'daarchiviare%' and nvl(i.description, '#') not like 'archiviati%' and d.DOCBASETYPE in ('API', 'APC') and i.vatledgerno is not null", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			
			ResultSet rs = stmt.executeQuery();
			
			MInvoice i;
			MBPartner b;
						
			while (rs.next()) {
			
				i = MInvoice.get(Env.getCtx(), rs.getInt(1));
				b = MBPartner.get(Env.getCtx(), i.getC_BPartner_ID());
				
				Util.addError("La fattura ["+i.getDocumentNo()+"] ["+i.getDescription()+"] ["+b.getName()+"] ha dei problemi con il relativo PDF\n");
								
			}			
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	public static void createPdf (String from, String to, MInvoice i) throws DocumentException, IOException {
		
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
		
		PdfReader pdfReader = new PdfReader(from);
        
		PdfStamper pdfStamper = new PdfStamper(pdfReader,new FileOutputStream(to));
        
        // Testo
		PdfContentByte content = pdfStamper.getOverContent(1);
        content.beginText();
        content.setFontAndSize(bf, 15);
        content.setColorFill(Color.RED);
        content.setColorStroke(Color.RED);
        content.setLineWidth(1);
        content.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
        content.showTextAligned(PdfContentByte.ALIGN_LEFT
        		,"### Data protocollo: " +
        		 i.get_ValueAsString("vatledgerdate").substring(0, 10) + " - " +
        		 "N. protocollo: " +
        		 i.get_ValueAsString("vatledgerno") + " ###"
        		,10,10,0);
        content.endText();
        
        pdfStamper.close();
		
	}
	
}
