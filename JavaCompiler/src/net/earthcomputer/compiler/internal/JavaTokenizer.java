package net.earthcomputer.compiler.internal;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.compiler.CompilerException;

public class JavaTokenizer {

	private final Reader reader;
	private final char[] buffer = new char[32];
	private int lenToRead = 0;
	private int index = 0;

	public String sval;
	public int line = 1;

	static final List<Character> boringChars = new ArrayList<Character>();

	static {
		for (char c = ' ' + 1; c < 128; c++) {
			if ((c < '0' || c > '9') && (c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && c != '$' && c != '_' && c > ' '
					&& c != '(' && c != ')' && c != '{' && c != '}' && c != '[' && c != ']' && c != ';' && c != ','
					&& c != '.' && c != '\'' && c != '"' && c != '?')
				boringChars.add(c);
		}
	}

	public JavaTokenizer(Reader reader) {
		this.reader = reader;
	}

	public boolean nextToken() throws IOException {
		sval = null;
		int c = nextChar();
		switch (c) {
		case -1:
			return false;
		case '(':
		case ')':
		case '{':
		case '}':
		case '[':
		case ']':
		case ';':
		case ',':
		case '.':
			sval = String.valueOf((char) c);
			break;
		case '\'':
		case '"':
			sval = String.valueOf((char) c);
			boolean escaped = false;
			while (true) {
				int c1 = peek();
				if (c1 == -1 || c1 == '\r' || c1 == '\n')
					throw new CompilerException("Unclosed string or character literal");
				sval += (char) c1;
				nextChar();
				if (c1 == '\\' && !escaped)
					escaped = true;
				else if (c1 == c && !escaped)
					break;
				else
					escaped = false;
			}
			break;
		case '\r':
			if (peek() == '\n')
				nextChar();
		case '\n':
			line++;
			return nextToken();
		case '/':
			int c1 = peek();
			if (c1 == '/') {
				while (c1 != '\r' && c1 != '\n') {
					if (nextChar() == -1)
						return false;
					c1 = peek();
				}
				return nextToken();
			} else if (c1 == '*') {
				nextChar();
				while (true) {
					c1 = nextChar();
					if (c1 == -1)
						throw new CompilerException("Reached the end of the file before multiline comment is closed");
					else if (c1 == '*' && peek() == '/') {
						nextChar();
						return nextToken();
					}
				}
			}
		default:
			if (c <= ' ') {
				while (peek() <= ' ')
					if (nextChar() == -1)
						return false;
				return nextToken();
			} else if (Character.isJavaIdentifierPart((char) c)) {
				sval = String.valueOf((char) c);
				while (true) {
					c1 = peek();
					if (c1 == -1 || !Character.isJavaIdentifierPart((char) c1))
						return true;
					nextChar();
					sval += (char) c1;
				}
			} else {
				sval = String.valueOf((char) c);
				while (true) {
					c1 = peek();
					if (c1 == -1 || !boringChars.contains((char) c1))
						return true;
					nextChar();
					sval += (char) c1;
				}
			}
		}
		return true;
	}

	private int peek() throws IOException {
		int r = nextChar();
		index--;
		return r;
	}

	private int nextChar() throws IOException {
		if (index >= lenToRead - 1) {
			index = 0;
			lenToRead = reader.read(buffer, 0, buffer.length);
			if (lenToRead == -1) {
				lenToRead = 0;
				return -1;
			}
		} else {
			index++;
		}
		return buffer[index];
	}

}
