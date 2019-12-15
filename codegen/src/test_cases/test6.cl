-- Error: Dispatch on void
class TP {
	tp() : Int 
	{
		1
	};
};

class Main {
	i : TP;
	main() : Int 
	{
		i@TP.tp()
	};
};