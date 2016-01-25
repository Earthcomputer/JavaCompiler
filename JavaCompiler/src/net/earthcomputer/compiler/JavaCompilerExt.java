package net.earthcomputer.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

import net.earthcomputer.compiler.internal.UnicodeReplacerReader;

public class JavaCompilerExt {

	public static byte[] compile(Reader input) throws IOException {
		return compileWithoutUnicode(new UnicodeReplacerReader(input));
	}

	private static byte[] compileWithoutUnicode(Reader input) throws IOException {
		StreamTokenizer javaTokenizer = new StreamTokenizer(input);
		javaTokenizer.resetSyntax();
		javaTokenizer.whitespaceChars(0, ' ');
		javaTokenizer.wordChars('0', '9');
		javaTokenizer.wordChars('A', 'Z');
		javaTokenizer.wordChars('a', 'z');
		javaTokenizer.wordChars('_', '_');
		javaTokenizer.wordChars('$', '$');
		javaTokenizer.quoteChar('\'');
		javaTokenizer.quoteChar('"');
		javaTokenizer.slashSlashComments(true);
		javaTokenizer.slashStarComments(true);

		int token;
		while ((token = javaTokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
			switch (token) {
			case StreamTokenizer.TT_EOL:
				System.out.println("New line");
				break;
			case StreamTokenizer.TT_NUMBER:
				System.out.println("Number: " + javaTokenizer.nval);
				break;
			case StreamTokenizer.TT_WORD:
				System.out.println("Word: " + javaTokenizer.sval);
				break;
			default:
				if (javaTokenizer.sval == null) {
					System.out.println("Token: " + (char) token);
				} else {
					System.out.println("Quoted: " + (char) token + javaTokenizer.sval + (char) token);
				}
			}
		}
		
		System.out.println("\730");
		
		return null;
	}

}
