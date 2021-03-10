package com.hack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.hack.Parser.statement;

public class VMTranslator {

	public static void main(String[] args) throws IOException {
		//args = new String[] { "G:\\Automation Journey\\nand2tetris\\projects\\08\\ProgramFlow\\FibonacciSeries" };
		// "G:\\Automation
		// Journey\\nand2tetris\\projects\\07\\StackArithmetic\\StackTest\\StackTest.vm"
		// };
//"G:\\Automation Journey\\nand2tetris\\projects\\07\\MemoryAccess\\PointerTest\\PointerTest.vm" };
//				"G:\\Automation Journey\\nand2tetris\\projects\\07\\MemoryAccess\\StaticTest\\StaticTest.vm"};

		if (args.length == 0) {
			System.out.println("Enter valid filenames");
			System.exit(0);
		}

		Parser parser = new Parser();

		for (int i = 0; i < args.length; i++) {

			String checkfilepath = args[i];
			File checkfile = new File(checkfilepath);
			boolean isCheckFile = checkfile.isFile();
			boolean isCheckDirectory = checkfile.isDirectory();

			List<String> filesList = new ArrayList<String>();

			if (isCheckDirectory) {

				Files.walk(Paths.get(checkfilepath)).filter(Files::isRegularFile)
						.filter(f -> f.toAbsolutePath().toString().endsWith(".vm"))
						.forEach(x -> filesList.add(x.toAbsolutePath().toString()));

			} else if (isCheckFile
					&& checkfile.getName().substring(checkfile.getName().length()).equalsIgnoreCase(".vm")) {

				filesList.add(args[i]);
			} else
				continue;

			File fsout = new File(checkfile.getPath() + "\\"
					+ (isCheckFile ? checkfile.getName().substring(0, checkfile.getName().length() - 3)
							: checkfile.getName())
					+ ".asm");
			if (fsout.exists())
				fsout.delete();
			BufferedWriter bfwrite = new BufferedWriter(new FileWriter(fsout));

			for (String filepath : filesList) {

				File file = new File(filepath);

				Constants.FileName = file.getName().substring(0, file.getName().length() - 3);
				CodeWriter codeWriter = new CodeWriter();

//				File fsout = new File(filepath.substring(0, filepath.length() - 2) + "asm");
//				if (fsout.exists())
//					fsout.delete();
//				BufferedWriter bfwrite = new BufferedWriter(new FileWriter(fsout));
				BufferedReader bfread = new BufferedReader(new FileReader(file));

				try {
					String line;
					int lineCnt = 0;
					while ((line = bfread.readLine()) != null) {
						String processedLine = parser.Parse(line);
						if (processedLine != null) {
							statement stm = parser.IdentifyStatement(processedLine);
							System.out.println("// " + processedLine);
							bfwrite.append("// " + processedLine);
							bfwrite.newLine();
							String[] asmLines = codeWriter.encode(processedLine, stm);
							if (asmLines == null)
								continue;
							for (String strAsm : asmLines) {
								bfwrite.append(
										strAsm + "\t\t\t\t\t" + "//" + (strAsm.startsWith("(") ? "" : lineCnt++));
								bfwrite.newLine();
								System.out.println(strAsm);
							}
							bfwrite.newLine();

						}
					}

					String[] asmLines = codeWriter.EndProgram();
					if (asmLines == null)
						continue;
					for (String strAsm : asmLines) {
						bfwrite.append(strAsm + "\t\t\t\t\t" + "//" + lineCnt++);
						bfwrite.newLine();
						System.out.println(strAsm);
					}

				} catch (Exception ex) {
					System.out.println(ex);
				} finally {

					bfread.close();
					// bfwrite.flush();
					// bfwrite.close();
				}
//			} else {
//				System.out.println("Invalid file: " + filepath);
			}

			bfwrite.flush();
			bfwrite.close();
		}

	}

//	private static String[] getVMFiles(String path) {
//		File files = new File(path);
//		List<String> lstVM = new ArrayList<String>();
//		if (files.isDirectory()) {
//			
//for(String path: files.list)
//				// return files.listFiles();
//			
//		}
//		return null;
//	}

}
