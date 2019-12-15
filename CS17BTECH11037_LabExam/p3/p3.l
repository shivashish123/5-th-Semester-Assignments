%{
	#include"y.tab.h" 
	int yylex();
	void yyerror(const char*);
%}

%x Nstate Kstate
%%

[N][=][=]			   	{BEGIN(Nstate);return Nstart;}
<Nstate>[0-9]+         	{yylval.no=atoi(yytext);BEGIN(INITIAL);return N;}                                
[K][=][=] 				{BEGIN(Kstate);return Kstart;}
<Kstate>[0-9]+ 			{yylval.no=atoi(yytext);BEGIN(INITIAL);return K;}
[P][0-9]+				{yylval.txt=new string(yytext);return Pname;}
[=]						{return yytext[0];}
[PRS]+					{yylval.txt=new string(yytext);return Moves;}
[\n]					{return newline;}
[ ]						{return space;}

%%

int yywrap(void){
	return 1;
}

