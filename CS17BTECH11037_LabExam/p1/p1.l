%{
	#include"y.tab.h" 
	int yylex();
	void yyerror(const char*);
%}


%%

[i][p][t][a][b][l][e][s]     { return IPTABLE;}
[I][N][P][U][T]              { yylval.txt=new string(yytext);return TABLE;}                         
[O][U][T][P][U][T]           { yylval.txt=new string(yytext);return TABLE;} 
[F][O][R][W][A][R][D]        { yylval.txt=new string(yytext);return TABLE;} 
[-][D]						 { return COMD;}
[-][I]						 { return COMI;}
[-][R]						 { return COMR;}
[-][P]						 { return COMP;}
[-][p]						 { return OPP;}
[-][s]						 { return OPS;}
[-][d]						 { return OPD;}
[-][j]						 { return OPJ;}
[ ]							 { }
[a-zA-Z]+					 { yylval.txt=new string(yytext);return NAME; }
[0-9]+						 { yylval.no=atoi(yytext);return NUMB;}
[.0-9]+					 	 { yylval.txt=new string(yytext);return IPADDR;}
[\n]						 { return newline;}

%%

int yywrap(void){
	return 1;
}

