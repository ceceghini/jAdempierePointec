package it.pointec.adempiere;

import it.pointec.adempiere.model.Order;
import it.pointec.adempiere.model.Orders;
import it.pointec.adempiere.model.Product;
import it.pointec.adempiere.util.Util;
import it.pointec.adempiere.vendite.I_BPartner;
import it.pointec.adempiere.vendite.I_Product;

import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import org.compiere.util.DB;

import com.thoughtworks.xstream.XStream;

public class I_Order {
	
	private Hashtable<String, Order> _orders = new Hashtable<String, Order>();
	
	private I_Product _product;
	private I_BPartner _bpartner;

	public static void process() {
		
		I_Order o = new I_Order();
		
		o.initialCheck();
		Util.printErrorAndExit();
		
		o.downloadOrder();
		Util.printErrorAndExit();
		
		o.importIntoAdempiere();
		
	}
	
	private I_Order() {
		
		_product = new I_Product();
		_bpartner = new I_BPartner();
		
	}
	
	/**
	 * Verifica preventiva sulle tabelle di import 
	 */
	private void initialCheck() {
		
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
		
		n = DB.getSQLValue(null, "select count(*) n from i_order");
		if (n>0) {
			Util.addError("Record esistenti nella tabella i_order");
			return;
		}
		
	}
	
	/**
	 * Download degli ordini dai vari siti
	 */
	private void downloadOrder() {
		
		String last = "000003680";
		
		try {
			downloadFromMagento("http://www.lucebrillante.it", last);
			downloadFromMagento("http://www.tagliato.it", last);
			downloadFromMagento("http://www.stampaperfetta.it", last);
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
	private void downloadFromMagento(String baseURl, String last) throws IOException {
		
		// Download del file
		String url = baseURl + "/feed/adempiere/ordini.php?first_order_id="+last;
				
		Util.downloadFile(url, "/tmp/ordini.xml");
		
		FileReader f = new FileReader("/tmp/ordini.xml");
		
		// Conversione file xml
		XStream xstream = new XStream();
		xstream.processAnnotations(Orders.class);
		xstream.processAnnotations(Product.class);
				
		Orders orders = (Orders) xstream.fromXML(f);
		
		for (Order o : orders.getOrders()) {
			
			_bpartner.addBPartner(o.getBp());
			
			// Loop sui prodotti ordinati
			for (Product p : o.getProducts()) {
				
				_product.addProduct(p);
				
			}
			
			_orders.put(o.getOrder_id(), o);
			
		}
		
	}
	
	private void importIntoAdempiere() {
		
		_product.importIntoAdempiere();
		Util.printErrorAndExit();
		
		_product.process();
		Util.printErrorAndExit();
		
		_bpartner.importIntoAdempiere();
		Util.printErrorAndExit();
		
		_bpartner.process();
		Util.printErrorAndExit();
		
	}
}
