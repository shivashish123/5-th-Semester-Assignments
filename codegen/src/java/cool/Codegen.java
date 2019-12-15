package cool;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map.Entry;
import java.lang.*;

public class Codegen {
	// list of string constants
	public static List<String> globalStrings;
	public static HashMap<String, ClassNode> classes; // for retrieving class related info and class attributes and
														// features
	public static HashMap<String, AST.class_> nameToClass; // map of class name to class Node
	public static HashMap<String, Integer> classToIndex; // map of class name to class id (number)
	public static HashMap<Integer, String> indexToClass; // map of class id (number) to class name
	public static ArrayList<ArrayList<Integer>> classGraph; // adjacency list for Graph
	public static HashMap<String, String> DATA_TYPES = new HashMap<String, String>(); // Map of Basic Data types to its
																						// IR
	public static int stringCount = 0; // indicating number of string constants

	public Codegen(AST.program program, PrintWriter out) {

		out.println(BasicIR.DATA_LAYOUT);
		out.println(BasicIR.TARGET_TRIPLE);
		DATA_TYPES.put("Int", "i32");
		DATA_TYPES.put("Bool", "i32");
		DATA_TYPES.put("String", "[1024 x i8]*");
		classes = new HashMap<String, ClassNode>(); // map of classname and its corresponding classNode
		globalStrings = new ArrayList<String>(); // list of all string constants in program
		addBasicClasses(); // creating class nodes for basic classes
		CreateGraph(program.classes, out); // printing all classes declarations and methods IR in BFS order
		mainMethodIR(out); // printing main method IR
		for (String s : globalStrings) { // printing all string constants IR
			out.println(s);
		}
		out.println(BasicIR.ATTRIBUTES);
		out.println(BasicIR.CMETHODS);
		out.println(BasicIR.CMETHOD_HELPERS);
		out.println(BasicIR.ERRORS);
		out.println();
	}

	// prints the IR for main method
	public static void mainMethodIR(PrintWriter out) {
		String methodType = PrintIR.getIRType(classes.get("Main").methods.get("main").typeid);
		out.println("define void @main() #0 {\n" + "%1 = alloca %class.Main, align 4");
		out.println("call i32 @Main_Main(%class.Main* %1)");
		out.println("call " + methodType + " @Main_main(%class.Main* %1)\n" + "ret void\n}\n");
	}

	// add Basic Classes (i.e IO, String, Int, Object, Bool) in "classes"
	public static void addBasicClasses() {

		// Object Class
		List<AST.formal> objectFormals = new ArrayList<AST.formal>();
		objectFormals.add(new AST.formal("this", "Object", 0));
		HashMap<String, AST.method> objectMethods = new HashMap<String, AST.method>();
		objectMethods.put("abort", new AST.method("abort", objectFormals, "Object", new AST.no_expr(0), 0));
		classes.put("Object", new ClassNode("Object", null, new HashMap<String, AST.attr>(), objectMethods));

		// IO Class
		HashMap<String, AST.method> ioMethods = new HashMap<String, AST.method>();
		List<AST.formal> ioFormals = new ArrayList<AST.formal>();
		List<AST.formal> outStringFormals = new ArrayList<AST.formal>();
		ioFormals.add(new AST.formal("this", "IO", 0));
		outStringFormals.addAll(ioFormals);
		outStringFormals.add(new AST.formal("out_string", "String", 0));
		List<AST.formal> outIntFormals = new ArrayList<AST.formal>();
		outIntFormals.addAll(ioFormals);
		outIntFormals.add(new AST.formal("out_int", "Int", 0));
		ioMethods.putAll(objectMethods);
		ioMethods.put("out_string", new AST.method("out_string", outStringFormals, "IO", new AST.no_expr(0), 0));
		ioMethods.put("out_int", new AST.method("out_int", outIntFormals, "IO", new AST.no_expr(0), 0));
		ioMethods.put("in_string", new AST.method("in_string", ioFormals, "String", new AST.no_expr(0), 0));
		ioMethods.put("in_int", new AST.method("in_int", ioFormals, "Int", new AST.no_expr(0), 0));
		classes.put("IO", new ClassNode("IO", "Object", new HashMap<String, AST.attr>(), ioMethods));

		// String Class
		HashMap<String, AST.method> stringMethods = new HashMap<String, AST.method>();
		List<AST.formal> lengthFormals = new ArrayList<AST.formal>();
		lengthFormals.add(new AST.formal("this", "String", 0));
		List<AST.formal> concatFormals = new ArrayList<AST.formal>();
		concatFormals.addAll(lengthFormals);
		concatFormals.add(new AST.formal("that", "String", 0));
		List<AST.formal> substrFormals = new ArrayList<AST.formal>();
		substrFormals.addAll(lengthFormals);
		substrFormals.add(new AST.formal("index", "Int", 0));
		substrFormals.add(new AST.formal("len", "Int", 0));
		stringMethods.putAll(objectMethods);
		stringMethods.put("length", new AST.method("length", lengthFormals, "Int", new AST.no_expr(0), 0));
		stringMethods.put("concat", new AST.method("concat", concatFormals, "String", new AST.no_expr(0), 0));
		stringMethods.put("substr", new AST.method("substr", substrFormals, "String", new AST.no_expr(0), 0));
		classes.put("String", new ClassNode("String", "Object", new HashMap<String, AST.attr>(), stringMethods));

		// Int Class
		HashMap<String, AST.method> IntMethods = new HashMap<String, AST.method>();
		IntMethods.putAll(objectMethods);
		classes.put("Int", new ClassNode("Int", "Object", new HashMap<String, AST.attr>(), IntMethods));

		// Bool class
		HashMap<String, AST.method> BoolMethods = new HashMap<String, AST.method>();
		BoolMethods.putAll(objectMethods);
		classes.put("Bool", new ClassNode("Bool", "Object", new HashMap<String, AST.attr>(), BoolMethods));
	}

