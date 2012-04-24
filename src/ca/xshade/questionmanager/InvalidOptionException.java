package ca.xshade.questionmanager;

public class InvalidOptionException extends Exception {
	private static final long serialVersionUID = -8231537623701825085L;
	
	public InvalidOptionException() {
		super("That is not a valid option.");
	}
}
