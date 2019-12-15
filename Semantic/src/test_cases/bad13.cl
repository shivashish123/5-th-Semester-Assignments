-- Use of identifier that does not exist 

class A {
    a : Int;
};

class B {
    b : Int;
    f1() : Int {
        b <- a
    };
};

class Main {
    main() : Int {
        0
    };
};