package net.earthcomputer.compiler.internal;

public class OperatorToken extends AbstractToken {

	protected OperatorToken(int lineNumber, String sval) {
		super(lineNumber, TokenType.OPERATOR, sval);
	}

}
