package it.pointec.adempiere.bankstatement;

import java.math.BigDecimal;
import java.sql.Date;

public class I_BankStatement_Line {

	private Date _date;
	private String _description;
	private BigDecimal _gross_amount;
	private BigDecimal _charge_amount = new BigDecimal(0);
	private String _trxid = "";
	public Date get_date() {
		return _date;
	}
	public void set_date(Date _date) {
		this._date = _date;
	}
	public String get_description() {
		return _description;
	}
	public String get_description255() {
		if (_description.length()>255)
			return _description.substring(0, 255);
		else
			return _description;
	}
	public void set_description(String _description) {
		this._description = _description;
	}
	public BigDecimal get_gross_amount() {
		return _gross_amount;
	}
	public void set_gross_amount(BigDecimal _gross_amount) {
		this._gross_amount = _gross_amount;
	}
	public BigDecimal get_charge_amount() {
		return _charge_amount;
	}
	public void set_charge_amount(BigDecimal _charge_amount) {
		this._charge_amount = _charge_amount;
	}
	public String get_trxid() {
		return _trxid;
	}
	public void set_trxid(String _trxid) {
		this._trxid = _trxid;
	}
	public BigDecimal get_net_amount() {
		return _gross_amount.add(this._charge_amount);
	}
		
	
}
