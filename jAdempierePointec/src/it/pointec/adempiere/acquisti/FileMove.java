package it.pointec.adempiere.acquisti;

import java.awt.Color;
import java.io.FileOutputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

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
		//Util.printErrorAndExit();
		
		Archive();
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
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id from c_invoice where DOCSTATUS = 'CO' and ad_client_id = ? and ad_org_id = ? and C_DOCTYPE_ID in (?, ?, ?) and description not like 'daarchiviare%' and description not like 'archiviati%'", null);
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
	
	private static void Archive() {
		
		try {
			
			PreparedStatement stmt = DB.prepareStatement("select max (p.ENDDATE) from c_calendar c join c_year y on c.C_CALENDAR_ID = y.C_CALENDAR_ID join c_period p on y.C_YEAR_ID = p.C_YEAR_ID join C_PERIODCONTROL pc on p.C_PERIOD_ID = pc.C_PERIOD_ID where c.AD_CLIENT_ID = ? and pc.PERIODSTATUS = 'C' and p.STARTDATE < sysdate", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			//stmt.setInt(2, Ini.getInt("ad_org_id"));
			
			ResultSet rs = stmt.executeQuery();
			
			rs.next();
			
			Date date = rs.getDate(1);
			
			rs.close();
			stmt.close();
			
			stmt = DB.prepareStatement("select c_invoice_id from c_invoice where DOCSTATUS = 'CO' and ad_client_id = ? and ad_org_id = ? and C_DOCTYPE_ID in (?, ?, ?) and description like 'daarchiviare%' and vatledgerdate <= ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setInt(3, Ini.getInt("doc_type_id_invoice_acq"));
			stmt.setInt(4, Ini.getInt("doc_type_id_invoice_intra"));
			stmt.setInt(5, Ini.getInt("doc_type_id_creditmemo_acq"));
			stmt.setDate(6, date);
			
			//System.out.println(date.toString());
						
			rs = stmt.executeQuery();
			
			String fromPath = Ini.getString("fattureacquisto_start");
			String source;
			String dest;
			MInvoice i;
			MBPartner b;
			String nomeFileSource;
			String nomeFileDest;
			PdfContentByte content;
			PdfReader pdfReader;
			PdfStamper pdfStamper;
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
						
			while (rs.next()) {
			
				i = MInvoice.get(Env.getCtx(), rs.getInt(1));
				
				nomeFileSource = i.getDescription().split("\n")[1];
				
				source = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID()) + "_daarchiviare";
				dest = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID()) + "_archiviati";
				b = MBPartner.get(Env.getCtx(), i.getC_BPartner_ID());
				
				nomeFileDest = i.get_ValueAsString("vatledgerdate").substring(0, 4)+"/";
				nomeFileDest += "[" + i.get_ValueAsString("vatledgerno")+"]-";
				nomeFileDest += "[" + i.get_ValueAsString("vatledgerdate").substring(0, 10)+"]---";
				nomeFileDest += "[" + b.get_ValueAsString("name").replaceAll("[^a-zA-Z0-9]", "_") + "]-";
				nomeFileDest += "[" + i.get_ValueAsString("dateinvoiced").substring(0, 10)+"]-";
				nomeFileDest += "[" + i.get_ValueAsString("poreference").replaceAll("[^a-zA-Z0-9]", "_")+"]";
				nomeFileDest += ".pdf";
				
				if (Util.moveFile(source, dest, nomeFileSource, nomeFileDest)) {
					
					i.setDescription("archiviati\n"+nomeFileDest);
					
					System.out.println(i.getDocumentNo() + " nuovo stato [archiviato]");
					
					// Timbro
					source = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID()) + "_archiviati";
					dest = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID()) + "_dastampare";
					
					pdfReader = new PdfReader(source+"/"+nomeFileDest);
			        
			        pdfStamper = new PdfStamper(pdfReader,
		                    new FileOutputStream(dest+"/"+nomeFileDest));
			        
			        // Testo
			        content = pdfStamper.getOverContent(1);
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
					// Fine
				
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
			
			
			
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id from c_invoice where DOCSTATUS = 'CO' and ad_client_id = ? and ad_org_id = ? and C_DOCTYPE_ID in (?, ?, ?) and nvl(description, '#') not like 'daarchiviare%' and nvl(description, '#') not like 'archiviati%'", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setInt(3, Ini.getInt("doc_type_id_invoice_acq"));
			stmt.setInt(4, Ini.getInt("doc_type_id_invoice_intra"));
			stmt.setInt(5, Ini.getInt("doc_type_id_creditmemo_acq"));
			
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
	
	
	/*private static void prepareForPrint() {
		
		try {
			
			PreparedStatement stmt = DB.prepareStatement("select c_invoice_id from c_invoice where DOCSTATUS = 'CO' and ad_client_id = ? and ad_org_id = ? and C_DOCTYPE_ID in (?, ?, ?) and description like 'archiviati%' and vatledgerdate < ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setInt(3, Ini.getInt("doc_type_id_invoice_acq"));
			stmt.setInt(4, Ini.getInt("doc_type_id_invoice_intra"));
			stmt.setInt(5, Ini.getInt("doc_type_id_creditmemo_acq"));
			
			java.util.Date d = new java.util.Date();
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.MONTH, -3);
			
			Date date = new Date (c.getTimeInMillis());
			
			stmt.setDate(6, date);
			
			//System.out.println(date.toString());
						
			ResultSet rs = stmt.executeQuery();
			
			String fromPath = Ini.getString("fattureacquisto_start");
			String source;
			String dest;
			MInvoice i;
			MBPartner b;
			
			String nomeFileSource;
			String nomeFileDest;
			String[] a;
			PdfContentByte content;
			
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
			while (rs.next()) {
			
				i = MInvoice.get(Env.getCtx(), rs.getInt(1));
				
				nomeFileSource = i.getDescription().split("\n")[1];
				nomeFileDest = nomeFileSource;
				
				source = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID()) + "_archiviati";
				dest = fromPath + "/" + Util.doctypeidToPath(i.getC_DocType_ID()) + "_dastampare";
				
				// Overlay pdf image
				System.out.println("Elaborazione file: "+nomeFileSource);
		        PdfReader pdfReader = new PdfReader(source+"/"+nomeFileSource);
		        
		        PdfStamper pdfStamper = new PdfStamper(pdfReader,
	                    new FileOutputStream(dest+"/"+nomeFileDest));
		        
		        // Testo
		        
		        content = pdfStamper.getOverContent(1);
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
		        
				//System.out.println(source + "][" + dest + "][" + nomeFileSource + "][" + nomeFileDest);
				
				
								
			}			
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}*/
	
}
