package net.earthcomputer.compiler.internal;

public abstract class AbstractToken {

	public final int lineNumber;
	public final TokenType tokenType;
	private final String sval;

	protected AbstractToken(int lineNumber, TokenType tokenType, String sval) {
		this.lineNumber = lineNumber;
		this.tokenType = tokenType;
		this.sval = sval;
	}

	@Override
	public String toString() {
		return sval;
	}

	public static AbstractToken forString(int lineNumber, String token) {
		char firstChar = token.charAt(0);
		if (firstChar == '\'') {
			return new CharacterToken(lineNumber, token);
		} else if (firstChar == '"') {
			return new StringToken(lineNumber, token);
		} else if (firstChar < 128 && !Character.isLetterOrDigit(firstChar)) {
			return new OperatorToken(lineNumber, token);
		} else {
			return new WordToken(lineNumber, token);
		}
	}

	public static enum TokenType {
		WORD, OPERATOR, CHARACTER, STRING
	}

}
