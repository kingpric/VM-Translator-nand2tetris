package com.hack;

import java.util.ArrayList;
import java.util.List;

import com.hack.Parser.statement;

public class CodeWriter {

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

		// if segment is not constant
		// D=segment base addr + offset
		if (seg != segments.CONSTANT) {
			cmds.add("@" + seg.getSegment() + ((seg == segments.STATIC) ? "." + words[2] : ""));

			if (seg == segments.TEMP || seg == segments.POINTER)
				cmds.add("A=D+A");
			else
				cmds.add("A=D+M");

			//cmds.add("A=D+M");
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
		cmds.add("M=!M");

		// SP++
		cmds.add("@" + segments.STACK.getSegment());
		cmds.add("M=M+1");

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
