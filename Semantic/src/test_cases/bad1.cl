-- Program to demonstrate the working of cycle checker in the inheritance graph
-- It is evident that there is a cycle in this inheritance graph (E-->A-->B-->C-->D-->E)

Class A inherits E {
	a : Int;
};

Class B inherits A { 
	b : Int;
};

Class C inherits B { 
	c : Int;
};

Class D inherits C { 
	d : Int;
};

Class E inherits D { 
	e : Int;
};

Class F inherits B { 
	f : Int;
};

Class Main inherits A{ 
	some_object : B;
	main() : Object {
	   {				
	   	some_object;
	   }
	};
};