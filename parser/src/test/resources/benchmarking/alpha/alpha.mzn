%-----------------------------------------------------------------------------%
% Alphabet puzzle, a well-known cryptoarithmetic puzzle
%
% Guido Tack, tack@gecode.org
% 2007-02-22
%
% Ported from the Gecode example
%
%-----------------------------------------------------------------------------%

include "globals.mzn";

var 1..26: a :: is_output;
var 1..26: b :: is_output;
var 1..26: c :: is_output;
var 1..26: d :: is_output;
var 1..26: e :: is_output;
var 1..26: f :: is_output;
var 1..26: g :: is_output;
var 1..26: h :: is_output;
var 1..26: i :: is_output;
var 1..26: j :: is_output;
var 1..26: k :: is_output;
var 1..26: l :: is_output;
var 1..26: m :: is_output;
var 1..26: n :: is_output;
var 1..26: o :: is_output;
var 1..26: p :: is_output;
var 1..26: q :: is_output;
var 1..26: r :: is_output;
var 1..26: s :: is_output;
var 1..26: t :: is_output;
var 1..26: u :: is_output;
var 1..26: v :: is_output;
var 1..26: w :: is_output;
var 1..26: x :: is_output;
var 1..26: y :: is_output;
var 1..26: z :: is_output;
array[1..26] of var 1..26 : alpha =
  [a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z];

constraint
  b+a+l+l+e+t       = 45  /\
  c+e+l+l+o         = 43  /\
  c+o+n+c+e+r+t     = 74  /\
  f+l+u+t+e         = 30  /\
  f+u+g+u+e         = 50  /\
  g+l+e+e           = 66  /\
  j+a+z+z           = 58  /\
  l+y+r+e           = 47  /\
  o+b+o+e           = 53  /\
  o+p+e+r+a         = 65  /\
  p+o+l+k+a         = 59  /\
  q+u+a+r+t+e+t     = 50  /\
  s+a+x+o+p+h+o+n+e = 134 /\
  s+c+a+l+e         = 51  /\
  s+o+l+o           = 37  /\
  s+o+n+g           = 61  /\
  s+o+p+r+a+n+o     = 82  /\
  t+h+e+m+e         = 72  /\
  v+i+o+l+i+n       = 100 /\
  w+a+l+t+z         = 34;

constraint alldifferent(alpha);
  
solve satisfy;

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
