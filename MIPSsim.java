/**
 * I have neither given nor received any unauthorized aid on this assignment.
 * 
 * Author : Kinjal Jain
 * UFID : 1382-6564
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class MIPS {

	ArrayList<String> instructionsBuffer = new ArrayList<String>();
	ArrayList<String> registersBuffer = new ArrayList<String>();
	ArrayList<String> pRegistersBuffer = new ArrayList<String>();
	ArrayList<String> dataMemoryBuffer = new ArrayList<String>();
	HashMap<String, Integer> register = new HashMap<String, Integer>();
	HashMap<Integer, Integer> dataMemory = new HashMap<Integer, Integer>();
	ArrayList<String> INM = new ArrayList<String>();
	String INB = "";
	String AIB = "";
	String LIB = "";
	String ADB = "";
	ArrayList<String> REB = new ArrayList<String>();

	String pINB = "";
	String pAIB = "";
	String pLIB = "";
	String pADB = "";
	ArrayList<String> pREB = new ArrayList<String>();

	ArrayList<String> RGF = new ArrayList<String>();
	ArrayList<String> DAM = new ArrayList<String>();
	boolean execution_unit = true, first_cycle = true;
	int cycle = 0;
	int bufferValue = 0;
	FileWriter fstream, fstream1;
	BufferedWriter output, simoutput;

	public MIPS() throws IOException {
		fstream1 = new FileWriter("simulation.txt");
		simoutput = new BufferedWriter(fstream1);

		// Initializing Buffers
		// INM
		instructionsBuffer = fileReader("instructions.txt");
		// RGF
		registersBuffer = fileReader("registers.txt");
		pRegistersBuffer = registersBuffer;
		// DAM
		dataMemoryBuffer = fileReader("datamemory.txt");
		register = registerInitialize(registersBuffer);
		dataMemory = dmInitialize(dataMemoryBuffer);
		int i = 0;
		read();
		printer(0);
		while (i < 9) {
			decode();
			INB = pINB;
			pINB = "";
			AIB = pAIB;
			pAIB = "";
			LIB = pLIB;
			pLIB = "";
			ADB = pADB;
			pADB = "";
			pRegistersBuffer.clear();
			Iterator it = register.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				String k = "<" + pair.getKey() + "," + pair.getValue() + ">";
				pRegistersBuffer.add(k);
			}
			registersBuffer = (ArrayList<String>) pRegistersBuffer.clone();

			if (!pREB.isEmpty()) {
				REB.addAll(pREB);
				pREB.clear();
			}

			cycle += 1;

			if (INB != null && !INB.isEmpty()) {
				// issue selector
				if (INB.contains("<ADD") || INB.contains("<SUB")) {
					issue1();
				} else if (INB.contains("<LD")) {
					issue2();
				}
			}

			if (LIB != null && !LIB.isEmpty()) {
				addr();
			}

			if (AIB != null && !AIB.isEmpty()) {
				asu();
			}

			if (ADB != null && !ADB.isEmpty()) {
				load();
			}

			if (REB != null && !REB.isEmpty()) {
				write();
			}

			printer(cycle);

			if (!REB.isEmpty()) {
				REB.remove(0);
			}
			i++;
		}
		simoutput.close();

	}

	private void read() {
		if (instructionsBuffer.size() > 8 && INM.size() < 8) {
			while (INM.size() < 8) {
				INM.add(instructionsBuffer.get(bufferValue));
				bufferValue++;
			}
		} else {
			INM.addAll(instructionsBuffer);
		}
	}

	public void decode() {
		if (!INM.isEmpty()) {
			String inst = INM.get(0).substring(1, (INM.get(0)).length() - 1);
			String[] instSet = inst.split(",");
			String operation = instSet[0];
			String rf = instSet[1];
			String rs1 = instSet[2];
			String rs2 = instSet[3];
			String inb = "";
			if (operation.equalsIgnoreCase("add")
					|| operation.equalsIgnoreCase("sub")) {
				if (register.containsKey(rs1) && register.containsKey(rs2)) {
					inb = "<" + operation + "," + rf + "," + register.get(rs1)
							+ "," + register.get(rs2) + ">";
					pINB = inb;
				}
			} else if (operation.equalsIgnoreCase("ld")) {
				if (register.containsKey(rs1)) {
					inb = "<" + operation + "," + rf + "," + register.get(rs1)
							+ "," + rs2 + ">";
					pINB = inb;
				}
			}

			INM.remove(0);
		}

	}

	public void issue1() {
		pAIB = INB;
	}

	public void asu() {
		String inst = AIB.substring(1, AIB.length() - 1);
		String[] instSet = inst.split(",");
		String operation = instSet[0];
		String rf = instSet[1];
		String rs1 = instSet[2];
		String rs2 = instSet[3];
		String reb = "";
		int result = 0;
		if (operation.equalsIgnoreCase("add")) {
			result = Integer.parseInt(rs1) + Integer.parseInt(rs2);
		} else if (operation.equalsIgnoreCase("sub")) {
			result = Integer.parseInt(rs1) - Integer.parseInt(rs2);
		}
		reb = "<" + rf + "," + result + ">";
		pREB.add((pREB.isEmpty()) ? 0 : pREB.size() - 1, reb);

	}

	public void issue2() {
		pLIB = INB;
	}

	public void addr() {
		String inst = LIB.substring(1, LIB.length() - 1);
		String[] instSet = inst.split(",");
		String rf = instSet[1];
		String rs1 = instSet[2];
		int rs2 = Integer.parseInt(instSet[3]);
		int result = Integer.parseInt(rs1) + rs2;
		String adb = "<" + rf + "," + result + ">";
		pADB = adb;

	}

	public void load() {
		String inst = ADB.substring(1, ADB.length() - 1);
		String[] instSet = inst.split(",");
		String rf = instSet[0];
		int rs1 = Integer.parseInt(instSet[1]);
		String adb = "<" + rf + "," + dataMemory.get(rs1) + ">";
		pREB.add((pREB.isEmpty()) ? 0 : pREB.size() - 1, adb);

	}

	public void write() {
		String inst = REB.get(0).substring(1, REB.get(0).length() - 1);
		String[] instSet = inst.split(",");
		String rf = instSet[0];
		int rs1 = Integer.parseInt(instSet[1]);
		register.put(rf, rs1);
		pRegistersBuffer.add(REB.get(0));
	}

	private HashMap<String, Integer> registerInitialize(ArrayList<String> r) {
		HashMap<String, Integer> rVal = new HashMap<String, Integer>();
		for (int i = 0; i < r.size(); i++) {
			String[] rs = (r.get(i).substring(1, r.get(i).length() - 1))
					.split(",");
			String register = rs[0];
			Integer val = Integer.parseInt(rs[1]);
			rVal.put(register, val);
		}
		return rVal;
	}

	private HashMap<Integer, Integer> dmInitialize(ArrayList<String> d) {
		HashMap<Integer, Integer> dmVal = new HashMap<Integer, Integer>();
		for (int i = 0; i < d.size(); i++) {
			String[] rs = (d.get(i).substring(1, d.get(i).length() - 1))
					.split(",");
			Integer dataMemory = Integer.parseInt(rs[0]);
			Integer val = Integer.parseInt(rs[1]);
			dmVal.put(dataMemory, val);
		}
		return dmVal;
	}

	public ArrayList<String> fileReader(String fileName) {
		ArrayList<String> arr = new ArrayList<String>();
		BufferedReader br = null;
		try {
			String currentLine;
			br = new BufferedReader(new FileReader(fileName));
			while ((currentLine = br.readLine()) != null) {
				arr.add(currentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return arr;
	}

	private void printer(int cycle) throws IOException {
		Collections.sort(registersBuffer);
		simoutput.write("STEP " + cycle + ":\r\n");
		simoutput.write("INM:"
				+ INM.toString().replace("[", "").replace("]", "")
						.replace(", ", ",") + "\r\n");
		simoutput.write("INB:" + INB + "\r\n");
		simoutput.write("AIB:" + AIB + "\r\n");
		simoutput.write("LIB:" + LIB + "\r\n");
		simoutput.write("ADB:" + ADB + "\r\n");
		simoutput.write("REB:"
				+ REB.toString().replace("[", "").replace("]", "")
						.replace(", ", ",") + "\r\n");
		simoutput.write("RGF:"
				+ registersBuffer.toString().replace("[", "").replace("]", "")
						.replace(", ", ",") + "\r\n");
		simoutput.write("DAM:"
				+ dataMemoryBuffer.toString().replace("[", "").replace("]", "")
						.replace(", ", ",") + "\r\n");
		simoutput.write("\r\n");
	}

}

public class MIPSsim {
	public static void main(String[] args) throws IOException {
		MIPS msim = new MIPS();
	}
}
