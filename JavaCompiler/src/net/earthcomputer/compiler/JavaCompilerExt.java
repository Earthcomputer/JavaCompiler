package net.earthcomputer.compiler;

import java.io.IOException;
import java.io.Reader;

import net.earthcomputer.compiler.internal.JavaTokenizer;
import net.earthcomputer.compiler.internal.UnicodeReplacerReader;

public class JavaCompilerExt {

	public static byte[] compile(Reader input) throws IOException {
		return compileWithoutUnicode(new UnicodeReplacerReader(input));
	}

	private static byte[] compileWithoutUnicode(Reader input) throws IOException {
		JavaTokenizer tokenizer = new JavaTokenizer(input);
		
		while (tokenizer.nextToken()) {
			System.out.println("<" + tokenizer.sval + ">");
		}
		
		return null;
	}

}
