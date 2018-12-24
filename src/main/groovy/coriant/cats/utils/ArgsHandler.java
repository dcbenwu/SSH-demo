package coriant.cats.utils;

import java.util.Vector;

public class ArgsHandler {
	
	public static final String PHY_ATTRIBUTE_NAME = "name";
	public static final String PHY_ATTRIBUTE_TYPE = "type";
	public static final String PHY_ATTRIBUTE_ADDRESS = "address";
	public static final String PHY_ATTRIBUTE_PARENT = "parent";


	public static String getArgv(String argvs, Vector<String> v, char left, char right, boolean quoted) {
		if (!quoted) return ArgsHandler.getArgv(argvs, v, left, right);
		String expression = argvs;
		int p = expression.indexOf(left);
		if (p<0) {
			return "ArgsHandler.getArgv: Invalid SF expression "+expression+": missing "+left;
		}
		int q = expression.lastIndexOf(right);
		if (q<0) {
			return "ArgsHandler.getArgv: Invalid SF expression "+expression+": missing "+right;
		}
		String expr = expression.substring(p+1,q).trim();
		return ArgsHandler.getArgv(expr, v, left, right);
	}
	
	static final int ST_INIT = 0;
	static final int ST_QUOTE = 1;
	
	
	public static String getArgv(String argvs, Vector<String> v, char left, char right) {
		if (argvs == null || argvs.equals("")) return "OK";
		String expr = argvs;
		Vector<Integer> indices = new Vector<Integer>();
		int i = 0;
		int level = 0;
		int st = ST_INIT;
		while (i < expr.length()) {
			char ch = expr.charAt(i);
			switch (st) {
				case ST_INIT:
					if (ch == ',') {
						if (level == 0)
							indices.add(i);
					} else if (ch == left) {
						level++;
					} else if (ch == right) {
						level--;
					} else if (ch == '"')
						st = ST_QUOTE;
					break;
				case ST_QUOTE:
					if (ch == '"') {
						st = ST_INIT;
					}
					break;
				default:
			}
			i++;
		}
		if (st != ST_INIT)
			return "open quote";
		if (indices.size() > 0) {
			int j = 0;
			for (i=0; i<indices.size(); i++) {
				v.add(expr.substring(j, indices.elementAt(i)).trim());
				j = indices.elementAt(i)+1;
			}
			v.add(expr.substring(j).trim());
		} else {
			v.add(expr);
		}
		return "OK";
	}
	
	public static String getPhyAttrValue(String attrName, Vector<String> phy, Vector<String> err) {
		for (int i=0; i<phy.size(); i++) {
			String str = phy.elementAt(i);
			int p = str.indexOf('=');
			if (p< 0) {
				err.add("ArgsHandler.getPhyAttrValue: Invalid PhyEntitiy name_value_pair "+str);
				return null;
			}
			String attr = str.substring(0,p).trim();
			if (attr.equals(attrName)) {
				phy.remove(i);
				return str.substring(p+1).trim();
			}
		}
		err.add(0, "ArgsHandler.getPhyAttrValue: No value found for attr "+attrName);
		return null;
	}
	
	public static String getObjectName(String phyEntity, Vector<String> err) { 
		int p = phyEntity.indexOf('{');
		if (p < 0) {
			err.add(0, "ArgsHandler.getObjectName: invalid phyEntity - missing '{'");
			return null;
		}
		int q = phyEntity.indexOf('=', p);
		if (q < 0) {
			err.add(0, "ArgsHandler.getObjectName: invalid phyEntity - missing '='");
			return null;
		}
		int t = phyEntity.indexOf(',', q);
		if (t < 0) {
			err.add(0, "ArgsHandler.getObjectName: invalid phyEntity - missing ','");
			return null;
		}
		return phyEntity.substring(q+1,t).trim();
	}


}
