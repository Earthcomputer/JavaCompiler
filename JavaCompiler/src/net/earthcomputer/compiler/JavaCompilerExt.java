package net.earthcomputer.compiler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.earthcomputer.compiler.internal.AbstractToken;
import net.earthcomputer.compiler.internal.AbstractToken.TokenType;
import net.earthcomputer.compiler.internal.IntRef;
import net.earthcomputer.compiler.internal.JavaTokenizer;
import net.earthcomputer.compiler.internal.SemiCompiledClass;
import net.earthcomputer.compiler.internal.UnicodeReplacerReader;

public class JavaCompilerExt {

	public static void compile(File dirIn, File dirOut) throws IOException, CompilerException {
		if (!dirIn.isDirectory())
			throw new IllegalArgumentException("dirIn is not a directory");
		if (dirOut.exists() && !dirOut.isDirectory())
			throw new IllegalArgumentException("dirOut is not a directory");
		final Map<Path, Reader> readers = new HashMap<Path, Reader>();
		final Path pathIn = dirIn.toPath();
		final Path pathOut = dirOut.toPath();

		System.out.println("Searching for Java files");
		try {
			Files.walkFileTree(pathIn, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
					if (attr.isRegularFile() && file.toFile().getName().endsWith(".java")) {
						try {
							readers.put(pathIn.relativize(file), new BufferedReader(new FileReader(file.toFile())));
						} catch (FileNotFoundException e) {
							throw new RuntimeException("[WRAPPED]", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});

			final Map<Path, byte[]> outputs = compile(readers);

			System.out.println("Copying non-Java files");
			Files.walkFileTree(pathIn, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
					if (!attr.isDirectory() && (!attr.isRegularFile() || !file.toFile().getName().endsWith(".java"))) {
						try {
							File parentFile = file.toFile().getParentFile();
							if (parentFile != null && !parentFile.exists())
								parentFile.mkdirs();
							Files.copy(file, pathOut.resolve(pathIn.relativize(file)),
									StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							throw new RuntimeException("[WRAPPED]", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});

			System.out.println("Writing class files");
			for (Map.Entry<Path, byte[]> entry : outputs.entrySet()) {
				File parentFile = entry.getKey().toFile().getParentFile();
				if (parentFile != null && !parentFile.exists())
					parentFile.mkdirs();
				Files.copy(new ByteArrayInputStream(entry.getValue()), pathOut.resolve(entry.getKey()),
						StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (RuntimeException e) {
			if ("[WRAPPED]".equals(e.getMessage()) && e.getCause() instanceof IOException)
				throw (IOException) e.getCause();
			else
				throw e;
		}
	}

	public static Map<Path, byte[]> compile(Map<Path, Reader> inputs) throws IOException, CompilerException {
		Map<Path, SemiCompiledClass> semiCompiledClasses = new HashMap<Path, SemiCompiledClass>();
		System.out.println("Compiling stage 1 <parse to data structure>");
		for (Map.Entry<Path, Reader> entry : inputs.entrySet()) {
			System.out.println("-> " + entry.getKey());
			semiCompiledClasses
					.putAll(parseToDataStructure(entry.getKey(), new UnicodeReplacerReader(entry.getValue())));
		}

		System.out.println("Compiling stage 2 <associate data structures with one another>");
		for (Map.Entry<Path, SemiCompiledClass> entry : semiCompiledClasses.entrySet()) {
			System.out.println("-> " + entry.getKey());
			entry.getValue().link(semiCompiledClasses);
		}

		System.out.println("Compiling stage 3 <convert data structures into bytecode>");
		Map<Path, byte[]> outputs = new HashMap<Path, byte[]>();
		for (Map.Entry<Path, SemiCompiledClass> entry : semiCompiledClasses.entrySet()) {
			System.out.println("-> " + entry.getKey());
			outputs.put(entry.getKey(), entry.getValue().toBytecode());
		}
		return outputs;
	}

	private static Map<Path, SemiCompiledClass> parseToDataStructure(Path file, Reader input) throws IOException {

		JavaTokenizer tokenizer = new JavaTokenizer(input);

		List<AbstractToken> tokens = new ArrayList<AbstractToken>();

		while (tokenizer.nextToken()) {
			tokens.add(AbstractToken.forString(tokenizer.line, tokenizer.sval));
		}

		return parseTokensToDataStructure(file, tokens);
	}

	private static Map<Path, SemiCompiledClass> parseTokensToDataStructure(Path file, List<AbstractToken> tokens) {
		Map<Path, SemiCompiledClass> r = new HashMap<Path, SemiCompiledClass>();
		IntRef tokenIndex = new IntRef(0);

		String _package = getPackage(tokens, tokenIndex);
		checkPackageLocation(file, _package);

		return r;
	}

	private static String getPackage(List<AbstractToken> tokens, IntRef tokenIndex) {
		if (tokens.size() == 0 || !"package".equals(tokens.get(0).toString()))
			return "";

		tokenIndex.inc();

		StringBuilder packageBuilder = new StringBuilder();

		try {
			while (true) {
				AbstractToken token = tokens.get(tokenIndex.get());
				if (token.tokenType != TokenType.WORD) {
					throw new CompilerException("Invalid package declaration format", token.lineNumber);
				}
				packageBuilder.append(token.toString());
				tokenIndex.inc();

				token = tokens.get(tokenIndex.get());
				if (token.tokenType != TokenType.OPERATOR) {
					throw new CompilerException("Invalid package declaration format", token.lineNumber);
				}
				if (";".equals(token.toString())) {
					break;
				} else if (".".equals(token.toString())) {
					packageBuilder.append('/');
				} else {
					throw new CompilerException(
							"Invalid token in package declaration, " + token + ". Delete this token", token.lineNumber);
				}
				tokenIndex.inc();
			}
		} catch (IndexOutOfBoundsException e) {
			throw new CompilerException("End of file reached in the middle of the package declaration!");
		}

		return packageBuilder.toString();
	}

	private static void checkPackageLocation(Path file, String _package) {
		boolean goodPackage;
		if (_package.isEmpty()) {
			goodPackage = file.getParent() == null;
		} else {
			goodPackage = file.subpath(0, 1).resolveSibling(_package).equals(file.getParent());
		}
		if (!goodPackage) {
			throw new CompilerException("The package declaration does not match the path in the file system");
		}
	}

}
