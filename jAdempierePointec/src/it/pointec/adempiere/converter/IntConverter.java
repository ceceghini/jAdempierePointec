package it.pointec.adempiere.converter;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class IntConverter extends AbstractSingleValueConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(Integer.class);
	}

	@Override
	public Object fromString(String str) {
		return Integer.parseInt(str.replace(".0000", ""));
	}

	
	
}
