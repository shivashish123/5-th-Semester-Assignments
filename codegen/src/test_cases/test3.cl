-- Conditional statements 
-- CHecking if a number is even 
class A 
{
	evenCheck(n : Int) : Bool {
		{	
			if ((n -(n/2)*2) = 0) then true else false fi;
		}
	};	
};

class Main 
{
	a: A <- new A;
    n: Int;
    io : IO <- new IO;
    result :Bool;
	main(): Int {
		{
            io@IO.out_string("Enter a number : ");
            n <- io@IO.in_int();
            result <-a@A.evenCheck(n);
            if (result = true) then 1 else 0 fi;
			1;
		}
	};
};