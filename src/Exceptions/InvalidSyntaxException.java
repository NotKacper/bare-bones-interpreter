package Exceptions;

public class InvalidSyntaxException extends Exception {
	public InvalidSyntaxException(String errorMessage) {
		super(errorMessage);
	}
}