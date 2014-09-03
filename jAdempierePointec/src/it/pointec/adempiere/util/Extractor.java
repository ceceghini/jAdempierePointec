package it.pointec.adempiere.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		/*int count = 0;
		while (this._matcher.find())
			count++;
		
		if (count > 1)
			Util.addError("Numero di occorrenze trovate maggiore di 1 ["+_pattern+"]");
		
		this._matcher.reset();*/
		this._matcher.find();
		
	}
	
	public String group(int n) {
		return _matcher.group(n);
	}

}
