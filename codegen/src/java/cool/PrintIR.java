package cool;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Map.Entry;

public class PrintIR {
	static int regCount = 0; // current register count
	static int strCount = 1; // number of string constants
	static int totalIfCount = 0; // total number of condtionals
	static int totalLoopCount = 0; // total number of loop
	static List<String> phiRegList = new ArrayList<>();
	static String IRType; // return type in LLVM IR form for expression

	// Get IR type for corresponding COOL type
	public static String getIRType(String type) {
		if (Codegen.DATA_TYPES.containsKey(type)) {
			return Codegen.DATA_TYPES.get(type);
		} else
			return "%class." + type + "*";
	}

	// Get IR type from return type of expression of form type + %regCount
	public static String getClassType(String type) {
		if (type.length() > 12 && type.substring(0, 12).equals("[1024 x i8]*"))
			return "[1024 x i8]*";
		else
			return type.split(" ")[0];
	}

	// Get regCount from return type of expression of form type + %regCount
	public static String getRegCount(String type) {
		String[] s = type.split(" ");
		return s[s.length - 1];
	}

	// Print LLVM IR for class declarations
	public static void printClassDecl(ClassNode cl, PrintWriter out) {
		if (cl.name.equals("Object")) {
			out.println("%class.Object = type { i32, [1024 x i8] * }");
			return;
		}

		// Print all the attributes of class
		String attrsStr = "";
		attrsStr += "%class." + cl.parent + ", ";
		for (Entry<String, AST.attr> e : cl.attrs.entrySet()) {
			AST.attr a = e.getValue();
			attrsStr += getIRType(a.typeid) + ", ";
		}
		if (attrsStr.length() >= 2)
			attrsStr = attrsStr.substring(0, attrsStr.length() - 2);
		out.println("%class." + cl.name + " = type { " + attrsStr + " }");
	}

	// Print LLVM IR for all methods of given class
	public static void printClassMethods(ClassNode cl, PrintWriter out) {
		totalLoopCount = -1;
		totalIfCount = -1;
		regCount = 0;

		// Before printing methods, printing constructor of that class
		printConstructorOfClass(cl, out);
		// checking if method is basic methods of basic classes and printing their IR
		if (cl == null || cl.name == null)
			return;
		if (cl.name.equals("Int") || cl.name.equals("Bool"))
			return;
		else if (cl.name.equals("String")) {
			out.println(BasicIR.getMethod("CONCAT", cl));
			out.println(BasicIR.getMethod("COPY", cl));
			out.println(BasicIR.getMethod("LENGTH", cl));
			out.println(BasicIR.getMethod("SUBSTR", cl));
		} else if (cl.name.equals("Object")) {
			out.println(BasicIR.getMethod("ABORT", cl));
		} else if (cl.name.equals("IO")) {
			out.println(BasicIR.getMethod("OUT_STRING", cl));
			out.println(BasicIR.getMethod("IN_STRING", cl));
			out.println(BasicIR.getMethod("OUT_INT", cl));
			out.println(BasicIR.getMethod("IN_INT", cl));
		} else {
			HashMap<String, AST.formal> fmap = new HashMap<String, AST.formal>();
			// Generate the code for the Method here.
			if (cl.methods != null) {
				for (Entry<String, AST.method> entry : cl.methods.entrySet()) {
					totalLoopCount = -1;
					totalIfCount = -1;
					regCount = 0;
					phiRegList.clear();
					AST.method me = entry.getValue();

					if (BasicIR.BasicMethods.contains(me.name.toUpperCase())) {
						out.println(BasicIR.getMethod(me.name.toUpperCase(), cl));
						continue;
					}

					out.print("define " + getIRType(me.typeid) + " " + cl.methodsIRName.get(me.name) + "(");
					for (int i = 0; i < me.formals.size(); i++) {
						if (i != 0)
							out.print(", ");
						out.print(getIRType(me.formals.get(i).typeid) + " %" + me.formals.get(i).name);
						fmap.put(me.formals.get(i).name, me.formals.get(i));
					}
					fmap.put("#rettype", new AST.formal("ret", me.typeid, 0));
					out.println(") {");

					// print method body
					PrintIR4Expr(cl, fmap, me.body, out);
					String retType = IRType;
					String rType = getClassType(retType);

					// Print return statement of method
					if (!rType.equals(getIRType(me.typeid))) {
						// bitcasting return type of body to declared type of method
						if (rType.equals("i32")) {
							out.println("%" + (++regCount) + " = call noalias i8* @malloc(i64 8)");
							out.println("%" + (++regCount) + " = bitcast i8* %" + (regCount - 1) + " to "
									+ getIRType(me.typeid));
						} else {
							out.println("%" + (++regCount) + " = bitcast " + retType + " to " + getIRType(me.typeid));
						}
						retType = getIRType(me.typeid) + " %" + regCount;
					}
					out.println("ret " + retType);
					out.println("}\n");
				}
			}
		}
	}

