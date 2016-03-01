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
import java.util.HashMap;
import java.util.Map;

import net.earthcomputer.compiler.internal.JavaTokenizer;
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

			System.out.println("Compiling");
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
		Map<Path, byte[]> outputs = new HashMap<Path, byte[]>();
		for (Map.Entry<Path, Reader> entry : inputs.entrySet()) {
			outputs.putAll(compileWithoutUnicode(entry.getKey(), new UnicodeReplacerReader(entry.getValue())));
		}
		return outputs;
	}

	private static Map<Path, byte[]> compileWithoutUnicode(Path file, Reader input) throws IOException {
		JavaTokenizer tokenizer = new JavaTokenizer(input);

		while (tokenizer.nextToken()) {
			System.out.println("<" + tokenizer.sval + ">");
		}

		return new HashMap<Path, byte[]>();
	}

}
