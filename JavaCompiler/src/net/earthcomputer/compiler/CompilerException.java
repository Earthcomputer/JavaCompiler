package net.earthcomputer.compiler;

public class CompilerException extends RuntimeException {

	private static final long serialVersionUID = 4618472203387056727L;

	private final int lineNumber;

	public CompilerException(String message) {
		this(message, 0);
	}

	public CompilerException(String message, int lineNumber) {
		super(message);
		this.lineNumber = lineNumber;
	}

	public boolean hasLineNumber() {
		return lineNumber > 0;
	}

	public int getLineNumber() {
		return lineNumber;
	}

}
