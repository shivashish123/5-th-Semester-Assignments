Assignment: Code generation
Authors:
Ajinkya Bokade CS17BTECH11001
Shivashish Suman CS17BTECH11037

Introduction

In this assignment, we have implemented code generator for Cool. It produces LLVM-IR that implements
any program.

ClassNode.java

We have created class "ClassNode" which contains information about a class including its name,
parent name, attributes and methods. It contains "attrCount" map which gives the index of an
attribute in the class. "methodsIRName" contains mangled name of each function in a class which
is "<class-name>\_<function-name>".

BasicIR.java

It contains the functions for generating LLVM-IR for the basic data types. These functions
are involved in printing the IR corresponding to the functions of IO, Object and
String and Header information printed at start and function declarations.

Codegen.java

First we add the basic classes and their methods into the list of all the classes.Then graph
is created of all the classes within the program. After this, a breadth first traversal is
done giving each class a number as id so that we can directly access the children of a class
using adjacency list. During the BFS traversal,whenever a new class is inserted, it inherits
the attributes and methods of the parent class. We are also printing the class delarations
during BFS traversal. After the BFS traversal, we are printing the LLVM-IR for methods
of each class. Before generating the LLVM-IR for class methods, we are calling the
"printConstructorOfClass" function which generates the LLVM-IR for constructor of class.
The method printing is handled by "PrintIR4Expr" function which generates the IR
for each kind of expression.

Features of our Codegen

1. Class declaration

Each class is declared in IR as %class.classname = type . The class declaration is done which involves
inheriting from parent class. So first we declare Object Class as%class.Object = type i32, [1024 x i8] _ .
Note that i32 and [1024 x i8]_ are needed to maintain the class name and class size (memory needed to be
allocated to its object). For inheritance the child class always creates the first attribute as its parent.
Apart from that, type consists of the attributes defined in the function. If a class has an object of other
class as its attribute then it appears as a pointer to that class in the class declaration in IR. Int and
Bool appear as i32, while string as [1024 x i8]\*.

2. Handling static strings

A program typically has some strings that are used for printing to IO etc. These
strings get globally defined in the IR, as
@.str<number> = private unnamed_addr constant [<strlen> x i8] c"<str>\00", align 1

3. Constructor

For each class a constructor is defined, which initializes the attributes with ap-
propriate values. For Int, String and Bool if the initial values are not provided
0, false and ”” are initialized. For Objects, either the objects are initialized as
declared by new or if not then null is assigned. The constructor of the Main class
gets called by the main function which is the starting point of execution of an IR.
It is called as : call i32 @Main_Main(%class.Main\* %1)
Note that all classes constructors have class names (for ex. @Main_Main) and return i32

0. Whenever an object is created its constructor gets called.

1. Methods

A sample declaration of method is as follows :
define <ret*type> <function_mangled_name>(%class.<class_name>* %this [, type %formal_name]_){...}
For ex:
define i32 @A_fact( %class.A\* %this, i32 %n ){ } The first argument in a method is always
self pointer of its own class. After that the formals follow.
The function name is declared as @<Class_name>_<Function_name>.

5. Dispatch to void error

When we call a function we create an if construct, in which we call exit with
message defined by Abortdispvoid global variable upon a dispatch to void, which
aborts the program.

6. Division by zero error

In case of division we use sdiv. But to handle division by zero we must check
if the denominator is 0. To achieve this we have an if condition. In case the
denominator is zero we call exit with message defined by Abortdivby0 global
variable. Otherwise we continue with normal program flow.

7. If-Else statements

We have used phi nodes for getting values return values from body of if and
else statement. It decides which value to take depending on the branch we have
come from.

Expressions

Int_const, Bool_const:
Simply returns i32 as type and value as the value of constant

Object :
We check if it is a formal or an attribute. If formal, then we only
need to carefully load it otherwise if it is attribute, then we need to first
get the element pointer and then load it.

Complement :
We return 1 - x, because this converts true to false and vice versa.

Equal :
Use icmp eq operator

Less than or equal to :
Use icmp sle operator

Less than :
Use icmp slt operator

Negation :
We return 0 - x, for x.

Multiplication :
Used mul operator.

Subtraction :
Used sub operator.

Addition :
Used addition operator.

New :
First we allocate memory for the object using malloc with size as
obtained from class table. Then we bitcast it to proper type. Then we
call the constructor of the class.

Block :
Simply convert all the expressions within the block to IR.

Loop : Create 3 new blocks : predicate, loop body and loop end. The
condition expression of the loop goes to predicate which branches to either
end or body, and the body expression of the loop goes to loop body.

Static Dispatch :
First we must check for dispatch to void. Then we create the string that
will hold the parameters to the call. We need to use get element pointer
in case an attribute is being passed. After that we simply use the method call
as described earlier.

Condition :
To handle if-else construct we create ifbody, elsebody and endbody.
Eah block handles its corresponding expressions. At the end we need to
add the phi instruction as needed.

Assign :
Assignment can be done to an attribute which is achieved by having an additional
get element pointer. We use store for assignment, we do bitcast if the declared type doesn't
match with assigned type.

Test Cases

1. test1.cl
   Simple arithmetic expressions like add sub mul div and neg and using static dispatch on IO methods

2. test2.cl
   Loop to calculate factorial of given number n
   Static dispatch on function factorial

3. test3.cl
   Conditional statements
   CHecking if a number is even

4. test4.cl
   While loop to Calculate sum of first n numbers

5. test5.cl
   Runtime error
   Error: Division by 0

6. test6.cl
   Runtime error
   Error: Dispatch on void
