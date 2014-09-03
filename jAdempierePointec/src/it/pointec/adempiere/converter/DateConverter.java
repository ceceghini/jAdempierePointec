package it.pointec.adempiere.converter;

import it.pointec.adempiere.util.Util;

import java.sql.Date;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class DateConverter extends AbstractSingleValueConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(Date.class);
	}

	@Override
	public Object fromString(String str) {
		
		try {
			return Util.getDate(str, "yyyy-MM-dd");
		}
		catch (Exception e) {
			return null;
		}
		
	}

	
}

