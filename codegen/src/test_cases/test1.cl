-- simple arithmetic expressions and negation 

class Main inherits IO {
    firstNo : Int;
    secondNo : Int;
    thirdBool : Bool;
    a : A <- new A;
    io : IO <- new IO;
    result:Int;
    main() : Int {
        {
            thirdBool <- true;
            io@IO.out_string("Enter first number : ");
            firstNo <- io@IO.in_int();
            io@IO.out_string("\nEnter second number : ");
            secondNo <- io@IO.in_int();
            result <- a@A.add(firstNo, secondNo);
            result <- a@A.sub(firstNo, secondNo);
            result <- a@A.mul(firstNo, secondNo);
            result <- a@A.div(firstNo, secondNo);
            result <- a@A.neg(firstNo);
            0;
        }
    } ;
};
class A inherits IO{
    result : Int;
    resutl1 : Bool;
    a : IO <- new IO;
    add(firstNo : Int, secondNo : Int) : Int {
        {
            result <- firstNo + secondNo;
            a@IO.out_string("\nAddition of two numbers is : ");
            a@IO.out_int(result);
            0;
        }
    };
    sub(firstNo : Int, secondNo : Int) : Int {
        {
            result <- firstNo - secondNo;
            a@IO.out_string("\nSubtraction of two numbers is : ");
            a@IO.out_int(result);
            0;
        }
    };
    mul(firstNo : Int, secondNo : Int) : Int {
        {
            result <- firstNo * secondNo;
            a@IO.out_string("\nMultiplication of two numbers is : ");
            a@IO.out_int(result);
            0;
        }
    };
    div(firstNo : Int, secondNo : Int) : Int {
        {
            result <- firstNo / secondNo;
            a@IO.out_string("\nDivision of two numbers is : ");
            a@IO.out_int(result);
            0;
        }
    };
    neg(firstNo : Int) : Int {
        {
            result <- ~ firstNo;
            a@IO.out_string("\nNegation of first number is : ");
            a@IO.out_int(result);
            0;
        }
    };
};