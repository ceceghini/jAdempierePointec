package it.pointec.adempiere.bankstatement;

public interface I_I_BankStatement_Source {
	
	public int get_c_bankaccount_id();
	
	public int get_c_charge_id();
	
	public boolean has_c_charge_id();
	
	public String get_name();
	
	public boolean is_from_file();
	
	public String get_subpath();
	
	public String insertIntoAdempiere(String file, I_BankStatement bs) throws Exception;
		
}
