 package cool;
import java.util.*;
import cool.VisitorPattern;

public class Semantic{

	private static boolean errorFlag = false;
	public static void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
	}
	public boolean getErrorFlag(){
		return errorFlag;
	}

/*
	Don't change code above this line
*/

	public static Map<String, AST.class_> classMap;
	public static ScopeTable<Map<String, String>> objectEnv;
	public static ScopeTable<Map<String, List<String>>> methodEnv;
	String filename = "";
	boolean repeatClass = false;
	boolean mainClassExists = false; //flag if Main class is defined
	boolean mainMethodExists = false; //flag if main() method exists in Main class
	boolean noFormalsInMainMethod=false;
	ScopeTable<AST.attr> scopeTable;
	HashMap <String, List<String> > ObjectMethods;
	HashMap <String, List<String> > IOMethods;
	HashMap <String, List<String> > StringMethods;
	HashMap <String, List<String> > IntMethods;
	HashMap <String, List<String> > BoolMethods;
	public static Graph inheritanceGraph;

	public Semantic(AST.program program){
		// map of class name and class object
		classMap = new HashMap<String, AST.class_>(); 
		// scope table of map of attributes name and type
		objectEnv = new ScopeTable<Map<String, String>>();
		// scope table of map of method name and list of formal types
		methodEnv = new ScopeTable<Map<String, List<String>>>();
		objectEnv.enterScope();
		methodEnv.enterScope();

		inheritanceGraph = new Graph();
		// adding all basic classes such as Object, IO, Bool, Int, String in classMap
		builtInClasses();

		inheritanceGraph.addEdge("Object", "Int");
		inheritanceGraph.addEdge("Object", "String");
		inheritanceGraph.addEdge("Object", "Bool");
		inheritanceGraph.addEdge("Object", "IO");

		// Pass 1:  we checked if Main class exists or not and also ensured that basic classes such as Object, IO, Int, String, Bool are not redefined and neither 
		// are they being inherited and then adding these classes into classMap and add the edge between the class and its parent in inheritance graph
		for (AST.class_ c : program.classes) 
		{
			String cName = c.name;

			if (c.name.equals("Main"))
				mainClassExists = true;

			// Check for basic object/class redefinitions (which are invalid).
			if (cName.equals("Object")) 
			{
				reportError(c.filename, c.lineNo, "Redefinition of basic class Object.");
			} 
			else if (cName.equals("IO")) 
			{
				reportError(c.filename, c.lineNo, "Redefinition of basic class IO.");
			} 
			else if (cName.equals("Int")) 
			{
				reportError(c.filename, c.lineNo, "Redefinition of basic class Int.");
			} 
			else if (cName.equals("String")) 
			{
				reportError(c.filename, c.lineNo, "Redefinition of basic class String.");
			} 
			else if (cName.equals("Bool")) 
			{
				reportError(c.filename, c.lineNo, "Redefinition of basic class Bool.");
			} 
			else 
			{
				String c2Name = c.parent;
				// Check for invalid basic object/class inheritance.
				if (c2Name.equals("Int")) 
				{
					reportError(c.filename, c.lineNo, "Class " + cName + " cannot inherit class Int.");
				} 
				else if (c2Name.equals("String")) 
				{
					reportError(c.filename, c.lineNo, "Class " + cName + " cannot inherit class String.");
				} 
				else if (c2Name.equals("Bool")) 
				{
					reportError(c.filename, c.lineNo, "Class " + cName + " cannot inherit class Bool.");
				} 
				else 
				{
					if (classMap.get(c.name)!=null) 
					{
						reportError(c.filename, c.lineNo, "Class " + c.name + " was previously defined.");
						continue;
					}
					classMap.put(cName, c);
					inheritanceGraph.addEdge(c2Name, cName);
				}
			}

		}

		// Cycle detection, terminating if cycle is detected
		String isCycle = inheritanceGraph.detectCycle();
		if(!isCycle.equals("no_cycle"))
		{
			AST.class_ c = classMap.get(isCycle);
			reportError(c.filename, c.lineNo, "Class " + c.name + ", or an ancestor of " + c.name +", is involved in an inheritance cycle.");
			String temp = c.parent;
			while(!temp.toString().equals(c.name.toString()))
			{
				AST.class_ tempClass=classMap.get(temp);
				reportError(tempClass.filename, tempClass.lineNo, "Class " + tempClass.name + ", or an ancestor of " + tempClass.name + ", is involved in an inheritance cycle.");
				temp=tempClass.parent;
			}
			System.exit(0);
		}

		// Pass 2: It is checked if parent of class is defined or not. Also, two hashMaps are created for storing type of attributes and formals of methods of the class. For each class, each feature of class is traversed. 
		// If the features is attribute, then its checked if the attribute is already defined in the class or in its ancestor. If so, error is reported. Else, its name and type is added to attribute map. 
		// If feature is method, then its checked if its already defined in that class. Then its also checked that if the method exists in one of the ancestors of that class, then return type of method and 
		// the number of formals and type of each formal should be same (method overriding). Also its checked that the formal name isnt repeated. For this, set of formal names is created so that searching is faster. 
        // Checking of main method with no formals in Main class is also done here. Then, the methodMap is added to methodEnv and attributeMap is added to objectEnv.\\	
		for (AST.class_ c : program.classes) 
		{
			String parentName = c.parent;
			if (classMap.get(parentName) == null) 
			{
				reportError(c.filename, c.lineNo,"Class "+ c.name + " inherits from an undefined class " + parentName + ".");
			}
			Map<String, List<String>> methodMap;
			Map<String, String> attributeMap;
			methodMap = new HashMap<String, List<String>>();
			attributeMap = new HashMap<String, String>();
			for (AST.feature f : c.features) 
			{
				if (f instanceof AST.attr) 
				{
					AST.attr a = (AST.attr) f;
					if (attributeMap.containsKey(a.name)) 
					{
						reportError(c.filename, a.lineNo,"Attribute " + a.name.toString() + " is multiply defined in class.");
					} 
					else if (isAttrInherited(a, c)) 
					{
						reportError(c.filename, a.lineNo,"Attribute " + a.name.toString() + " is an attribute of an inherited class.");
					} 
					else if (classMap.get(a.typeid) == null) 
					{
						reportError(c.filename, a.lineNo,"Class " + a.typeid.toString() + " of attribute " + a.name.toString() +" is undefined.");
					}
					else
					{
						attributeMap.put(a.name, a.typeid);
					}
				} 
				else 
				{
					AST.method m = (AST.method) f;
					if (methodMap.containsKey(m.name)) 
					{
						reportError(c.filename, m.lineNo, "Method " + m.name + " is multiply defined.");
						continue;
					} 
					else 
					{
						Set<String> formalNames = new HashSet<String>();
						List<String> formalType = new ArrayList<String>();

						String par = c.parent;
						//check if method is defined in parent and params and types are same in derived as that of parent
						while(par != null)
						{
							AST.class_ p = classMap.get(par);
							for (AST.feature f1 : p.features) 
							{
								if (f1 instanceof AST.method) 
								{
									AST.method pm = (AST.method) f1;
									if (pm.name.equals(m.name))
									{
										if(m.formals.size() != pm.formals.size())
										{
											reportError(c.filename, m.lineNo, "Incompatible number of formal parameters in redefined method " + m.name + ".");
										}
										else
										{
											if(m.typeid.equals(pm.typeid) == false) 
											{
						                        // Means the return type of inherited function is different
						                        reportError(c.filename, m.lineNo, "In redefined method " + m.name + ", return type " + m.typeid + " is different from original return type " + pm.typeid + ".");
					                    	}
						                    // Now we check for the typeid of parameters
						                    for(int i=0; i<m.formals.size(); i++) 
						                    {
						                        if(m.formals.get(i).typeid.equals(pm.formals.get(i).typeid) == false) 
						                        {
						                            // Means the parameter typeid does not match with the corresponding parameter of parents type id
						                            reportError(c.filename, m.lineNo, "In redefined method " + m.name + ", parameter type " + m.formals.get(i).typeid + " is different from original type " + pm.formals.get(i).typeid + ".");
						                        }
						                    }
						                }
									}
								}
							}
							par = p.parent;
						}
						
						for (AST.formal param : m.formals) 
						{
							if (formalNames.contains(param.name)) 
							{
								reportError(c.filename, c.lineNo, "Formal parameter " + param.name + " is multiply defined.");
								continue;
							} 
							else 
							{
								formalNames.add(param.name);
							}

							formalType.add(param.typeid);
						}
						formalType.add(m.typeid);
						methodMap.put(m.name, formalType);

						if (c.name.equals("Main")) 
						{
							if (m.name.equals("main")) 
							{
								mainMethodExists = true;
								if(formalType.size() == 1)
									noFormalsInMainMethod = true;
							}
						}
					}
				}
			}
			methodEnv.insert(c.name, methodMap);
			objectEnv.insert(c.name, attributeMap);

		}
		//Main Class does not exist
		if (!mainClassExists)
		{
			System.err.println("Class Main is not defined.");
			errorFlag = true;
		}
		else if (!mainMethodExists)
		{
			System.err.println("No 'main' method in class Main."); 	
			errorFlag = true;
		} 
		else if (!noFormalsInMainMethod)
		{
			System.err.println("'main' method in class Main should have no arguments.");
			errorFlag = true;
		}
		// Pass 3: Each node is visited using visitor pattern mechanism. 	
		for(AST.class_ c:program.classes)
		{
			VisitorPattern.visitNode(c);
		}
	}
	// Checking if an attribute exists in one of its parent classes.
	public boolean isAttrInherited(AST.attr a, AST.class_ c) 
	{
		AST.class_  p = classMap.get(c.parent);
		while (p != null) {
			for (AST.feature f : p.features) 
			{
				if (f instanceof AST.attr) 
				{
					if (((AST.attr) f).name.equals(a.name))
						return true;
				}
			}
			p = classMap.get(p.parent);
		}
		return false;
	}

	private void builtInClasses() 
	{
		// Refer page 13 of COOL manual for basic classes and methods

		// Object class has abort(), type_name(), copy() methods 
		List<AST.feature> objMethods = new ArrayList<AST.feature>();
		objMethods.add(new AST.method("abort", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		objMethods.add(new AST.method("type_name", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
		objMethods.add(new AST.method("copy", new ArrayList<AST.formal>(), "Object", new AST.no_expr(0), 0));
		AST.class_ Object_class = new AST.class_("Object", filename, null, objMethods, 0);
		classMap.put("Object", Object_class);
		ObjectMethods = new HashMap <String, List<String> >();
        ObjectMethods.put("abort", new ArrayList<String>(Arrays.asList("Object")));
        ObjectMethods.put("type_name", new ArrayList<String>(Arrays.asList("String")));
        ObjectMethods.put("copy", new ArrayList<String>(Arrays.asList("Object")));
        methodEnv.insert("Object",ObjectMethods);

        // IO class has out_string, in_string, out_int, in_int methods plus all Object methods
		List<AST.feature> ioMethods = new ArrayList<AST.feature>();
		List<AST.formal> outString_formals = new ArrayList<AST.formal>();
		outString_formals.add(new AST.formal("out_string", "String", 0));
		List<AST.formal> outInt_formals = new ArrayList<AST.formal>();
		outInt_formals.add(new AST.formal("out_int", "Int", 0));
		ioMethods.add(new AST.method("out_string", outString_formals, "IO", new AST.no_expr(0), 0));
		ioMethods.add(new AST.method("out_int", outInt_formals, "IO", new AST.no_expr(0), 0));
		ioMethods.add(new AST.method("in_string", new ArrayList<AST.formal>(), "String", new AST.no_expr(0), 0));
		ioMethods.add(new AST.method("in_int", new ArrayList<AST.formal>(), "Int", new AST.no_expr(0), 0));
		AST.class_ IO_class = new AST.class_("IO", filename, "Object", ioMethods, 0);
		classMap.put("IO", IO_class);
		IOMethods = new HashMap <String, List<String> >();
        IOMethods.put("out_string", new ArrayList<String>(Arrays.asList("String","IO")));
        IOMethods.put("out_int", new ArrayList<String>(Arrays.asList("Int","IO")));
        IOMethods.put("in_string", new ArrayList<String>(Arrays.asList("String")));
        IOMethods.put("in_int", new ArrayList<String>(Arrays.asList("Int")));
        IOMethods.putAll(ObjectMethods); //inheriting all object methods
        methodEnv.insert("IO",IOMethods);

        // Int class
        List<AST.feature> intattr = new ArrayList<AST.feature>();
		AST.class_ Int_class = new AST.class_("Int", filename, "Object", intattr, 0);
        classMap.put("Int",Int_class);
        IntMethods = new HashMap <String, List<String> >();
        IntMethods.putAll(ObjectMethods); 
		methodEnv.insert("Int",IntMethods);

		// Bool class
		List<AST.feature> boolattr = new ArrayList<AST.feature>();
		AST.class_ BoolClass = new AST.class_("Bool", filename, "Object", boolattr, 0);
		classMap.put("Bool",BoolClass);
		BoolMethods = new HashMap <String, List<String> >();
		BoolMethods.putAll(ObjectMethods);
		methodEnv.insert("Bool",BoolMethods);

		// String class has length(), concat(), substr() methods.
		List<AST.feature> stringMethods = new ArrayList<AST.feature>();
		List<AST.formal> concatFormal = new ArrayList<AST.formal>();
		concatFormal.add(new AST.formal("s", "String", 0));
		List<AST.formal> substrFormal = new ArrayList<AST.formal>();
		substrFormal.add(new AST.formal("i", "Int", 0));
		substrFormal.add(new AST.formal("l", "Int", 0));
		stringMethods.add(new AST.method("length", new ArrayList<AST.formal>(), "Int", new AST.no_expr(0), 0));
		stringMethods.add(new AST.method("concat", concatFormal, "String", new AST.no_expr(0), 0));
		stringMethods.add(new AST.method("substr", substrFormal, "String", new AST.no_expr(0), 0));
		AST.class_ StrClass = new AST.class_("String", filename, "Object", stringMethods, 0);
		classMap.put("String", StrClass);
		StringMethods = new HashMap <String, List<String> >();
		StringMethods.putAll(ObjectMethods);
        StringMethods.put("length", new ArrayList<String>(Arrays.asList("Int")));
        StringMethods.put("concat", new ArrayList<String>(Arrays.asList("String","String")));
        StringMethods.put("substr", new ArrayList<String>(Arrays.asList("Int", "Int","String")));
        methodEnv.insert("String", StringMethods);
    }
}
