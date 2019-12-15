#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <bits/stdc++.h>

#define BUFFER_LENGTH 1024

int main(int argc, char *argv[])
{
    int sockfd=-1, sdconn=-1;
    int rc, on=1, rcdsize=BUFFER_LENGTH;
    char buffer[BUFFER_LENGTH];
    struct sockaddr_in6 serveraddr, clientaddr;

    in_port_t servPort = atoi(argv[1]);// Local port
    socklen_t addrlen=sizeof(clientaddr);
    char str[INET6_ADDRSTRLEN];

    int flag=1;

    // Server loop breaks when it receives message "stop"
    while(flag)
    {
        // create socket for incoming connections
        if ((sockfd = socket(AF_INET6, SOCK_STREAM, 0)) < 0)
        {
            perror("socket() failed");
            break;
        }

        if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR,(char *)&on,sizeof(on)) < 0)
        {
            perror("setsockopt(SO_REUSEADDR) failed");
            break;
        }

        // Set local parameters
        memset(&serveraddr, 0, sizeof(serveraddr));
        serveraddr.sin6_family = AF_INET6;
        serveraddr.sin6_port   = htons(servPort);
        serveraddr.sin6_addr   = in6addr_any;

        // Bind to the local address
        if (bind(sockfd,(struct sockaddr *)&serveraddr,sizeof(serveraddr)) < 0)
        {
            perror("bind() failed");
            break;
        }

        // Listen to the client
        if (listen(sockfd, 10) < 0)
        {
            perror("listen() failed");
            break;
        }

        // Wait for a client to connect
        if ((sdconn = accept(sockfd, NULL, NULL)) < 0)
        {
            perror("accept() failed");
            break;
        }
        else
        {
            getpeername(sdconn, (struct sockaddr *)&clientaddr, &addrlen);
            if(inet_ntop(AF_INET6, &clientaddr.sin6_addr, str, sizeof(str))) 
            {
                printf("------\nHandling client %s %d\n", str,ntohs(clientaddr.sin6_port));
            }
        }

        if (setsockopt(sdconn, SOL_SOCKET, SO_RCVLOWAT,(char *)&rcdsize,sizeof(rcdsize)) < 0)
        {
            perror("setsockopt(SO_RCVLOWAT) failed");
            break;
        }

        // Receive data
        rc = recv(sdconn, buffer, sizeof(buffer), 0);
        if (rc < 0)
        {
            perror("recv() failed");
            break;
        }
        
        if(strcmp(buffer,"stop")==0)
            flag=0;
        printf("\nMessage received:\n");
        printf("%s\n\n",buffer);

        if (rc == 0 || rc < sizeof(buffer))
        {
            printf("The client closed the connection before all of the\n");
            printf("data was sent\n");
            break;
        }

        // Echo the data back to the client                                 
        rc = send(sdconn, buffer, sizeof(buffer), 0);
        if (rc < 0)
        {
            perror("send() failed");
            break;
        }
        if (sockfd != -1)
            close(sockfd);
        if (sdconn != -1)
            close(sdconn);
    }
    printf("End of Program\n");
    return 0;
}

