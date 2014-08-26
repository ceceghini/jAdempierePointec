package it.pointec.adempiere.bankstatement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MTable;
import org.compiere.process.BankStatementPayment;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import it.adempiere.pointec.util.Ini;
import it.adempiere.pointec.util.Util;
import it.pointec.adempiere.Adempiere;

public class Riconcilia {
	
	private String _sql;
	private String _sqlR;
	private String date2;
	private String date5;
	private String date7;
	private String date9;
	private String date11;
	private String date13;
	private String date15;
	private String date17;
	private String date19;
	
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		elabora();
		
	}
	
	public static void elabora() {
		
		Riconcilia r = new Riconcilia();
		
		r.riconciliaDB();
		
		r.setBusinessPartner();
		
		r.setAddebiti();
		
		r.elaboraPagamenti();
		
		r.postElaborazione();
		
	}

	public Riconcilia() {
		
		_sql  = "select l.c_bankstatementline_id, i.c_invoice_id, i.c_bpartner_id"
				  + "	from c_bankstatementline l"
				  + "	  join c_invoice i "
				  + "        on l.ad_client_id = i.ad_client_id"
				  + "       and l.ad_org_id = i.ad_org_id"
				  + "       and i.c_payment_id is null"
				  + "       and i.ISPAID = 'N'"
				  + "       and i.c_doctype_id = " + Ini.getString("doc_type_id_invoice")
				  + "       and abs (l.trxamt - i.grandtotal) <= 0.03"
				  + "	  join c_bpartner b"
				  + "	    on i.c_bpartner_id = b.c_bpartner_id"
				  + "	  join c_bankstatement bs"
				  + "	    on l.c_bankstatement_id = bs.c_bankstatement_id"
				  + " where memo = 'daelaborare'"
				  + "   and l.ad_client_id = ?"
				  + "   and l.ad_org_id = ?";
		
		_sqlR  = "select l.c_bankstatementline_id, i.c_invoice_id, i.c_bpartner_id"
				  + "	from c_bankstatementline l"
				  + "	  join c_invoice i "
				  + "        on l.ad_client_id = i.ad_client_id"
				  + "       and l.ad_org_id = i.ad_org_id"
				  + "       and i.c_payment_id is null"
				  + "       and i.ISPAID = 'N'"
				  + "       and i.c_doctype_id = " + Ini.getString("doc_type_id_invoice_acq")
				  + "       and abs (l.trxamt + i.grandtotal) <= 0.01"
				  + "	  join c_bpartner b"
				  + "	    on i.c_bpartner_id = b.c_bpartner_id"
				  + "	  join c_bankstatement bs"
				  + "	    on l.c_bankstatement_id = bs.c_bankstatement_id"
				  + " where memo = 'daelaborare'"
				  + "   and l.ad_client_id = ?"
				  + "   and l.ad_org_id = ?";
		
		date2 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 2) or"
		      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 2))";
		
		date5 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 5) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 5))";
		
		date7 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 7) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 7))";
		
		date9 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 9) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 9))";
		
		date11 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 11) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 11))";
		
		date13 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 13) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 13))";
		
		date15 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 15) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 15))";
		
		date17 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 17) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 17))";
		
		date19 = "   and ((l.valutadate between i.dateinvoiced and i.dateinvoiced + 19) or"
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 19))";
		
		DB.executeUpdate("update c_bankstatementline set c_invoice_id = null, c_bpartner_id = null where ad_client_id = 1000002 and ad_org_id = 1000002 and c_payment_id is null", null);
		
	}
	
	public void riconciliaDB() {
		
		try {
			String sql = "select query, acquisti, vendite from POINTEC_RICONCILIAZIONI where active = 'Y'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			
	        while (rs.next ()) {
				
	        	if (rs.getString(3).compareTo("Y")==0) {
		        	eseguiRiconciliazione(false, date2, rs.getString(1));
		        	eseguiRiconciliazione(false, date5, rs.getString(1));
		        	eseguiRiconciliazione(false, date7, rs.getString(1));
		        	eseguiRiconciliazione(false, date9, rs.getString(1));
		        	eseguiRiconciliazione(false, date11, rs.getString(1));
		        	eseguiRiconciliazione(false, date13, rs.getString(1));
		        	eseguiRiconciliazione(false, date15, rs.getString(1));
		        	eseguiRiconciliazione(false, date17, rs.getString(1));
		        	eseguiRiconciliazione(false, date19, rs.getString(1));
	        	}
	        	
	        	if (rs.getString(2).compareTo("Y")==0) {
	        		eseguiRiconciliazione(true, date2, rs.getString(1));
	        		eseguiRiconciliazione(true, date5, rs.getString(1));
	        		eseguiRiconciliazione(true, date7, rs.getString(1));
	        		eseguiRiconciliazione(true, date9, rs.getString(1));
	        		eseguiRiconciliazione(true, date11, rs.getString(1));
		        	eseguiRiconciliazione(true, date13, rs.getString(1));
		        	eseguiRiconciliazione(true, date15, rs.getString(1));
		        	eseguiRiconciliazione(true, date17, rs.getString(1));
		        	eseguiRiconciliazione(true, date19, rs.getString(1));
	        	}
				
			}
	        
	        pstmt.close();
	        rs.close();
	        
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	private void eseguiRiconciliazione(boolean bReverse, String date, String append) {
		
		StringBuffer sql;
		
		if (bReverse)
			sql = new StringBuffer(_sqlR);
		else
			sql = new StringBuffer(_sql);
		
		sql.append(append);
		
		sql.append(date);
		
		//System.out.println(sql);
		riconciliaAndInsert(sql.toString());
		Util.printErrorAndExit();
		
	}
	
	
	/***
	 * Abbinamento fattura riga estratto conto
	 * @param sql
	 */
	public void riconciliaAndInsert(String sql) {
		
		try {
			
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			
			PreparedStatement stmtU = DB.prepareStatement("update c_bankstatementline set c_invoice_id = ?, c_bpartner_id = ? where c_bankstatementline_id = ?", null);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next ()) {
				
				stmtU.setInt(1, rs.getInt(2));
				stmtU.setInt(2, rs.getInt(3));
				stmtU.setInt(3, rs.getInt(1));
				
				stmtU.executeUpdate();
				
			}
			
			DB.close(rs);
			DB.close(stmt);
			DB.close(stmtU);
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/***
	 * Imposta il bp e crea un pagamento senza fattura associata. BP tipo dicon
	 */
	public void setBusinessPartner() {
		
		// DICON
		DB.executeUpdate("update c_bankstatementline set c_bpartner_id = 1000291 where c_bpartner_id is null and upper(eftmemo) like '%DICON SRL%' and memo = 'daelaborare' and c_payment_id is null", null);
		
		// Bernardina Maschione
		DB.executeUpdate("update c_bankstatementline set c_bpartner_id = 1000828 where c_bpartner_id is null and lower(eftmemo) like 'mascione bernardina d.i.%' and memo = 'daelaborare' and c_payment_id is null", null);
		
		// a2zworld
		DB.executeUpdate("update c_bankstatementline set c_bpartner_id = 1000759 where c_bpartner_id is null and upper(eftmemo) like 'GIROCONTO A2Z WORLD SRL%' and memo = 'daelaborare' and c_payment_id is null", null);
		
		// life 365
		DB.executeUpdate("update c_bankstatementline set c_bpartner_id = 1000833 where c_bpartner_id is null and upper(eftmemo) like '%LIFE365%' and memo = 'daelaborare' and c_payment_id is null", null);
		
		// gbf
		DB.executeUpdate("update c_bankstatementline set c_bpartner_id = 1000838 where c_bpartner_id is null and upper(eftmemo) like '%GBF SRL UNIPERSONALE%' and memo = 'daelaborare' and c_payment_id is null", null);
		
	}
	
	public void setAddebiti() {
		
		try {
			String sql = "select query from POINTEC_ADDEBITI where active = 'Y'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			
	        while (rs.next ()) {
				
	        	DB.executeUpdate(rs.getString(1), null);
				
			}
	        
	        pstmt.close();
	        rs.close();
	        
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	private void postElaborazione() {
		
		DB.executeUpdate(
				"update c_bankstatementline" +
				"	set memo = null" +
				" where (c_payment_id is not null or c_charge_id is not null)" +
				"   and memo = 'daelaborare'" +
				"   and ad_client_id = " + Ini.getString("ad_client_id") +
				"   and ad_org_id = " + Ini.getString("ad_org_id"), null);
		
	}
	
	/***
	 * Elaborazione pagamenti in sospeso
	 */
	public void elaboraPagamenti() {
		
		try {
			
			// Pagamenti 1-1 con fattura
			PreparedStatement stmt = DB.prepareStatement("select c_bankstatementline_id from c_bankstatementline where c_invoice_id is not null and c_payment_id is null and ad_client_id = ? and ad_org_id = ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			ResultSet rs = stmt.executeQuery();
			while (rs.next ()) {
				
				elaboraPagamento(rs.getInt(1));
				
			}
			
			rs.close();
			stmt.close();
			
			// Pagamenti solo BP
			stmt = DB.prepareStatement("select c_bankstatementline_id from c_bankstatementline where c_bpartner_id is not null and c_payment_id is null and ad_client_id = ? and ad_org_id = ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			rs = stmt.executeQuery();
			while (rs.next ()) {
				
				elaboraPagamento(rs.getInt(1));
				
			}
			
			rs.close();
			stmt.close();
			
			
		}
		catch (Exception e) {
			//e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			Util.addError(sw.toString());
		}
		
	}
	
	/***
	 * Elaborazione line estratto conto e creazione pagamento 
	 * @param recordID
	 */
	private void elaboraPagamento(int recordID) {
		
		try {
			
			// Importazione prodotti
			String trxName = "elaboraPagamento";
			
			int AD_Process_ID =  MProcess.getProcess_ID("C_BankStatement Payment", trxName);
			ProcessInfo pi = new ProcessInfo ("C_BankStatement Payment",AD_Process_ID);
			
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
	        instance.saveEx();
	        
	        pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
	        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
	        pi.setTable_ID(MTable.getTable_ID("C_BankStatementLine"));
	        pi.setRecord_ID(recordID);
	
	        BankStatementPayment process = new BankStatementPayment();
	        
	        process.startProcess(Env.getCtx(), pi, Trx.get(trxName, false));
			
		}
		catch (Exception e) {
			Util.addError(e);
		}		
		
	}
}
