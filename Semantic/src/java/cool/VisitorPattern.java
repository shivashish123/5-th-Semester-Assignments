package cool;

import java.util.*;
import cool.Semantic;

public class VisitorPattern {
	// Returns the type for an attribute name. Checks for current class and each of its ancestors
    public static String attrType(String attrName, AST.class_ c) 
    {
    	if (Semantic.objectEnv.lookUpGlobal(c.name) != null
				&& (Semantic.objectEnv.lookUpGlobal(c.name)).get(attrName) != null)
    	{
			return (Semantic.objectEnv.lookUpGlobal(c.name)).get(attrName);
		}
		String parentName = c.parent;
		while (parentName != null) {
			AST.class_ par = Semantic.classMap.get(parentName);
			for (AST.feature f : par.features) {
				if (f instanceof AST.attr) {
					if (((AST.attr) f).name.equals(attrName))
						return Semantic.objectEnv.lookUpGlobal(par.name).get(attrName);
				}
			}
			parentName=par.parent;
		}
		return null;
	}

	// It increments the scope and add the new variables (newScopeParamMap) in scope for specified class
	public static void updateObjectEnv(ScopeTable<Map<String, String>> objectEnv, AST.class_ c, Map<String, String> newScopeParamMap) {
		Map<String,String> m = Semantic.objectEnv.lookUpGlobal(c.name);
		Map<String,String> m1 = new HashMap<String,String>();
		for(Map.Entry<String,String > e: m.entrySet() )
			m1.put(e.getKey(),e.getValue());
		Semantic.objectEnv.enterScope();
		for(Map.Entry<String,String > e: newScopeParamMap.entrySet() )
			m1.put(e.getKey(),e.getValue());
		Semantic.objectEnv.insert(c.name, m1);
	}

	// returns the list of parameters for methodname and searches in current class and each of its ancestors 
	public static List<String> getFormalList(String methodName, AST.class_ c) 
	{
		if (Semantic.methodEnv.lookUpGlobal(c.name) != null && (Semantic.methodEnv.lookUpGlobal(c.name)).get(methodName) != null)
			return Semantic.methodEnv.lookUpGlobal(c.name).get(methodName);
		String parentName = c.parent;
		while (parentName != null) 
		{
			AST.class_ par = Semantic.classMap.get(parentName);
			for (AST.feature f : par.features) 
			{
				if (f instanceof AST.method) 
				{
					if (((AST.method) f).name.equals(methodName))
						return  Semantic.methodEnv.lookUpGlobal(par.name).get(methodName);
				}
			}
			parentName = par.parent;
		}
		return null;
	}

	// visit node for classes in the AST 
    public static void visitNode(AST.class_ cur) {
        // visiting nodes i.e features (methods and attributes) of each class
        for(AST.feature f : cur.features) {
            if (f instanceof AST.attr)
            {
				visitNode((AST.attr) f, cur);
            }
			else if (f instanceof AST.method)
			{
				visitNode((AST.method) f, cur);
			}
        }
    }

