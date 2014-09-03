package it.pointec.adempiere.vendite;

import it.pointec.adempiere.model.Product;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.process.ImportProduct;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

public class I_Product {
	
	private PreparedStatement _stmt;
	private Hashtable<String, Product> _products = new Hashtable<String, Product>();
	
	public I_Product() {
		
		try {
			_stmt = DB.prepareStatement("insert into I_PRODUCT (ad_org_id, ad_client_id, i_product_id, c_bpartner_id, value, name, description, iso_code, pricelist, m_product_category_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", null);
			_stmt.setInt(1, Ini.getInt("ad_org_id"));
			_stmt.setInt(2, Ini.getInt("ad_client_id"));
			_stmt.setInt(4, Ini.getInt("c_bp_group_id"));
			_stmt.setString(8, "EUR");
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	protected void finalize () throws SQLException {
		_stmt.close();
	}

	/**
	 * Elaborazione prodotti presenti nelle tabelle di import
	 */
	public void process() {
		
		try {
			
			// Importazione prodotti
			String trxName = "processProduct";
			
			int AD_Process_ID =  MProcess.getProcess_ID("Import_Product", trxName);
			ProcessInfo pi = new ProcessInfo ("Import_Product",AD_Process_ID);
			
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
	        instance.saveEx();
	        
	        pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
	        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
	
	        //  Add Parameters
	        MPInstancePara para20 = new MPInstancePara(instance, 20);
	        para20.setParameter("AD_Client_ID", Ini.getInt("ad_client_id"));
	        para20.saveEx();
	        
	        MPInstancePara para10 = new MPInstancePara(instance, 10);
	        para10.setParameter("M_PriceList_Version_ID", Ini.getInt("m_pricelist_version_id"));
	        para10.saveEx();
	        
	        ImportProduct process = new ImportProduct();
	        
	        process.startProcess(Env.getCtx(), pi, Trx.get(trxName, false));     
	
	        //Verifica importazione
	        String sql = "select i_product_id, i_errormsg, value  from i_product where i_isimported <> 'Y'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next ()) {
				
				Util.addError("PRODOTTO NON IMPORTATO--> i_product_id: ["+rs.getString(1)+"] - value: ["+rs.getString(3)+"] - i_errormsg: ["+rs.getString(2)+"]\n");
				
			}
			
			pstmt.close();
			rs.close();
			
			DB.executeUpdate("delete from I_PRODUCT where i_isimported = 'Y'", null);
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Inserimento dei prodotti nelle tabelle di importo di adempiere
	 */
	public void importIntoAdempiere() {
		
		Product p;
		int id;
		
		try {
		
			for(String k : _products.keySet()){
				
				p = _products.get(k);
				
				Util.setCurrent(p.getSku());
				
				// Inserimento prodotti nella tabella di import
				id = Util.getNextSequence("i_product");
				_stmt.setInt(3, id);
				
				_stmt.setString(5, p.getSku());
				_stmt.setString(6, Util.trunc(p.getName(), 60));
				_stmt.setString(7, p.getName());
				_stmt.setBigDecimal(9, p.getPrice());
				_stmt.setInt(10, p.getM_product_category_id());
				
				_stmt.execute();
					
				Util.increaseSequence("i_product");
				
			}
		
			Util.setCurrent(null);
			
		}
		catch (Exception e) {
			Util.addError(e);
		}		
		
	}
	
	/**
	 * Aggiunta di un prodotto alla lista dei prodotti da importare
	 * @param p
	 */
	public void addProduct(Product p) {
		
		if (!_products.containsKey(p.getSku())) {
			_products.put(p.getSku(), p);
		}
		
	}
}
