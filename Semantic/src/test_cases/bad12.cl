(* Incorrect static types of arithmetic operations *)

class A {
    a : String;
    b : String;
    c : Bool;
    d : Bool;
    e : Int;
    f : B;
    f1() : Int {
        {
            a+b;
            a+c;
            a+f;
        }
    };
    f2() : Int {
        {
            c-d;
            a-e;
            a-f;
        }
    };
    f3() : Int {
        {
            a*b;
            a*c;
            c*d;
            a*e;
        }
    };
    f4() : Int {
        {
            a/b;
            a/c;
            a/e;
            e/f;
        }
    };
};

class B {
    b : Int;
};

class C {
    c : Int;
};

class Main {
    main() : Int {
        0
    };
};