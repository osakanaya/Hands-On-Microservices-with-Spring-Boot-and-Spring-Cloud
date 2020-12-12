package se.magnus.util.exceptions;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 5295631130073866665L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}
	
}
