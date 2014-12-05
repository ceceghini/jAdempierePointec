package it.pointec.adempiere.acquisti;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.model.InvoicePassive;
import it.pointec.adempiere.util.Extractor;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.compiere.model.MInvoice;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.process.ImportInvoice;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

public class ImportFattureAcquisto {
	
	private PreparedStatement _stmt;
	private PreparedStatement _stmtBP;
	private List<InvoicePassive> _fatture = new ArrayList<InvoicePassive>();
	private String _type;
	private int _c_doctype_id = 0;
	private Date dataIva;
	
	private ImportFattureAcquisto (String _type) {
		
		try {
			
			this._type = _type;
			if (_type.compareTo("fornitori")==0)
				_c_doctype_id = Ini.getInt("doc_type_id_invoice_acq");
			
			if (_c_doctype_id==0) {
				Util.addError("_c_doctype_id non impostato. "+_type);
				Util.printErrorAndExit();
			}
			
			// Controlli iniziali
			initialCheck();
			Util.printErrorAndExit();
			
			_stmt = DB.prepareStatement("insert into I_INVOICE (ad_org_id, ad_client_id, i_invoice_id, c_doctype_id, documentno, issotrx, salesrep_id, c_paymentterm_id, C_BPartner_ID, dateinvoiced, dateacct, productvalue, qtyordered, priceactual, c_tax_id, taxamt, description, M_PriceList_ID) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", null);
			_stmt.setInt(1, Ini.getInt("ad_org_id"));
			_stmt.setInt(2, Ini.getInt("ad_client_id"));
			_stmt.setString(6, "Y");
			//_stmt.setInt(7, Ini.get_salesrep_id());
			_stmt.setInt(15, Ini.getInt("c_tax_id"));
			
			_stmt.setNull(8, Types.INTEGER);
			_stmt.setNull(7, Types.INTEGER);
			
			_stmt.setInt(18, 1000000);
			
			_stmtBP = DB.prepareStatement("select REGEX_POREFERENCE, GROUP_POREFERENCE, REGEX_DATEINVOICED, GROUP_DATEINVOICED, REGEX_GRANDTOTAL, GROUP_GRANDTOTAL, DATE_INVOICE_FORMAT, EXCLUDE, SKU, C_BPARTNER_ID, replace from POINTEC_FORNITORI where cartella = ? and is_active = 'Y'", null);
			
			String sql = "select valore from pointec_parametri";
			String v = DB.getSQLValueString(null, sql);
			dataIva = Util.getDate(v, "dd/MM/yyyy");
			
			System.out.println("Data liquidazione IVA caricata: ["+dataIva.toString()+"]");
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}

	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		if (args.length>0)
			if (args[0].compareTo("true")==0)
				FileMove.elabora();
		
		elabora();
		
		if (args.length>0)
			if (args[0].compareTo("true")==0)
				FileMove.elabora();

	}
	
	public static void elabora() {
		
		// Importazione fatture acquisti
		ImportFattureAcquisto ifa = new ImportFattureAcquisto("fornitori");
		
		ifa.elaboraDirectory();
		Util.printErrorAndExit();
		
		ifa.insertIntoAdempiere();
		Util.printErrorAndExit();
		
		ifa.process();
		Util.printErrorAndExit();
		
	}
	
	/**
	 * Verifica preventiva sulle tabelle di import 
	 */
	private void initialCheck() {
		
		int n = DB.getSQLValue(null, "select count(*) n from i_invoice");
		if (n>0) {
			Util.addError("Record esistenti nella tabella i_invoice");
			return;
		}
		
	}
	
