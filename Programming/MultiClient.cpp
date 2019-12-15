#include <bits/stdc++.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define BUFSIZE 1024
using namespace std;

void prin(int sockfd)
{
	while(1)
	{
		char buffer[BUFSIZE];
		memset(buffer, 0, BUFSIZE);
		ssize_t recvLen = recv(sockfd, buffer, BUFSIZE - 1, 0);
		if (recvLen > 0) 
		{
			string y(buffer);
			cout<<"Message Received:\n";
			cout<<y<<endl;
			cout<<endl;
		}
	}
}

int main(int argc, char **argv) {
	
	char *servIP = argv[1];
	char echoString[BUFSIZE];

	in_port_t servPort = atoi(argv[2]);
	
	//Creat a socket

	int flag=0;
	cout<<"Enter a message: \n";

	int sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (sockfd < 0) {
		perror("socket() failed");
		exit(-1);
	}
	
	// Set the server address
	struct sockaddr_in servAddr;
	memset(&servAddr, 0, sizeof(servAddr));
	servAddr.sin_family = AF_INET;
	int err = inet_pton(AF_INET, servIP, &servAddr.sin_addr.s_addr);
	if (err <= 0) {
		perror("inet_pton() failed");
		exit(-1);
	}
	servAddr.sin_port = htons(servPort);
	
	// Connect to server
	if (connect(sockfd, (struct sockaddr *) &servAddr, sizeof(servAddr)) < 0) {
		perror("connect() failed");
		exit(-1);
	}
	
	size_t echoStringLen = strlen(echoString);

	thread mythread=std::thread(prin,sockfd);
	while(flag==0)
	{	
		string str;
		getline(cin,str);
		if(str=="stop")
		{
			flag=1;
		}
		int i=0;
		for(;i<str.length();i++)
			echoString[i]=str[i];
		echoString[i]='\0';
		size_t echoStringLen = strlen(echoString);
		// Send string to server
		ssize_t sentLen = send(sockfd, echoString, echoStringLen, 0);
		if (sentLen < 0) {
			perror("send() failed");
			exit(-1);
		} else if (sentLen != echoStringLen) {
			perror("send(): sent unexpected number of bytes");
			exit(-1);
		}
	}
	
	mythread.join();
	close(sockfd);
	exit(0);
}