	// visit node for method 
	public static void visitNode(AST.method node, AST.class_ cur)
	{
		Map<String, String> newScopeParamMap = new HashMap<String, String>();
		newScopeParamMap.put("self", cur.name);
		//adding formals name and type in newscope list
		for (AST.formal f : node.formals) 
		{
			String name_ = f.name;
			String type_ = f.typeid;
			newScopeParamMap.put(name_, type_);
		}
		//adding this to new scope
		updateObjectEnv(Semantic.objectEnv, cur, newScopeParamMap); //Enter a new scope with new bindings
		visitNode(node.body, cur);
		Semantic.objectEnv.exitScope(); //exiting scope as method completes

		//checking types
		String T0_prime_string = node.body.type;
		if (Semantic.classMap.get(node.typeid) == null) 
		{
			Semantic.reportError(cur.filename, node.lineNo,
					"Undefined return type " + node.typeid + " in method " + node.name + ".");
		} 
		//if actual type returned by method body doesnt conform with declared return type then report error
		else if (Semantic.inheritanceGraph.conformance(T0_prime_string, node.typeid) == false ) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Inferred return type " + T0_prime_string + " of method "
					+ node.name + " does not conform to declared return type " + node.typeid + ".");
		}
	}

	public static void visitNode(AST.attr node, AST.class_  cur)
	{
		//obtaining the attribute type map from parent scopes
		Map<String, String> attributeTypeMap = Semantic.objectEnv.lookUpGlobal(cur.name);
		//getting attribute type map 
		String T0 = attributeTypeMap.get(node.name);
		if(T0 != null)
		{
			// if type of attribute matches with declared type of attribute
			if(T0.equals(node.typeid)) 
			{
				// if value of attribute i.e expression has some type, i.e not of type no_expr
				if (node.value instanceof AST.no_expr == false) 
				{
					Map<String, String> newScopeAttrTypeList = new HashMap<String, String>();

					// updating the type of attributes in new scope
					updateObjectEnv(Semantic.objectEnv, cur, newScopeAttrTypeList); //Enter a new scope with new bindings.
					
					visitNode(node.value, cur);
					String T1 = node.value.type;
					// if the type of value of attribute R-value doesnt conform with declared type of attr, report error
					if (!Semantic.inheritanceGraph.conformance(T1.toString(), T0.toString())) 
					{
						Semantic.reportError(cur.filename, node.lineNo,
								"Inferred type " + T1.toString() + " of initialization of attribute " + node.name
										+ " does not conform to declared type " + node.typeid + ".");
					}
					Semantic.objectEnv.exitScope(); //exiting scope.
				}
			}
		}
	}

	// visit Node for block (list of expressions)
	public static void visitNode(AST.block node, AST.class_  cur) 
	{
		// iterating through all expressions of block 
		for (AST.expression expr : node.l1) 
		{
			// calling visit node for each expression
			visitNode(expr, cur);
			node.type = expr.type;
		}
	}

	public static void visitNode(AST.assign node, AST.class_ cur) 
	{
		// getting type of the node of the current class or its ancestors
		String type = attrType(node.name, cur);
		if (type == null) 
		{
			Semantic.reportError(cur.filename, node.lineNo,
					"Identifier: " + node.name + " in class " + cur.name.toString() + " is undefined");
			node.type = "Object";
		} 
		else 
		{
			// calling visit node for expression of RHS of assignment
			visitNode(node.e1, cur);
			// getting its type
			String type_prime = node.e1.type;
			// checking if type of expression conforms with declared type of attribute in assignment
			if (Semantic.inheritanceGraph.conformance(type_prime.toString(), type.toString())) 
			{
				node.type = type_prime;
			} 
			else 
			{
				Semantic.reportError(cur.filename, node.lineNo,
						"Type " + type_prime.toString()
								+ " of assigned expression does not conform to declared type " + type.toString()
								+ " of identifier " + node.name + ".");
				node.type = "Object";
			}
		}
	}

	// visit node for all different types of expression nodes
	public static void visitNode(AST.expression node, AST.class_ cur) 
	{
		if (node instanceof AST.assign)
			visitNode((AST.assign) node, cur);
		else if (node instanceof AST.static_dispatch)
			visitNode((AST.static_dispatch) node, cur);
		else if (node instanceof AST.dispatch)
			visitNode((AST.dispatch) node, cur);
		else if (node instanceof AST.cond)
			visitNode((AST.cond) node, cur);
		else if (node instanceof AST.loop)
			visitNode((AST.loop) node, cur);
		else if (node instanceof AST.block)
			visitNode((AST.block) node, cur);
		else if (node instanceof AST.let)
			visitNode((AST.let) node, cur);
		else if (node instanceof AST.typcase)
			visitNode((AST.typcase) node, cur);
		else if (node instanceof AST.new_)
			visitNode((AST.new_) node, cur);
		else if (node instanceof AST.isvoid)
			visitNode((AST.isvoid) node, cur);
		else if (node instanceof AST.plus)
			visitNode((AST.plus) node, cur);
		else if (node instanceof AST.sub)
			visitNode((AST.sub) node, cur);
		else if (node instanceof AST.mul)
			visitNode((AST.mul) node, cur);
		else if (node instanceof AST.divide)
			visitNode((AST.divide) node, cur);
		else if (node instanceof AST.comp)
			visitNode((AST.comp) node, cur);
		else if (node instanceof AST.lt)
			visitNode((AST.lt) node, cur);
		else if (node instanceof AST.leq)
			visitNode((AST.leq) node, cur);
		else if (node instanceof AST.eq)
			visitNode((AST.eq) node, cur);
		else if (node instanceof AST.neg)
			visitNode((AST.neg) node, cur);
		else if (node instanceof AST.object)
			visitNode((AST.object) node, cur);
		else if (node instanceof AST.int_const)
			visitNode((AST.int_const) node, cur);
		else if (node instanceof AST.string_const)
			visitNode((AST.string_const) node, cur);
		else if (node instanceof AST.bool_const)
			visitNode((AST.bool_const) node, cur);
	}

	// First, we visit the caller node. We also check that type of caller conforms with actual type of statc_dispatch node.
	// Then we maintain a list of actuals by visiting each actual. 
	// Then we check that the number of actuals (arguments) are same as the number of parameters in actual method 
	// and that type of each actual conforms with the type of parameter.
	public static void visitNode(AST.static_dispatch node, AST.class_ cur) 
	{
		visitNode(node.caller, cur);
		Boolean check=false;
		String T0 = node.caller.type;
		List<String> actualTypes = new ArrayList<String>();
		for (AST.expression exp : node.actuals) 
		{
			visitNode(exp, cur);
			actualTypes.add(exp.type);
		}

		if (Semantic.inheritanceGraph.conformance(T0.toString(), node.typeid.toString()) == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Expression type " + T0.toString()
					+ " does not conform to declared static dispatch type " + node.typeid.toString());
		}

		Map<String, List<String>> curr_methods_map = Semantic.methodEnv.lookUpGlobal(node.typeid);
		List<String> formalTypes = curr_methods_map.get(node.name);
		if (formalTypes == null) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Dispatch to undefined method " + node.name + ".");
			check=true;
		} 
		else 
		{
			if (actualTypes.size() != formalTypes.size() - 1) 
			{
				Semantic.reportError(cur.filename, node.lineNo,
						"Method " + node.name + " called with wrong number of arguments.");
				check=true;
			} 
			else 
			{
				for (int i = 0; i < actualTypes.size(); i++) 
				{
					if (Semantic.inheritanceGraph.conformance(actualTypes.get(i).toString(), formalTypes.get(i).toString()) == false) 
					{
						Semantic.reportError(cur.filename, node.lineNo, "Inferred type " + actualTypes.get(i).toString()
								+ " does not conform to formal type " + formalTypes.get(i).toString());
						check=true;
						break;
					}
				}
			}
		}
		if(check)
			node.type = "Object";
		else
		{
			String T_return;
			T_return = formalTypes.get(formalTypes.size() - 1);
			node.type = T_return;		
		}
	}

	// First, we visit the caller node. Then we maintain a list of actuals by visiting each actual. 
	// Then we check that the number of actuals (arguments) are same as the number of parameters in actual method 
	// and that type of each actual conforms with the type of parameter. 
	public static void visitNode(AST.dispatch node, AST.class_ cur) {

		Boolean check=false;
		visitNode(node.caller, cur);
		String T0 = node.caller.type;
		List<String> actualTypes = new ArrayList<String>();
		for (AST.expression exp : node.actuals) 
		{
			visitNode(exp, cur);
			actualTypes.add(exp.type);
		}
		String T0_prime;
		T0_prime = T0;

		Map<String, List<String>> curr_methods_map = Semantic.methodEnv.lookUpGlobal(cur.name);
		List<String> formalTypes = getFormalList(node.name, Semantic.classMap.get(T0_prime.toString()));
		if (formalTypes == null) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Dispatch to undefined method " + node.name + ".");
			check=true;
		} 
		else 
		{
			if (actualTypes.size() != formalTypes.size() - 1) 
			{
				Semantic.reportError(cur.filename, node.lineNo,
						"Method " + node.name + " called with wrong number of arguments.");
				check=true;
			} 
			else 
			{
				for (int i = 0; i < actualTypes.size(); i++) 
				{
					if (Semantic.inheritanceGraph.conformance(actualTypes.get(i).toString(), formalTypes.get(i).toString()) == false) 
					{
						Semantic.reportError(cur.filename, node.lineNo,
								"In call of method " + node.name + ", type " + actualTypes.get(i).toString()
										+ " of parameter " + "variable" + " does not conform to declared type "
										+ formalTypes.get(i).toString());
						check=true;
						break;
					}
				}
			}
		}
		if(check)
			node.type = "Object";
		else
		{
			String T_return;
			T_return = formalTypes.get(formalTypes.size() - 1);
			node.type = T_return;
		}	
	}

	public static void visitNode(AST.cond node, AST.class_ cur) 
	{
		Boolean check=false;
		// visiting predicate node
		visitNode(node.predicate, cur);
		// condition statment should have boolean type
		if (node.predicate.type.equals("Bool") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Predicate of 'if' does not have type Bool.");
			check=true;
		}
		// visiting if node 
		visitNode(node.ifbody, cur);
		// visiting else node 
		visitNode(node.elsebody, cur);
		// assign the least common ancestor of types of if and else body to node
		if(!check)
			node.type = Semantic.classMap.get(Semantic.inheritanceGraph.lca(node.ifbody.type, node.elsebody.type)).name;
		else
			node.type = "Object";
	}

	public static void visitNode(AST.loop node, AST.class_ cur) 
	{
		// visiting predicate node of loop
		visitNode(node.predicate, cur);
		String T1 = node.predicate.type;
		// type of predicate in the loop must be boolean
		if (T1.equals("Bool") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Loop condition does not have type Bool.");
		} 
		else 
		{
			visitNode(node.body, cur);
		}
		node.type = "Object";
	}

	// First, We visit the predicate node. We check that type of predicate node is Bool. 
	// Then for each branch, we enter the new scope by updating object environment with new set of variables and after leaving the branch we exit scope. 
	// We check if the declared type of branch is unique by maintaining a set of types of branch types. Then we visit the expression node 
	// and add its type in a list casetypes. After visiting all branches, 
	// we check if the case contains at least one branch or not. Also, we find the least common ancestor of types of each of the branches and assign this type to node type.
	public static void visitNode(AST.typcase node, AST.class_ cur) 
	{
		visitNode(node.predicate, cur);
		Set<String> branch_decl = new HashSet<String>();
		List<String> casetypes = new LinkedList<String>();
		for (AST.branch br : node.branches) 
		{
			Map<String, String> newBindings = new HashMap<String, String>();
			newBindings.put(br.name, br.type);
			updateObjectEnv(Semantic.objectEnv, cur, newBindings);

			// Checks if declared types of each branch is unique.
			if (branch_decl.contains(br.type)) 
			{
				Semantic.reportError(cur.filename, node.lineNo,
						"Duplicate branch " + br.type.toString() + " in case statement.");
				node.type = "Object";
			} 
			else 
			{
				branch_decl.add(br.type);
			}

			// Evaluate the expression, then exit the scope.
			AST.expression e = br.value;
			visitNode(e, cur);
			String caseType = e.type;
			casetypes.add(caseType);
			Semantic.objectEnv.exitScope();
		}
		if (casetypes.isEmpty()) 
		{ // If there is no case branch, error.
			Semantic.reportError(cur.filename, node.lineNo, "There should be at least one branch in cases");
			node.type = "Object";
		} 
		else 
		{
			// Calculates the Least Upper Bound of the case expressions.
			String case_lub = casetypes.remove(0);
			while (casetypes.isEmpty() == false) 
			{
				String lub_string = Semantic.inheritanceGraph.lca(case_lub.toString(), casetypes.remove(0).toString());
				case_lub = Semantic.classMap.get(lub_string).name;
			}
			node.type = case_lub;
		}
	}

	// Here we check that the type of the value node of let conforms with the declared type. 
	// Then, we add this to new scope and assigns the type of let node as type of its body.
	public static void visitNode(AST.let node, AST.class_ cur) 
	{
		String T0_prime_string;
		String type_decl = node.typeid;
		T0_prime_string = type_decl;

		if ((node.value instanceof AST.no_expr) == false) 
		{
			visitNode(node.value, cur);
			String T1 = node.value.type;
			if (Semantic.inheritanceGraph.conformance(T1.toString(), T0_prime_string) == false) 
			{
				Semantic.reportError(cur.filename, node.lineNo,
						"Inferred type " + T1.toString() + " does not conform to indentifier " + node.name.toString()
								+ "'s declared type " + T0_prime_string);
			}
		}
		Map<String, String> newBindings = new HashMap<String, String>();
		newBindings.put(node.name, type_decl);
		updateObjectEnv(Semantic.objectEnv, cur, newBindings);
		visitNode(node.body, cur);
		node.type = node.body.type;
		Semantic.objectEnv.exitScope();
	}

	public static void visitNode(AST.plus node, AST.class_ cur) 
	{
		// visting first element to left of plus 
		Boolean check=false;
		visitNode(node.e1, cur);
		// only integers can be added, else reporting error
		if (node.e1.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo,"First Element of plus should be Int");
			check=true;
		}
		// visiting second element to right of plus
		visitNode(node.e2, cur);
		// only integers can be added, else reporting error
		if (node.e2.type.equals("Int") == false) {
			Semantic.reportError(cur.filename, node.lineNo, "Second Element of plus should be Int");
			check=true;
		}
		if(!check)
			node.type = "Int";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.sub node, AST.class_ cur) 
	{
		Boolean check=false;
		// visiting first element of subtraction operator
		visitNode(node.e1, cur);
		// only integers can be subtracted, else reporting error
		if (node.e1.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "First Element of sub should be Int");
			check=true;
		}
		// visiting first element of subtraction operator
		visitNode(node.e2, cur);
		// only integers can be subtracted, else reporting error
		if (node.e2.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Second Element of sub should be Int");
			check=true;
		}
		if(!check)
			node.type = "Int";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.mul node, AST.class_ cur) 
	{
		Boolean check=false;
		// visiting first element before multiplication operator
		visitNode(node.e1, cur);
		// only integers can be multiplied, else reporting error
		if (node.e1.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "First Element of mul should be Int");
			check=true;
		}
		// visiting second element of multiplication operator
		visitNode(node.e2, cur);
		// only integers can be multiplicated, else reporting error
		if (node.e2.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Second Element of mul should be Int");
			check=true;
		}
		if(!check)
			node.type = "Int";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.divide node, AST.class_ cur) 
	{
		Boolean check=false;
		// visting first element 
		visitNode(node.e1, cur);
		// only integers can be divided, else reporting error
		if (node.e1.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "First Element of divide should be Int");
			check=true;
		}
		// visiting second element
		visitNode(node.e2, cur);
		// only integers can be divided, else reporting error
		if (node.e2.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Second Element of divide should be Int");
			check=true;
		}
		if(!check)
			node.type = "Int";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.neg node, AST.class_ cur) 
	{
		Boolean check=false;
		// visiting node
		visitNode(node.e1, cur);
		// only integers can be negated, else reporting error
		if (node.e1.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Expr of neg should be Int");
			check=true;
		}
		if(!check)
			node.type = "Int";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.lt node, AST.class_ cur) 
	{
		Boolean check=false;
		// visting first node element
		visitNode(node.e1, cur);
		// only integers can be multiplicated, else reporting error
		if (node.e1.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "First Expr of less than should be Int type");
			check=true;
		}
		visitNode(node.e2, cur);
		// only integers can be multiplicated, else reporting error
		if (node.e2.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Second Expr of less than should be Int type");
			check=true;
		}
		if(!check)
			node.type = "Bool";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.eq node, AST.class_ cur) 
	{
		Boolean check=false;
		// visiting first element of equality op
		visitNode(node.e1, cur);
		// getting its type
		String T1 = node.e1.type;
		// visiting second element of equality op
		visitNode(node.e2, cur);
		// getting its type
		String T2 = node.e2.type;
		Boolean e1Type = (T1.equals("Int") || T1.equals("String") || T1.equals("Bool"));
        Boolean e2Type = (T2.equals("Int") || T2.equals("String") || T2.equals("Bool"));
        if ((e1Type || e2Type)) 
        {
            if (e1Type.equals(e2Type) == false) 
            {
            	check = true;
                Semantic.reportError(cur.filename, node.lineNo, "Incompatible types " + e1Type + " & " + e2Type + " for equality testing");
            }
        }
		if(!check)
			node.type = "Bool";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.leq node, AST.class_ cur) 
	{
		Boolean check=false;
		// visiting first element before leq op
		visitNode(node.e1, cur);
		// element must be integer
		if (node.e1.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "First Expr of less than should be Int type");
			check=true;
		}
		// visiting second element after leq op
		visitNode(node.e2, cur);
		// element must be integer
		if (node.e2.type.equals("Int") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Second Expr of less than should be Int type");
			check=true;
		}
		if(!check)
			node.type = "Bool";
		else
			node.type = "Object";
	}

	// checks that type of expression is Bool.
	public static void visitNode(AST.comp node, AST.class_ cur) 
	{
		Boolean check=false;
		visitNode(node.e1, cur);
		if (node.e1.type.equals("Bool") == false) 
		{
			Semantic.reportError(cur.filename, node.lineNo, "Expression of not should be Bool type");
			check=true;
		}
		if(!check)
			node.type = "Bool";
		else
			node.type = "Object";
	}

	public static void visitNode(AST.int_const node, AST.class_ cur) 
	{
		node.type = "Int";
	}

	public static void visitNode(AST.bool_const node, AST.class_ cur) 
	{
		node.type = "Bool";
	}

	public static void visitNode(AST.string_const node, AST.class_ cur) 
	{
		node.type = "String";
	}

	public static void visitNode(AST.new_ node, AST.class_ cur) 
	{
		node.type = node.typeid;
	}

	public static void visitNode(AST.isvoid node, AST.class_ cur) 
	{
		visitNode(node.e1, cur);
		node.type = "Bool";
	}

	public static void visitNode(AST.no_expr node, AST.class_ cur) 
	{
		node.type = "_no_type";
	}

	// Checks if the identifier is in the scope or not.
	public static void visitNode(AST.object node, AST.class_ cur) 
	{
		Map<String, String> varMap = Semantic.objectEnv.lookUpGlobal(cur.name);
		String type = attrType(node.name, cur);
		if (type == null) 
		{
			Semantic.reportError(cur.filename, node.lineNo,"Identifier: " + node.name + " in class " + cur.name.toString() + " is undefined");
			node.type = "Object";
		} 
		else 
		{
			node.type = type;
		}
	}
}