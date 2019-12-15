yacc -d p1.y
lex p1.l
g++ y.tab.c lex.yy.c
./a.out