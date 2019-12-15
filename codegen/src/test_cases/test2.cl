-- Performs mul and add to calculate factorial of given number n 
-- Static dispatch on function factorial
class A {
	value : Int;
	temp:Int;
	factorial(n : Int) : Int {
		if n < 0 then 1 else {
			value <- 1;
			temp <- 1;
			while temp <= n loop {
				value <- value * temp;
				temp <- temp+1;
			} pool;
			value;
		} fi
	};
};

class Main {
	a : A <- new A;
	io : IO <- new IO;
	n : Int;
	main() : Object {{
		io@IO.out_string("Enter a number : ");
		n <- io@IO.in_int();
		io@IO.out_int(a@A.factorial(n));
		io@IO.out_string("\n");
		new Object;
	}};
};