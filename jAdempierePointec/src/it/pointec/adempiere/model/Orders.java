package it.pointec.adempiere.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("orders")
public class Orders {

	@XStreamImplicit(itemFieldName = "order")
	private List<Order> orders = new ArrayList<Order>();

	public List<Order> getOrders() {
		return orders;
	}
	
}
