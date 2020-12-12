package se.magnus.util.exceptions;

public class InvalidInputException extends RuntimeException {

	private static final long serialVersionUID = 2908542931643121924L;

	public InvalidInputException() {
		super();
	}

	public InvalidInputException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public InvalidInputException(String arg0) {
		super(arg0);
	}

	public InvalidInputException(Throwable arg0) {
		super(arg0);
	}
}
