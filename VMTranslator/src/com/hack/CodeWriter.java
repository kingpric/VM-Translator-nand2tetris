package com.hack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hack.Parser.statement;

public class CodeWriter {

	// Used in creating label for logical operation jump
	private int eqCounter = 0, ltCounter = 0, gtCounter = 0, retCounter = 0;

	/**
	 * This method convert vm code to asm code
	 * 
	 * @param operation
	 * @param stm
	 * @return Assembly codes
	 * @throws Exception
	 */
	public String[] encode(String operation, statement stm) throws Exception {
		switch (stm) {
		case pop:
			return pop(operation);
		case push:
			return push(operation);
		case add:
			return add();
		case sub:
			return sub();
		case neg:
			return neg();
		case eq:
			return eq();
		case lt:
			return lt();
		case gt:
			return gt();
		case or:
			return or();
		case and:
			return and();
		case not:
			return not();
		case label:
			return label(operation);
		case GOTO:
			return Goto(operation);
		case IF:
			return IfGoto(operation);
		case call:
			return call(operation);
		case Function:
			return Function(operation);
		case Return:
			return Return();

		default:
			return null;

		}
	}

	/**
	 * This method will generate asm code for <b><i>pop segment i</i></b>
	 * 
	 * @param operation
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] pop(String operation) throws Exception {
		String words[] = operation.strip().split(" ");
		segments seg = findSegment(words[1]);

		List<String> cmds = new ArrayList<String>();
		String offsetSegment = words[2];

		if (seg == segments.CONSTANT) {
			throw new Exception("pop command cannot contain CONSTANT");
		} else if (seg == segments.POINTER && (Integer.parseInt(words[2]) < 0 || Integer.parseInt(words[2]) > 1))
			throw new Exception("Pointer command has invalid offset");

		// R13 = Segment Base Address + offset
		if (seg != segments.STATIC) {
			cmds.add("@" + offsetSegment);
			cmds.add("D=A");
		}

		cmds.add("@" + seg.getSegment() + ((seg == segments.STATIC) ? "." + words[2] : ""));
		if (seg == segments.TEMP || seg == segments.POINTER)
			cmds.add("D=D+A");
		else if (seg == segments.STATIC) {
			cmds.add("D=A");
		} else
			cmds.add("D=D+M");

		cmds.add("@" + segments.R13.getSegment());
		cmds.add("M=D");

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// A=R13, M=D
		cmds.add("@" + segments.R13.getSegment());
		cmds.add("A=M");
		cmds.add("M=D");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will generate asm code for <b><i>push segment i</i></b>
	 * 
	 * @param operation
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] push(String operation) throws Exception {
		String words[] = operation.strip().split(" ");
		segments seg = findSegment(words[1]);

		List<String> cmds = new ArrayList<String>();
		String offsetSegment = words[2];

		if (seg == segments.POINTER && (Integer.parseInt(words[2]) < 0 || Integer.parseInt(words[2]) > 1))
			throw new Exception("Pointer command has invalid offset");

		// D=offset
		if (seg != segments.STATIC) {
			cmds.add("@" + offsetSegment);
			cmds.add("D=A");
		}

		// if segment is not constant D=segment base addr + offset
		if (seg != segments.CONSTANT) {
			cmds.add("@" + seg.getSegment() + ((seg == segments.STATIC) ? "." + words[2] : ""));

			if (seg == segments.TEMP || seg == segments.POINTER)
				cmds.add("A=D+A");
			else if (seg == segments.STATIC) {
				cmds.add("D=A");
			} else
				cmds.add("A=D+M");

			// cmds.add("A=D+M");
			cmds.add("D=M");
		}

		// *SP = D
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("A=M");
		cmds.add("M=D");

		// SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);

	}

	/**
	 * This method will created asm code for <b><i>add</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] add() {

		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// SP--, D=D+M
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=D+M");

		// *SP=D, SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("A=M");
		cmds.add("M=D");
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>sub</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] sub() {

		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// SP--, D=M-D, M=D
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("M=M-D");
		// cmds.add("M=D");

		// *SP=D, SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>neg</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] neg() {

		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("M=-M");

		// SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>eq</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] eq() {
		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// SP--,
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");

		// D=M-D, if D==0, set M=-1 (true) and Jump to end
		cmds.add("D=D-M");
		cmds.add("M=-1");
		cmds.add("@" + Constants.FileName + "_JEQ_" + (++eqCounter));
		cmds.add("D;JEQ");

		// if execute means D!=0 and set M=0 (false)
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("A=M");
		cmds.add("M=0");

		// *SP=D, SP++
		cmds.add("(" + Constants.FileName + "_JEQ_" + (eqCounter) + ")");
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>gt</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] gt() {
		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// SP--,
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");

		// D=M-D, if D>0, set M=-1 (true) and Jump to end
		cmds.add("D=M-D");
		cmds.add("M=-1");
		cmds.add("@" + Constants.FileName + "_JGT_" + (++gtCounter));
		cmds.add("D;JGT");

		// if execute means D!=0 and set M=0
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("A=M");
		cmds.add("M=0");

		// *SP=D, SP++
		cmds.add("(" + Constants.FileName + "_JGT_" + (gtCounter) + ")");
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>lt</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] lt() {
		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// SP--,
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");

		// D=M-D, if D<0, set M=-1 (true) and Jump to end
		cmds.add("D=M-D");
		cmds.add("M=-1");
		cmds.add("@" + Constants.FileName + "_JLT_" + (++ltCounter));
		cmds.add("D;JLT");

		// if execute means D!=0 and set M=0
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("A=M");
		cmds.add("M=0");

		// *SP=D, SP++
		cmds.add("(" + Constants.FileName + "_JLT_" + (ltCounter) + ")");
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>or</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] or() {
		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// SP--,
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");

		// M=M|D
		cmds.add("M=D|M");

		// SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>and</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] and() {
		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");

		// SP--,
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");

		// M=M|D
		cmds.add("M=D&M");

		// SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>not</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] not() {
		List<String> cmds = new ArrayList<String>();

		// SP--, D = *SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("M=!M");

		// SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>label</i></b> creation
	 * 
	 * @param operation
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] label(String operation) {
		String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		// creating label: (<LABEL>)
		cmds.add("(" + Constants.FileName.toUpperCase()
				+ (Constants.FunctionName != null ? "." + Constants.FunctionName : "") + "$" + words[1] + ")");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>goto</i></b> operation
	 * 
	 * @param operation
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] Goto(String operation) {
		String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		// jump to location <LABEL>
		cmds.add("@" + Constants.FileName.toUpperCase()
				+ (Constants.FunctionName != null ? "." + Constants.FunctionName : "") + "$" + words[1]);
		cmds.add("0;JMP");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>if-goto</i></b> operation
	 * 
	 * @param operation
	 * @return Assembly codes
	 * @throws Exception
	 */
	private String[] IfGoto(String operation) {

		String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		// if SP*!=0 then jump to location <LABEL>
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");
		cmds.add("@" + Constants.FileName.toUpperCase()
				+ (Constants.FunctionName != null ? "." + Constants.FunctionName : "") + "$" + words[1]);
		cmds.add("D;JNE");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will get/pop value from stack to <b><i>Register A</i></b>
	 * 
	 * @param value:
	 * @return
	 */
	private String[] popValue() {

		List<String> cmds = new ArrayList<String>();
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("A=M");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will push value to the stack from <b><i>Register A</i></b>
	 * 
	 * @param value: A value/label location to be pushed
	 * @return
	 */
	private String[] pushValue() {

		List<String> cmds = new ArrayList<String>();
		cmds.add("D=A");
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("A=M");
		cmds.add("M=D");
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

		return cmds.toArray(new String[0]);

	}

	/**
	 * This method will save state of caller function and setup environment for
	 * callee/called function <br />
	 * Input parameter syntax <b>call <i>functionName</i> nARGS</b>
	 * 
	 * @param operation
	 * @return Assembly codes
	 */
	private String[] call(String operation) {
		String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		String retAddress = Constants.FileName.toUpperCase()
				+ (Constants.FunctionName != null ? ".RET_" + Constants.FunctionName : "") + "$" + retCounter++;

		// Saving state of caller function
		// SP*=retAddress, SP++
		cmds.add("@" + retAddress);
		cmds.addAll(Arrays.asList(pushValue()));

		// SP*=LCL, SP++
		cmds.add("@" + segments.LOCAL.getSegment());
		cmds.add("A=M");
		cmds.addAll(Arrays.asList(pushValue()));

		// SP*=ARG, SP++
		cmds.add("@" + segments.ARGUMENT.getSegment());
		cmds.add("A=M");
		cmds.addAll(Arrays.asList(pushValue()));

		// SP*=THIS, SP++
		cmds.add("@" + segments.THIS.getSegment());
		cmds.add("A=M");
		cmds.addAll(Arrays.asList(pushValue()));

		// SP*=THAT, SP++
		cmds.add("@" + segments.THAT.getSegment());
		cmds.add("A=M");
		cmds.addAll(Arrays.asList(pushValue()));

		// Setting environment for callee/called functions
		// ARG = SP-5-nARGS
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("D=M");
		cmds.add("@5");
		cmds.add("D=D-A");
		cmds.add("@" + words[2]);
		cmds.add("D=D-A");
		cmds.add("@" + segments.ARGUMENT.getSegment());
		cmds.add("M=D");

		// LCL = SP
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("D=M");
		cmds.add("@" + segments.LOCAL.getSegment());
		cmds.add("M=D");

		// goto callee/called function
		cmds.add("@" + words[1]);
		cmds.add("0;JMP");
		cmds.add("(" + retAddress + ")");

		return cmds.toArray(new String[0]);

	}

	/**
	 * This method will generate asm code for function creation <br />
	 * Input parament syntax: <b>function <i>functionName</i> nVars</b>
	 * 
	 * @param operation
	 * @return: Assembly codes
	 */
	private String[] Function(String operation) {

		String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		// (<functionName>)
		cmds.add("(" + words[1] + ")");

		// push 0; nVars time to stack
		for (int i = 0; i < Integer.parseInt(words[2]); i++) {
			cmds.add("@0");
			cmds.addAll(Arrays.asList(pushValue()));
		}

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will generate asm code for returing from calle/called function,
	 * thus restoring state of called function.<br />
	 * Input parameter syntax: <b><i>return</i></b>
	 * 
	 * @return Assembly codes
	 */
	private String[] Return() {

		// String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		// endFrame(R13) = LCL
		cmds.add("@" + segments.LOCAL.getSegment());
		cmds.add("D=M");
		cmds.add("@R13");
		cmds.add("M=D");

		// retAddress(R14) = *(R13 - 5)
		cmds.add("@5");
		cmds.add("D=A");
		cmds.add("@R13");
		cmds.add("A=M-D");
		cmds.add("D=M");
		cmds.add("@R14");
		cmds.add("M=D");

		// Restoring state of caller function
		// *ARG = pop()
		cmds.addAll(Arrays.asList(popValue()));
		cmds.add("D=A");
		cmds.add("@" + segments.ARGUMENT.getSegment());
		cmds.add("A=M");
		cmds.add("M=D");
		cmds.add("@" + segments.ARGUMENT.getSegment());
		cmds.add("D=M+1");

		// SP = ARG + 1
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=D");

		// THAT = *(endFrame - 1)
		cmds.add("@R13");
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");
		cmds.add("@" + segments.THAT.getSegment());
		cmds.add("M=D");

		// THIS = *(endFrame - 1)
		cmds.add("@R13");
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");
		cmds.add("@" + segments.THIS.getSegment());
		cmds.add("M=D");

		// ARG = *(endFrame - 1)
		cmds.add("@R13");
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");
		cmds.add("@" + segments.ARGUMENT.getSegment());
		cmds.add("M=D");

		// LCL = *(endFrame - 1)
		cmds.add("@R13");
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");
		cmds.add("@" + segments.LOCAL.getSegment());
		cmds.add("M=D");

		cmds.add("@R14");
		cmds.add("A=M");
		cmds.add("0;JMP");

		return cmds.toArray(new String[0]);

	}

	/**
	 * This method will created asm code for <b><i>Bootstrap</i></b> program
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	public static String[] BootstrapProgram() {
		List<String> cmds = new ArrayList<String>();
		CodeWriter cw = new CodeWriter();
		cmds.add("@256");
		cmds.add("D=A");
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=D");
		// cmds.add("@Sys.init");
		// cmds.add("0;JMP");

		cmds.addAll(Arrays.asList(cw.call("call Sys.init 0")));

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method will created asm code for <b><i>End</i></b> operation
	 * 
	 * @return Assembly codes
	 * @throws Exception
	 */
	public String[] EndProgram() {
		List<String> cmds = new ArrayList<String>();

		cmds.add("(" + Constants.FileName + "_END)");
		cmds.add("@" + Constants.FileName + "_END");
		cmds.add("0;JMP");

		return cmds.toArray(new String[0]);
	}

	/**
	 * This method return segment enum
	 * 
	 * @param segment
	 * @return segments
	 * @throws Exception
	 */
	private segments findSegment(String segment) {
		for (segments seg : segments.values()) {
			if (segment.equalsIgnoreCase(seg.toString())) {
				return seg;
			}
		}
		return null;
	}

	/**
	 * ENUM of various segments
	 * 
	 * @author kingp
	 *
	 */
	private enum segments {
		LOCAL("LCL"), ARGUMENT("ARG"), THIS("THIS"), THAT("THAT"), TEMP("5"), R13("R13"), R14("R14"), R15("R15"),
		STATIC(Constants.FileName), STACK("SP"), CONSTANT("0"), POINTER("THIS");

		String segment;

		public String getSegment() {
			if (this == STATIC) {
				return Constants.FileName;
			}
			return this.segment;
		}

		private segments(String segment) {
			this.segment = segment;

		}
	}

}
