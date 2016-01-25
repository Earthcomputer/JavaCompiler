package net.earthcomputer.compiler.internal;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.compiler.CompilerException;

public class UnicodeReplacerReader extends Reader {

	private Reader reader;

	private static final int STATE_NORMAL = 0;
	private static final int STATE_WAITFOR_U = 1;
	private static final int STATE_WAITFOR_1 = 2;
	private static final int STATE_WAITFOR_2 = 3;
	private static final int STATE_WAITFOR_3 = 4;
	private static final int STATE_WAITFOR_4 = 5;
	private int unicodeState = STATE_NORMAL;
	private int unicodeChar = 0;
	private List<Character> charsFromLast = new ArrayList<Character>();

	public UnicodeReplacerReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		ArrayList<char[]> toAppend = new ArrayList<char[]>();
		char[] cbuf1 = new char[len];
		char[] cbuf2;

		// Add charsFromLast to the list of appending arrays and clear it
		if (!charsFromLast.isEmpty()) {
			cbuf2 = new char[charsFromLast.size()];
			for (int i = 0; i < cbuf2.length; i++) {
				cbuf2[i] = charsFromLast.get(i);
			}
			toAppend.add(cbuf2);
			charsFromLast.clear();
		}

		// Read
		int amtRead = reader.read(cbuf1, 0, len);
		// If read nothing, and haven't got anything from last time, finished
		// reading
		if (amtRead == -1 && toAppend.isEmpty())
			return -1;
		
		// Loop through character read, checking for unicode
		int lastStart = 0;
		for (int i = 0; i < amtRead; i++) {
			char c = cbuf1[i];
			if (c == '\\' && unicodeState == STATE_NORMAL) {
				unicodeState = STATE_WAITFOR_U;
				if (lastStart != i) {
					cbuf2 = new char[i - lastStart];
					System.arraycopy(cbuf1, lastStart, cbuf2, 0, cbuf2.length);
					toAppend.add(cbuf2);
					lastStart = i;
				}
			} else if (c == 'u' && (unicodeState == STATE_WAITFOR_U || unicodeState == STATE_WAITFOR_1)) {
				unicodeState = STATE_WAITFOR_1;
			} else if (unicodeState >= STATE_WAITFOR_1) {
				c = Character.toLowerCase(c);
				if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')))
					throw new CompilerException(
							"Invalid hex digit " + c + ". Must use hex digits to encode unicode characters");
				unicodeState++;
				unicodeChar = (unicodeChar << 4) | "0123456789abcdef".indexOf(c);
				if (unicodeState > STATE_WAITFOR_4) {
					unicodeState = STATE_NORMAL;
					lastStart = i + 1;
					toAppend.add(new char[] { (char) unicodeChar });
					unicodeChar = 0;
				}
			} else {
				if (unicodeState == STATE_WAITFOR_U) {
					toAppend.add(new char[] { '\\' });
					unicodeState = STATE_NORMAL;
					lastStart = i;
				}
			}
		}
		// Flush
		if (lastStart != amtRead) {
			cbuf2 = new char[amtRead - lastStart];
			System.arraycopy(cbuf1, lastStart, cbuf2, 0, cbuf2.length);
			toAppend.add(cbuf2);
		}

		// Append
		amtRead = 0;
		for (int i = 0; i < toAppend.size(); i++) {
			cbuf2 = toAppend.get(i);
			int destPos = off + amtRead;
			if (destPos < len) {
				int amtToRead = Math.min(cbuf2.length, len - amtRead);
				System.arraycopy(cbuf2, 0, cbuf, destPos, amtToRead);
				amtRead += amtToRead;
				if (amtToRead != cbuf2.length) {
					for (int j = amtToRead; j < cbuf2.length; j++) {
						charsFromLast.add(cbuf2[j]);
					}
				}
			}
		}
		return amtRead;
	}

}
