package com.hack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.hack.Parser.statement;

public class VMTranslator {

	public static void main(String[] args) throws IOException {
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

			boolean doBootstrap = false;
			if (filesList.stream().filter(s -> s.toLowerCase().contains("sys.vm")).count() > 0) {
				doBootstrap = true;
			}

			int lineCnt = 0;

			if (doBootstrap) {
				Constants.FileName = "BootstrapProgram";
				String[] asmLines = CodeWriter.BootstrapProgram();
				if (asmLines == null)
					continue;
				for (String strAsm : asmLines) {
					bfwrite.append(strAsm + "\t\t\t\t\t" + "//" + (strAsm.startsWith("(") ? "" : lineCnt++));
					bfwrite.newLine();
					System.out.println(strAsm);
				}
				bfwrite.newLine();

			}

			for (String filepath : filesList) {

				File file = new File(filepath);

				Constants.FileName = file.getName().substring(0, file.getName().length() - 3);
				CodeWriter codeWriter = new CodeWriter();

				BufferedReader bfread = new BufferedReader(new FileReader(file));

				try {
					String line;
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
						bfwrite.append(strAsm + "\t\t\t\t\t" + "//" + (strAsm.startsWith("(") ? "" : lineCnt++));
						bfwrite.newLine();
						System.out.println(strAsm);
					}

				} catch (Exception ex) {
					System.out.println(ex);
				} finally {

					bfread.close();
				}
			}

			bfwrite.flush();
			bfwrite.close();
		}

	}
}
