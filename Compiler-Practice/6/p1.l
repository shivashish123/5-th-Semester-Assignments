%{
	#include "y.tab.h"
	int yylex();
	string u;
%}

%x name

WHITE	[ \n\t\r]
ALPHA	[a-zA-Z0-9]

%%

[-+]?[0-9]+[/][0-9]+		{yylval.txt=new string(yytext);return numb1;}
[-+]?[0-9]+					{yylval.txt=new string(yytext);return numb2;}
[a-z]						{ u=yytext[0];BEGIN(name);}
<name>{ALPHA}				{ u=u+yytext[0];}
<name>.						{ yylval.txt=new string(u);BEGIN(INITIAL);unput(yytext[0]);return namev;}
[>][=]						{ return GT;}
[<][=]						{ return LT;}
[=]							{ return EQ;}

{WHITE}				{}
.					{ return yytext[0];}
%%

int yywrap(){return 1;}