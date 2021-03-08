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
			if (stm.toString().equalsIgnoreCase(words[0])) {
				return stm;
			}
		}
		return statement.whitespace;
	}

	enum statement {
		whitespace("Whitespace"), pop("Memory"), push("Memory"), add("Arithmetic"), sub("Arithmetic"),
		neg("Arithmetic"), eq("Logical"), gt("Logical"), lt("Logical"), and("Logical"), or("Logical"), not("Logical");

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
