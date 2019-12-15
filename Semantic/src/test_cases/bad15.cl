-- Redefine attributes or methods in the same scope 

class A {
    a : Int;
    a : String;
    a : Int;
};

class C {
    f1() : Int {
        0
    };
    f1(x : Int) : Int {
        0
    };
};

class Main {
    main() : Int {
        0
    };
};