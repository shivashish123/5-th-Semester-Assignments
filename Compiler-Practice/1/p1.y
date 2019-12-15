%{
#include<stdio.h>
#include<stdlib.h>
#include<math.h>
void yyerror(const char* s);
int yylex();	
int calc(int,int,char);
%}

%union {int num;char opt;}
%start statement
%token<num> number
%token<opt> operator
%type<num> expression
%token newline

%right  '^'
%left  '+' '-'
%left  '*' '/'

%%

statement : line statement
		  |	line;

line : expression newline	{printf("%d\n",$1);};

expression : expression '+' expression { $$ = calc($1,$3,'+');}
			|expression '-' expression { $$ = calc($1,$3,'-');}
			|expression '*' expression { $$ = calc($1,$3,'*');}
			|expression '/' expression { $$ = calc($1,$3,'/');}
			|expression '^' expression { $$ = calc($1,$3,'^');}
		    | number {$$ = $1;};
		   
%%
int calc(int x1,int x2,char o)
{
	int u1; 
	switch(o)
	{
		case '+' : u1=x1+x2;
					break;
		case '-' : u1=x1-x2;
					break;
		case '*' : u1=x1*x2;
					break;
		case '/' : u1=x1/x2;
					break;
		case '^' : u1=pow(x1,x2);
					break;
	}
	return u1;
}
int main()
{
	yyparse();
}
void yyerror(const char *s){fprintf(stderr,"%s\n",s);}
