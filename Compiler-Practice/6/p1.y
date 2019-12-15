%code requires{
	#include<string>
	#include<iostream>
	using namespace std;
}

%{
	#include<bits/stdc++.h>
	#include<fstream>
	using namespace std;
	int yylex();
	void yyset_in(FILE*);
	void yyerror(const char*);
	ofstream f;	
	vector< vector<string> > v;
	vector<string> temp;
	map<string,int> m;
	void update(int);
	void upd1(string);
	void upd2(string);
	void upd3(string);
	int ind=1;
	int pos=0;
%}

%union	{string* txt;}
%token<txt> numb1 numb2 namev
%token GT LT EQ
%start st

%%
	st 		:	'{' '[' SInit ']' ':' Constra '}'
	SInit   :	Init 									{	update(ind);}
	Init 	:	InsName ',' Init	
			| 	InsName
			;
	InsName : 	namev									{m.insert(make_pair(*$1,ind));ind++;}
	Constra : 	Constraint ',' Constra
			|	Constraint
			;
	Constraint : Seq GT numb1			{upd1(*$3);}					
			| Seq GT numb2				{upd1(*$3);}
			| Seq LT numb1				{upd2(*$3);}
			| Seq LT numb2				{upd2(*$3);}
			| Seq EQ numb1				{upd3(*$3);}	
			| Seq EQ numb2				{upd3(*$3);}
			;
	Seq 	: numb1 namev Seq			{int u1=m[*$2]; v[pos][u1]=*$1;}
			| numb2 namev Seq			{int u1=m[*$2]; v[pos][u1]=*$1;}
			| '+' namev Seq				{int u1=m[*$2]; v[pos][u1]="1";}
			| namev	Seq					{int u1=m[*$1]; v[pos][u1]="1";}
			| '-' namev	Seq				{int u1=m[*$2]; v[pos][u1]="-1";}
			| numb1 namev				{int u1=m[*$2]; v[pos][u1]=*$1;}
			| numb2 namev				{int u1=m[*$2]; v[pos][u1]=*$1;}
			| '+' namev					{int u1=m[*$2]; v[pos][u1]="1";}
			| namev						{int u1=m[*$1]; v[pos][u1]="1";}
			| '-' namev					{int u1=m[*$2]; v[pos][u1]="-1";}
			;
%%

void update(int x)
{
	string y="0";
	for(int i=0;i<=x;i++)
		temp.push_back(y);
	v.push_back(temp);
}

void upd1(string x)
{
	int r1=x.length();
	if(x[0]=='-')
	{
		string temp=x.substr(1,r1-1);
		v[pos][ind]=temp;
	}
	else if(x[0]=='+')
	{
		x[0]='-';
		v[pos][ind]=x;
	}
	else if (x[0]!='0')
	{
		string temp="-"+x;
		v[pos][ind]=temp;
	}
	v[pos][0]="1";
	v.push_back(temp);
	pos++;
}

void upd2(string x)
{
	v[pos][ind]=x;
	for(int j=1;j<ind;j++)
	{
		string y=v[pos][j];
		int r1=y.length();
		if(y[0]=='-')
		{
			string temp=y.substr(1,r1-1);
			v[pos][j]=temp;
		}
		else if(y[0]=='+')
		{
			y[0]='-';
			v[pos][j]=y;
		}
		else if(y[0]!='0')
		{
			string temp="-"+y;
			v[pos][j]=temp;
		}
	}
	v[pos][0]="1";
	v.push_back(temp);
	pos++;
}

void upd3(string x)
{
	int r1=x.length();
	if(x[0]=='-')
	{
		string temp=x.substr(1,r1-1);
		v[pos][ind]=temp;
	}
	else if(x[0]=='+')
	{
		x[0]='-';
		v[pos][ind]=x;
	}
	else if (x[0]!='0')
	{
		string temp="-"+x;
		v[pos][ind]=temp;
	}
	v[pos][0]="0";
	v.push_back(temp);
	pos++;
}

int main()
{
	FILE* in;
	in=fopen("test.txt","r");
	yyset_in(in);
	yyparse();
	cout<<pos<<" "<<(ind-1)<<endl;
	for(int i=0;i<pos;i++)
	{
		for(int j=0;j<=ind;j++)
		{
			cout<<v[i][j]<<" ";
		}
		cout<<endl;
	}
	fclose(in);
	return 0;
}

void yyerror(const char*s){fprintf(stderr,"%s",s);}