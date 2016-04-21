package it.pointec.adempiere.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.compiere.util.DB;

/**
 * Classe contenente funzioni di utilità generica
 * @author cesare
 *
 */
public class Util {
	
	private static StringBuffer _error = new StringBuffer();
	private static boolean _has_error = false;
	private static boolean _debug = false;
	
	/**
	 * Download e salvataggio di un file
	 * @param url	File sorgente
	 * @param file	File di destinazione
	 * @throws IOException
	 */
	public static void downloadFile(String url, String file) throws IOException {
		
		/*URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(file);
	    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	    fos.close();*/
		
		URL website = new URL(url);
		URLConnection c = website.openConnection();
		c.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:40.0) Gecko/20100101 Firefox/40.0");
		InputStream i = c.getInputStream();
		OutputStream o = new FileOutputStream(file);
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = i.read(bytes)) != -1) {
			o.write(bytes, 0, read);
		}
		o.close();
		
		
	}
	
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
	 * Recupero id successivo di una tabella
	 * @param table	nome tabella
	 * @return
	 */
	public static int getNextSequence(String table) {
		
		String sql = "select CURRENTNEXT from ad_sequence where lower(name) = '"+table+"'";

		return DB.getSQLValue(null, sql);
		
	}
	
	/**
	 * Aggiornamento id successivo di una tabella
	 * @param table
	 */
	public static void increaseSequence(String table) {
		
		String sql = "update ad_sequence set CURRENTNEXT = CURRENTNEXT + 1 where lower(name) = '"+table+"'";
	
		DB.executeUpdate(sql, null);
	
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
	
	/**
	 * Trunc di una stringa
	 * @param s	Stringa
	 * @param lenght	Numero caratteri
	 * @return
	 */
	public static String trunc(String s, int lenght) {
		
		if (s.length()>lenght)
			return s.substring(0, lenght);
		else
			return s;
		
	}
	
	/**
	 * Conversione di una stringa in data
	 * @param s			Stringa da convertire
	 * @param format	Formato data
	 * @return
	 * @throws ParseException
	 */
	public static Date getDate(String s, String format) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date d = new Date(dateFormat.parse(s).getTime());
		return d;
	}
	
	/**
	 * Conversione di un pdf in testo
	 * @param f	File pdf da convertire
	 * @return
	 */
	public static String parsePdf(File f) {
		
		COSDocument cosDoc = null;
		PDDocument  pdDoc = null;
		String parsedText;
		
		try {
			PDFTextStripper pdfStripper = new PDFTextStripper();
			PDFParser parser = new PDFParser(new FileInputStream(f));
			parser.parse();
			cosDoc = parser.getDocument();
			pdDoc = new PDDocument(cosDoc);
			parsedText = pdfStripper.getText(pdDoc);
			
	        cosDoc.close();
	        pdDoc.close();
	        
	        return parsedText;
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        try {
	            if (cosDoc != null)
	                cosDoc.close();
	            if (pdDoc != null)
	                pdDoc.close();
	        } catch (Exception e1) {
	            e.printStackTrace();
	        }

	    }
		
		return null;
		
	}
	
	/**
	 * Conversione di una stringa in bigdecimal
	 * @param s
	 * @return
	 */
	public static BigDecimal getImporto(String s) {
		
		String str = s.replace("€", "").trim();
		
		if (str.indexOf(".") == -1 && str.indexOf(",") == -1)
			return new BigDecimal(str);
		
		if (str.substring(str.length()-3, str.length()-2).compareTo(",")==0) {
			// Terzultimo carattere , vanno rimossi i punti e convertita la virgola in punti
			return new BigDecimal( str.replace(".", "").replace(",", "."));
		}
		
		if (str.substring(str.length()-2, str.length()-1).compareTo(",")==0) {
			// Terzultimo carattere , vanno rimossi i punti e convertita la virgola in punti
			return new BigDecimal( str.replace(".", "").replace(",", "."));
		}
		
		if (str.substring(str.length()-3, str.length()-2).compareTo(".")==0) {
			// Terzultimo carattere . vanno rimossi eventual virgole
			return new BigDecimal( str.replace(",", ""));
		}
		
		Util.addError(s+" Importo non convertibile\n");
		
		return new BigDecimal(0);
		
		//return new BigDecimal(s.replace("€", "").trim().replace(".", "").replace(",", "."));
		
	}
	
	/**
	 * Spostamento di un file
	 * @param source			Percorso sorgente
	 * @param dest				Percorso destinazione
	 * @param nomeFileSource	Nome file sorgente
	 * @param nomeFileDest		Nome file destinazione
	 * @return
	 */
	public static boolean moveFile(String source, String dest, String nomeFileSource, String nomeFileDest) {
		
		Util.debug("Util.moveFile ["+source+"] ["+dest+"] ["+nomeFileSource+"] ["+nomeFileDest+"]");
		
		// Verifico se il file sorgente esiste
		File f_source = new File(source + "/" + nomeFileSource);
		if (!f_source.exists()) {
			Util.addError("File non esistente [" + source + "/" + nomeFileSource + "]\n");
			return false;
		}
		
		// Verifico se la directory di destinazione esiste altrimenti la creo
		String[] a = nomeFileDest.split("/");
		File d;
		if (a.length>1) {
			d = new File(dest + "/" + a[0]);
		}
		else {
			d = new File(dest);
		}
		
		if (!d.exists())
			d.mkdir();
		
		// Spostamento vero e proprio del file
		
		File f_dest = new File(dest + "/" + nomeFileDest);
		
		//System.out.println(f_source.getAbsolutePath() + " >> " + f_dest.getAbsolutePath());
		return f_source.renameTo(f_dest);
		
	}
	
	/**
	 * Recupero percorso fatture sulla base del doctype_id
	 * @param c_doctype_id
	 * @return
	 */
	/*public static String doctypeidToPath(int c_doctype_id, String type) {
		
		return Ini.getString(type) + "/" + Ini.getString("fatture_" + Integer.toString(c_doctype_id));
		
		//return Ini.getString("fatture_" + Integer.toString(c_doctype_id));
	}*/
	
	/*public static String getPath(String type) {
		
		return Ini.getString(type) + "/";
		
	}
	
	public static String getPathByType(int c_doctype_id) {
		
		return Ini.getString("fatture_" + Integer.toString(c_doctype_id));
		
	}*/
	
	public static String getDaElaborare(int c_doctype_id) {
		
		return getDaElaborare("fatture_" + Integer.toString(c_doctype_id));
		//return Ini.getString("daelaborare") + "/" + Ini.getString("fatture_" + Integer.toString(c_doctype_id));
		
	}
	
	public static String getDaElaborare(String type) {
		
		return Ini.getString("daelaborare") + "/" + Ini.getString(type);
		
	}
	
	public static String getDaArchiviare(int c_doctype_id) {
		
		return Ini.getString("daarchiviare") + "/" + Ini.getString("fatture_" + Integer.toString(c_doctype_id));
		
	}
	
	public static String getArchivio(int c_doctype_id, String year) {
		
		return getArchivio("fatture_" + Integer.toString(c_doctype_id), year);
		//return Ini.getString("archivio")+ "/" + year + "/" + Ini.getString("fatture_" + Integer.toString(c_doctype_id));
		
	}
	
	public static String getArchivio(String type, String year) {
		
		return Ini.getString("archivio")+ "/" + year + "/" + Ini.getString(type);
		
	}
	
	public static String getDaStampare(int c_doctype_id, String year) {
		
		return Ini.getString("archivio")+ "/" + year + "/" + Ini.getString("fatture_" + Integer.toString(c_doctype_id)) + " PER STAMPA";
		
	}
	
	
	/**
	 * Recupero aliquota iva
	 * @return
	 */
	public static BigDecimal get_aliquota_iva1() {
		return Ini.getBigDecimal("aliquota_iva").add(new BigDecimal(1));
	}
	
	/**
	 * Verifica se la stringa contiene un numero
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str)  
	{  
		try  
		{  
			Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
			return true;  
		}
	
	
	/**
	 * Impostazione debug
	 */
	public static void setDebug() {
		
		if (Ini.getBoolean("debug"))
			_debug = true;
		
	}
	
	/**
	 * debug line
	 * @param str
	 */
	public static void debug(String str) {
		if (_debug)
			System.out.println(str);
	}
	
}
