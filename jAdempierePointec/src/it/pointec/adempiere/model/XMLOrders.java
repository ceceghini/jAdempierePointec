package it.pointec.adempiere.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("orders")
public class XMLOrders {

	@XStreamImplicit(itemFieldName = "order")
	private List<XMLOrder> orders = new ArrayList<XMLOrder>();

	public List<XMLOrder> getOrders() {
		return orders;
	}
	
}
