package it.adempiere.pointec.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.compiere.util.DB;

public class Util {
	
	private static StringBuffer _error = new StringBuffer();
	private static boolean _has_error = false;
	private static String _current = null;
	
	// Download di un xml dal sito magento
	public static void downloadFile(String url, String file) throws IOException {
		
		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(file);
	    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	    fos.close();
		
	}
	
	public static void addError(String msg) {
		_error.append(msg);
		_has_error = true;
	}
	
	public static void addError (Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		_error.append(sw.toString());
		_has_error = true;
	}
	
	public static boolean HasError() {
		return _has_error;
	}
	
	public static String getError() {
		return _error.toString();
	}
	
	public static void resetError() {
		_error = new StringBuffer();
		_has_error = false;
	}
	
	public static int getNextSequence(String table) {
		
		String sql = "select CURRENTNEXT from ad_sequence where lower(name) = '"+table+"'";

		return DB.getSQLValue(null, sql);
		
	}
	
	public static void increaseSequence(String table) {
		
		String sql = "update ad_sequence set CURRENTNEXT = CURRENTNEXT + 1 where lower(name) = '"+table+"'";
	
		DB.executeUpdate(sql, null);
	
	}
	
	public static void printErrorAndExit() {
		
		if (!Util.HasError())
			return;
		
		//if (_email) {
		//	sendMail("cesare@pointec.it", Util.getError());
		//}
		//else {
			System.err.println("#################### ERRORE ########################");
			System.err.println(getError());
			if (_current!=null)
				System.err.println("Current record: "+_current);
			System.err.println("####################################################");
		//}
		
		System.exit(-1);
		
	}
	
	public static String trunc(String s, int lenght) {
		
		if (s.length()>lenght)
			return s.substring(0, lenght);
		else
			return s;
		
	}
	
	public static void setCurrent(String s) {
		_current = s;
	}

	public static void Elaborato(String subPath, String file, String bs_name) throws IOException {
		
		if (_has_error)
			return;
		
		String source = Ini.getString("filepath") + "/" + subPath + "/" + file;
		
		File f_source = new File(source);

		if (!f_source.exists())
			return;
		
		// file destinazione
		//long lastModified = f_source.lastModified();
		//Date date = new Date(lastModified);
		
		String dest = Ini.getString("filepath_elaborati") + "/" + subPath;
		
		File d_dest = new File(dest);
		
		if (!d_dest.exists())
			d_dest.mkdir();
		
		
		dest = Ini.getString("filepath_elaborati") + "/" + subPath + "/" + bs_name;
		
		File f_dest = new File(dest);
		
		f_source.renameTo(f_dest);
		
	}
	
	public static Date getDate(String s, String format) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date d = new Date(dateFormat.parse(s).getTime());
		return d;
	}
	
	public static String getSqlPath() {
		return "/home/cesare/Scrivania/bank/sql";
	}
		
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
	
	public static boolean moveFile(String source, String dest, String nomeFile) {
		
		// Verifico se il file sorgente esiste
		File f_source = new File(source + "/" + nomeFile);
		if (!f_source.exists()) {
			Util.addError("File non esistente [" + source + "/" + nomeFile + "]\n");
			return false;
		}
		
		// Verifico se la directory di destinazione esiste altrimenti la creo
		String[] a = nomeFile.split("/");
		File d = new File(dest + "/" + a[0]);
		if (!d.exists())
			d.mkdir();
		
		// Spostamento vero e proprio del file
		File f_dest = new File(dest + "/" + nomeFile);
		
		//System.out.println(f_source.getAbsolutePath() + " >> " + f_dest.getAbsolutePath());
		f_source.renameTo(f_dest);
		
		return true;
		
	}
	
	public static String doctypeidToPath(int c_doctype_id) {
		
		if (c_doctype_id == Ini.getInt("doc_type_id_invoice_acq"))
			return "fornitori";
		
		if (c_doctype_id == Ini.getInt("doc_type_id_invoice_intra"))
			return "fornitori_intra";
		
		if (c_doctype_id == Ini.getInt("doc_type_id_creditmemo_acq"))
			return "fornitori";
		
		
		
		return "";
		
	}
	
	public static BigDecimal get_aliquota_iva1() {
		return Ini.getBigDecimal("aliquota_iva").add(new BigDecimal(1));
	}
	
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
}
