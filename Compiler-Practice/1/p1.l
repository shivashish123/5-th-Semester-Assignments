%{
	#include "y.tab.h"
	void yyerror(const char*s);
	int yylex();
%}
%%
[0-9]+  {yylval.num=atoi(yytext); return number;}
[-+^*/]	{return yytext[0];}
[\n]	{return newline;}
%%
int yywrap(void)	{return 1;}