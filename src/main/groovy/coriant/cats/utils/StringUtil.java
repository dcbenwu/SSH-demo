package coriant.cats.utils;

import java.util.TreeMap;
import java.util.Vector;

public class StringUtil {
	
	public static boolean isEmpty(String str) {
		return (str == null || str.trim().equals(""));
	}
	public static String[] split(char c, String str) {
		int p = str.indexOf(c);
		if (p<0) return new String[] {str};
		Vector<String> v = new Vector<String>();
		int q = 0;
		while (p>=0) {
			v.add(str.substring(q, p));
			q = p+1;
			p = str.indexOf(c, q);
		}
		if (q < str.length()) {
			v.add(str.substring(q));
		}
		String[] r = new String[v.size()];
		for (int i=0; i<r.length; i++) r[i] = v.elementAt(i);
		return r;
	}
	
	public static String[] split(String s, String str) {
		int p = str.indexOf(s);
		if (p<0) return new String[] {str};
		Vector<String> v = new Vector<String>();
		int q = 0;
		while (p>=0) {
			v.add(str.substring(q, p));
			q = p+s.length();
			p = str.indexOf(s, q);
		}
		if (q<str.length()) {
			v.add(str.substring(q));
		}
		String[] r = new String[v.size()];
		for (int i=0; i<r.length; i++) r[i] = v.elementAt(i);
		return r;
	}
	
	
	public static String[] splitWithDoubleQuotes(String src, char s) {
		if (src == null) return null;
		if (src.indexOf('"') < 0) {
			String sep = ""+s;
			return src.split(sep);
		}
		int st = ST_INIT;
		int i = 0;
		int start = 0;
		Vector<String> v = new Vector<String>();
		while (i < src.length()) {
			char c = src.charAt(0);
			switch (st) {
				case ST_INIT:
					if (c == '"') {
						st = ST_QUOTE;
						start = i;
					} else if (c == s) {
						// 
						v.add("");
					} else {
						st = ST_NAME;
						start = i;
					}
					break;
				case ST_QUOTE:
					if (c == '"') {
						st = ST_NAME;
					}
					break;
				case ST_NAME:
					if (c == '"') {
						st = ST_QUOTE;
					} else if (c == s) {
						st = ST_INIT;
						v.add(src.substring(start, i-1));
					}
					break;
				default:
					
			}
			i++;
		}
		if (v.size() > 0) {
			return v.toArray(new String[v.size()]);
		}
		return null;
	}
	
	 public static boolean startOf(String[] words, String prefix) {
		 for (int i=0; i<words.length; i++) {
			 if (words[i].startsWith(prefix)) return true;
		 }
		 return false;
	 }
	 
	 public static boolean oneOf(String[] words, String word) {
		 for (int i=0; i<words.length; i++) {
			 if (words[i].equals(word)) return true;
		 }
		 return false;
	 }
	 
	 public static boolean doubleQuoted(String word) {
		 return (word != null && word.length() >1 && word.startsWith("\"") && word.endsWith("\""));
	 }
	 
	 public static boolean inList(char[] list, char c) {
		 if (list == null) return false;
		 for (int i=0; i<list.length; i++) if (c == list[i] ) return true;
		 return false;
	 }
	 
	 public static boolean inList(String[] list, String c) {
		 if (list == null) return false;
		 for (int i=0; i<list.length; i++) 
			 if (c.equals(list[i]) ) 
				 return true;
		 return false;
	 }
	 
	  static final int ST_INIT = 0;
	  static final int ST_NAME = 1;
	  static final int ST_COMMENTS = 2;
	  static final int ST_BLOCK = 3;
	  static final int ST_QUOTE = 4;
	  static final int ST_BLOCK_QUOTE = 5;
	  static final int ST_BLOCK_COMMENTS = 6;
	 
	 public static String expresionToWords(String expr, char[] symbols, Vector<String> words) {
		 if (expr == null || expr.trim().equals("")) return "empty expression";
		 int level = 0;
		 int st = ST_INIT;
		 char[] space = new char[]{' ', '\n', '\t'};
		 int start = 0;
		 char c = 0;
		 for (int i=0; i<expr.length(); i++) {
			 c = expr.charAt(i);
			 switch (st) {
			 case ST_INIT:
				 if (c == '"') {
					 start = i;
					 st = ST_QUOTE;
				 } else if (inList(symbols, c))
					 words.add(String.valueOf(c));
				 else if (!inList(space, c)) {
					 st = ST_NAME;
					 start = i;
				 }
				 break;
			 case ST_NAME:
				 if (inList(space, c)) {
					 words.add(expr.substring(start, i));
					 st = ST_INIT;
				 } else if (inList(symbols, c)) {
					 words.add(expr.substring(start, i));
					 words.add(String.valueOf(c));
					 st = ST_INIT;
				 }
				 break;
			 case ST_QUOTE:
				 if (c == '"') {
					 words.add(expr.substring(start, i+1));
					 st = ST_INIT;
				 }
				 break;
			 }
		 }
		 if (st == ST_NAME && start < expr.length()) {
			 words.add(expr.substring(start));
		 } else if (!words.isEmpty() && inList(space, c)) {
			 if (words.lastElement().length() > 1 || !inList(symbols, words.lastElement().charAt(0))) {
				 words.add(" ");
			 }
		 }
		 return "OK";
	 }
	 
