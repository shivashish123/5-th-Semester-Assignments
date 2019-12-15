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
	int maxX,maxY;
	vector<int> v[4];
%}

%union {string* txt;int no;}

%start S
%token<no> NUMB
%token<txt> NAME
%token LE LT UNION INTERSECTION Xvar Yvar

%%
    S   :	line S
    	|   line
        ;
    line : NUMB NUMB		{ maxX=$1;maxY=$2;}
    	| Expr
    	;
    Expr: INEQ UNION Expr
    	| INEQ INTERSECTION Expr
    	| INEQ
    	;
    INEQ : '{' NUMB LE Xvar LE NUMB	',' NUMB LE Yvar LE NUMB '}'	{v[0].push_back($2);v[1].push_back($6);v[1].push_back($8);v[1].push_back($12);}
    	 | '{' NUMB LT Xvar LE NUMB	',' NUMB LE Yvar LE NUMB '}'	{v[0].push_back($2+1);v[1].push_back($6);v[1].push_back($8);v[1].push_back($12);}
    	 | '{' NUMB LE Xvar LT NUMB	',' NUMB LE Yvar LE NUMB '}'	{v[0].push_back($2);v[1].push_back($6-1);v[1].push_back($8)v[1].push_back($12);}
    	 | '{' NUMB LT Xvar LT NUMB	',' NUMB LE Yvar LE NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LE NUMB	',' NUMB LE Yvar LT NUMB '}'	{v[0].push_back($2);}
    	 | '{' NUMB LT Xvar LE NUMB	',' NUMB LE Yvar LT NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LT NUMB	',' NUMB LE Yvar LT NUMB '}'	{v[0].push_back($2);}
    	 | '{' NUMB LT Xvar LT NUMB	',' NUMB LE Yvar LT NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LE NUMB	',' NUMB LT Yvar LT NUMB '}'	{v[0].push_back()}
    	 | '{' NUMB LT Xvar LE NUMB	',' NUMB LT Yvar LT NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LT NUMB	',' NUMB LT Yvar LT NUMB '}'	{v[0].push_back($2);}
    	 | '{' NUMB LT Xvar LT NUMB	',' NUMB LT Yvar LT NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LE NUMB	',' NUMB LT Yvar LE NUMB '}'	{v[0].push_back()}
    	 | '{' NUMB LT Xvar LE NUMB	',' NUMB LT Yvar LE NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LT NUMB	',' NUMB LT Yvar LE NUMB '}'	{v[0].push_back($2);}
    	 | '{' NUMB LT Xvar LT NUMB	',' NUMB LT Yvar LE NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LE NUMB	',' NUMB LT Yvar LT NUMB '}'	{v[0].push_back()}
    	 | '{' NUMB LT Xvar LE NUMB	',' NUMB LE Yvar LT NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar LT NUMB	',' NUMB LE Yvar LT NUMB '}'	{v[0].push_back($2);}
    	 | '{' NUMB LT Xvar LT NUMB	',' NUMB LE Yvar LT NUMB '}'	{v[0].push_back($2+1);}
    	 | '{' NUMB LE Xvar ',' Yvar LE NUMB '}'
    	 ;

%%

void yyerror(const  char *s){
	fprintf(stderr,"%s",s);
}

int main(){
	yyparse();
	return 0;
}
