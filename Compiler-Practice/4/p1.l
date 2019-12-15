%{
	#include "y.tab.h"
	void yyerror(const char*);
	int yylex();
	string s;
%}

Brackets "["|"]"
INco	 "\""

%x str slst dolc
%%


[0-9]+			{yylval.num=atoi(yytext); return number;}
{Brackets}		{return br;}
{INco}			{BEGIN(str);s="";}
<str>{INco}		{yylval.txt=new string(s);BEGIN(INITIAL);return strings;}
<str>.			{s=s+yytext[0];}
"#"				{BEGIN(slst);}
<slst>[0-9]+	{yylval.num=atoi(yytext);BEGIN(INITIAL);return sleeptime;}
"$i"			{return dollarI;}
"$b"			{return dollarB;}
"$h"			{return dollarH;}
"$c"			{BEGIN(dolc);}
<dolc>[0-9]+	{yylval.num=atoi(yytext);BEGIN(INITIAL);return dollarC;}
[ \n\t\r]		{}
.				{return yytext[0];}

%%

int yywrap(){return 1;}