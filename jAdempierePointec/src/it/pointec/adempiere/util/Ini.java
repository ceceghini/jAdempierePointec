package it.pointec.adempiere.util;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Classe di utilita per la gestione del file ini
 * @author cesare
 *
 */
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
		
		String v = p.getProperty(key);
		
		if (v==null) {
			Util.addError("Proprietà non trovata ["+key+"]\n");
			return 0;
		}
		
		return Integer.parseInt(v);
		
			
	}
	
	public static boolean getBoolean(String key) {
		
		String v = p.getProperty(key);
		
		if (v==null) {
			Util.addError("Proprietà non trovata ["+key+"]\n");
			return false;
		}
		
		if (Integer.parseInt(v)==1)
			return true;
		else
			return false;
	}

	public static String getString(String key) {
		
		String v = p.getProperty(key);
		
		if (v==null) {
			Util.addError("Proprietà non trovata ["+key+"]\n");
			return "";
		}
		
		return v;
	}

	public static BigDecimal getBigDecimal(String key) {
		
		String v = p.getProperty(key);
		
		if (v==null) {
			Util.addError("Proprietà non trovata ["+key+"]\n");
			return new BigDecimal(0);
		}
		
		return new BigDecimal(v);
	}
	
}
