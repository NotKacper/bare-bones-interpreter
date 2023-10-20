package Interpreter_Classes;

import Exceptions.DecrementationException;

public class Variable {

	private int value = 0;

	public void update(String command) throws DecrementationException {
		switch (command) {
			case "incr" -> incr();
			case "decr" -> decr();
			case "clear" -> clear();
		}
	}

	public int getValue() {
		return value;
	}

	public void clear() {
		value = 0;
	}

	public void incr() {
		value++;
	}

	public void decr() throws DecrementationException {
		if (value > 0) value--;
		else throw new DecrementationException("Error: Cannot decrement a variable with value 0");
	}

}
