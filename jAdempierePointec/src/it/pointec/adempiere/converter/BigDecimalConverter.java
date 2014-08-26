package it.pointec.adempiere.converter;

import java.math.BigDecimal;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class BigDecimalConverter extends AbstractSingleValueConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(BigDecimal.class);
	}

	@Override
	public Object fromString(String str) {
		if (str=="")
			return null;
		else
			return new BigDecimal(str)	;
	}

	
}

