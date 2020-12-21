package se.magnus.util.exceptions;

public class EventProcessingException extends RuntimeException {

	private static final long serialVersionUID = -6788126178138442052L;

	public EventProcessingException() {
		super();
	}

	public EventProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventProcessingException(String message) {
		super(message);
	}

	public EventProcessingException(Throwable cause) {
		super(cause);
	}
}
