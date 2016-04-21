package it.pointec.adempiere.archive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MPInstance;
import org.compiere.model.MPeriodControl;
import org.compiere.model.MProcess;
import org.compiere.model.MTable;
import org.compiere.process.PeriodControlStatus;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import it.pointec.adempiere.Adempiere;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

public class ClosePeriod {

	public static void main(String[] args) {
		
		// Inizializzazione adempiere
		Adempiere a = new Adempiere();
		a.inizializza();
		
		elabora();

	}
	
	private static void elabora()  {
		
		try {
			String sql = "select pc.C_PERIODCONTROL_ID, pc.PERIODSTATUS from C_Year y join C_Period p on p.C_YEAR_ID = y.C_YEAR_ID join C_PeriodControl pc on pc.C_PERIOD_ID = p.C_PERIOD_ID where y.ad_client_id = ? and y.fiscalyear = ? and p.periodno <= ? and pc.periodstatus <> 'C'";
			PreparedStatement stmt = DB.prepareStatement(sql, null);
			stmt.setInt(1, Ini.getInt("ad_client_id"));
			stmt.setInt(2, Ini.getInt("generate_invoice_year"));
			stmt.setInt(3, Ini.getInt("period_close"));
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				Close(rs);
				
			}
		}
		catch (Exception e) {
			Util.addError(e);
		}
		
	}
	
private static void Close(ResultSet rs) {
		
		try {
			
			MPeriodControl p = new MPeriodControl(Env.getCtx(), rs.getInt(1), null);
			
			p.set_ValueOfColumn("PeriodAction", "C");
			p.save();
			
			String trxName = "C_PeriodControl_Process";
			
			int AD_Process_ID =  MProcess.getProcess_ID("C_PeriodControl_Process", trxName);
			ProcessInfo pi = new ProcessInfo ("C_PeriodControl_Process",AD_Process_ID);
			
			MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
	        instance.saveEx();
	        
	        pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
	        pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
	        pi.setTable_ID(MTable.getTable_ID("C_PeriodControl"));
	        pi.setRecord_ID(rs.getInt(1));
	
	        PeriodControlStatus process = new PeriodControlStatus();
	        
	        process.startProcess(Env.getCtx(), pi, Trx.get(trxName, false));
			
		}
		catch (Exception e) {
			Util.addError(e);
		}	
		
	}
	
}