	 public static int matchingBrackets(String src, char open, char close) {
		 // = 0: matching
		 // > 0: more open than close
		 // < 0: more close than open
		 int level = 0;
		 int st = ST_INIT;
		 for (int i=0; i<src.length(); i++) {
			 char c = src.charAt(i);
			 switch (st) {
			 case ST_INIT:
				 if (c == open) {
					 st = ST_BLOCK;
					 level++;
				 } else if (c == close) {
					 return Integer.MIN_VALUE;
				 } else if (c == '#') {
					 st = ST_COMMENTS;
				 } else if (c == '"') {
					 st = ST_QUOTE;
				 }
				 break;
			 case ST_BLOCK:
				 if (c == open) {
					 level++;
				 } else if (c == close) {
					 level--;
					 if (level ==0) 
						 st = ST_INIT;
				 } else if (c == '#') {
					 st = ST_BLOCK_COMMENTS;
				 } else if (c == '"') {
					 st = ST_BLOCK_QUOTE;
				 }				 
				 break;
			 case ST_COMMENTS:
				 if (c == '\n') {
					 st = ST_INIT;
				 }				 
				 break;
			 case ST_QUOTE:
				 if (c == '"') {
					 st = ST_INIT;
				 }				 
				 break;
			 case ST_BLOCK_QUOTE:
				 if (c == '"') {
					 st = ST_BLOCK;
				 }				 
				 break;
			 case ST_BLOCK_COMMENTS:
				 if (c == '\n') {
					 st = ST_BLOCK;
				 }				 
				 break;
				 default:
			 }
		 }
		 return level;
	 }

	 public static String[] getSpaceHints() {
		 return getSingleHint(" ");
	 }

	 public static String[] getSingleHint(String hint) {
		 String[] hints = new String[2];
		 hints[0] = "";
		 hints[1] = hint;
		 return hints;
	 }

	 public static String[] getHintsList(String[] items, String prefix) {
		if (items == null || items.length == 0) {
			String [] r = new String[1];
			r[0] = null;
			return r;
		}
		TreeMap<String, String> map = new TreeMap<String, String>();
		for (int i=0; i<items.length; i++) {
			map.put(items[i], items[i]);
		}
		String[] keys = map.keySet().toArray(new String[map.size()]);
		String[] w = new String[keys.length+1];
		if (prefix == null || prefix.trim().equals("")) 
			w[0] = "";
		else
			w[0] = prefix.trim();
		for (int i=0; i<keys.length; i++) w[i+1] = keys[i];
		return w;
	}

	 public static String[] getHintsListWithFilter(String[] items, String prefix) {
		if (items == null || items.length == 0) {
			String [] r = new String[1];
			r[0] = null;
			return r;
		}
		TreeMap<String, String> map = new TreeMap<String, String>();
		for (int i=0; i<items.length; i++) {
			if (prefix != null && !prefix.trim().equals("") && !items[i].startsWith(prefix.trim())) continue;
			map.put(items[i], items[i]);
		}
		if (map.size() == 0) return null;
		String[] keys = map.keySet().toArray(new String[map.size()]);
		String[] w = new String[keys.length+1];
		if (prefix == null || prefix.trim().equals("")) 
			w[0] = "";
		else
			w[0] = prefix.trim();
		for (int i=0; i<keys.length; i++) w[i+1] = keys[i];
		return w;
	}
	 
	 public static String[] removeFromList(String[] list, String item) {
		 if (list == null || list.length <2) return null;
		 String[] l = new String[list.length-1];
		 int j =0;
		 for (int i=0; i<list.length-1; i++) {
			 if (list[i].equals(item)) {
				 j++;
			 }
			 l[i] = list[i+j];
		 }
		 return l;
	 }

	 public static String[] mergeLists(String[] list1, String[] list2) {
		 if (list1 == null || list1.length == 0) return list2;
		 if (list2 == null || list2.length == 0) return list1;
		 
		 TreeMap<String, String> map = new TreeMap<String, String>();
		 for (int i=0; i<list1.length; i++) map.put(list1[i], list1[i]);
		 for (int i=0; i<list2.length; i++) map.put(list2[i], list2[i]);
		 return map.keySet().toArray(new String[map.size()]);
	 }
	 
	 static final String[] mDefaultHints = {""};
	 public static String[] getDefaultHints() {
		 return mDefaultHints;
	 }
	 
	 public static String convertToDaysHoursMinutesSeconds(long duration) {
		 long days = duration / 86400000;
		 long rem = duration % 86400000;
		 long hh = 0;
		 long mm = 0;
		 long ss = 0;
		 long ms = 0;
		 if (rem > 0) {
			 hh = rem / 3600000;
			 rem = rem % 360000;
			 if (rem > 0) {
				 mm = rem / 60000;
				 rem = rem % 60000;
				 if (rem> 0) {
					 ss = rem / 1000;
					 rem = rem % 1000;
					 if (rem > 0) 
						 ms = rem;
				 }
			 }
		 }
		 return days + "::"+hh+":"+mm+":"+ss+"."+ms;
	 }

	 public static Number parseNumber(String str) {
		 if (str == null) return null;
		 str = str.trim();
		 Number number = null;
		 try {
			 number = Long.parseLong(str);
			 return number;
		 } catch (Exception e) {
			 try {
				 number = Double.parseDouble(str);
				 return number;
			 } catch (Exception ee) {
				 if (str.startsWith("0x")) {
					 try {
						 number = Long.parseLong(str.substring(2), 16);
						 return number;
					 } catch (Exception eee) {
						 //do nothing;
					 }
				 }
			 }
		 }
		 return number;
	 }
}
