package it.pointec.adempiere.model;

import it.adempiere.pointec.util.Ini;

import java.math.BigDecimal;
import java.sql.Date;

public class InvoicePassive {

	private String _filename;
	private String _fullpath;
	private int _c_bpartner_id;
	private String _poreference;
	private Date _dateinvoiced;
	private BigDecimal _grandtotal;
	private int _c_doctype_id;
	private String _sku;
	
	public int get_c_bpartner_id() {
		return _c_bpartner_id;
	}
	public String get_poreference() {
		return _poreference;
	}
	public void set_poreference(String _poreference) {
		this._poreference = _poreference;
	}
	public Date get_dateinvoiced() {
		return _dateinvoiced;
	}
	/*public Date get_dateacct() {
		
		Calendar c = Calendar.getInstance(); 
		c.setTime(_dateinvoiced); 
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.DATE, -1);
		
		return new java.sql.Date(c.getTimeInMillis());
		
		
	}*/
	public void set_dateinvoiced(Date _dateinvoiced) {
		this._dateinvoiced = _dateinvoiced;
	}
	public String get_filename() {
		return _filename;
	}
	public void set_filename(String _filename) {
		this._filename = _filename;
	}
	public String get_fullpath() {
		return _fullpath;
	}
	public void set_fullpath(String _fullpath) {
		this._fullpath = _fullpath;
	}
	public BigDecimal get_price() {
		return _grandtotal.multiply(Ini.getBigDecimal("aliquota_iva_per")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal get_tax_amount() {
		return _grandtotal.subtract(_grandtotal.multiply(Ini.getBigDecimal("aliquota_iva_per"))).setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}
	public void set_grandtotal(BigDecimal _grandtotal) {
		this._grandtotal = _grandtotal;
	}
	public int get_c_doctype_id() {
		return _c_doctype_id;
	}
	public void set_c_doctype_id(int _c_doctype_id) {
		this._c_doctype_id = _c_doctype_id;
	}
	public void set_c_bpartner_id(int _c_bpartner_id) {
		this._c_bpartner_id = _c_bpartner_id;
	}
	public String get_sku() {
		return _sku;
	}
	public void set_sku(String _sku) {
		this._sku = _sku;
	}
	
}
