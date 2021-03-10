package com.hack;

import java.util.ArrayList;
import java.util.List;

import com.hack.Parser.statement;

public class CodeWriter {

	// Used in creating label for logical operation jump
	private int eqCounter = 0, ltCounter = 0, gtCounter = 0, endCounter = 0, labelCounter = 0;

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
		case Function:
			return function(operation);
		default:
			return null;

		}
	}

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
		cmds.add("@" + offsetSegment);
		cmds.add("D=A");

		cmds.add("@" + seg.getSegment() + ((seg == segments.STATIC) ? "." + words[2] : ""));
		if (seg == segments.TEMP || seg == segments.POINTER)
			cmds.add("D=D+A");
		else
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

	private String[] push(String operation) throws Exception {
		String words[] = operation.strip().split(" ");
		segments seg = findSegment(words[1]);

		List<String> cmds = new ArrayList<String>();
		String offsetSegment = words[2];

		if (seg == segments.POINTER && (Integer.parseInt(words[2]) < 0 || Integer.parseInt(words[2]) > 1))
			throw new Exception("Pointer command has invalid offset");

		// D=offset
		cmds.add("@" + offsetSegment);
		cmds.add("D=A");

		// if segment is not constant D=segment base addr + offset
		if (seg != segments.CONSTANT) {
			cmds.add("@" + seg.getSegment() + ((seg == segments.STATIC) ? "." + words[2] : ""));

			if (seg == segments.TEMP || seg == segments.POINTER)
				cmds.add("A=D+A");
			else
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

	private String[] label(String operation) {
		String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		//creating label: (<LABEL>)
		cmds.add("(" + Constants.FileName.toUpperCase()
				+ (Constants.FunctionName != null ? "." + Constants.FunctionName : "") + "$" + words[1] + ")");

		return cmds.toArray(new String[0]);
	}

	private String[] Goto(String operation) {
		String[] words = operation.split(" ");

		List<String> cmds = new ArrayList<String>();

		//jump to location <LABEL>
		cmds.add("@" + Constants.FileName.toUpperCase()
				+ (Constants.FunctionName != null ? "." + Constants.FunctionName : "") + "$" + words[1]);
		cmds.add("0;JMP");

		return cmds.toArray(new String[0]);
	}

	private String[] IfGoto(String operation) {

		String[] words = operation.split(" ");

		// if SP*!=0 then jump to location <LABEL>
		List<String> cmds = new ArrayList<String>();
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M-1");
		cmds.add("A=M");
		cmds.add("D=M");
		cmds.add("@" + Constants.FileName.toUpperCase()
				+ (Constants.FunctionName != null ? "." + Constants.FunctionName : "") + "$" + words[1]);
		cmds.add("D;JNE");

		return cmds.toArray(new String[0]);
	}

	private String[] function(String Operation) {
		
		return null;
		
	}
	
	public String[] EndProgram() {
		List<String> cmds = new ArrayList<String>();

		cmds.add("(" + Constants.FileName + "_END)");
		cmds.add("@" + Constants.FileName + "_END");
		cmds.add("0;JMP");

		return cmds.toArray(new String[0]);
	}

	private segments findSegment(String segment) {
		for (segments seg : segments.values()) {
			if (segment.equalsIgnoreCase(seg.toString())) {
				return seg;
			}
		}
		return null;
	}

	private enum segments {
		LOCAL("LCL"), ARGUMENT("ARG"), THIS("THIS"), THAT("THAT"), TEMP("5"), R13("R13"), R14("R14"), R15("R15"),
		STATIC(Constants.FileName), STACK("SP"), CONSTANT("0"), POINTER("THIS");

		String segment;

		public String getSegment() {
			return this.segment;
		}

		private segments(String segment) {
			this.segment = segment;

		}
	}

}