	// Whenever a new class is inserted, - Inherits the attributes and methods of
	// the parent class
	public static void insertClass(AST.class_ c) {

		HashMap<String, AST.attr> attrs = new HashMap<String, AST.attr>();
		HashMap<String, AST.method> methods = new HashMap<String, AST.method>();

		ClassNode p = classes.get(c.parent);
		HashMap<String, AST.attr> pAttrs = p.attrs;

		// Inserting attributes of parents of class "c"
		if (pAttrs != null)
			attrs.putAll(pAttrs);

		int sizeOfClass = 0;
		if (p != null)
			sizeOfClass = p.size;

		// Inserting attributes and methods of class "c"
		for (AST.feature e : c.features) {
			if (e.getClass() == AST.attr.class) {
				AST.attr ae = (AST.attr) e;
				attrs.put(ae.name, ae);
				if (ae.typeid == "Int" || ae.typeid == "Bool")
					sizeOfClass += 4;
				else
					sizeOfClass += 8;
			} else {
				AST.method me = (AST.method) e;
				me.formals.add(0, new AST.formal("this", c.name, 0));
				methods.put(me.name, me);
			}
		}

		ClassNode cl = new ClassNode(c.name, c.parent, attrs, methods);
		cl.size = sizeOfClass;
		classes.put(c.name, cl);
	}

	public void CreateGraph(List<AST.class_> classList, PrintWriter out) {
		nameToClass = new HashMap<String, AST.class_>(); // map of class name to class Node
		classToIndex = new HashMap<String, Integer>(); // map of class name to class id (number)
		indexToClass = new HashMap<Integer, String>(); // map of class id (number) to class name
		classGraph = new ArrayList<ArrayList<Integer>>(); // adjacency list for Graph

		/* Basic Classes */
		classToIndex.put("Object", 0);
		indexToClass.put(0, "Object");

		classToIndex.put("Int", 1);
		indexToClass.put(1, "Int");

		classToIndex.put("String", 2);
		indexToClass.put(2, "String");

		classToIndex.put("Bool", 3);
		indexToClass.put(3, "Bool");

		classToIndex.put("IO", 4);
		indexToClass.put(4, "IO");

		classGraph.add(new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4))); // for Object
		classGraph.add(new ArrayList<Integer>()); // for IO
		classGraph.add(new ArrayList<Integer>()); // for String
		classGraph.add(new ArrayList<Integer>()); // for bool
		classGraph.add(new ArrayList<Integer>()); // for int

		Integer index = 5; // (5 classes) already present

		// assigning an integer corresponding to each class.

		for (AST.class_ e : classList) {
			indexToClass.put(index, e.name);
			classToIndex.put(e.name, index);
			index++;
			classGraph.add(new ArrayList<Integer>());
			nameToClass.put(e.name, e);
		}

		// We are creating an undirected graph in this method.
		for (AST.class_ e : classList) {
			int u = classToIndex.get(e.parent);
			int v = classToIndex.get(e.name);
			classGraph.get(u).add(v); // adding an edge from parent -> child in the graph
		}

		/* Print Class Declarations */
		Queue<Integer> q = new LinkedList<Integer>();
		q.clear();
		q.offer(0);
		while (!q.isEmpty()) {
			Integer u = q.poll();
			if (u != 2 && u != 1 && u != 0 && u != 3 && u != 4) {
				insertClass(nameToClass.get(indexToClass.get(u))); // insert classes in BFS-order so that methods and
																	// attributes can be inherited.
			}
			String s = indexToClass.get(u);
			PrintIR.printClassDecl(classes.get(s), out);
			for (Integer v : classGraph.get(u)) {
				q.offer(v);
			}
		}
		/* prints class method definitions */
		q.clear();
		q.offer(0);
		while (!q.isEmpty()) {
			int u = q.poll();
			PrintIR.printClassMethods(classes.get(indexToClass.get(u)), out);
			for (Integer v : classGraph.get(u)) {
				q.offer(v);
			}
		}
	}

}
