package it.pointec.adempiere.bankstatement;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.process.ImportBankStatement2;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

public class I_BankStatement {
	
	private PreparedStatement _stmt;
	private String _startpath;
	private I_Source _source;
	private int _i;
	private String _bs_name;
	private String _year;
		
	/**
	 * Costrutture
	 */
	public I_BankStatement() {
		
		_startpath = Util.getDaElaborare("bankstatement");
		_source = (I_Source) this;
		
		try {
			
			_stmt = DB.prepareStatement("INSERT INTO I_BANKSTATEMENT (AD_ORG_ID, AD_CLIENT_ID, I_BANKSTATEMENT_ID, C_BANKACCOUNT_ID, EFTSTATEMENTLINEDATE, TRXAMT, EFTTRXID, EFTAMT, CHARGEAMT, STMTAMT, NAME, C_CHARGE_ID, LINE, DATEACCT, VALUTADATE, LINEDESCRIPTION, EFTMEMO, STATEMENTLINEDATE, STATEMENTDATE, MEMO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", null);
			
			_stmt.setInt(1, Ini.getInt("ad_org_id"));
			_stmt.setInt(2, Ini.getInt("ad_client_id"));
			_stmt.setInt(4, _source.get_c_bankaccount_id());
			
			if (_source.get_c_charge_id() > 0)
				_stmt.setInt(12, _source.get_c_charge_id());
			else
				_stmt.setNull(12, 1);
			
			_stmt.setString(20, "daelaborare");
			
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	public void initialize() {
		
		_i = 0;
		_bs_name = null;
		_year = null;
		
	}
	
	/**
	 * Chiusura della classe
	 */
	protected void finalize () throws SQLException {
		_stmt.close();
	}
	
	/**
	 * Import in adempiere
	 */
	public void importIntoAdempiere() {
		
		try {
			
			// Verifica se ci sono dati nella tabella di importazione
			int n = DB.getSQLValue(null, "select count(*) n from i_bankstatement");
			
			if (n>0) {
				Util.addError("Record esistenti nella tabella i_bankstatement");
				return;
			}
			
			String path = _startpath + "/" + _source.get_subpath();
			
			Util.debug("Elaborazione directory ["+path+"]");
		
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
						
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++)  {
					
					initialize();
					
					Util.debug("Elaborazione file ["+path+"/"+listOfFiles[i].getName()+"]");
					_source.insertIntoAdempiere(path+"/"+listOfFiles[i].getName());
					
					Util.printErrorAndExit();
					
					process();
					
					Util.printErrorAndExit();
					
					FileElaborato(path+"/"+listOfFiles[i].getName());
					
					Util.printErrorAndExit();
					 
				}
			}

		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	public void insertLineIntoAdempiere(I_BankStatement_Line l) throws SQLException {
		
		int id = Util.getNextSequence("i_bankstatement");
		_i++;
		
		if (_bs_name==null) {
			
			DateFormat dateFormat = new SimpleDateFormat(_source.get_dateformat());
			_bs_name = _source.get_name() + " [" + dateFormat.format(l.get_date()) + "]";
			
			dateFormat = new SimpleDateFormat("yyyy");
			_year = dateFormat.format(l.get_date());
			
		}
		
		_stmt.setInt(3, id);
		_stmt.setDate(5, l.get_date());
		_stmt.setBigDecimal(6, l.get_gross_amount());
		_stmt.setString(7, l.get_trxid());
		_stmt.setBigDecimal(8, l.get_gross_amount());
		_stmt.setBigDecimal(9, l.get_charge_amount());
		_stmt.setBigDecimal(10, l.get_net_amount());
		_stmt.setInt(13, _i);
		_stmt.setDate(14,  l.get_date());
		_stmt.setDate(15,  l.get_date());
		_stmt.setString(16, l.get_description255().trim());
		_stmt.setString(17, l.get_description255().trim());
		_stmt.setDate(18, l.get_date());
		_stmt.setString(11, _bs_name);
		
		Calendar nextNotifTime = Calendar.getInstance();
		nextNotifTime.setTime(l.get_date());
		nextNotifTime.add(Calendar.MONTH, 1);
		nextNotifTime.set(Calendar.DATE, 1);
		nextNotifTime.add(Calendar.DATE, -1);
		_stmt.setDate(19,  new Date(nextNotifTime.getTimeInMillis()));
		
		_stmt.execute();
		
		Util.increaseSequence("i_bankstatement");
		
	}
	
	public void process() {
		
		try {
			
			String trxName = "processBankStatement";
			
			int AD_Process_ID =  MProcess.getProcess_ID("Import_BankStatement", trxName);
			ProcessInfo pi = new ProcessInfo ("Import_BankStatement",AD_Process_ID);
			
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
	        instance.saveEx();
	        
	        pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
	        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
	
	        //  Add Parameters
	        MPInstancePara para10 = new MPInstancePara(instance, 10);
	        para10.setParameter("AD_Client_ID", Ini.getInt("ad_client_id"));
	        para10.saveEx();
	        	        
	        MPInstancePara para20 = new MPInstancePara(instance, 20);
	        para20.setParameter("AD_Org_ID", Ini.getInt("ad_org_id"));
	        para20.saveEx();
	        
	        MPInstancePara para30 = new MPInstancePara(instance, 30);
	        para30.setParameter("C_BankAccount_ID", _source.get_c_bankaccount_id());
	        para30.saveEx();
	        
	        ImportBankStatement2 process = new ImportBankStatement2();
	        
	        process.startProcess(Env.getCtx(), pi, Trx.get(trxName, false));     
	
	        //Verifica importazione
	        String sql = "select i_bankstatement_id, i_errormsg  from i_bankstatement where i_isimported <> 'Y'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next ()) {
				
				Util.addError("MOVIMENTO BANCARIO NON IMPORTATO --> i_bankstatement_id: ["+rs.getString(1)+"] - i_errormsg: ["+rs.getString(2)+"]\n");
				
			}
			
			DB.executeUpdate("delete from I_BANKSTATEMENT where i_isimported = 'Y'", null);
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
	public void FileElaborato(String source) throws IOException {
		
		if (Util.HasError())
			return;
		
		File f_source = new File(source);
		
		String dest = Util.getArchivio("estratti conto", _year) + "/" + _source.get_subpath();
				
		Util.moveFile(f_source.getParent(), dest, f_source.getName(), _bs_name + "." + _source.get_extension());
		
	}
	
	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		Util.debug("Import bank statement");
		
		Import();
		Util.printErrorAndExit();
				
	}
	
	/***
	 * Import degli estratti conti bancari
	 */
	private static void Import() {
		
		I_BankStatement i;
		// CRGiovo
		i = new CRGiovo();
		
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// IWBank
		i = new IWBank();
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// CartaIW
		//i = new CartaIW();
		//i.importIntoAdempiere();
		//Util.printErrorAndExit();
		
		// Paypal
		i = new Paypal();
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// Sda
		i = new Sda();
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
		// Amazon
		//i = new Amazon();
		//i.importIntoAdempiere();
		//Util.printErrorAndExit();
		
		// Cartasi
		i = new CartaSi();
		i.importIntoAdempiere();
		Util.printErrorAndExit();
		
	}
	
}
