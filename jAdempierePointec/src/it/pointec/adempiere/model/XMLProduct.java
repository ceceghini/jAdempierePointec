package it.pointec.adempiere.model;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamConverter;

import it.pointec.adempiere.converter.IntConverter;
import it.pointec.adempiere.util.Ini;

public class XMLProduct {

	private String sku;
	
	private String name;
	
	@XStreamConverter(IntConverter.class)
	private int qty_ordered;
	
	private BigDecimal price;
	
	private int m_product_category_id;
	
	public XMLProduct(String _sku, BigDecimal _price) {
		
		sku = _sku;
		price = _price;
		qty_ordered = 1;
		
	}
	
	public String getSku() {
		return sku;
	}
	public String getName() {
		return name;
	}
	public int getQty_ordered() {
		return qty_ordered;
	}
	public BigDecimal getTax_amount() {
		return getPrice().multiply(Ini.getBigDecimal("aliquota_iva"));
	}
	public BigDecimal getPrice() {
		return price.multiply(Ini.getBigDecimal("aliquota_iva_per"));
		//return price.subtract(getTax_amount());
		
	}
	public BigDecimal getPriceWithTax() {
		return price;
	}
	public int getM_product_category_id() {
		return m_product_category_id;
	}
	
	
		
}
