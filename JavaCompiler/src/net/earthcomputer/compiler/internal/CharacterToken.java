package net.earthcomputer.compiler.internal;

public class CharacterToken extends AbstractToken {

	protected CharacterToken(int lineNumber, String sval) {
		super(lineNumber, TokenType.CHARACTER, sval);
	}

}
