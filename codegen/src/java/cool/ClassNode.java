package cool;

import java.util.HashMap;
import java.util.Map.Entry;

// ClassNode for each class
public class ClassNode {
	public String name; // name of class
	public String parent; // name of parent of class
	public HashMap<String, AST.attr> attrs; // map of attributes of class
	public HashMap<String, AST.method> methods; // map of methods of class
	public HashMap<String, Integer> attrCount; // map of index of attributes in class
	public HashMap<String, String> methodsIRName; // IRName of method of class
	public int size = 0; // size of class

	ClassNode(String nm, String par, HashMap<String, AST.attr> atrs, HashMap<String, AST.method> method) {
		attrs = new HashMap<String, AST.attr>();
		methods = new HashMap<String, AST.method>();
		name = nm;
		parent = par;
		attrs.putAll(atrs);
		methods.putAll(method);

		// assigning index to attributes of class
		attrCount = new HashMap<String, Integer>();
		int c = 1;
		for (Entry<String, AST.attr> e : attrs.entrySet()) {
			attrCount.put(e.getKey(), c);
			c++;
		}

		// storing mangled name of methods of class
		methodsIRName = new HashMap<String, String>();
		for (Entry<String, AST.method> e : methods.entrySet()) {
			String s = "@" + name + "_" + e.getKey();
			methodsIRName.put(e.getKey(), s);
		}
	}
}