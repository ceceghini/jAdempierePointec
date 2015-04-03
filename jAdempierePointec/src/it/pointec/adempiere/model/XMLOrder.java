package it.pointec.adempiere.model;

import it.pointec.adempiere.converter.BigDecimalConverter;
import it.pointec.adempiere.converter.DateConverter;
import it.pointec.adempiere.util.Ini;
import it.pointec.adempiere.util.Util;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class XMLOrder {

	//private String increment_id;
	
	private String order_id;
	
	@XStreamConverter(DateConverter.class)
	private Date created_at;
	
	private BigDecimal grand_total;
	
	private BigDecimal shipping_amount;
	
	private BigDecimal discount_amount;

	@XStreamConverter(BigDecimalConverter.class)
	private BigDecimal cod_fee;

	private String payment_method;
	
	private String last_trans_id;
	
	private XMLBPartner bp;
	
	@XStreamImplicit(itemFieldName = "product")
	private List<XMLProduct> products = new ArrayList<XMLProduct>();

	//public String getIncrement_id() {
	//	return increment_id;
	//}
	
	public String getOrder_id() {
		return order_id;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public BigDecimal getGrand_total() {
		return grand_total.setScale(2);
	}

	public BigDecimal getShipping_amount() {
		return shipping_amount;
	}
	
	public BigDecimal getDiscountAmount() {
		return discount_amount;
	}

	public BigDecimal getCod_fee() {
		return cod_fee;
	}

	public String getPayment_method() {
		return payment_method;
	}

	public String getLast_trans_id() {
		return last_trans_id;
	}

	public XMLBPartner getBp() {
		return bp;
	}

	public List<XMLProduct> getProducts() {
		return products;
	}
	
	public BigDecimal getItemAmount() {
		
		BigDecimal tot= new BigDecimal(0);
		
		for (XMLProduct p : products) {
			tot = tot.add(p.getPrice().multiply(new BigDecimal(p.getQty_ordered()).multiply(Util.get_aliquota_iva1())));
		}
		return tot;
		
	}
	
	public void addExtraProduct() {
		
		XMLProduct p;
		
		if (this.shipping_amount != null) {
			if (this.shipping_amount.compareTo(new BigDecimal(0))!=0) {
				p = new XMLProduct(Ini.getString("prodotto_spedizione"), this.shipping_amount);
				this.products.add(p);
			}
		}
		
		if (this.cod_fee != null) {
			if (this.cod_fee.compareTo(new BigDecimal(0))!=0) {
				p = new XMLProduct(Ini.getString("prodotto_contrassegno"), this.cod_fee);
				this.products.add(p);
			}
		}
		
		if (this.discount_amount != null) {
			if (this.discount_amount.compareTo(new BigDecimal(0))!=0) {
				p = new XMLProduct(Ini.getString("prodotto_sconto"), this.discount_amount);
				this.products.add(p);
			}
		}
		
	}
	
}
