package it.pointec.adempiere.archive;

import java.util.Calendar;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import it.pointec.adempiere.Adempiere;

import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class Iva {

	//private MPrintFormat format;
	//private MProcess processLiquidazione;
	//private MClient client;
	
	private Iva() {
		
		//client = MClient.get(Env.getCtx());
		//processLiquidazione = MProcess.get (Env.getCtx(), 1000102);
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Adempiere a = new Adempiere();
		a.inizializza();
		
		Iva i = new Iva();
		i.generateLiquidazione();
		i.generateRegistriIva();
		

	}
	
	private void generateRegistriIva() {
		
		try {
			String sql = "select name, LIT_VATLEDGERDEF_ID from LIT_VATLedgerDef where ISACTIVE = 'Y'";
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				this.generateRegistriIva(rs.getInt(2), rs.getString(1));
				
			}
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	private void generateRegistriIva(int vatledger_id, String name_vat) throws IOException {
		
		name_vat = name_vat.replace(' ', '-');
		
		int year = Ini.getInt("generate_invoice_year");
		
		Calendar cFrom = Calendar.getInstance();
		Calendar cTo = Calendar.getInstance();
		Timestamp tFrom;
		Timestamp tTo; 
		
		// 1° Trimestre
		
		cFrom.set(year, Calendar.JANUARY, 1, 0, 0, 0);
		cTo.set(year, Calendar.MARCH, 31, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		String name = name_vat + "-1trim";
		int m = 1;
		int n = this.generateRegistriIva(tFrom, tTo, name, Integer.toString(year), vatledger_id, m);
		m += n;
		
		// 2° Trimestre
		cFrom.set(year, Calendar.APRIL, 1, 0, 0, 0);
		cTo.set(year, Calendar.JUNE, 30, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		name = name_vat + "-2trim";
		this.generateRegistriIva(tFrom, tTo, name, Integer.toString(year), vatledger_id, m);
		m += n;
		
		// 3° Trimestre
		cFrom.set(year, Calendar.JULY, 1, 0, 0, 0);
		cTo.set(year, Calendar.SEPTEMBER, 30, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		name = name_vat + "-3trim";
		this.generateRegistriIva(tFrom, tTo, name, Integer.toString(year), vatledger_id, m);
		m += n;
		
		// 4° Trimestre
		cFrom.set(year, Calendar.OCTOBER, 1, 0, 0, 0);
		cTo.set(year, Calendar.DECEMBER, 31, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		name = name_vat + "-4trim";
		this.generateRegistriIva(tFrom, tTo, name, Integer.toString(year), vatledger_id, m);
		
	}
	
	private int generateRegistriIva(Timestamp tFrom, Timestamp tTo, String name, String year, int vatledger_id, int n) throws IOException {
		
		System.out.println("Generazione registri iva ["+name+"]");
		
		MProcess process = MProcess.get (Env.getCtx(), 1000101);
		
		ProcessInfo pi = new ProcessInfo (process.getName(), process.getAD_Process_ID());
		
		MPInstance pInstance = new MPInstance(Env.getCtx(), process.getAD_Process_ID(), 0);
		pInstance.saveEx();
		//System.out.println(pInstance.get_ID());
				
		pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
		pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
		pi.setClassName(process.getClassname());
		pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
		
		MPInstancePara para = new MPInstancePara(pInstance, 10);
		para.setParameterName("DateAcct");
		para.setP_Date(tFrom);
		para.setP_Date_To(tTo);
		para.saveEx();
		
		para = new MPInstancePara(pInstance, 20);
		para.setParameter("LIT_VATLedgerDef_ID", vatledger_id);
		para.saveEx();
		
		para = new MPInstancePara(pInstance, 30);
		para.setParameter("FirstPageNo", n);
		para.saveEx();
		
		pi.setPrintPreview (false);
		pi.setIsBatch(true);
		
		Trx trx = Trx.get(Trx.createTrxName("WebPrc"), true);
		process.processIt(pi, trx);
		trx.commit();
		trx.close();
		
		File f_source = pi.getPDFReport();
		String basepath = Util.getArchivio("iva", year);
		
		File f_dest = new File(basepath + "/" + "registro--"+name+".pdf");
		
		Util.moveFile(f_source, f_dest);
		
		PDDocument doc = PDDocument.load(f_dest);
		//System.out.println(doc.getNumberOfPages());
		
		System.out.println("Generazione registri iva ["+name+"] ["+f_dest.getAbsolutePath()+"]");
		
		return doc.getNumberOfPages();
		
	}
	
	private void generateLiquidazione() {
		
		int year = Ini.getInt("generate_invoice_year");
		
		Calendar cFrom = Calendar.getInstance();
		Calendar cTo = Calendar.getInstance();
		Timestamp tFrom;
		Timestamp tTo; 
		
		// 1° Trimestre
		
		cFrom.set(year, Calendar.JANUARY, 1, 0, 0, 0);
		cTo.set(year, Calendar.MARCH, 31, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		String name = "1trim";
		this.generateLiquidazione(tFrom, tTo, name, Integer.toString(year));
		
		// 2° Trimestre
		cFrom.set(year, Calendar.APRIL, 1, 0, 0, 0);
		cTo.set(year, Calendar.JUNE, 30, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		name = "2trim";
		this.generateLiquidazione(tFrom, tTo, name, Integer.toString(year));
		
		// 3° Trimestre
		cFrom.set(year, Calendar.JULY, 1, 0, 0, 0);
		cTo.set(year, Calendar.SEPTEMBER, 30, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		name = "3trim";
		this.generateLiquidazione(tFrom, tTo, name, Integer.toString(year));
		
		// 4° Trimestre
		cFrom.set(year, Calendar.OCTOBER, 1, 0, 0, 0);
		cTo.set(year, Calendar.DECEMBER, 31, 0, 0, 0);
		tFrom = new Timestamp(cFrom.getTimeInMillis());
		tTo = new Timestamp(cTo.getTimeInMillis());
		
		name = "4trim";
		this.generateLiquidazione(tFrom, tTo, name, Integer.toString(year));
		
		
	}
	
	private void generateLiquidazione(Timestamp tFrom, Timestamp tTo, String name, String year) {
		
		System.out.println("Generazione liquidazione iva ["+name+"]");
		
		MProcess process = MProcess.get (Env.getCtx(), 1000102);
		//process.get_
		
		ProcessInfo pi = new ProcessInfo (process.getName(), process.getAD_Process_ID());
		
		MPInstance pInstance = new MPInstance(Env.getCtx(), process.getAD_Process_ID(), 0);
		pInstance.saveEx();
		//System.out.println(pInstance.get_ID());
				
		pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
		pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
		pi.setClassName(process.getClassname());
		pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
		
		MPInstancePara para = new MPInstancePara(pInstance, 10);
		para.setParameterName("DateAcct");
		para.setP_Date(tFrom);
		para.setP_Date_To(tTo);
		para.saveEx();
		
		para = new MPInstancePara(pInstance, 20);
		para.setParameter("PlafondType", "N");
		para.saveEx();
		
		para = new MPInstancePara(pInstance, 30);
		para.setParameter("ProRata", 0);
		para.saveEx();
		
		para = new MPInstancePara(pInstance, 40);
		para.setParameter("IsDetailedSummary", "N");
		para.saveEx();
		
		pi.setPrintPreview (false);
		pi.setIsBatch(true);
		
		Trx trx = Trx.get(Trx.createTrxName("WebPrc"), true);
		process.processIt(pi, trx);
		trx.commit();
		trx.close();
		
		File f_source = pi.getPDFReport();
		String basepath = Util.getArchivio("iva", year);
		
		File f_dest = new File(basepath + "/" + "liquidazione--"+name+".pdf");
		
		Util.moveFile(f_source, f_dest);
		
		System.out.println("Generazione liquidazione iva ["+name+"] ["+f_dest.getAbsolutePath()+"]");
		
	}

}
