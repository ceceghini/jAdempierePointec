package it.pointec.adempiere.vendite;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.model.XMLOrder;
import it.pointec.adempiere.model.XMLOrders;
import it.pointec.adempiere.model.XMLProduct;
import it.pointec.adempiere.process.ImportInvoice2;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;

import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.thoughtworks.xstream.XStream;

/***
 * Elaborazione delle Invoice provenienti dall'xml, inserimento nella tabella i_invoice
 * e successivo import nella tabella c_invoice
 * @author cesare
 *
 */
public class ProcessInvoice {

	private Hashtable<String, XMLOrder> _orders = new Hashtable<String, XMLOrder>();
	private PreparedStatement _stmt;
	private ProcessProduct _product;
	private ProcessBPartner _bpartner;
	
	private String _first;
	
	public ProcessInvoice() {
		
		_product = new ProcessProduct();
		_bpartner = new ProcessBPartner();
		
		try {
			_stmt = DB.prepareStatement("insert into I_INVOICE (ad_org_id, ad_client_id, i_invoice_id, c_doctype_id, documentno, issotrx, salesrep_id, c_paymentterm_id, C_BPartner_ID, dateinvoiced, dateacct, productvalue, qtyordered, priceactual, c_tax_id, taxamt, description, M_PriceList_ID, vatledgerdate, vatledgerno, poreference) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", null);
			_stmt.setInt(1, Ini.getInt("ad_org_id"));
			_stmt.setInt(2, Ini.getInt("ad_client_id"));
			_stmt.setString(6, "Y");
			_stmt.setInt(7, Ini.getInt("salesrep_id"));
			_stmt.setInt(15, Ini.getInt("c_tax_id"));
			_stmt.setInt(18, Ini.getInt("m_pricelist_version_id"));
			_stmt.setNull(5, Types.INTEGER);
			_stmt.setNull(20, Types.INTEGER);
			
			//_first = Ini.getString("first_invoice");
			_first="";
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	protected void finalize () throws SQLException {
		_stmt.close();
	}
	
	/**
	 * Verifica preventiva sulle tabelle di import 
	 */
	public void initialCheck() {
		
		int n = DB.getSQLValue(null, "select count(*) n from i_product");
		if (n>0) {
			Util.addError("Record esistenti nella tabella i_product");
			return;
		}
		
		n = DB.getSQLValue(null, "select count(*) n from i_bpartner");
		if (n>0) {
			Util.addError("Record esistenti nella tabella i_bpartner");
			return;
		}
		
		n = DB.getSQLValue(null, "select count(*) n from i_invoice");
		if (n>0) {
			Util.addError("Record esistenti nella tabella i_invoice");
			return;
		}
		
	}
	
	/**
	 * Download degli ordini dai vari siti
	 */
	public void downloadOrder() {
		
		//String last = "000003680";
		
		try {
			downloadFromMagento("http://www.lucebrillante.it");
			//downloadFromMagento("http://www.tagliato.it");
			downloadFromMagento("http://www.stampaperfetta.it");
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Download dell'ordine dal sito magento
	 * @param baseUrl	sito internet
	 * @throws IOException
	 */
	public void downloadFromMagento(String baseURl) throws IOException {
		
		// Download del file
		String url;
		
		url = baseURl + "/feed/adempiere/fatture.php?first_invoice_id="+_first;
		
		System.out.println("Elaborazione url ["+url+"]");
		Util.downloadFile(url, "/tmp/ordini.xml");
		
		FileReader f = new FileReader("/tmp/ordini.xml");
		
		// Conversione file xml
		XStream xstream = new XStream();
		xstream.processAnnotations(XMLOrders.class);
		xstream.processAnnotations(XMLOrder.class);
		xstream.processAnnotations(XMLProduct.class);
				
		XMLOrders orders = (XMLOrders) xstream.fromXML(f);
		
		if (orders.getOrders()==null)
			return;
		
		int n;
		
		for (XMLOrder o : orders.getOrders()) {
			
			n = DB.getSQLValue(null, "select count(poreference) from c_invoice where poreference = ?", o.getOrder_id());
			
			if (n==0) {
				_bpartner.addBPartner(o.getBp());
				
				// Loop sui prodotti ordinati
				for (XMLProduct p : o.getProducts()) {
					
					_product.addProduct(p);
					
				}
				
				_orders.put(o.getOrder_id(), o);
			}
			
		}
		
	}
	
	/**
	 * Verifica congruenza dei dati scaricati
	 */
	public void Check() {
		
		XMLOrder o;
		
		for(String k : _orders.keySet()){
			
			o = _orders.get(k);
			
			// Verifica totali
			BigDecimal shipping = o.getShipping_amount();
			
			BigDecimal fee;
			if (o.getCod_fee() == null)
				fee = new BigDecimal(0);
			else
				fee = o.getCod_fee();
			
			BigDecimal discount;
			if (o.getDiscountAmount() == null)
				discount = new BigDecimal(0);
			else
				discount = o.getDiscountAmount();
			
			BigDecimal chk_total = o.getItemAmount().add(fee).add(shipping).add(discount);
			BigDecimal chk_total2 = chk_total.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			
			if (o.getGrand_total().subtract(chk_total2).abs().compareTo(new BigDecimal(0.01))>0) {
				Util.addError("["+k+"] Totali non congruenti: ["+o.getGrand_total()+"] ["+chk_total+"] ["+chk_total2+"]\n");
			}	
						
		}
		
	}
	
	/**
	 * Importazione dei dati nella tabella i_invoice
	 */
	private void importIntoAdempiere() {
		
		XMLOrder o;
		MBPartner bp;
		int bpId;
		int id;
		
		try {
			
			for(String k : _orders.keySet()){
				
				o = _orders.get(k);
				o.addExtraProduct();
				
				if (o.getBp().getIs_business_address()==1)
					_stmt.setInt(4, Ini.getInt("doc_type_id_invoice"));
				else
					_stmt.setInt(4, Ini.getInt("doc_type_id_corrispettivo"));
				
				_stmt.setDate(10, o.getCreated_at());	
				_stmt.setDate(11, o.getCreated_at());	// Data fattura
				_stmt.setDate(19, o.getCreated_at());	// Data iva
				_stmt.setString(17, o.getLast_trans_id());
				
				_stmt.setString(21, o.getOrder_id());	// Riferimento ordine
				
				bp = null;
				bpId = 0;
				
				if (o.getBp().getTaxcode()!=null)
					bp = MBPartner.get(Env.getCtx(), o.getBp().getTaxcode());
				if (bp == null && o.getBp().getVatnumber()!=null)
					bp = MBPartner.get(Env.getCtx(), o.getBp().getVatnumber());
				if (bp == null && o.getBp().getEmail()!=null) {
					
					bpId = DB.getSQLValue(null, "select C_BPARTNER_ID from ad_user where ad_client_id = ? and ad_org_id = ? and email = ?", Ini.getInt("ad_client_id"), Ini.getInt("ad_org_id"), o.getBp().getEmail());
					if (bpId > 0) 
						bp = MBPartner.get(Env.getCtx(), bpId);
					
				}
				
				if (bp == null)
					Util.addError("BP non trovato ["+o.getOrder_id()+"] ["+o.getBp().getTaxcode()+"] ["+o.getBp().getVatnumber()+"] ["+o.getBp().getCompany()+"]\n");
				else
					_stmt.setInt(9, bp.get_ID());
				
				// Metodo di pagamento
				if (o.getPayment_method().compareTo("cashondelivery")==0)
					_stmt.setInt(8, Ini.getInt("paymenttem_contrassegno"));
				else if (o.getPayment_method().compareTo("paypal_standard")==0)
					_stmt.setInt(8, Ini.getInt("paymenttem_paypal"));
				else if (o.getPayment_method().compareTo("bankpayment")==0)
					_stmt.setInt(8, Ini.getInt("paymenttem_banca"));
				else if (o.getPayment_method().compareTo("banktransfer")==0)
					_stmt.setInt(8, Ini.getInt("paymenttem_banca"));
				else if (o.getPayment_method().compareTo("amazon")==0)
					_stmt.setInt(8, Ini.getInt("paymenttem_amazon"));
				else {
					Util.addError("[" + o.getPayment_method() + "] metodo di pagamento non gestito\n");
					return;
				}
				
				
				// Loop fra i prodotti
				
				for (XMLProduct p : o.getProducts()) {
				
					// Inserimento prodotti nella tabella di import
					id = Util.getNextSequence("i_invoice");
					_stmt.setInt(3, id);
					
					_stmt.setString(12, p.getSku());
					_stmt.setInt(13, p.getQty_ordered());
					_stmt.setBigDecimal(14, p.getPriceWithTax());
					_stmt.setBigDecimal(16, p.getTax_amount());
					
					_stmt.execute();
						
					Util.increaseSequence("i_invoice");
					
				}
				
			}
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/***
	 * Chiamata al processo adempiere che effettua l'import delle fatture
	 */
	private void process() {
		
		try {
			
			String trxName = "processInvoice";
			
			int AD_Process_ID =  MProcess.getProcess_ID("Import_Invoice", trxName);
			ProcessInfo pi = new ProcessInfo ("Import_Invoice",AD_Process_ID);
			
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
	        instance.saveEx();
	        
	        pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
	        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
	
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
	        
	        ImportInvoice2 process = new ImportInvoice2();
	        
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
	
	/**
	 * Verifiche ed aggiornamenti da effettuarsi post inserimento
	 */
	private void postProcess() {
		
		try {
			PreparedStatement stmt = DB.prepareStatement("update c_invoice set C_BANKACCOUNT_ID = ? where C_PAYMENTTERM_ID = ? and C_DOCTYPE_ID in (?, ?) and C_BANKACCOUNT_ID is null", null);
			
			stmt.setInt(1, Ini.getInt("paymenttem_amazon"));
			stmt.setInt(2, Ini.getInt("bankaccount_amazon"));
			stmt.setInt(3, Ini.getInt("doc_type_id_invoice"));
			stmt.setInt(4, Ini.getInt("doc_type_id_corrispettivo"));
			stmt.executeUpdate();
			
			stmt.setInt(1, Ini.getInt("paymenttem_contrassegno"));
			stmt.setInt(2, Ini.getInt("bankaccount_contrassegno"));
			stmt.executeUpdate();
			
			stmt.setInt(1, Ini.getInt("paymenttem_paypal"));
			stmt.setInt(2, Ini.getInt("bankaccount_paypal"));
			stmt.executeUpdate();
			
			stmt.setInt(1, Ini.getInt("paymenttem_banca"));
			stmt.setInt(2, Ini.getInt("bankaccount_banca"));
			stmt.executeUpdate();
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Chiamata alle varie procedure di inserimento dei prodotti, bpartner e invoice
	 */
	public void importAndProcess() {
		
		if (Ini.getBoolean("import_product")) {
			_product.importIntoAdempiere();
			Util.printErrorAndExit();
		
			_product.process();
			Util.printErrorAndExit();
		}
		
		if (Ini.getBoolean("import_bp")) {
			_bpartner.importIntoAdempiere();
			Util.printErrorAndExit();
		
			_bpartner.process();
			Util.printErrorAndExit();
		}
		
		if (Ini.getBoolean("import_invoice")) {
			importIntoAdempiere();
			Util.printErrorAndExit();
		
			process();
			Util.printErrorAndExit();
			
			postProcess();
			Util.printErrorAndExit();
		}
	}
	
	/**
	 * Metodo statico per l'esecuzione
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		// Fatture
		System.out.println("INIZIO ELABORAZIONE INVOICE");
		ProcessInvoice o = new ProcessInvoice();
		
		o.initialCheck();
		Util.printErrorAndExit();
		
		o.downloadOrder();
		Util.printErrorAndExit();
		
		o.Check();
		Util.printErrorAndExit();
		
		o.importAndProcess();
		Util.printErrorAndExit();
		
	}
}
