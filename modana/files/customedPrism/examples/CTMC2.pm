ctmc

module c

s : [0..1] init 0;

[] s=0 -> 1 : (s'=1);
[] s=1 -> (s'=0) ;
[] s=0 -> 2 : (s'=2);
[] s=2 -> (s'=0) ;

endmodule
