-- Rules for case 

class A {
    a : Int;
};

class B inherits A {
    b : Int;
};

class C inherits B {
    c : Int;
};

class D {
    a : A;
    b : B;
    c : C;
    d : Int;
    foo1() : Int {
        {
            case a of
                a1 : A => a1;
                a2 : B => a2;
                a3 : C => a3;
            esac;
            0;
        }
    };
    foo2() : Object {
        {
            case d of 
                a : String => "A";
                b : Bool => 1;
                c : Object => d;
            esac;
        }
    };
};

class Main {
    main() : Int {
        0
    };
};