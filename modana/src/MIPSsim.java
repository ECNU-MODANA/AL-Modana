/* On my honor, I have neither given nor received unauthorized aid on this assignment */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class MIPSsim {
	HashMap<String, String> category1 = new HashMap<String, String>();
	HashMap<String, String> category2 = new HashMap<String, String>();
	HashMap<String, String> category3 = new HashMap<String, String>();
	List<HashMap<String, Integer>> ins = new ArrayList<HashMap<String,Integer>>();
	List<String> insList = new ArrayList<String>();
	List<String> insNameList = new ArrayList<String>();
	static final int basePC = 128; 
	static int pc = 128;
	HashMap<Integer, Integer> memories = new HashMap<Integer, Integer>();
	int[] gpr = new int[32];
	public MIPSsim() {
		init();
	}
	public void parse(){
		BufferedReader br = null;
		BufferedWriter bw = null;
		String newLine = "\r";
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("linux"))
			newLine = "\n";
		try{
		    Scanner sc=new Scanner(System.in);
		    System.out.println("input the file address£º");
		    String m=sc.next();
			br = new BufferedReader(new FileReader(m));
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("disassembly.txt"));
			bw = new BufferedWriter(writer);
			String line = null;
			boolean end = false;
			String writeLine = "";
			while((line = br.readLine()) != null){
				writeLine +=line +"\t"+ pc +"\t";
				String substring = line.substring(0, 3);
					if(substring.equals("000")){
						String insName = category1.get(line.substring(3, 6));
						if(insName.equals("J")&&!end){
							writeLine += insName + " #"+Integer.parseInt(line.substring(6, 32)+"00", 2)+newLine;
							HashMap<String, Integer> desc = new HashMap<String, Integer>();
							desc.put("address", Integer.parseInt(line.substring(6, 32)+"00", 2));
							desc.put("pc", pc);
							ins.add(desc);
							insList.add(insName + " #"+Integer.parseInt(line.substring(6, 32)+"00", 2));
							insNameList.add(insName);
						}
						else if(insName.equals("BEQ")&&!end){
							writeLine += insName +" R"+Integer.parseInt(line.substring(6,11),2)+
									", R"+Integer.parseInt(line.substring(11,16),2)
									+", #"+Integer.parseInt(line.substring(16, 32), 2)*4+newLine;
							HashMap<String, Integer> desc = new HashMap<String, Integer>();
							desc.put("rs", Integer.parseInt(line.substring(6, 11), 2));
							desc.put("rt", Integer.parseInt(line.substring(11, 16), 2));
							desc.put("offset", Integer.parseInt(line.substring(16, 32), 2)*4);
							desc.put("pc", pc);
							ins.add(desc);
							insList.add(insName +" R"+Integer.parseInt(line.substring(6,11),2)+
									", R"+Integer.parseInt(line.substring(11,16),2)
									+", #"+Integer.parseInt(line.substring(16, 32), 2)*4);
							insNameList.add(insName);
						}
						else if(insName.equals("BGTZ")&&!end){
							writeLine += insName +" R"+Integer.parseInt(line.substring(6,11),2)+
									", #"+Integer.parseInt(line.substring(16, 32), 2)*4+newLine;
							HashMap<String, Integer> desc = new HashMap<String, Integer>();
							desc.put("rs", Integer.parseInt(line.substring(6, 11), 2));
							desc.put("rt", Integer.parseInt(line.substring(11, 16), 2));
							desc.put("offset", Integer.parseInt(line.substring(16, 32), 2)*4);
							desc.put("pc", pc);
							ins.add(desc);
							insList.add(insName +" R"+Integer.parseInt(line.substring(6,11),2)+
									", #"+Integer.parseInt(line.substring(16, 32), 2)*4);
							insNameList.add(insName);
						}
						else if(insName.equals("BREAK")&&!end){
							writeLine += insName+newLine;
							end = true;
							HashMap<String, Integer> desc = new HashMap<String, Integer>();
							desc.put("pc", pc);
							ins.add(desc);
							insList.add(insName);
							insNameList.add(insName);
						}else if((insName.equals("SW")||insName.equals("LW"))&&!end){
							writeLine += insName +" R"+Integer.parseInt(line.substring(11,16),2)+
									", "+Integer.parseInt(line.substring(16, 32), 2)+"(R"
									+Integer.parseInt(line.substring(6,11),2)+")"+newLine;
							HashMap<String, Integer> desc = new HashMap<String, Integer>();
							desc.put("base", Integer.parseInt(line.substring(6, 11), 2));
							desc.put("rt", Integer.parseInt(line.substring(11, 16), 2));
							desc.put("offset", Integer.parseInt(line.substring(16, 32), 2));
							desc.put("pc", pc);
							ins.add(desc);
							insList.add(insName +" R"+Integer.parseInt(line.substring(11,16),2)+
									", "+Integer.parseInt(line.substring(16, 32), 2)+"(R"
									+Integer.parseInt(line.substring(6,11),2)+")");
							insNameList.add(insName);
						}else {
							writeLine += Integer.parseInt(line, 2)+newLine;
							memories.put(pc, Integer.parseInt(line, 2));
						}
					}else if(substring.equals("110")&&!end){
						String insName = category2.get(line.substring(13,16));
						writeLine += insName+" R"+Integer.parseInt(line.substring(16,21),2)+
								", R"+Integer.parseInt(line.substring(3,8),2)+
								", R"+Integer.parseInt(line.substring(8,13),2)+newLine;
						HashMap<String, Integer> desc = new HashMap<String, Integer>();
						desc.put("rs", Integer.parseInt(line.substring(3, 8), 2));
						desc.put("rt", Integer.parseInt(line.substring(8, 13), 2));
						desc.put("rd", Integer.parseInt(line.substring(16, 21), 2));
						desc.put("pc", pc);
						ins.add(desc);
						insList.add(insName+" R"+Integer.parseInt(line.substring(16,21),2)+
								", R"+Integer.parseInt(line.substring(3,8),2)+
								", R"+Integer.parseInt(line.substring(8,13),2));
						insNameList.add(insName);
					}else if(substring.equals("111")){
						String insName = category3.get(line.substring(13, 16));
						if(insName==null&&end){
							writeLine += -1*(Integer.parseInt(Encode(line.substring(0,32)), 2)+1)+newLine;
							memories.put(pc, -1*(Integer.parseInt(Encode(line.substring(0,32)), 2)+1));
						}else{
							writeLine += insName +" R"+Integer.parseInt(line.substring(8,13),2)+
								", R"+Integer.parseInt(line.substring(3,8),2)
								+", #"+Integer.parseInt(line.substring(16, 32), 2)+newLine;
							HashMap<String, Integer> desc = new HashMap<String, Integer>();
							desc.put("rs", Integer.parseInt(line.substring(3, 8), 2));
							desc.put("rt", Integer.parseInt(line.substring(8, 13), 2));
							desc.put("imme", Integer.parseInt(line.substring(16, 32), 2));
							desc.put("pc", pc);
							ins.add(desc);
							insList.add(insName +" R"+Integer.parseInt(line.substring(8,13),2)+
									", R"+Integer.parseInt(line.substring(3,8),2)
									+", #"+Integer.parseInt(line.substring(16, 32), 2));
							insNameList.add(insName);
					}
				}
						
				pc += 4;
				}
			bw.write(writeLine.substring(0, writeLine.length()-1));
			bw.flush();
			bw.close();
			String simTrace = "";
			//simulation
			pc = basePC;
			int cycle = 1;
			while(!insNameList.get((pc-basePC)/4).contains("BREAK")){
				simTrace += "--------------------\r"+"Cycle:"+ cycle++ +"\t"+pc+"\t"+insList.get((pc-basePC)/4)+"\r\r"+"Registers"+"\r";
				Logic();
				for(int i=0;i<4;i++){
					if(i*8<10)
						simTrace += "R0"+i*8 +":\t";
					else
						simTrace += "R"+i*8 +":\t";
					for(int j=0;j<8;j++)
						if(j==7)
							simTrace += gpr[i*8+j]+"\r";
						else 
							simTrace += gpr[i*8+j] + "\t";
				}
				simTrace += "\r" + "Data" + "\r";
				int memIndex = basePC + 4*ins.size();
				for(int i=0;i<2;i++){
					simTrace += memIndex +i*4*8 +":\t";
					memIndex += i*4*8;
					for(int j=0;j<8;j++){
						if(j!=7)
							simTrace += memories.get(memIndex + j*4)+ "\t";
						else
							simTrace += memories.get(memIndex + j*4);
					}
					simTrace += "\r";
				}
				simTrace += "\r";
			}
			simTrace += "--------------------\r"+"Cycle:"+ cycle++ +"\t"+pc+"\t"+insList.get((pc-basePC)/4)+"\r\r"+"Registers"+"\r";
			for(int i=0;i<4;i++){
				if(i*8<10)
					simTrace += "R0"+i*8 +":\t";
				else
					simTrace += "R"+i*8 +":\t";
				for(int j=0;j<8;j++)
					if(j==7)
						simTrace += gpr[i*8+j]+"\r";
					else 
						simTrace += gpr[i*8+j] + "\t";
			}
			simTrace += "\r" + "Data" + "\r";
			int memIndex = basePC + 4*ins.size();
			for(int i=0;i<2;i++){
				simTrace += memIndex +i*4*8 +":\t";
				memIndex += i*4*8;
				for(int j=0;j<8;j++){
					if(j!=7)
						simTrace += memories.get(memIndex + j*4)+ "\t";
					else
						simTrace += memories.get(memIndex + j*4);
				}
				simTrace += "\r";
			}
			writer = new OutputStreamWriter(new FileOutputStream("simulation.txt"));
			bw = new BufferedWriter(writer);
			bw.write(simTrace);
			bw.flush();
			bw.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		MIPSsim ms = new MIPSsim();
		ms.parse();

	}
	private void init(){
		category1.put("000", "J");
		category1.put("010", "BEQ");
		category1.put("100", "BGTZ");
		category1.put("101", "BREAK");
		category1.put("110", "SW");
		category1.put("111", "LW");
		
		category2.put("000", "ADD");
		category2.put("001", "SUB");
		category2.put("010", "MUL");
		category2.put("011", "AND");
		category2.put("100", "OR");
		category2.put("101", "XOR");
		category2.put("110", "NOR");
		
		category3.put("000", "ADDI");
		category3.put("001", "ANDI");
		category3.put("010", "ORI");
		category3.put("011", "XORI");
	}
	public String Encode(String str){
		char[] charList = str.toCharArray();
		String fuck = "";
		for(int i=0;i<charList.length;i++)
			fuck += Integer.parseInt(String.valueOf(charList[i]))==0? '1' : '0';
		return fuck;
	}
	public void Logic(){
		HashMap<String, Integer> current = ins.get((pc-basePC)/4);
		String insNameString = insNameList.get((pc-basePC)/4);
		if(insNameString.equals("J"))
			pc = current.get("address");
		else if(insNameString.equals("BEQ")){
			int rs = current.get("rs");
			int rt = current.get("rt");
			if(gpr[rs] == gpr[rt])
				pc += current.get("offset")+4;
			else 
				pc +=4;
		}else if(insNameString.equals("BGTZ")){
			int rs = current.get("rs");
			if(gpr[rs] > 0)
				pc += current.get("offset")+4;
			else 
				pc +=4;
		}else if(insNameString.equals("BREAK")){
			
		}else if(insNameString.equals("SW")){
			int rt = current.get("rt");
			int base = current.get("base");
			memories.put(gpr[base]+current.get("offset"),gpr[rt]);
			pc +=4;
		}else if(insNameString.equals("LW")){
			int rt = current.get("rt");
			int base = current.get("base");
			gpr[rt] = memories.get(gpr[base]+current.get("offset"));
			pc +=4;
		}else if(insNameString.equals("ADD")){
			gpr[current.get("rd")] = gpr[current.get("rs")] + gpr[current.get("rt")];
			pc +=4;
		}else if(insNameString.equals("SUB")){
			gpr[current.get("rd")] = gpr[current.get("rs")] - gpr[current.get("rt")];
			pc +=4;
		}else if(insNameString.equals("MUL")){
			gpr[current.get("rd")] = gpr[current.get("rs")] * gpr[current.get("rt")];
			pc +=4;
		}else if(insNameString.equals("OR")){
			gpr[current.get("rd")] = gpr[current.get("rs")] == 1 ? 1 : gpr[current.get("rt")]== 1 ? 1 : 0;
			pc +=4;
		}else if(insNameString.equals("XOR")){
			gpr[current.get("rd")] = gpr[current.get("rs")] == gpr[current.get("rt")] ? 0 : 1;
			pc +=4;
		}else if(insNameString.equals("NOR")){
			gpr[current.get("rd")] = gpr[current.get("rs")] == 1 ? 0 : gpr[current.get("rt")] == 1 ? 0 : 1;
			pc +=4;
		}else if(insNameString.equals("ADDI")){
			gpr[current.get("rt")] = gpr[current.get("rs")] + current.get("imme");
			pc +=4;
		}else if(insNameString.equals("ANDI")){
			gpr[current.get("rt")] = gpr[current.get("rs")] == current.get("imme") ? 1 : 0;
			pc +=4;
		}else if(insNameString.equals("ORI")){
			gpr[current.get("rt")] = gpr[current.get("rs")] == 1 ? 1 : current.get("imme") == 1 ? 1 : 0;
			pc +=4;
		}else if(insNameString.equals("XORI")){
			gpr[current.get("rt")] = gpr[current.get("rs")] == current.get("imme") ? 0 : 1;
			pc +=4;
		}
		
		
	}
	
}
