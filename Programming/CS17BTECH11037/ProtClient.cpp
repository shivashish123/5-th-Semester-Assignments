
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <bits/stdc++.h>

#define BUFFER_LENGTH 1024

int main(int argc, char *argv[])
{
   
   	// Server IP address as first argument
   	// Server port number as second argument
   	// Message string as third argument

	int sockfd, rc, bytesReceived=0;
	char buffer[BUFFER_LENGTH];
	char server[BUFFER_LENGTH];
	char servport[BUFFER_LENGTH];
	struct in6_addr serveraddr;
	struct addrinfo hints, *res=NULL;

	strcpy(server, argv[1]);
	strcpy(servport, argv[2]);
    strcpy(buffer, argv[3]);

	memset(&hints, 0, sizeof(hints));
	hints.ai_flags    = AI_NUMERICSERV;
	hints.ai_family   = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;

	int err = inet_pton(AF_INET, server, &serveraddr);
	if (err == 1)    // IPv4 text address
	{
		hints.ai_family = AF_INET;
		hints.ai_flags |= AI_NUMERICHOST;
	}
	else
	{
		err = inet_pton(AF_INET6, server, &serveraddr);
		if (rc == 1) // IPv6 text address
		{
			hints.ai_family = AF_INET6;
			hints.ai_flags |= AI_NUMERICHOST;
		}
	}

	rc = getaddrinfo(server, servport, &hints, &res);
	if (rc != 0)
	{
		printf("Host not found --> %s\n", gai_strerror(rc));
		if (rc == EAI_SYSTEM)
			perror("getaddrinfo() failed");
	}

	//Creat a socket
	sockfd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
	if (sockfd < 0)
	{
	 	perror("socket() failed");
	}

	// Connect to server
	rc = connect(sockfd, res->ai_addr, res->ai_addrlen);
	if (rc < 0)
	{
		perror("connect() failed");
	}

	// Send string to server
	rc = send(sockfd, buffer, sizeof(buffer), 0);
	if (rc < 0)
	{
		perror("send() failed");
	}

	// Receive string from server

	while (bytesReceived < BUFFER_LENGTH)
	{
		rc = recv(sockfd, & buffer[bytesReceived],BUFFER_LENGTH - bytesReceived, 0);
		if (rc < 0)
		{
			perror("recv() failed");
		}
		else if (rc == 0)
		{
			printf("The server closed the connection\n");
		}
		bytesReceived += rc;
	}
	
	close(sockfd);
	return 0;
}