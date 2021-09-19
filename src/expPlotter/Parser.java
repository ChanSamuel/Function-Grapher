package expPlotter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Parser {
	public static String recognizedVar;
	
	/* ***************
	 * PATTERNS
	 * ***************
	 */
	
	public static Pattern PLUS = Pattern.compile("\\+");
	public static Pattern MINUS = Pattern.compile("-");
	public static Pattern TIMES = Pattern.compile("\\*");
	public static Pattern DIVIDE = Pattern.compile("/");
	public static Pattern EXPON = Pattern.compile("\\^");
	public static Pattern OPEN_BRACKET = Pattern.compile("\\(");
	public static Pattern CLOSE_BRACKET = Pattern.compile("\\)");
	public static Pattern DECIMAL = Pattern.compile("\\.");
	public static Pattern INTEGER = Pattern.compile("[0-9]");
	public static Pattern VARIABLE = Pattern.compile("[a-zA-Z]");
	
	/* ************************
	 * USEFUL CHECKING METHODS:
	 * ************************
	 */
	
	public static boolean checkEat(String check, Scanner s) {
		if (s.hasNext(check)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkEat(Pattern check, Scanner s) {
		if (s.hasNext(check)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}
	
	public static String checkSpit(String check, Scanner s) {
		if (s.hasNext(check)) {
			return s.next();
		} else {
			return null;
		}
	}
	
	public static String checkSpit(Pattern check, Scanner s) {
		if (s.hasNext(check)) {
			return s.next();
		} else {
			return null;
		}
	}
	
	public static void requireEat(String check, String failPoint, Scanner s) {
		if (s.hasNext(check)) {
			s.next();
		} else {
			throw new ParseFailedException("Required " + check + " got "
		+ (s.hasNext() ? s.next(): "") + " at " + failPoint, null);
		}
	}
	
	public static void requireEat(Pattern check, String failPoint, Scanner s) {
		if (s.hasNext(check)) {
			s.next();
		} else {
			throw new ParseFailedException("Required " + check.pattern() + " got "
		+ (s.hasNext() ? s.next(): "") + " at " + failPoint, null);
		}
	}
	
	public static String requireSpit(String check, String failPoint, Scanner s) {
		if (s.hasNext(check)) {
			return s.next();
		} else {
			throw new ParseFailedException("Required " + check + " got "
		+ (s.hasNext() ? s.next(): "") + " at " + failPoint, null);
		}
	}
	
	public static String requireSpit(Pattern check, String failPoint, Scanner s) {
		if (s.hasNext(check)) {
			return s.next();
		} else {
			throw new ParseFailedException("Required " + check.pattern() + " got "
		+ (s.hasNext() ? s.next(): "") + " at " + failPoint, null);
		}
	}
	
	
}


/* ***************
 * PARSING CLASSES
 * ***************
 */

class ParseFailedException extends RuntimeException {
	
	public ParseFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public ParseFailedException(String msg) {
		super(msg);
	}
	
	public ParseFailedException(Throwable cause) {
		super(cause);
	}
}

interface ParseNode {
	double evaluate(double x);
	ParseNode parse(Scanner s);
}

class ExpNode implements ParseNode {
	
	ArrayList<ParseNode> children = new ArrayList<ParseNode>();
	// This Map tells us whether we should add or minus the next term.
	Map<ParseNode, Boolean> addMap = new HashMap<ParseNode, Boolean>(); 
	
	@Override
	public double evaluate(double x) {
		double sum = children.get(0).evaluate(x);
		
		for (int i = 1; i < children.size(); i++) {
			ParseNode child = children.get(i);
			if (addMap.get(child)) { // If true, we add the terms.
				sum += child.evaluate(x);
			} else { // Otherwise, we subtract.
				sum -= child.evaluate(x);
			}
		}
		
		return sum;
	}

	@Override
	public ParseNode parse(Scanner s) {
		children.add(new TermNode().parse(s));
		
		while (true) {
			if (Parser.checkEat(Parser.PLUS, s)) {
				ParseNode nextTerm = new TermNode().parse(s);
				children.add(nextTerm);
				
				addMap.put(nextTerm, true);
			} else if (Parser.checkEat(Parser.MINUS, s)) {
				ParseNode nextTerm = new TermNode().parse(s);
				children.add(nextTerm);
				
				addMap.put(nextTerm, false);
			} else {
				break;
			}
		}
		
		return this;
	}
	
}

class TermNode implements ParseNode {
	
	ArrayList<ParseNode> children = new ArrayList<ParseNode>();
	// This Map tells us whether we should mutiply or divide the next term.
	Map<ParseNode, Boolean> multMap = new HashMap<ParseNode, Boolean>(); 
	
	@Override
	public double evaluate(double x) {
		double result = children.get(0).evaluate(x);
		
		for (int i = 1; i < children.size(); i++) {
			ParseNode child = children.get(i);
			if (multMap.get(child)) { // If true, we multiply the factors.
				result *= child.evaluate(x);
			} else { // Otherwise, we divide.
				result /= child.evaluate(x);
			}
		}
		
		return result;
	}

	@Override
	public ParseNode parse(Scanner s) {
		children.add(new FactorNode().parse(s));
		
		while (true) {
			if (Parser.checkEat(Parser.TIMES, s) || 
					s.hasNext(Parser.OPEN_BRACKET) || s.hasNext(Parser.VARIABLE)) {
				ParseNode nextTerm = new FactorNode().parse(s);
				children.add(nextTerm);
				
				multMap.put(nextTerm, true);
			} else if (Parser.checkEat(Parser.DIVIDE, s)) {
				ParseNode nextTerm = new FactorNode().parse(s);
				children.add(nextTerm);
				
				multMap.put(nextTerm, false);
			} else {
				break;
			}
		}
		
		return this;
	}
	
}

class FactorNode implements ParseNode {
	
	ParseNode child;
	boolean negative = false;
	
	@Override
	public double evaluate(double x) {
		if (negative) {
			return -child.evaluate(x);
		} else {
			return child.evaluate(x);
		}
	}

	@Override
	public ParseNode parse(Scanner s) {
		
		while (Parser.checkEat(Parser.MINUS, s)) {
			negative = !negative;
		}
		
		child = new BaseNode().parse(s);
		
		return this;
	}
	
}

class BaseNode implements ParseNode {
	
	ParseNode base;
	ParseNode exponent;
	
	@Override
	public double evaluate(double x) {
		if (exponent != null) {
			return Math.pow(base.evaluate(x), exponent.evaluate(x));
		} else {
			return base.evaluate(x);
		}
	}

	@Override
	public ParseNode parse(Scanner s) {
		base = new PrimaryNode().parse(s);
		
		if (Parser.checkEat(Parser.EXPON, s)) {
			exponent = new FactorNode().parse(s);
		}
		
		return this;
	}
	
}

class PrimaryNode implements ParseNode {
	
	ParseNode child;
	
	@Override
	public double evaluate(double x) {
		return child.evaluate(x);
	}

	@Override
	public ParseNode parse(Scanner s) {
		
		if (s.hasNext(Parser.VARIABLE)) {
			child = new VariableNode().parse(s);
		} else if (s.hasNext(Parser.INTEGER)) {
			child = new NumberNode().parse(s);
		} else {
			Parser.requireEat(Parser.OPEN_BRACKET, "PRIMARY", s);
			child = new ExpNode().parse(s);
			Parser.requireEat(Parser.CLOSE_BRACKET, "PRIMARY", s);
		}
		
		return this;
	}
	
}

class VariableNode implements ParseNode {
	
	@Override
	public double evaluate(double x) {
		return x;
	}

	@Override
	public ParseNode parse(Scanner s) {
		// If there has been a letter which has already been used as a character, then check that the next token
		// matches this letter. Otherwise, make this token the recognised character.
		if (Parser.recognizedVar == null) {
			Parser.recognizedVar = s.next();
		} else {
			Parser.requireEat(Parser.recognizedVar, "VARIABLE", s);
		}
		
		return this;
	}
	
}

class NumberNode implements ParseNode {
	
	double number;
	
	@Override
	public double evaluate(double x) {
		return number;
	}

	@Override
	public ParseNode parse(Scanner s) {
		// First, parse the integer part of this number.
		String num = Parser.requireSpit(Parser.INTEGER, "NUMBER", s);
		while (s.hasNext(Parser.INTEGER)) {
			num += s.next();
		}
		
		// Next, parse the decimal part of this number if it exists.
		String decimal = "";
		if (Parser.checkEat(Parser.DECIMAL, s)) {
			decimal = ".";
			
			// There must now be atleast one number after the decimal place.
			decimal += Parser.requireSpit(Parser.INTEGER, "NUMBER", s);
			
			// Concat until we run out of digit characters.
			while (s.hasNext(Parser.INTEGER)) {
				decimal += s.next();
			}
		}
		
		// Now we convert the number from String to a double.
		this.number = Double.valueOf(num + decimal);
		
		return this;
	}
	
}
