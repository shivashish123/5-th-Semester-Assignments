%code requires{
	#include<string>
	#include <iostream>
	using namespace std;
	void yyerror(const char *c);
}

%{
#include <iostream>
#include <vector>
#include <stdio.h>
#include <fstream> 
using namespace std;
int yylex(); 
int fir=0;
void printSet();
void printRelation();
void yyset_in(FILE*);
vector<string> v,v1,v2;
ofstream f;
%}

%union { string *txt;}
%start statement
%token<txt> newline 
%token<txt> relation big small set andi
%token<txt> number
%type<txt> Init Init1 Bi Sm Exp

%%

statement: Relation statement
		| Relation
		| Set statement
		| Set;

Relation : relation '=' '[' Init  ']' '-' '>'  '{' Init1  '-' '>'  Init1 '|' Cond2 '}' newline {printRelation();}
		| relation '=' '[' Init  ']' '-' '>'  '{' Init1  '-' '>'  Init1 '|' Cond2 '}' {printRelation();}
	;

Set : set '=' '[' Init  ']' '-' '>' '{' Init1 '|' Cond '}' newline {printSet();}
	| set '=' '[' Init  ']' '-' '>' '{' Init1 '|' Cond '}' {printSet();}
	;

Init :	Bi ',' Init	
	| Bi
	;

Bi : big {v1.push_back(*$1);}

Init1 : Sm ',' Init1 
	| Sm
	;

Sm : small	{v.push_back(*$1);}
	;

Cond:	condition ',' Cond
	|	condition
	;

Cond2 : condition andi Cond2
	| condition
	;

condition : number '<' small '<' number {string s1=*$1+'<'+*$3;string s2=*$3+'<'+*$5; v2.push_back(s1);v2.push_back(s2);}
		|	number '<' small '<' big {string s1=*$1+'<'+*$3;string s2=*$3+'<'+*$5; v2.push_back(s1);v2.push_back(s2);}
		|	big '<' small '<' big {string s1=*$1+'<'+*$3;string s2=*$3+'<'+*$5; v2.push_back(s1);v2.push_back(s2);}
		|	big '<' small '<' number {string s1=*$1+'<'+*$3;string s2=*$3+'<'+*$5; v2.push_back(s1);v2.push_back(s2);}
		| 	small '=' Exp {string s1=*$1+'='+*$3; v2.push_back(s1);}
		;

Exp		: small '+' Exp		{string y=*$1+'+'+*$3;$$ = new string(y);}
		| small '-' Exp		{string y=*$1+'-'+*$3;$$ = new string(y);}
		| small '*' Exp		{string y=*$1+'*'+*$3;$$ = new string(y);}
		| small '/' Exp		{string y=*$1+'/'+*$3;$$ = new string(y);}
		| big '+' Exp		{string y=*$1+'+'+*$3;$$ = new string(y);}
		| big '-' Exp		{string y=*$1+'-'+*$3;$$ = new string(y);}
		| big '*' Exp		{string y=*$1+'*'+*$3;$$ = new string(y);}
		| big '/' Exp		{string y=*$1+'/'+*$3;$$ = new string(y);}
		| small				{string y=*$1;$$ = new string(y);}
		| big				{string y=*$1;$$ = new string(y);}
%%

void printSet()
{
	f<<"Set\nDims: ";
	for(int i=0;i<v.size();i++)
	{	
		f<<v[i]<<" ";
	}
	f<<"\nParams: ";
	for(int i=0;i<v1.size();i++)
	{	
		f<<v1[i]<<" ";
	}
	f<<"\nConstraints:\n";
	for(int i=0;i<v2.size();i++)
	{	
		f<<v2[i]<<"\n";
	}
	v.clear();v1.clear();v2.clear();
}

void printRelation()
{
	f<<"Relation\nDims: ";
	for(int i=0;i<v.size();i++)
	{	
		f<<v[i]<<" ";
	}
	f<<"\nParams: ";
	for(int i=0;i<v1.size();i++)
	{	
		f<<v1[i]<<" ";
	}
	f<<"\nConstraints:\n";
	for(int i=0;i<v2.size();i++)
	{	
		f<<v2[i]<<"\n";
	}
	v.clear();v1.clear();v2.clear();
}

int main()
{
	FILE* in;
	f.open("out.txt");
	in=fopen("test.txt","r");
	yyset_in(in);
	yyparse();	
	fclose(in);
}

void yyerror(const char*s ){fprintf(stderr,"%s\n",s);}
