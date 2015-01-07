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

public class Order {

	//private String increment_id;
	
	private String order_id;
	
	@XStreamConverter(DateConverter.class)
	private Date created_at;
	
	private BigDecimal grand_total;
	
	private BigDecimal shipping_amount;

	@XStreamConverter(BigDecimalConverter.class)
	private BigDecimal cod_fee;

	private String payment_method;
	
	private String last_trans_id;
	
	private BPartner bp;
	
	@XStreamImplicit(itemFieldName = "product")
	private List<Product> products = new ArrayList<Product>();

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

	public BigDecimal getCod_fee() {
		return cod_fee;
	}

	public String getPayment_method() {
		return payment_method;
	}

	public String getLast_trans_id() {
		return last_trans_id;
	}

	public BPartner getBp() {
		return bp;
	}

	public List<Product> getProducts() {
		return products;
	}
	
	public BigDecimal getItemAmount() {
		
		BigDecimal tot= new BigDecimal(0);
		
		for (Product p : products) {
			tot = tot.add(p.getPrice().multiply(new BigDecimal(p.getQty_ordered()).multiply(Util.get_aliquota_iva1())));
		}
		return tot;
		
	}
	
	public void addShippingAndFeeProduct() {
		
		Product p;
		
		if (this.shipping_amount != null) {
			if (this.shipping_amount.compareTo(new BigDecimal(0))!=0) {
				p = new Product(Ini.getString("prodotto_spedizione"), this.shipping_amount);
				this.products.add(p);
			}
		}
		
		if (this.cod_fee != null) {
			if (this.cod_fee.compareTo(new BigDecimal(0))!=0) {
				p = new Product(Ini.getString("prodotto_contrassegno"), this.cod_fee);
				this.products.add(p);
			}
		}
		
	}
	
}
