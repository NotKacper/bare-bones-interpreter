import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxMatcher {
	Pattern commandExpression = Pattern.compile("(incr|decr|clear) [A-Za-z](\\d|[A-Za-z])*;");
	Pattern loopExpression = Pattern.compile("while [A-Za-z](\\d|[A-Za-z])* not 0 do;");
	Pattern loopEndExpression = Pattern.compile("end;");
	Matcher commandMatcher;
	Matcher loopMatcher;
	Matcher loopEndMatcher;

	public String matchToSyntax(String line) {
		commandMatcher = commandExpression.matcher(line);
		if (commandMatcher.find()) return "command";
		loopMatcher = loopExpression.matcher(line);
		if (loopMatcher.find()) return "loop";
		loopEndMatcher = loopEndExpression.matcher(line);
		if (loopEndMatcher.find()) return "end";
		return "error";
	}

}
