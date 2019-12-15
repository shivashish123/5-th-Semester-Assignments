-- Arithmetic operations (binary and unary) and Comparison operations

class A {
    a : Bool;
    b : Int;
    foo() : Bool { {
        b = ~ b;
        a = not a;
        a;
    } };
};

class B {
    a : Int;
    b : Int;
    c : Int;
    d : Int;
    foo() : Int { {
    	a = a+b;
    	c = a*b;
    	b = a-c;
    	d = c/b;
        d;
    } };
};

class C {
    a : Int;
    b : Int;
    foo() : Bool {
            {
                a<b;
                a<=b;
            }
        };
};

class Main {
    main () : Int {
        0
    };
};