	/***
	 * Process delle fatture appena inserite in i_invoice
	 */
	private void process() {
		
		try {
			
			// Importazione prodotti
			String trxName = "processInvoice";
			
			int AD_Process_ID =  MProcess.getProcess_ID("Import_Invoice", trxName);
			ProcessInfo pi = new ProcessInfo ("Import_Invoice",AD_Process_ID);
			
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
	        instance.saveEx();
	        
	        pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
	        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
	
	        //  Add Parameters
	        MPInstancePara para = new MPInstancePara(instance, 10);
	        para.setParameter("AD_Client_ID", Ini.getInt("ad_client_id"));
	        para.saveEx();
	        
	        para = new MPInstancePara(instance, 20);
	        para.setParameter("AD_Org_ID", Ini.getInt("ad_org_id"));
	        para.saveEx();
	        
	        para = new MPInstancePara(instance, 30);
	        para.setParameter("DeleteOldImported", "N");
	        para.saveEx();
	        
	        para = new MPInstancePara(instance, 40);
			para.setParameter("DocAction", MInvoice.DOCACTION_Complete);
			para.save();        
	        
	        ImportInvoice process = new ImportInvoice();
	        
	        process.startProcess(Env.getCtx(), pi, Trx.get(trxName, false));     
	
	        //Verifica importazione
	        String sql = "select i_invoice_id, i_errormsg from i_invoice where i_isimported <> 'Y'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			
	        while (rs.next ()) {
				
				Util.addError("INVOICE NON IMPORTATO--> i_invoice_id: ["+rs.getString(1)+"] - i_errormsg: ["+rs.getString(2)+"]\n");
				
			}
	        
	        pstmt.close();
	        rs.close();
			
			DB.executeUpdate("delete from i_invoice where i_isimported = 'Y'", null);
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
	}
	
	/***
	 * Inserimento in i_invoice delle fatture
	 */
	private void insertIntoAdempiere() {
		
		int id;
		int n;
				
		try {
			
			for(InvoicePassive acq : _fatture){
				
				n = DB.getSQLValue(null, "select count(*) from c_invoice where ad_client_id = ? and ad_org_id = ? and (C_DOCTYPE_ID = 1000005 or C_DOCTYPETARGET_ID = 1000005) and C_BPARTNER_ID = ? and (POREFERENCE = ? or description like ?)", Ini.getInt("ad_client_id"), Ini.getInt("ad_org_id"), acq.get_c_bpartner_id(), acq.get_poreference(), "%#"+acq.get_poreference());
				
				if (n==0) {
					
					Util.setCurrent(acq.get_poreference());
					id = Util.getNextSequence("i_invoice");
					
					_stmt.setInt(4, acq.get_c_doctype_id());
					//documentno = "FAIMP-"+Util.getNextSequence("AP Invoice - Import");
					_stmt.setString(5, Integer.toString(id));
					//_stmt.setNull(5, Types.VARCHAR);
					_stmt.setDate(10, acq.get_dateinvoiced());
					
					_stmt.setDate(11, acq.get_dateinvoiced());
					
					_stmt.setString(17, acq.get_fullpath()+"#"+acq.get_poreference());
					
					_stmt.setInt(9, acq.get_c_bpartner_id());
					//System.out.println(acq.get_c_bpartner_id());
					_stmt.setInt(3, id);
					
					_stmt.setString(12, acq.get_sku());
					_stmt.setInt(13, 1);
					_stmt.setBigDecimal(14, acq.get_price());
					_stmt.setBigDecimal(16, acq.get_tax_amount());
					
					_stmt.execute();
						
					Util.increaseSequence("i_invoice");
					//Util.increaseSequence("AP Invoice - Import");
				}
				else {
					System.out.println("Fattura già presente: ["+acq.get_fullpath()+"]");
				}				
				
			}
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/***
	 * Elaborazione directory
	 */
	private void elaboraDirectory() {
		
		File folder = new File(Ini.getString("fattureacquisto_start")+"/"+_type);
		File[] listOfFiles = folder.listFiles();
		
		for (File d : listOfFiles) {
			
			try {
				
				System.out.println("Elaborazione directory: ["+d.getName()+"]");
				
				elaboraFornitore(d);
				
				System.out.println("Elaborazione directory: ["+d.getName()+"] completata");
				
			} catch(Exception e) {
				//System.out.println(d.getName());
			}
			
		}
		
	}
	
	private void elaboraFornitore(File d) {
		
		try {
			// Recupero i dati del fornitore
			_stmtBP.setString(1, d.getName());
			ResultSet rs = _stmtBP.executeQuery();
			if (rs.next()) {
				
				File[] listOfFiles = d.listFiles();
				InvoicePassive i;
				
				// Loop fra i file
				for (File f : listOfFiles) {
					
					i = extractFromFile(f, rs, d);
					if (i!=null)
						_fatture.add(i);
					
				}
				
			}
			
		
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	private InvoicePassive extractFromFile(File f, ResultSet rs, File d) throws SQLException, ParseException {
		
		String parsedText = "";
		
		try {
		
			InvoicePassive 	i = new InvoicePassive();
			i.set_filename(f.getName());
			i.set_fullpath(d.getName()+"/"+f.getName());
			
			parsedText = Util.parsePdf(f);
			
			// Replace text
			if (rs.getString(11)!=null) {
				String[] replace = rs.getString(11).split("\n");
				String[] part;
				for(String s : replace) {
					
					part = s.split("#");
					
					parsedText = parsedText.replace(part[0], part[1]);
					
				}
				
			}
			
			//System.out.println(parsedText);
					
			if (rs.getString(8)!=null)
				if (parsedText.contains(rs.getString(8))) {
					//System.out.println("["+d.getName()+"] ["+f.getName()+"]scartato");
					return null;
			}
								
			Extractor ex = new Extractor(parsedText);
			
			// POREFERENCE
			ex.set_pattern(rs.getString(1));
			i.set_poreference(ex.group(rs.getInt(2)));
			
			// DATEINVOICED
			if (rs.getString(3)!=null)
				ex.set_pattern(rs.getString(3));
			
			//System.out.println(ex.group(rs.getInt(4)));
			i.set_dateinvoiced(Util.getDate(ex.group(rs.getInt(4)), rs.getString(7)));
			
			
			// GRANDTOTAL
			if (rs.getString(5)!=null)
				ex.set_pattern(rs.getString(5));
			
			i.set_grandtotal(Util.getImporto(ex.group(rs.getInt(6))));
			
			i.set_c_bpartner_id(rs.getInt(10));
			i.set_sku(rs.getString(9));
			i.set_c_doctype_id(_c_doctype_id);
			
			// La data di ultima liquidazione IVA è successiva alla data della fattura. La fattura va inserita manualmente.
			if (dataIva.compareTo(i.get_dateinvoiced())>0) {
				
				//System.out.println("Data fattura successiva all'ultima liquidazione IVA. Inserire manualmente");
				Util.addError("Data fattura successiva all'ultima liquidazione IVA. Inserire manualmente ["+i.get_fullpath()+"]\n");
				
			}
			
			//System.out.println("["+d.getName()+"]["+i.get_poreference()+"] ["+i.get_dateinvoiced()+"] ["+i.get_price()+"]");
			//System.out.println("["+d.getName()+"]["+f.getName()+"]");
			
			return i;
		}
		catch (Exception e) {	
			Util.addError("{\n");
			Util.addError("["+d.getName()+"] ["+f.getName()+"]\n");
			Util.addError("\n");
			Util.addError(parsedText);
			Util.addError("}\n");
			Util.addError(e);
			return null;
		}
		
	}
	
}
