%{
	#include"y.tab.h" 
	int yylex();
	void yyerror(const char*);
%}

%%

[0-9]+		{ yylval.no=atoi(yytext);return NUMB;}
[<][=]		{ return LE;}
[<]			{ return LT;}
[u]			{ return UNION;}
[n]			{ return INTERSECTION;}
[x]			{ return Xvar;}
[y]			{ return Yvar;}
[ ]			{}
.			{ return yytext[0];}                                       
%%

int yywrap(void){
	return 1;
}

