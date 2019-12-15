-- Declaration and definition of attributes

class A {
    a : Int <- 5 ;
    b : String ;
    c : C <- new C ;
};
class B {
    a : Bool <- true ;
    b : A <- new A;

};
class C {
    a : String <- "ABC" ;
    b : B;
    c : Int;
};

class Main {
    main () : Int {
        0
    };
};