package it.pointec.adempiere.bankstatement;

public interface I_Source {

	public int get_c_bankaccount_id();

	public int get_c_charge_id();

	public String get_subpath();
	
	public String get_extension();
	
	public String get_name();
	
	public String get_dateformat();
	
	public void insertIntoAdempiere(String file) throws Exception;
}
