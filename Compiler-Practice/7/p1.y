%code requires{
	#include<string>
	using namespace std;
}

%{
	#include<string>
	#include<bits/stdc++.h>
	using namespace std;
	void yyset_in(FILE*);
	void yyerror(const char*);
	int yylex();
	ofstream f;
%}

%union {string* name;}
%start st
%token <name> MON
%token newline

%%

st 	: MON		  {cout<<*$1;}
	| MON newline {cout<<*$1;}
   	;

%%

int main()
{
	FILE* f;
	f=fopen("test.txt","r");
	yyset_in(f);
	yyparse();
	fclose(f);
	return 0;
}

void yyerror(const char* s){fprintf(stderr,"%s",s);}

