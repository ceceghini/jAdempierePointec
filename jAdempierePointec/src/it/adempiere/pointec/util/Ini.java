package it.adempiere.pointec.util;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Properties;

public class Ini {
	
	private static Properties p = new Properties();
	
	public static void loadPropery() {
		
		try {
			p.load(new FileInputStream("import.ini"));
						
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		
	}
	
	public static int getInt(String key) {
		return Integer.parseInt(p.getProperty(key));
	}

	public static String getString(String key) {
		return p.getProperty(key);
	}

	public static BigDecimal getBigDecimal(String key) {
		return new BigDecimal(p.getProperty(key));
	}
	
	public static boolean istrue(String key) {
		return Boolean.parseBoolean(p.getProperty(key));
	}
	
}
