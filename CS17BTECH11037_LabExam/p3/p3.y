%code requires{
	#include<string>
	#include<bits/stdc++.h>
	using namespace std;
}


%define parse.error verbose
%{
	#include<bits/stdc++.h>
	using namespace std;
	extern int yylex();
	void yyerror(const char*s);
	vector<int> win_count;
	vector<int> play_seq;
	vector<string> player;
	vector<string> mov;
	map<string,int> id;
	int yylex();
	int n,k;
	int ind=0;
	void upd(int);

%}

%union {string* txt;int no;}

%start S

%token<no> N K 
%token newline space Nstart Kstart
%token<txt> Pname Moves
%type<txt> PlayerName

%%
    S   : line S
    	| line
        ;

    line : Nstart N newline				{ n=$2;upd(n);}
    	 | Kstart K newline     		{ k=$2;}
    	 | PlayerList
    	 | PlayerSeq  
    	 ; 			

    PlayerList : PlayerName '=' '=' Moves newline {int idx=id[*$1];mov[idx]=*$4;}
    		   ;

   	PlayerName : Pname 								{id.insert(make_pair(*$1,ind));player.push_back(*$1);ind++;$$=$1;}
   			   ;

    PlayerSeq : PlayerName2 space PlayerSeq
    		  | PlayerName2
    		  | PlayerName2 newline
    		  ;

    PlayerName2 : Pname 								{play_seq.push_back(id[*$1]);}
    			;


%%

void yyerror(const  char *s){
	fprintf(stderr,"%s",s);
}

int main(){
	yyparse();
	int p1=play_seq[0],p2=play_seq[1];
	play_seq.push_back(0);
	int j1=0,j2=0;
	for(int i=1;i<n;i++)
	{
		cout<<player[p1]<<" X "<<player[p2]<<endl;
		int fl=0;
		for(int j=0;j<k;j++)
		{
			if(mov[p1][j1]!=mov[p2][j2])
			{
				fl=1;break;
			}
			else
			{
				cout<<player[p1]<< "(" << mov[p1][j1] << ") X " << player[p2]<< "(" << mov[p2][j2] << ")-->T\n";
			}
			j1=(j1+1)%k;
			j2=(j2+1)%k;
		}
		if(fl==0)
		{
			j1=(j1+k-1)%k;
			j2=(j2+k-1)%k;
			if(win_count[p1]>win_count[p2])
			{
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p1]<<endl;
				win_count[p1]++;
				p2=play_seq[i+1];
				j1=(j1+1)%k;
				j2=0;
			}
			else if(win_count[p1]<win_count[p2])
			{
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p2]<<endl;win_count[p2]++;
				p1=p2;
				p2=play_seq[i+1];
				j1=(j2+1)%k;
				j2=0;
			}
			else
			{
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p1]<<endl;win_count[p1]++;
				p2=play_seq[i+1];
				j1=(j1+1)%k;
				j2=0;
			}
		}
		else
		{
			cout<<player[p1]<< "(" << mov[p1][j1] << ") X " << player[p2]<< "(" << mov[p2][j2] << ")-->";
			if(mov[p1][j1]=='R' && mov[p2][j2]=='S')
			{
				cout<<player[p1]<<endl;
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p1]<<endl;
				win_count[p1]++;
				p2=play_seq[i+1];
				j1=(j1+1)%k;
				j2=0;
			}
			else if(mov[p1][j1]=='R' && mov[p2][j2]=='P')
			{
				cout<<player[p2]<<endl;
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p2]<<endl;
				win_count[p2]++;
				p1=p2;
				p2=play_seq[i+1];
				j1=(j2+1)%k;
				j2=0;
			}
			else if(mov[p1][j1]=='S' && mov[p2][j2]=='P')
			{
				cout<<player[p1]<<endl;
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p1]<<endl;
				win_count[p1]++;
				p2=play_seq[i+1];
				j1=(j1+1)%k;
				j2=0;
			}
			else if(mov[p1][j1]=='S' && mov[p2][j2]=='R')
			{
				cout<<player[p2]<<endl;
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p2]<<endl;
				win_count[p2]++;
				p1=p2;
				p2=play_seq[i+1];
				j1=(j2+1)%k;
				j2=0;
			}
			else if(mov[p1][j1]=='P' && mov[p2][j2]=='R')
			{
				cout<<player[p1]<<endl;
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p1]<<endl;
				win_count[p1]++;
				p2=play_seq[i+1];
				j1=(j1+1)%k;
				j2=0;
			}
			else if(mov[p1][j1]=='P' && mov[p2][j2]=='S')
			{
				cout<<player[p2]<<endl;
				cout<<player[p1]<<" X "<<player[p2]<<"-->"<<player[p2]<<endl;
				win_count[p2]++;
				p1=p2;
				p2=play_seq[i+1];
				j1=(j2+1)%k;
				j2=0;
			}

		}
		cout<<endl;
	}
	cout<<player[p1]<<" is the winner of Rock, Paper, Scissors Competition held at Elan 2020, IITH.\n";
	return 0;
}
void upd(int x)
{
	for(int i=0;i<x;i++)
	{
		win_count.push_back(0);
		mov.push_back("");
	}
}
void upd2(int y,string z)
{
	mov[y]=z;
}
