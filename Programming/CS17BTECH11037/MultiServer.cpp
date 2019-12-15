#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <bits/stdc++.h>
#include <unistd.h>
#include <thread>
#define BUFSIZE 1024
#define N 4   // change this to change number of clients active at a time

using namespace std;
static const int MAXPENDING = 5; // Maximum outstanding connection requests

int clntSock[N];


// function to handle multiple clients
void func(int j)
{
	for (;;) {

		// Receive data
		char buffer[BUFSIZE];
		memset(buffer, 0, BUFSIZE);
		ssize_t recvLen = recv(clntSock[j], buffer, BUFSIZE - 1, 0);
		if (recvLen > 0) 
		{
			char buffer2[BUFSIZE],buffer3[BUFSIZE];
			buffer[recvLen] = '\n';
			string mys(buffer);
			int i=0;
			for(;i<mys.length();i++)
			{
				if(mys[i]=='-')
				{
					break;
				}
				buffer2[i]=mys[i];
			}
			buffer2[i]='\0';
			i++;
			int l=0;
			for(;i<mys.length();i++,l++)
			{
				buffer3[l]=mys[i];
			}
			buffer3[l]='\0';
			int idx=atoi(buffer3);
			ssize_t sentLen = send(clntSock[idx], buffer2, recvLen, 0);
		}
	}

}

int main(int argc, char ** argv) {

	if (argc != 2) {
		perror("<server port>");
		exit(-1);
	}

	in_port_t servPort = atoi(argv[1]); // Local port

	// create socket for incoming connections
	int servSock;
	if ((servSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
		perror("socket() failed");
		exit(-1);
	}

	// Set local parameters
	struct sockaddr_in servAddr;
	memset(&servAddr, 0, sizeof(servAddr));
	servAddr.sin_family = AF_INET;
	servAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servAddr.sin_port = htons(servPort);

	// Bind to the local address
	if (bind(servSock, (struct sockaddr *) &servAddr, sizeof(servAddr)) < 0) {
		perror("bind() failed");
		exit(-1);
	}

	// Listen to the client
	if (listen(servSock, MAXPENDING) < 0) {
		perror("listen() failed");
		exit(-1);
	}

	for(int i=0;i<N;i++)
	{
		struct sockaddr_in clntAddr;
		socklen_t clntAddrLen = sizeof(clntAddr);

		// Wait for a client to connect
		clntSock[i] = accept(servSock, (struct sockaddr *) &clntAddr, &clntAddrLen);
		if (clntSock < 0) {
			perror("accept() failed");
			exit(-1);
		}

		char clntIpAddr[INET_ADDRSTRLEN];
		if (inet_ntop(AF_INET, &clntAddr.sin_addr.s_addr, 
				clntIpAddr, sizeof(clntIpAddr)) != NULL) {
			printf("----\nHandling client %s %d\n", 
					clntIpAddr, ntohs(clntAddr.sin_port));
		} else {
			puts("----\nUnable to get client IP Address");
		}
	}

	thread th[N];
	for(int i=0;i<N;i++)
		th[i]=std::thread(func,i);
	for(int i=0;i<N;i++)
		th[i].join();
	// Server Loop
	
	for(int j=0;j<N;j++)
		close(clntSock[j]);
	printf("End of Program\n");

}
