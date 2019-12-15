-- Method redefinitions ( Same function in inherited classes )

class A {
    foo( x : Int , y : String ) : Int {
        0
    };
};

class B inherits A {
    foo( a : Int , b : String ) : Int {
        1
    };
};

class Main {
    main () : Int {
        0
    };
};