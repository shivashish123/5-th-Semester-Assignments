-- Type of method body does not conform to declared return type 

class A {
    a : Int;
};

class B inherits A {
    b : Int;
};

class C {
    c : Int;
};

class D {
    f1() : B {
        new A
    };
    f2() : A {
        new B
    };
    f3() : B {
        new C
    };
};

class Main {
    main() : Int {
        0
    };
};