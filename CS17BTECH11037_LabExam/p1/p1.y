%code requires{
	#include<string>
	#include<bits/stdc++.h>
	using namespace std;
}

%define parse.error verbose
%{
	#include<bits/stdc++.h>
	using namespace std;
	extern int yylex();
	void yyerror(const char*s);
	struct node{
		string target; string prot;string source;
		string destination;
	};
	string arr[4];
	vector<node> in,forw,out;
	void ip_pol(string,string);
	void ip_ins(string,int);
	void ip_del(string,int);
	void ip_rep(string,int);
	string inp="ACCEPT",forwp="ACCEPT",outp="ACCEPT";
%}

%union {string* txt;int no;}

%start S
%token<txt> TABLE COMD COMI COMR COMP OPP OPS OPD OPJ IPADDR NAME
%token<no> NUMB
%token IPTABLE space newline

%%
    S   :	line S  						
    	|   line							
        ;

    line	 	: 	IPT COMI TABLE NUMB OPTIONS newline		{ ip_ins(*$3,$4);}
    			| 	IPT COMI TABLE OPTIONS newline			{ ip_ins(*$3,1);}
		    	|  	IPT COMD TABLE NUMB newline				{ ip_del(*$3,$4);}
		    	|  	IPT COMP TABLE NAME	newline				{ ip_pol(*$3,*$4);}
		    	|  	IPT COMR TABLE NUMB OPTIONS	newline		{ ip_rep(*$3,$4);}
		    	|   IPT COMI TABLE NUMB OPTIONS 			{ ip_ins(*$3,$4);}
    			| 	IPT COMI TABLE OPTIONS 					{ ip_ins(*$3,1);}
		    	|  	IPT COMD TABLE NUMB 		 			{ ip_del(*$3,$4);}
		    	|  	IPT COMP TABLE NAME						{ ip_pol(*$3,*$4);}
		    	|  	IPT COMR TABLE NUMB OPTIONS				{ ip_rep(*$3,$4);}
		    	;

	IPT : IPTABLE {arr[0]="all";arr[1]="anywhere";arr[2]="anywhere";arr[3]="";}
		;

    OPTIONS : OPTION OPTIONS	
    		| OPTION
    		;
    OPTION : OPP NAME 						{arr[0]=*$2;}
    	   | OPS IPADDR						{arr[1]=*$2;}
    	   | OPD IPADDR						{arr[2]=*$2;}
    	   | OPJ NAME 						{arr[3]=*$2;}
    	   ;

%%

void yyerror(const  char *s){
	fprintf(stderr,"%s",s);
}

void ip_pol(string t, string nam)
{
	if(t=="INPUT")
	{
		inp=nam;
	}
	else if(t=="OUTPUT")
	{
		outp=nam;
	}
	else
	{
		forwp=nam;
	}
}
void ip_rep(string t, int pos)
{
	node temp;
	temp.prot = arr[0];
	temp.source = arr[1];
	temp.destination =	arr[2];
	temp.target = arr[3];
	pos--;
	if(t=="INPUT")
	{
		in[pos]=temp;
	}
	else if(t=="OUTPUT")
	{
		out[pos]=temp;
	}
	else
	{
		forw[pos]=temp;
	}
}
void ip_ins(string t, int pos)
{
	node temp;
	temp.prot = arr[0];
	temp.source = arr[1];
	temp.destination =	arr[2];
	temp.target = arr[3];
	pos--;
	if(t=="INPUT")
	{
		int l=in.size();
		if(pos==in.size())
		{
			in.push_back(temp);
		}
		else
		{
			in.push_back(in[l-1]);
			for(int i=l-1;i>pos;i--)
				in[i]=in[i-1];
			in[pos]=temp;
		}
	}
	else if(t=="OUTPUT")
	{
		int l=out.size();
		if(pos==out.size())
		{
			out.push_back(temp);
		}
		else
		{
			out.push_back(in[l-1]);
			for(int i=l-1;i>pos;i--)
				out[i]=out[i-1];
			out[pos]=temp;
		}
	}
	else
	{
		int l=forw.size();
		if(pos==forw.size())
		{
			forw.push_back(temp);
		}
		else
		{
			forw.push_back(in[l-1]);
			for(int i=l-1;i>pos;i--)
				forw[i]=forw[i-1];
			forw[pos]=temp;
		}
	}
}
void ip_del(string t, int pos)
{
	if(t=="INPUT")
	{
		int l=inp.size();
		if(pos==inp.size())
		{
			in.pop_back();
		}
		else
		{
			for(int i=pos;i<l-1;i++)
				inp[i]=inp[i+1];
			in.pop_back();
		}
	}
	else if(t=="OUTPUT")
	{
		int l=out.size();
		if(pos==out.size())
		{
			out.pop_back();
		}
		else
		{
			for(int i=pos;i<l-1;i++)
				out[i]=out[i+1];
			out.pop_back();
		}
	}
	else
	{
		int l=forw.size();
		if(pos==forw.size())
		{
			forw.pop_back();
		}
		else
		{
			for(int i=pos;i<l-1;i++)
				forw[i]=forw[i+1];
			forw.pop_back();
		}
	}
}
int main(){
	yyparse();
	cout<<"Chain INPUT (policy "<<inp<<")\n";
	cout<<"target\t\t\t\tprot\t\t\t\tsource\t\t\t\tdestination\n";
	for(int i=0;i<in.size();i++)
		cout<<in[i].target<<"\t\t\t\t"<<in[i].prot<<"\t\t\t\t"<<in[i].source<<"\t\t\t\t"<<in[i].destination<<"\n";
	cout<<"\n\n";
	cout<<"Chain FORWARD (policy "<<forwp<<")\n";
	cout<<"target\t\t\t\tprot\t\t\t\tsource\t\t\t\tdestination\n";
	for(int i=0;i<forw.size();i++)
		cout<<forw[i].target<<"\t\t\t\t"<<forw[i].prot<<"\t\t\t\t"<<forw[i].source<<"\t\t\t\t"<<forw[i].destination<<"\n";
	cout<<"\n\n";
	cout<<"Chain OUTPUT (policy "<<outp<<")\n";
	cout<<"target\t\t\t\tprot\t\t\t\tsource\t\t\t\tdestination\n";
	for(int i=0;i<out.size();i++)
		cout<<out[i].target<<"\t\t\t\t"<<out[i].prot<<"\t\t\t\t"<<out[i].source<<"\t\t\t\t"<<out[i].destination<<"\n";
	cout<<"\n\n";
	return 0;
}
