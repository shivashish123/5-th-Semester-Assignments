#include <bits/stdc++.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define BUFSIZE 1024

static const int MAXPENDING = 5; // Maximum outstanding connection requests
using namespace std; 

map<string,int> m; // map from ip to integer id
vector<string> ipaddr; // vector of ip-addresses
vector< vector<string> > v;	// vector of messages received from each ip-address 

int main(int argc, char ** argv) {

	int uniqString=0;
	int flag=0;

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

	cout<<"----\n";
	// Server Loop
	for (;;) {
		struct sockaddr_in clntAddr;
		socklen_t clntAddrLen = sizeof(clntAddr);

		// Wait for a client to connect
		int clntSock = accept(servSock, (struct sockaddr *) &clntAddr, &clntAddrLen);
		if (clntSock < 0) {
			perror("accept() failed");
			exit(-1);
		}

		char clntIpAddr[INET_ADDRSTRLEN];
		if (inet_ntop(AF_INET, &clntAddr.sin_addr.s_addr, 
				clntIpAddr, sizeof(clntIpAddr)) != NULL) {
			printf("Message received from client %s %d\n----\n", 
					clntIpAddr, ntohs(clntAddr.sin_port));
		} else {
			puts("----\nUnable to get client IP Address");
		}

		// Receive data
		char buffer[BUFSIZE];
		memset(buffer, 0, BUFSIZE);
		ssize_t recvLen = recv(clntSock, buffer, BUFSIZE - 1, 0);
		if (recvLen < 0) {
			perror("recv() failed");
			exit(-1);
		}
		buffer[recvLen] = '\n';

		// ****************************************
		string temp,temp2;
		for(int i=0;i<recvLen;i++)
			temp+=buffer[i];

		for(int i=0;i<strlen(clntIpAddr);i++)
			temp2+=clntIpAddr[i];

		if(temp=="stop")
		{
			for(int i=0;i<v.size();i++)
			{
				cout<<"All Messages received from "<<ipaddr[i]<<" are :\n";
				for(int j=0;j<v[i].size();j++)
					cout<<v[i][j]<<endl;
				cout<<endl;
				cout<<"------------\n";
				cout<<endl;
			}
			flag=1;
		}

		else if(m.count(temp2)==0)
		{
			vector<string> y;
			v.push_back(y);
			v[uniqString].push_back(temp);
			m.insert(make_pair(temp2,uniqString++));
			ipaddr.push_back(temp2);
		}
		else
		{
			int indx=m[temp2];
			v[indx].push_back(temp);
		}

		//*****************************

		//fputs(buffer, stdout);

		while (recvLen > 0) {
			// printf("Begining of Client Loop\n");
			// Send the received data back to client
			ssize_t sentLen = send(clntSock, buffer, recvLen, 0);
			if (sentLen < 0) {
				perror("send() failed");
				exit(-1);
			} else if (sentLen != recvLen) {
				perror("send() sent unexpected number of bytes");
				exit(-1);
			}

			// See if there is more data to receive
			memset(buffer, 0, BUFSIZE);
			recvLen = recv(clntSock, buffer, BUFSIZE, 0);
			if (recvLen < 0) {
				perror("recv() failed");
				exit(-1);
			} else if (recvLen > 0) { // some data was remaining
				buffer[recvLen] = '\n';
				fputs(buffer, stdout);
			}
			// printf("End of Client Loop\n");
		}
		if(flag==1)
		{
			close(clntSock);
			break;
		}
		// printf("End of Server Loop\n");
	}

	printf("End of Program\n");

}