	// Print LLVM IR for constructor of class
	public static void printConstructorOfClass(ClassNode irclass, PrintWriter out) {
		if (irclass.name == "Int" || irclass.name == "Bool" || irclass.name == "String")
			return;
		if (irclass.name == "Object") {
			out.println("define i32 @Object_Object ( %class.Object* %this ) noreturn {");
			out.println("ret i32 0");
			out.println("}\n");
			return;
		}
		if (irclass.name == "IO") {
			out.println("define i32 @IO_IO ( %class.IO* %this ) noreturn {");
			out.println("ret i32 0");
			out.println("}\n");
			return;
		}
		regCount = 0;
		out.println("; Constructor of class " + irclass.name);
		out.print("define i32 @" + irclass.name + "_" + irclass.name);
		out.print(" ( %class." + irclass.name + "* " + "%this" + ") { \n");
		int attrIndex = 1;
		HashMap<String, AST.formal> fmap = new HashMap<String, AST.formal>();
		for (Entry<String, AST.attr> entry : irclass.attrs.entrySet()) {
			// printing LLVM IR for each attribute of class
			AST.attr a = entry.getValue();
			if (!(a.value instanceof AST.no_expr)) {
				AST.assign e = new AST.assign(a.name, a.value, 0);
				e.type = a.typeid;
				PrintIR4Expr(irclass, fmap, e, out);
				attrIndex++;
			}
			// assigning false as default to Bool attribute
			else if (a.typeid.equals("Bool")) {
				AST.assign e = new AST.assign(a.name, new AST.bool_const(false, 0), 0);
				e.type = "Bool";
				PrintIR4Expr(irclass, fmap, e, out);
				attrIndex++;
			}
			// assigning empty string as default to String attribute
			else if (a.typeid.equals("String")) {
				AST.assign e = new AST.assign(a.name, new AST.string_const("", 0), 0);
				e.type = "String";
				PrintIR4Expr(irclass, fmap, e, out);
				attrIndex++;
			}
			// assigning 0 as default to Int attribute
			else if (a.typeid.equals("Int")) {
				AST.assign e = new AST.assign(a.name, new AST.int_const(0, 0), 0);
				e.type = "Int";
				PrintIR4Expr(irclass, fmap, e, out);
				attrIndex++;
			}
			// other class attribute assigning null value
			else {
				String attrType = "%class." + irclass.name;
				out.println("%" + (++regCount) + " = getelementptr inbounds " + attrType + ", " + attrType
						+ "* %this, i32 0, i32 " + (attrIndex));
				out.println("store " + getIRType(a.typeid) + " null, " + getIRType(a.typeid) + "* %" + regCount
						+ ", align 4");
				attrIndex++;
			}
		}
		// current class
		String currCl = getIRType(irclass.name) + " %this";
		ClassNode cl = irclass;
		// traversing the parents of this class until Object is reached
		while (!(cl.name).equals("Object")) {
			// pointer to parent class is stored as first index in class declaration
			out.println("%" + (++regCount) + " = getelementptr inbounds %class." + cl.name + ", %class." + cl.name
					+ "* " + getRegCount(currCl) + ", i32 0, i32 0");
			currCl = "%class." + cl.parent + "* %" + regCount;
			cl = Codegen.classes.get(cl.parent);
		}

		// storing size of class in first atribute of class Object
		out.println("%" + (++regCount) + " = getelementptr inbounds %class.Object, %class.Object* "
				+ getRegCount(currCl) + ", i32 0, i32 0");
		out.println("store i32 " + irclass.size + ", i32* %" + regCount);
		// storing name of class in second atribute of class Object and adding the name
		// of class as string constant
		String strType = "[" + (irclass.name.length() + 1) + " x i8]";
		Codegen.globalStrings.add("@.str" + (strCount++) + " = private unnamed_addr constant " + strType + " c\""
				+ irclass.name + "\\00\", align 1\n");
		// bitcasting string type to [1024 x i8]*
		out.println("%" + (++regCount) + " = bitcast " + strType + "* @.str" + (strCount - 1) + " to [1024 x i8]*");
		out.println("%" + (++regCount) + " = getelementptr inbounds %class.Object, %class.Object* "
				+ getRegCount(currCl) + ", i32 0, i32 1");
		out.println("store [1024 x i8]* %" + (regCount - 1) + ", [1024 x i8]** %" + regCount);
		out.println("ret i32 0");
		out.println("}\n");
	}

