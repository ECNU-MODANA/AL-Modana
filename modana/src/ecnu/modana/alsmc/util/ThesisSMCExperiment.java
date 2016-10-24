package ecnu.modana.alsmc.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import ecnu.modana.alsmc.main.State;

public class ThesisSMCExperiment {

	public static void main(String[] args) {
	    String[] strs = getValueType(UserFile.stateDoubleNum, UserFile.stateIntNum);
	    System.out.println(strs.length);
	    for (int i = 0; i < strs.length; i++) {
			System.out.print(strs[i]+"  ");
		}
		//getValueNames();
	}
	public static String[] getValueType(int initDoubleNum,int initIntNum) {
		String[] strs = new String[initDoubleNum+initIntNum];
		for (int i = 0; i < initDoubleNum; i++) {
			strs[i] = "Double";
		}
		for (int j = initDoubleNum; j < initDoubleNum+initIntNum; j++) {
			strs[j] = "Byte";
		}
		return strs;
	}
	
	public static void getValueNames() {
		String path = UserFile.fmuPath;
		FileInputStream f = null;
		try {
			f = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(f));
		try {
			String strQuery = br.readLine();
			String[] strs = strQuery.split("\\{");
			strQuery = strs[1];
			strs = strQuery.split("\\}");
			strQuery = strs[0];
			State.valueNames = strQuery.split(",");
			for (int i = 0; i < State.valueNames.length; i++) {
				System.out.print(State.valueNames[i]+"  ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
