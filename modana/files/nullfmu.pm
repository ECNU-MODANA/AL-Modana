dtmc

module fmu

s : [0..2] init 0;
c : [0..1]init 0;
v : [0..2] init 0;

[] s=0 & c=1 -> 0.2 : (s'=1)  & (v'=1) + 0.8 : (s'=2)  & (v'=2);
[] s=1 -> 1 : (s'=0) ;
[] s=2 -> 1 : (s'=0) ;

endmodule
