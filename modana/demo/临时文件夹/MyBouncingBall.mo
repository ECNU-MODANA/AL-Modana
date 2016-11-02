model MyBouncingBall "the bouncing ball model" 
  parameter Real g=9.82;  // gravitational acc. 
  Real h(start=4, fixed=true), in_v(start=0, fixed=true),out_v(start=0);
  Integer con_start(start=0);
  //Integer out_v(start=0);
equation 
  der(h) = in_v; 
  der(in_v) = -g; 
  der(out_v)= 0;
  when h < 0 then     
		con_start=1;
		reinit(out_v,-in_v*0.1); 
  end when;
end MyBouncingBall;
