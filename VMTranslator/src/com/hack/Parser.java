package com.hack;

public class Parser {

	public String Parse(String line) {
		line = line.replaceAll("\\s{2,}", " ").strip();
		if (line.startsWith("//") || line == "") {
			return null;
		}

		String processedLine = "";

		for (char ch : line.toCharArray()) {
			if (ch == '/') {
				break;
			}

			processedLine = processedLine + ch;
		}

		if (processedLine == "")
			return null;

		return processedLine;
	}

	public statement IdentifyStatement(String operation) {
		String[] words = operation.split(" ");
		for (statement stm : statement.values()) {
			if (stm.getCmdType().equalsIgnoreCase(words[0])) {
				return stm;
			}
		}
		return statement.whitespace;
	}

	enum statement {
		whitespace("Whitespace"),

		// Arithmetic
		add("add"), sub("sub"), neg("neg"),

		// Logical
		eq("eq"), gt("gt"), lt("lt"), and("and"), or("or"), not("not"),

		// Memory
		pop("pop"), push("push"),

		// Braching
		label("label"), GOTO("goto"), IF("if-goto"),

		// Function
		Function("function"), call("call"), Return("return"),;

		String cmdType;

		public String getCmdType() {
			return this.cmdType;
		}

		private statement(String cmdType) {
			this.cmdType = cmdType;
		}
	}

	enum cmdType {
		ArithmeticLogical, Logical, Memory, Braching, Function, Whitespace;
	}

	enum cmdArithmetic {
		add("+"), sub("-"), neg("!");

		String operation;

		cmdArithmetic(String operation) {
			this.operation = operation;
		}

		public String getOperation() {
			return operation;
		}
	}

//	enum Memory {
//		pop, push;
//
//	}

}
