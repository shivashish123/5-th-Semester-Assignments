%{
#include "y.tab.h"
void yyerror(const char* s);
int yylex();
%}

%%
[D]			{ return set;}
[\[]		{return yytext[0];}
[R]			{ return relation;}
[a][n][d]	{ return andi;}
[a-z]+		{ yylval.txt = new string(yytext); return small;}
[A-Z]+		{ yylval.txt = new string(yytext); return big;}
[0-9]+		{ yylval.txt = new string(yytext); return number;}
[\n]		{ return newline;}
[ ]			{}
. 			{return yytext[0];}
%%

int yywrap(void){return 1;}