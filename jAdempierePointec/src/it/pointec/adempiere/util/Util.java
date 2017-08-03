package it.pointec.adempiere.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Classe contenente funzioni di utilità generica
 * @author cesare
 *
 */
public class Util {
	
	private static StringBuffer _error = new StringBuffer();
	private static boolean _has_error = false;
		
	/**
	 * Download e salvataggio di un file
	 * @param url	File sorgente
	 * @param file	File di destinazione
	 * @throws IOException
	 */
	/**
	 * Aggiunta di un errore al log
	 * @param msg	Messaggio di errore
	 */
	public static void addError(String msg) {
		_error.append(msg);
		_has_error = true;
	}
	
	/**
	 * Aggiunta di un exception al log
	 * @param e	Exception
	 */
	public static void addError (Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		addError(sw.toString());
	}
	
	/**
	 * Sono presenti errori nel log
	 * @return
	 */
	public static boolean HasError() {
		return _has_error;
	}
	
	/**
	 * Log degli errori
	 * @return
	 */
	public static String getError() {
		return _error.toString();
	}
	
	/**
	 * Azzeramento log errori
	 */
	public static void resetError() {
		_error = new StringBuffer();
		_has_error = false;
	}
	
	/**
	 * Output degli errori ed eventuale uscita
	 */
	public static void printErrorAndExit() {
		
		if (!Util.HasError())
			return;
		
		System.err.println("#################### ERRORE ########################");
		System.err.println(getError());
		System.err.println("####################################################");
			
		System.exit(-1);
		
	}
	
	public static Date getDate(String s, String format) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date d = new Date(dateFormat.parse(s).getTime());
		return d;
	}
	
	public static Calendar getCalendar(String s, String format) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Calendar cal  = Calendar.getInstance();
		cal.setTime(dateFormat.parse(s));
		return cal;
	}
	
	public static boolean moveFile(File f_source, File f_dest) {
		
		if (!f_source.exists()) {
			Util.addError("File non esistente [" + f_source.getAbsolutePath() + "]\n");
			return false;
		}
		
		File f_parent = new File(f_dest.getParent());
		if (!f_parent.exists())
			f_parent.mkdir();
		
		return f_source.renameTo(f_dest);
		
	}
	
	public static boolean moveFile(String source, String dest, String nomeFileSource, String nomeFileDest) {
		
		File f_source = new File(source + "/" + nomeFileSource);
		File f_dest = new File(dest + "/" + nomeFileDest);
		
		return moveFile(f_source, f_dest);
		
	}
	
	public static String getArchivio(int c_doctype_id, String year) {
		
		String path = Ini.getString("archivio")+ "/" + year + "/" + Ini.getString("fatture_" + Integer.toString(c_doctype_id));
		
		checkPath(path);
		
		return path;
				
	}
	
	public static String getArchivio(String type, String year) {
		
		String path = Ini.getString("archivio")+ "/" + year + "/" + type;
		
		checkPath(path);
		
		return path;
		
	}
	
	private static void checkPath(String path) {
		File f = new File(path);
		if (!f.exists())
			f.mkdirs();
	}	
	
	
}
