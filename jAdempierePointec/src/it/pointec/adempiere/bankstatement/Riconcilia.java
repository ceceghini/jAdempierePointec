package it.pointec.adempiere.bankstatement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MBankStatement;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MTable;
import org.compiere.process.BankStatementPayment;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class Riconcilia {
	
	/*private String _sql;
	private String _sqlR;
	private String date2;
	private String date5;
	private String date7;
	private String date9;
	private String date11;
	private String date13;
	private String date15;
	private String date17;
	private String date19;*/
	
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		elabora();
		
	}
	
	public static void elabora() {
		
		Riconcilia r = new Riconcilia();
		
		r.execute_RICONCILIA_UPDATE();
		
		r.riconciliaVendite();
		
		r.elaboraPagamenti();
		
		r.postElaborazione();
		
		r.CompletaEstrattoConto();
		
		Util.printErrorAndExit();
		
	}

	public Riconcilia() {
		
		/*_sql  = "select l.c_bankstatementline_id, i.c_invoice_id, i.c_bpartner_id"
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
			      + "       (i.dateinvoiced between l.valutadate and l.valutadate + 19))";*/
		
		DB.executeUpdate("update c_bankstatementline set c_invoice_id = null, c_bpartner_id = null where ad_client_id = 1000002 and ad_org_id = 1000002 and c_payment_id is null", null);
		
	}
	
	/*private void riconciliaDB() {
		
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
		
	}*/
	
	/*private void eseguiRiconciliazione(boolean bReverse, String date, String append) {
		
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
		
	}*/
	
	private void riconciliaVendite() {
		
		
		try {
		
			int invoice_id = 0;
			String sql = "select c_bankstatementline_id, efttrxid, eftmemo, eftamt, eftstatementlinedate from C_BANKSTATEMENTLINE where memo = 'daelaborare' and EFTAMT > 0 and ad_client_id = ? and ad_org_id = ?";
			//sql += " and eftmemo = 'BONIFICO A VOSTRO FAVORE ORDINANTE: UNISID SRL CAUSALE: Note: SALDO ORDINE N. 4473 DEL 02 04 201'";
			
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next ()) {
				
				invoice_id = 0;
				
				// Se è presente il trxid recupero la fattura per trxid
				if (rs.getString(2)!=null) {
					
					invoice_id = getInvoiceByTrxId(rs.getString(2));
					
					if (invoice_id!=0) {
						elaboraLinea(rs.getInt(1), invoice_id);
					}
					
				}
				
				if (invoice_id == 0) {
					// il trxid non è presente oppure non è stata trovata la fattura procedo con dei like
					invoice_id = getInvoiceByLike(rs.getString(3), rs.getBigDecimal(4), rs.getDate(5), false, false);
					if (invoice_id==0)
						invoice_id = getInvoiceByLike(rs.getString(3), rs.getBigDecimal(4), rs.getDate(5), true, false);
					if (invoice_id==0)
						invoice_id = getInvoiceByLike(rs.getString(3), rs.getBigDecimal(4), rs.getDate(5), false, true);
					
					if (invoice_id!=0) {
						elaboraLinea(rs.getInt(1), invoice_id);
					}
					
					
					
				}
				
			}
			
			rs.close();
			stmt.close();
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/**
	 * Recupero l'invoice id sulla base della transazione
	 * @param trx
	 * @return
	 */
	private int getInvoiceByTrxId(String trx) {
		
		try {
			
			String sql = "select c_invoice_id from c_invoice where ad_client_id = ? and ad_org_id = ? and description = ?";
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setString(3, trx);
						
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int invoice_id = rs.getInt(1);
				rs.close();
				stmt.close();
				return invoice_id;
			}
			else {
				rs.close();
				stmt.close();
				return 0;
			}
			
		}
		catch (Exception e) {
			Util.addError(e);
			return 0;
		}
		
		
	}
	
	private int getInvoiceByLike (String eftmemo, BigDecimal eftamt, Date eftstatementlinedate, boolean dateInvoiceEqual, boolean dateInvoceMinus) {
		
		try {
			String sql = "select query from POINTEC_RICONCILIAZIONI where active = 'Y' order by sort";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			int invoice_id;
			
			while (rs.next ()) {
				
				invoice_id = getInvoice(eftmemo, eftamt, rs.getString(1), eftstatementlinedate, dateInvoiceEqual, dateInvoceMinus);
				if (invoice_id != 0) {
					rs.close();
					pstmt.close();
					return invoice_id;
				}
				
			}
			rs.close();
			pstmt.close();
			return 0;
			
		}
		catch (Exception e) {
			Util.addError(e);
			return 0;
		}
		
	}
	
	private int getInvoice(String eftmemo, BigDecimal eftamt, String query, Date eftstatementlinedate, boolean dateInvoiceEqual, boolean dateInvoceMinus) {
		
		try {
			
			// Verifico il numero di righe
			String sql = "select count(i.c_invoice_id)"
					   + "  from c_invoice i"
					   + "    join C_BPARTNER b"
					   + "		on i.c_bpartner_id = b.c_bpartner_id"
					   + " where docstatus = 'CO'"
					   + "   and i.ISPAID = 'N'"
					   + "   and c_doctype_id in (1000002, 1000042)"
					   + "   and i.ad_client_id = ? and i.ad_org_id = ?"
					   + "   and i.grandtotal = ?";
			sql += query;
			
			if (dateInvoiceEqual)
				sql += " and i.dateinvoiced = ?";
			
			if (dateInvoceMinus)
				sql += " and i.dateinvoiced < ?";
		
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setBigDecimal(3, eftamt);
			stmt.setString(4, eftmemo);
			
			if (dateInvoiceEqual || dateInvoceMinus)
				stmt.setDate(5, eftstatementlinedate);
			
			ResultSet rs = stmt.executeQuery();
			
			rs.next();
			int numeroRecord = rs.getInt(1);
			
			rs.close();
			stmt.close();
			
			if (numeroRecord == 0) 
				return 0;
			
			sql = "select i.c_invoice_id"
					   + "  from c_invoice i"
					   + "    join C_BPARTNER b"
					   + "		on i.c_bpartner_id = b.c_bpartner_id"
					   + " where docstatus = 'CO'"
					   + "   and i.ISPAID = 'N'"
					   + "   and c_doctype_id in (1000002, 1000042)"
					   + "   and i.ad_client_id = ? and i.ad_org_id = ?"
					   + "   and i.grandtotal = ?";
			sql += query;
			
			if (dateInvoiceEqual)
				sql += " and i.dateinvoiced = ?";
			
			if (dateInvoceMinus)
				sql += " and i.dateinvoiced < ?";
			
			stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			stmt.setBigDecimal(3, eftamt);
			stmt.setString(4, eftmemo);
			
			
			if (dateInvoiceEqual || dateInvoceMinus)
				stmt.setDate(5, eftstatementlinedate);
			
			rs = stmt.executeQuery();
			
			rs.next();
			int id_invoice = rs.getInt(1);
			rs.close();
			stmt.close();
			return id_invoice;
						
			
		}
		catch (Exception e) {
			Util.addError(e);
			return 0;
		}
		
	}
	
	private void elaboraLinea(int c_bankstatementline_id, int c_invoice_id) {
		
		try {
			
			String sql = "update C_BANKSTATEMENTLINE set memo = null, (c_bpartner_id, c_invoice_id) = (select c_bpartner_id, c_invoice_id from c_invoice where c_invoice_id = ?) where c_bankstatementline_id = ?";
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, c_invoice_id);
			stmt.setInt(2, c_bankstatementline_id);
			
			stmt.execute();
			
			stmt.close();
			
			elaboraPagamento(c_bankstatementline_id);
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	/***
	 * Abbinamento fattura riga estratto conto
	 * @param sql
	 */
	/*private void riconciliaAndInsert(String sql) {
		
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
	*	
	}*/
	
	private void execute_RICONCILIA_UPDATE() {
		
		try {
			String sql = "select query from POINTEC_RICONCILIA_UPDATE where active = 'Y'";
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
				" where c_payment_id is not null" +
				"   and memo = 'daelaborare'" +
				"   and ad_client_id = " + Ini.getString("ad_client_id") +
				"   and ad_org_id = " + Ini.getString("ad_org_id"), null);
		
	}
	
	/***
	 * Elaborazione pagamenti in sospeso
	 */
	private void elaboraPagamenti() {
		
		try {
			
			// Pagamenti solo BP
			PreparedStatement stmt = DB.prepareStatement("select c_bankstatementline_id from c_bankstatementline where c_bpartner_id is not null and c_payment_id is null and ad_client_id = ? and ad_org_id = ?", null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("ad_org_id"));
			ResultSet rs = stmt.executeQuery();
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
	
	private void CompletaEstrattoConto() {
		
		MBankStatement bs;
		
		try {
			String sql = "select b.name"
					   + "     , b.c_bankstatement_id"
//				       + "	   , (select count(*) from c_bankstatementline l2 where b.c_bankstatement_id = l2.c_bankstatement_id) a"
//				       + "	   , (select count(*) from c_bankstatementline l2 where b.c_bankstatement_id = l2.c_bankstatement_id and l2.memo = 'daelaborare') b"
				       + "  from c_bankstatement b"
				       + " where (select count(*) from c_bankstatementline l2 where b.c_bankstatement_id = l2.c_bankstatement_id and l2.memo = 'daelaborare') = 0"
				       + "   and b.docstatus <> 'CO'"
				       + "   and b.ad_client_id = ?";
			
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, Ini.getInt("ad_client_id"));
			ResultSet rs = pstmt.executeQuery();
			
	        while (rs.next ()) {
			
	        	System.out.println("Completamento estratto conto: ["+rs.getString(1)+"]");
	        	
	        	bs = new MBankStatement(Env.getCtx(), rs.getInt(2), null);
	        	
	        	bs.processIt("PR");
				bs.processIt("CO");
				
				bs.save();
				
				System.out.println("Completamento estratto conto: ["+rs.getString(1)+"] completata con successo");
				
			}
	        
	        pstmt.close();
	        rs.close();
	        
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
}
