import java.io.IOException;

public class InterpreterInterface {
	public static void main(String[] args) throws IOException {
		InterpreterInterface bareBonesInterface = new InterpreterInterface();
		bareBonesInterface.start();
	}

	// find ways to extract the IOHandler methods from interpreter to encapsulate each component into the interface
	// also add debugging as an option again.
	public void start() throws IOException {
		Interpreter interpreter = new Interpreter();
		boolean running = true;
		while (running) {
			interpreter.setDebugging(IOHandler.getUserInput("Would you like to debug this file? y/n").equalsIgnoreCase("y"));
			interpreter.run();
			// allows user to interpret multiple files in one run of the program.
			running = !IOHandler.getUserInput("Do you want to interpret another file? y/n").equalsIgnoreCase("n");
		}
	}
}
