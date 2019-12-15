-- Rules for let expression

class A {
    a : Int;
    b : Int;
    c : String;
    foo() : Int {
        let d : Int <- 5 in
        let b : String in
        let c : Int <- 10 in {
            d <- d + a;
            b <- "ABC";
            c <- a + d;
            0;
        }  
    };
};

class Main {
    main () : Int {
        0
    };
};