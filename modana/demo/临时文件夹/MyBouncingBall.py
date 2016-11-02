#!C:/Python27
import pylab as P           # For plotting
from pyfmi import load_fmu  # For loading the FMU 
# Import the compiler function
from pymodelica import compile_fmu
# Import load_fmu from pyfmi
from pyfmi import load_fmu

# Specify Modelica model and model file (.mo or .mop)
# Compile the model and save the return argument, which is the file name of the FMU
my_fmu=compile_fmu("MyBouncingBall","D:/ProgramFiles/JModelica.org-1.16/MyDemo/MyBouncingBall/MyBouncingBall.mo",'auto','me','1.0',compile_to="D:/ProgramFiles/JModelica.org-1.16/MyDemo/MyBouncingBall/")

model = load_fmu(my_fmu)
#my_fmu.set('v',100)
#my_fmu.set('g',150)
save(my_fmu,"D:/ts.fmu")
#model.save("D:/ProgramFiles/JModelica.org-1.15/MyDemo/BouncingBall/bouncingBall1.fmu")
res = model.simulate(final_time=5.)
# Retrieve the result for the variables
h_res = res['h']
v_res = res['in_v']
t     = res['time'] 

# Plot the solution
# Plot the height
fig = P.figure()
P.clf()
P.subplot(2,1,1)
P.plot(t, h_res)
P.ylabel('Height (m)')
P.xlabel('Time (s)')
# Plot the velocity
P.subplot(2,1,2)
P.plot(t, v_res)
P.ylabel('Velocity (m/s)')
P.xlabel('Time (s)')
P.suptitle('FMI Bouncing Ball')
P.show()

