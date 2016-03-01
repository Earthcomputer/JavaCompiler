package net.earthcomputer.compiler.internal;

public class StringToken extends AbstractToken {

	public StringToken(int lineNumber, String sval) {
		super(lineNumber, TokenType.STRING, sval);
	}

}
