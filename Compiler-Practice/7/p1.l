%{
	#include "y.tab.h"
	int yylex();
%}

MONTH	JAN|FEB|MAR

%%

{MONTH}				{ yylval.name=new string(yytext);return MON;}
[\n]				{ return newline;}

%%

int yywrap(){return 1;}