-- WHile loop 
-- Calculates sum of first n numbers
class Main {
	i : Int;
	n: Int;
	result: Int;
	io : IO <- new IO;
	main() : Int {
		{
			result <- 0;
			io@IO.out_string("Enter a number n: ");
			n <- io@IO.in_int();
			while i < n loop
			{
				io@IO.out_int(i);
				io@IO.out_string(" ");
				result <- result+i;
				i <- i+1;
			}
			pool;
			io@IO.out_string("Sum of first ");
			io@IO.out_int(n);
			io@IO.out_string(" numbers is: ");
			io@IO.out_int(result);
			io@IO.out_string("\n");
			0;
		}
	};
};