	// Print LLVM IR method for each expression
	public static void PrintIR4Expr(ClassNode node, HashMap<String, AST.formal> fmap, AST.expression exp,
			PrintWriter out) {
		// int const
		if (exp.getClass() == AST.int_const.class) {
			AST.int_const e = (AST.int_const) exp;
			IRType = "i32 " + e.value;
		}

		// bool const
		else if (exp.getClass() == AST.bool_const.class) {
			AST.bool_const e = (AST.bool_const) exp;
			boolean val = e.value;
			int v = val ? 1 : 0;
			IRType = "i32 " + v;
		}

		// string const
		else if (exp.getClass() == AST.string_const.class) {
			AST.string_const str = (AST.string_const) exp;
			String type = "[" + (str.value.length() + 1) + " x i8]";
			Codegen.globalStrings.add("@.str" + (strCount++) + " = private unnamed_addr constant " + type + " c\""
					+ str.value + "\\00\", align 1\n");
			out.println("%" + (++regCount) + " = bitcast " + type + "* @.str" + (strCount - 1) + " to [1024 x i8]*");
			IRType = "[1024 x i8]* %" + regCount;
		}

		// assign
		else if (exp.getClass() == AST.assign.class) {
			AST.assign e = (AST.assign) exp;
			String lhsType = getIRType(e.type);
			AST.expression exp1 = e.e1;
			PrintIR4Expr(node, fmap, exp1, out);
			String rhsType = IRType;
			String rhstype = getClassType(rhsType);

			// Types do not match, so did bitcast
			if (!(rhstype).equals(lhsType)) {
				if ((rhstype).equals("i32")) {
					out.println("%" + (++regCount) + " = call noalias i8* @malloc(i64 8)"); // Object size
					out.println("%" + (++regCount) + " = bitcast i8* %" + (regCount - 1) + " to " + lhsType);
				} else {
					out.println("%" + (++regCount) + " = bitcast " + rhsType + " to " + lhsType);
				}
				IRType = lhsType + " %" + regCount;
			}

			if (node.attrs.containsKey(e.name)) {
				out.println("%" + ++regCount + " = getelementptr inbounds %class." + node.name + ", %class." + node.name
						+ "* %this, i32 0, i32 " + node.attrCount.get(e.name));
				out.println("store " + IRType + ", " + lhsType + "* %" + regCount + ", align 4");
			} else { // it is present in formals
				out.println("%" + e.name + " = alloca " + getIRType(exp.type) + ", align 4");
				out.println("store " + IRType + ", " + lhsType + "* %" + e.name + ", align 4");
			}
		}

		// Complement operator
		else if (exp instanceof AST.comp) {
			AST.comp e = (AST.comp) exp;
			PrintIR4Expr(node, fmap, e.e1, out);
			String retType = IRType;
			retType = retType.substring(4);
			out.println("%" + (++regCount) + " = sub nsw i32 1, " + retType);
			IRType = "i32 %" + regCount;
		}

		// plus operator
		else if (exp.getClass() == AST.plus.class) {
			AST.plus expr = (AST.plus) exp;
			PrintIR4Expr(node, fmap, expr.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			PrintIR4Expr(node, fmap, expr.e2, out);
			String retType2 = IRType;
			retType2 = retType2.substring(4);
			out.println("%" + ++regCount + " = add nsw i32 " + retType1 + ", " + retType2);
			IRType = "i32 %" + regCount;
		}

		// sub operator
		else if (exp.getClass() == AST.sub.class) {
			AST.sub expr = (AST.sub) exp;
			PrintIR4Expr(node, fmap, expr.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			PrintIR4Expr(node, fmap, expr.e2, out);
			String retType2 = IRType;
			retType2 = retType2.substring(4);
			out.println("%" + ++regCount + " = sub nsw i32 " + retType1 + ", " + retType2);
			IRType = "i32 %" + regCount;
		}

		// mul operator
		else if (exp.getClass() == AST.mul.class) {
			AST.mul expr = (AST.mul) exp;
			PrintIR4Expr(node, fmap, expr.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			PrintIR4Expr(node, fmap, expr.e2, out);
			String retType2 = IRType;
			retType2 = retType2.substring(4);
			out.println("%" + ++regCount + " = mul nsw i32 " + retType1 + ", " + retType2);
			IRType = "i32 %" + regCount;
		}

		// div operator
		else if (exp.getClass() == AST.divide.class) {
			AST.divide expr = (AST.divide) exp;
			PrintIR4Expr(node, fmap, expr.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			PrintIR4Expr(node, fmap, expr.e2, out);
			String retType2 = IRType;
			retType2 = retType2.substring(4);
			out.println("%" + (++regCount) + " = icmp eq i32 0, " + retType2);
			totalIfCount++;
			// Checking if divison by zero is happening and calling exit
			out.println("br i1 %" + regCount + ", label %ifbody" + totalIfCount + ", label %elsebody" + totalIfCount);
			out.println();
			out.println("ifbody" + totalIfCount + ":");
			phiRegList.add("ifbody" + totalIfCount);
			out.println("%" + (++regCount) + " = bitcast [22 x i8]* @Abortdivby0 to [1024 x i8]*");
			out.println("%" + (++regCount) + " = call %class.IO* @IO_out_string( %class.IO* null, [1024 x i8]* %"
					+ (regCount - 1) + ")");
			out.println("call void @exit(i32 1)");
			out.println("br label %elsebody" + totalIfCount);
			out.println();
			out.println("elsebody" + totalIfCount + ":");
			phiRegList.add("elsebody" + totalIfCount);
			out.println("%" + (++regCount) + " = sdiv i32 " + retType1 + ", " + retType2);
			IRType = "i32 %" + regCount;
		}

		// equal to operator
		else if (exp.getClass() == AST.eq.class) {
			AST.eq e = (AST.eq) exp;
			PrintIR4Expr(node, fmap, e.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			PrintIR4Expr(node, fmap, e.e2, out);
			String retType2 = IRType;
			retType2 = retType2.substring(4);
			out.println("%" + ++regCount + " = icmp eq i32 " + retType1 + ", " + retType2);
			IRType = "i32 %" + regCount;
		}

		// less than operator
		else if (exp.getClass() == AST.lt.class) {
			AST.lt e = (AST.lt) exp;
			PrintIR4Expr(node, fmap, e.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			PrintIR4Expr(node, fmap, e.e2, out);
			String retType2 = IRType;
			retType2 = retType2.substring(4);
			out.println("%" + (++regCount) + " = icmp slt i32 " + retType1 + ", " + retType2);
			IRType = "i32 %" + regCount;
		}

		// less than equal to operator
		else if (exp.getClass() == AST.leq.class) {
			AST.leq e = (AST.leq) exp;
			PrintIR4Expr(node, fmap, e.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			PrintIR4Expr(node, fmap, e.e2, out);
			String retType2 = IRType;
			retType2 = retType2.substring(4);
			out.println("%" + ++regCount + " = icmp sle i32 " + retType1 + ", " + retType2);
			IRType = "i32 %" + regCount;
		}

		// negation (not exp) operator
		else if (exp.getClass() == AST.neg.class) {
			AST.neg e = (AST.neg) exp;
			PrintIR4Expr(node, fmap, e.e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			out.println("%" + (++regCount) + " = sub nsw i32 0, " + retType1);
			IRType = "i32 %" + regCount;
		}

		// new_
		else if (exp.getClass() == AST.new_.class) {
			AST.new_ e = (AST.new_) exp;
			String type = getIRType(e.typeid);
			int size = Codegen.classes.get(e.typeid).size;
			out.println("%" + (++regCount) + " = call noalias i8* @malloc(i64 " + size + ")");
			out.println("%" + (++regCount) + " = bitcast i8* %" + (regCount - 1) + " to " + type);
			out.println("%" + (++regCount) + " = call i32 @" + e.typeid + "_" + e.typeid + "( " + type + " %"
					+ (regCount - 1) + " )");
			IRType = getIRType(e.typeid) + " %" + (regCount - 1);
		}

		// object
		else if (exp.getClass() == AST.object.class) {
			AST.object e = (AST.object) exp;
			String type = getIRType(e.type);
			// if atrribute is pointer to class i.e. %this
			if (e.name.equals("this")) {
				out.println("%" + ++regCount + " = alloca " + type + ", align 4");
				out.println("store " + type + " %this, " + type + "* %" + regCount + ", align 4");
				out.println("%" + ++regCount + " = load " + type + ", " + type + "* %" + (regCount - 1) + ", align 4");
				IRType = type + " %" + regCount;
			}

			// if it is present in formals
			else if (fmap.containsKey(e.name)) {
				out.println("%" + ++regCount + " = alloca " + type + ", align 4");
				out.println("store " + type + " %" + e.name + ", " + type + "* %" + regCount + ", align 4");
				out.println("%" + ++regCount + " = load " + type + ", " + type + "* %" + (regCount - 1) + ", align 4");
				IRType = type + " %" + regCount;
			}

			// it is present as attribute of class.
			else {
				out.println("%" + ++regCount + " = getelementptr inbounds %class." + node.name + ", %class." + node.name
						+ "* %this, i32 0, i32 " + node.attrCount.get(e.name));
				out.println("%" + ++regCount + " = load  " + type + ", " + type + "* %" + (regCount - 1) + ", align 4");
				IRType = type + " %" + regCount;
			}
		}

		// block
		else if (exp.getClass() == AST.block.class) {
			AST.block e = (AST.block) exp;
			List<AST.expression> expressionsList = new ArrayList<AST.expression>();
			expressionsList = e.l1;
			String retType = "";
			for (int i = 0; i < expressionsList.size(); ++i) {
				AST.expression e2 = new AST.expression();
				e2 = expressionsList.get(i);
				PrintIR4Expr(node, fmap, e2, out);
				retType = IRType;
			}
			IRType = retType;
		}

		// if then else conditional
		else if (exp.getClass() == AST.cond.class) {
			int currentIfCount = ++totalIfCount;
			AST.cond e = (AST.cond) exp;
			AST.expression e1 = e.predicate;
			AST.expression e2 = e.ifbody;
			AST.expression e3 = e.elsebody;
			// predicate
			PrintIR4Expr(node, fmap, e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			out.println(
					"br i1 " + retType1 + ", label %ifbody" + currentIfCount + ", label %elsebody" + currentIfCount);

			out.println("ifbody" + currentIfCount + ":");
			phiRegList.add("ifbody" + currentIfCount);
			PrintIR4Expr(node, fmap, e2, out);
			String retTypeIfBody = getRegCount(IRType);
			String ifBodyRegCount = phiRegList.get(phiRegList.size() - 1);
			out.println("br label %endbody" + currentIfCount);

			out.println("elsebody" + currentIfCount + ":");
			phiRegList.add("elsebody" + currentIfCount);
			PrintIR4Expr(node, fmap, e3, out);
			String retTypeElseBody = getRegCount(IRType);
			String elseBodyRegCount = phiRegList.get(phiRegList.size() - 1);

			out.println("br label %endbody" + currentIfCount);
			out.println("endbody" + currentIfCount + ":");
			phiRegList.add("endbody" + currentIfCount);
			out.println("%" + (++regCount) + " = phi " + getIRType(e.type) + " [" + retTypeIfBody + ", %"
					+ ifBodyRegCount + "], [" + retTypeElseBody + ", %" + elseBodyRegCount + "]");
			IRType = getIRType(e.type) + " %" + regCount;
		}

		// while loop
		else if (exp.getClass() == AST.loop.class) {
			String retType;
			AST.loop e = (AST.loop) exp;
			AST.expression e1 = e.predicate;
			AST.expression e2 = e.body;
			int currentLoopCount = ++totalLoopCount;
			out.println("br label %" + "predicate" + currentLoopCount);
			out.println("predicate" + currentLoopCount + ":");
			phiRegList.add("predicate" + currentLoopCount);
			PrintIR4Expr(node, fmap, e1, out);
			String retType1 = IRType;
			retType1 = retType1.substring(4);
			out.println("br i1 " + retType1 + ", label %loopbody" + currentLoopCount + ", label %loopend"
					+ currentLoopCount);
			out.println("loopbody" + currentLoopCount + ":");
			phiRegList.add("loopbody" + currentLoopCount);
			PrintIR4Expr(node, fmap, e2, out);
			retType = IRType;
			out.println("br label %predicate" + currentLoopCount);
			out.println("loopend" + currentLoopCount + ":");
			phiRegList.add("loopend" + currentLoopCount);
			IRType = retType;
		}

		// Static dispatch
		else if (exp instanceof AST.static_dispatch) {
			AST.static_dispatch e = (AST.static_dispatch) exp;
			PrintIR4Expr(node, fmap, e.caller, out);
			String retType = IRType;
			String callerClass = e.caller.type;
			// Printing IR for each actual of function
			List<String> actualsList = new ArrayList<>();
			for (AST.expression actual : e.actuals) {
				PrintIR4Expr(node, fmap, actual, out);
				actualsList.add(IRType);
			}
			// Check if dispatch to void is happening and if so, calling exit function
			totalIfCount++;
			out.println("%" + (++regCount) + " = icmp eq " + retType + ", null");
			out.println("br i1 %" + regCount + ", label %ifbody" + totalIfCount + ", label %elsebody" + totalIfCount);
			out.println("ifbody" + totalIfCount + ":");
			phiRegList.add("ifbody" + totalIfCount);
			out.println("%" + (++regCount) + " = bitcast [25 x i8]* @Abortdispvoid to [1024 x i8]*");
			out.println("%" + (++regCount) + " = call %class.IO* @IO_out_string( %class.IO* null, [1024 x i8]* %"
					+ (regCount - 1) + ")");
			out.println("call void @exit(i32 1)");
			out.println("br label %elsebody" + totalIfCount);
			out.println("elsebody" + totalIfCount + ":");
			phiRegList.add("elsebody" + totalIfCount);
			// Traversing the class to its parent until the correct type is encountered
			ClassNode cl = Codegen.classes.get(callerClass);
			while (!callerClass.equals(e.typeid)) {
				out.println("%" + (++regCount) + " = getelementptr inbounds " + "%class." + callerClass + ", "
						+ getIRType(callerClass) + " " + getRegCount(retType) + ", i32 0, i32 0");
				callerClass = cl.parent;
				retType = "%class." + cl.parent + "* %" + regCount;
				cl = Codegen.classes.get(cl.parent);
			}
			// calling the function
			String allActuals = retType;
			for (int i = 0; i < actualsList.size(); i++)
				allActuals += ", " + actualsList.get(i);
			out.println("%" + (++regCount) + " = call " + getIRType(e.type) + " " + "@" + e.typeid + "_" + e.name + "("
					+ allActuals + ")");
			IRType = getIRType(e.type) + " %" + regCount;
		}
	}
}
