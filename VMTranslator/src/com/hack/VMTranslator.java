package com.hack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hack.Parser.statement;

public class VMTranslator {

	// public static String filename;

	public static void main(String[] args) throws IOException {
		args = new String[] {
				"G:\\Automation Journey\\nand2tetris\\projects\\07\\MemoryAccess\\BasicTest\\BasicTest.vm" };

		if (args.length == 0) {
			System.out.println("Enter valid filenames");
			System.exit(0);
		}

		CodeWriter codeWriter = new CodeWriter();

		for (int i = 0; i < args.length; i++) {

			String filepath = args[i];
			File file = new File(filepath);
			if (file.exists() && file.isFile()) {
				Constants.FileName = file.getName().substring(0, file.getName().length() - 3);
				// List<String> lstASM = new ArrayList<String>();

				Parser parser = new Parser();

				File fsout = new File(filepath.substring(0, filepath.length() - 2) + "asm");
				if (fsout.exists())
					fsout.delete();
				BufferedWriter bfwrite = new BufferedWriter(new FileWriter(fsout));
				BufferedReader bfread = new BufferedReader(new FileReader(file));

				try {
					String line;
					int lineCnt = 0;
					while ((line = bfread.readLine()) != null) {
						String processedLine = parser.Parse(line);
						if (processedLine != null) {
							statement stm = parser.IdentifyStatement(processedLine);
							// System.out.println("processedLine: " + processedLine + "\n\rStatment type: "
							// + stm);
							System.out.println("// " + processedLine);
							// .add("//" + processedLine);
							bfwrite.append("// " + processedLine);
							bfwrite.newLine();
							String[] asmLines = codeWriter.encode(processedLine, stm);
							if (asmLines == null)
								continue;
							for (String strAsm : asmLines) {
								bfwrite.append(strAsm + "\t\t\t\t\t" + "//" + lineCnt++);
								bfwrite.newLine();
								System.out.println(strAsm);
								// lstASM.add(strAsm);
							}
							bfwrite.newLine();

						}
					}
				} catch (Exception ex) {
					System.out.println(ex);
				} finally {

					bfread.close();
					bfwrite.flush();
					bfwrite.close();
				}
			} else {
				System.out.println("Invalid file: " + filepath);
			}
		}

	}

}
