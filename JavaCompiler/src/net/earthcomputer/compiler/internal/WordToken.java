package net.earthcomputer.compiler.internal;

public class WordToken extends AbstractToken {

	protected WordToken(int lineNumber, String sval) {
		super(lineNumber, TokenType.WORD, sval);
	}

}
