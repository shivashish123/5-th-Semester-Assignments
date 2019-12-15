For the first program :

	For first feature
		1. g++ EchoServer1.cpp -o server
		2 ./server <port>
		3. g++ EchoClient1.cpp -o client
		4. ./client <ip-address> <port>

	For second feature

		1. g++ EchoServer2.cpp -o server
		2 ./server <port>
		3. g++ EchoClient2.cpp -o client
		4. ./client <ip-address> <port> <message>


For second program (1-Server-Many-Clients) : {Note : Change "#define N 4" to change number of clients}
	
	1. g++ MultiServer.cpp -pthread -std=c++11 -o server
	2 ./server <port>
	3. g++ MultiClient.cpp -pthread -std=c++11 -o client
	4. ./client <ip-address> <port>

For third program (Protocol-Independent) :
	1. gcc ProtServer.c -o server
	2 ./server <port>
	3. gcc ProtClient.cpp -o client
	4. ./client <ip-address> <port> <message>


