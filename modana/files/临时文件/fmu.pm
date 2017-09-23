dtmc

module fmuD

s : [0..2] init 0;
con_start : [0..1]init 0;
in_v : [-200000000..200000000]init 0;
out_v : [-200000000..200000000] init 0;

[] s=2 -> 1 : (s'=0)  & (con_start'=0);
[] s=1 -> 1 : (s'=0)  & (con_start'=0);
[] s=0 & con_start=1 -> 0.5 : (s'=1)  & (out_v'=in_v*5) + 0.5 : (s'=2)  & (out_v'=in_v*8);

endmodule
