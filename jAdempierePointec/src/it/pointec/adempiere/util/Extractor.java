package it.pointec.adempiere.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe per il parsing e l'estrazione di string sql
 * @author cesare
 *
 */
public class Extractor {
	
	private String _text;
	private Matcher _matcher;
	private Pattern _pattern;
	
	public Extractor(String s) {
		_text = s;
	}
	
	public void set_pattern(String _pattern) {
		this._pattern = Pattern.compile(_pattern);
		this._matcher = this._pattern.matcher(this._text);
		
		this._matcher.find();
		
	}
	
	public String group(int n) {
		return _matcher.group(n);
	}

}
