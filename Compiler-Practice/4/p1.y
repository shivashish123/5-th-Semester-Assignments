%code requires{
	#include<string>
	#include<iostream>
	void yyerror(const char*);
	using namespace std;
}

%{
	#include<fstream>
	#include<iostream>
	using namespace std;
	void yyset_in(FILE*);
	ofstream f;
	int yylex();
%}

%union	{string* txt;int num;}
%start st
%token<num> number
%type<txt> st line
%token<txt> strings br newline dollarH dollarB dollarI 
%token<num> sleeptime dollarC

%left '+' '-'
%left '*' '/' '%'

%%
	st: line st
	  |	line
	;

	line : strings {string y=*$1;f<<"cout<<\""<<y<<"\""<<"<<endl;\n";}
		| sleeptime	{int y=$1; f<<"sleep("<<y<<");\n";}
		| '@' number { f<<"s.push("<<$2<<");\n";}
		| '@' { f<<"s.push(arr[poin]);\n";}
		| '<' {f<<"poin++;\n";}
		| '>' {f<<"poin--;\n";}
		| '&' number { f<<"arr[poin] = "<<$2<<";\n";}
		| '&' { f<<"arr[poin] = s.top();\ns.pop()\n";}
		| dollarH {f<<"cout<<hex<<arr[poin]<<endl;\n";}
		| dollarI {f<<"cout<<arr[poin]<<endl;\n";}
		| dollarB {f<<"temp1=arr[poin];\ncout<<temp<<endl;\n";}
		| dollarC {f<<"char x = (char)"<<$1<<";\ncout<<x<<endl;\n";}
		| '+' number {f<<"arr[poin]+="<<$2<<";\n";}
		| '+' 		{f<<"temp=s.top();s.pop();arr[poin]+=temp;\n";}
		| '-' number {f<<"arr[poin]-="<<$2<<";\n";}
		| '-' {f<<"temp=s.top();s.pop();arr[poin]-=temp;\n";}
		| '*' number {f<<"arr[poin]*="<<$2<<";\n";}
		| '*' {f<<"temp=s.top();s.pop();arr[poin]*=temp;\n";}
		| '/' number {f<<"arr[poin]/="<<$2<<";\n";}
		| '/' {f<<"temp=s.top();s.pop();arr[poin]/=temp;\n{\n";} 
		| '(' number {f<<"while( arr[poin]!="<<$2<<" )\n{\n";}
		| '(' 		 {f<<"while( arr[poin]!=0 )\n{\n";}
		| ')'		{f<<"}\n";}
		;

%%

int main()
{
	FILE* in;
	in=fopen("test.txt","r");
	f.open("out.cpp");
	yyset_in(in);
	f<<"#include<bits/stdc++.h>\n#include <unistd.h>\nusing namespace std;\n";
	f<<"stack<int> s;\nint arr[260];\nint poin=0;\nint temp;\nbitset<17> temp1;\n";
	f<<"int main(){\n";
	yyparse();
	f<<"return 0;\n}\n";
	f.close();
	fclose(in);
}

void yyerror(const char*s){fprintf(stderr,"%s\n",s);};