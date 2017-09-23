ctmc

module c

s : [0..1] init 0;
in_v : [-200000000..200000000]init 0;
out_v : [-200000000..200000000] init 0;
con_c : [0..1] init 0;

[] s=0 -> 1 : (s'=1)  & (con_c'=1);
[] s=1 -> (s'=0)  & (out_v'=in_v*8);

endmodule
