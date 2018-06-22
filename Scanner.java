/**
* Initial code for the Scanner for the class project in COP5556 Programming Language Principles 
* at the University of Florida, Spring 2018.
* 
* This software is solely for the educational benefit of students 
* enrolled in the course during the Spring 2018 semester.  
* 
* This software, and any software derived from it,  may not be shared with others or posted to public web sites,
* either during the course or afterwards.
* 
*  @Beverly A. Sanders, 2018
*/

package cop5556sp18;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
//import java.util.HashMap;

public class Scanner {

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL,
		KW_Z/* Z */, KW_default_width/* default_width */, KW_default_height/* default_height */, 
		KW_width /* width */, KW_height /* height*/, KW_show/*show*/, KW_write /* write */, KW_to /* to */,
		KW_input /* input */, KW_from /* from */, KW_cart_x/* cart_x*/, KW_cart_y/* cart_y */, 
		KW_polar_a/* polar_a*/, KW_polar_r/* polar_r*/, KW_abs/* abs */, KW_sin/* sin*/, KW_cos/* cos */, 
		KW_atan/* atan */, KW_log/* log */, KW_image/* image */, KW_int/* int */, KW_float /* float */, 
		KW_boolean/* boolean */, KW_filename/* filename */, KW_red /* red */, KW_blue /* blue */, 
		KW_green /* green */, KW_alpha /* alpha*/, KW_while /* while */, KW_if /* if */,KW_SLEEP /* sleep */, 
		OP_ASSIGN/* := */,OP_EXCLAMATION/* ! */, OP_QUESTION/* ? */, OP_COLON/* : */, OP_EQ/* == */, 
		OP_NEQ/* != */,	OP_GE/* >= */, OP_LE/* <= */, OP_GT/* > */, OP_LT/* < */, OP_AND/* & */, OP_OR/* | */, 
		OP_PLUS/* +*/, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, OP_POWER/* ** */, 
		OP_AT/* @ */, LPAREN/*( */, RPAREN/* ) */, LSQUARE/* [ */, RSQUARE/* ] */, LBRACE /*{ */, 
		RBRACE /* } */, LPIXEL /* << */, RPIXEL /* >> */, SEMI/* ; */, COMMA/* , */, DOT /* . */, EOF;
	}

	/**
	 * Class to represent Tokens.
	 * 
	 * This is defined as a (non-static) inner class which means that each Token
	 * instance is associated with a specific Scanner instance. We use this when
	 * some token methods access the chars array in the associated Scanner.
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position of first character of this token in the input. Counting starts at 0
								// and is incremented for every character.
		public final int length; // number of characters in this token

		public Token(Kind kind, int pos, int length) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		public String getText() {
			return String.copyValueOf(chars, pos, length);
		}

		/**
		 * precondition: This Token's kind is INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		/**
		 * precondition: This Token's kind is FLOAT_LITERAL]
		 * 
		 * @returns the float value represented by the token
		 */
		public float floatVal() {
			assert kind == Kind.FLOAT_LITERAL;
			return Float.valueOf(String.copyValueOf(chars, pos, length));
		}

		/**
		 * precondition: This Token's kind is BOOLEAN_LITERAL
		 * 
		 * @returns the boolean value represented by the token
		 */
		public boolean booleanVal() {
			assert kind == Kind.BOOLEAN_LITERAL;
			return getText().equals("true");
		}

		/**
		 * Calculates and returns the line on which this token resides. The first line
		 * in the source code is line 1.
		 * 
		 * @return line number of this Token in the input.
		 */
		public int line() {
			return Scanner.this.line(pos) + 1;
		}

		/**
		 * Returns position in line of this token.
		 * 
		 * @param line.
		 *            The line number (starting at 1) for this token, i.e. the value
		 *            returned from Token.line()
		 * @return
		 */
		public int posInLine(int line) {
			return Scanner.this.posInLine(pos, line - 1) + 1;
		}

		/**
		 * Returns the position in the line of this Token in the input. Characters start
		 * counting at 1. Line termination characters belong to the preceding line.
		 * 
		 * @return
		 */
		public int posInLine() {
			return Scanner.this.posInLine(pos) + 1;
		}

		public String toString() {
			int line = line();
			return "[" + kind + "," + String.copyValueOf(chars, pos, length) + "," + pos + "," + length + "," + line
					+ "," + posInLine(line) + "]";
		}

		/**
		 * Since we override equals, we need to override hashCode, too.
		 * 
		 * See
		 * https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#hashCode--
		 * where it says, "If two objects are equal according to the equals(Object)
		 * method, then calling the hashCode method on each of the two objects must
		 * produce the same integer result."
		 * 
		 * This method, along with equals, was generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		/**
		 * Override equals so that two Tokens are equal if they have the same Kind, pos,
		 * and length.
		 * 
		 * This method, along with hashcode, was generated by eclipse.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 * 
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}// Token

	/**
	 * Array of positions of beginning of lines. lineStarts[k] is the pos of the
	 * first character in line k (starting at 0).
	 * 
	 * If the input is empty, the chars array will have one element, the synthetic
	 * EOFChar token and lineStarts will have size 1 with lineStarts[0] = 0;
	 */
	int[] lineStarts;

	int[] initLineStarts() {
		ArrayList<Integer> lineStarts = new ArrayList<Integer>();
		int pos = 0;

		for (pos = 0; pos < chars.length; pos++) {
			lineStarts.add(pos);
			char ch = chars[pos];
			while (ch != EOFChar && ch != '\n' && ch != '\r') {
				pos++;
				ch = chars[pos];
			}
			if (ch == '\r' && chars[pos + 1] == '\n') {
				pos++;
			}
		}
		// convert arrayList<Integer> to int[]
		return lineStarts.stream().mapToInt(Integer::valueOf).toArray();
	}

	int line(int pos) {
		int line = Arrays.binarySearch(lineStarts, pos);
		if (line < 0) {
			line = -line - 2;
		}
		return line;
	}

	public int posInLine(int pos, int line) {
		return pos - lineStarts[line];
	}

	public int posInLine(int pos) {
		int line = line(pos);
		return posInLine(pos, line);
	}

	/**
	 * Sentinal character added to the end of the input characters.
	 */
	static final char EOFChar = 128;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus an additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;
	
	/**
	 * A HashMap of all reserved Keywords
	 */
	public HashMap<String, Kind> KeyWords = new HashMap<String, Kind>();
	
	/**
	 * A temporary storage of chars to check if it is a keyword before creating token
	 */
	
	StringBuilder tape = new StringBuilder();

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFChar;
		tokens = new ArrayList<Token>();
		lineStarts = initLineStarts();
		KeyWords.put("Z", Kind.KW_Z);
		KeyWords.put("default_width", Kind.KW_default_width);
		KeyWords.put("default_height", Kind.KW_default_height);
		KeyWords.put("show", Kind.KW_show);
		KeyWords.put("write", Kind.KW_write);
		KeyWords.put("to", Kind.KW_to);
		KeyWords.put("input", Kind.KW_input);
		KeyWords.put("from", Kind.KW_from);
		KeyWords.put("cart_x", Kind.KW_cart_x);
		KeyWords.put("cart_y", Kind.KW_cart_y);
		KeyWords.put("polar_a", Kind.KW_polar_a);
		KeyWords.put("polar_r", Kind.KW_polar_r);
		KeyWords.put("abs", Kind.KW_abs);
		KeyWords.put("sin", Kind.KW_sin);
		KeyWords.put("cos", Kind.KW_cos);
		KeyWords.put("atan", Kind.KW_atan);
		KeyWords.put("log", Kind.KW_log);
		KeyWords.put("image", Kind.KW_image);
		KeyWords.put("int", Kind.KW_int);
		KeyWords.put("float", Kind.KW_float);
		KeyWords.put("filename", Kind.KW_filename);
		KeyWords.put("boolean", Kind.KW_boolean);
		KeyWords.put("red", Kind.KW_red);
		KeyWords.put("blue", Kind.KW_blue);
		KeyWords.put("green", Kind.KW_green);
		KeyWords.put("alpha", Kind.KW_alpha);
		KeyWords.put("while", Kind.KW_while);
		KeyWords.put("if", Kind.KW_if);
		KeyWords.put("width", Kind.KW_width);
		KeyWords.put("height", Kind.KW_height);
		KeyWords.put("true", Kind.BOOLEAN_LITERAL);
		KeyWords.put("false", Kind.BOOLEAN_LITERAL);
		KeyWords.put("sleep", Kind.KW_SLEEP);
	}




	 private enum State {START,AFTER_LANGULAR,AFTER_RANGULAR,AFTER_ASTERIX,AFTER_COLON,AFTER_EQUAL,AFTER_NOT,IDENT,LITERAL,AFTER_DOT,AFTER_DIV,AFTER_ZERO,IN_COMMENT};  //TODO:  this is incomplete

	 
	 //TODO: Modify this to deal with the entire lexical specification
	public Scanner scan() throws LexicalException {
		int pos = 0;
		State state = State.START;
		int startPos = 0;
		while (pos < chars.length) {
			char ch = chars[pos];
			switch(state) {
				case START: {
					startPos = pos;
					switch (ch) {
						case ' ':
						case '\n':
						case '\r':
						case '\t':
						case '\f': {
							pos++;
						}
						break;
						case EOFChar: {
							tokens.add(new Token(Kind.EOF, startPos, 0));
							pos++; // next iteration will terminate loop
						}
						break;
						case ';': {
							tokens.add(new Token(Kind.SEMI, startPos, 1));
							pos++;
						}
						break;
						case '(': {
							tokens.add(new Token(Kind.LPAREN, startPos, 1));
							pos++;
						}
						break;
						case ')': {
							tokens.add(new Token(Kind.RPAREN, startPos, 1));
							pos++;
						}
						break;
						case '[': {
							tokens.add(new Token(Kind.LSQUARE, startPos, 1));
							pos++;
						}
						break;
						case ']': {
							tokens.add(new Token(Kind.RSQUARE, startPos, 1));
							pos++;
						}
						break;
						case ',': {
							tokens.add(new Token(Kind.COMMA, startPos, 1));
							pos++;
						}
						break;
						case '{': {
							tokens.add(new Token(Kind.LBRACE, startPos, 1));
							pos++;
						}
						break;
						case '}': {
							tokens.add(new Token(Kind.RBRACE, startPos, 1));
							pos++;
						}
						break;
						case '.': {
							tape.append(ch);
							state = State.AFTER_DOT;
							pos++;
						}
						break;
						case '<': {
							state = State.AFTER_LANGULAR;
							pos++;
						}
						break;
						case '>': {
							state = State.AFTER_RANGULAR;
							pos++;
						}
						break;
						case '!': {
							state = State.AFTER_NOT;
							pos++;
						}
						break;
						case '?': {
							tokens.add(new Token(Kind.OP_QUESTION, startPos, 1));
							pos++;
						}
						break;
						case ':': {
							state = State.AFTER_COLON;
							pos++;
						}
						break;
						case '=': {
							state = State.AFTER_EQUAL;
							pos++;
						}
						break;
						case '&': {
							tokens.add(new Token(Kind.OP_AND, startPos, 1));
							pos++;
						}
						break;
						case '|': {
							tokens.add(new Token(Kind.OP_OR, startPos, 1));
							pos++;
						}
						break;
						case '+': {
							tokens.add(new Token(Kind.OP_PLUS, startPos, 1));
							pos++;
						}
						break;
						case '-': {
							tokens.add(new Token(Kind.OP_MINUS, startPos, 1));
							pos++;
						}
						break;
						case '*': {
							state = State.AFTER_ASTERIX;
							pos++;
						}
						break;
						case '/': {
							state = State.AFTER_DIV;
							pos++;
						}
						break;
						case '%': {
							tokens.add(new Token(Kind.OP_MOD, startPos, 1));
							pos++;
						}
						break;
						case '@': {
							tokens.add(new Token(Kind.OP_AT, startPos, 1));
							pos++;
						}
						break;
						case '0': {
							tape.append(Character.getNumericValue(ch));
							state = State.AFTER_ZERO;
							pos++;
						}
						break;
						default: {
							if(Character.isDigit(ch))
							{
								tape.append(Character.getNumericValue(ch));
								state = State.LITERAL;
								pos++;
							}
							else if (Character.isLetter(ch))
							{
								tape.append((char)ch);
								state = State.IDENT;
								pos++;
							}
							else
							{
								error(pos, line(pos), posInLine(pos), "illegal char");
							}
							
						}
					}//switch ch
				}
				break;	// End START
				
				case AFTER_LANGULAR: {
					switch(ch) {
					case '<': {
						tokens.add(new Token(Kind.LPIXEL, startPos, 2));
						pos++;
						state = State.START;
					}
					break;
					case '=': {
						tokens.add(new Token(Kind.OP_LE, startPos, 2));
						pos++;
						state = State.START;
					}
					break;
					
					default: {
						tokens.add(new Token(Kind.OP_LT, startPos, 1));
						state = State.START;
					} // end default
					} // end switch inside AFTER_LANGULAR
				
				}	
				break;	// End AFTER_LANGULAR
				
				case AFTER_RANGULAR: {
					switch(ch) {
					case '>': {
						tokens.add(new Token(Kind.RPIXEL, startPos,2));
						pos++;
						state = State.START;
					}
					break;
					case '=': {
						tokens.add(new Token(Kind.OP_GE, startPos, 2));
						pos++;
						state = State.START;
					}
					break;
					default:{
						tokens.add(new Token(Kind.OP_GT, startPos, 1));
						state = State.START;	
					}
					}	// end switch inside AFTER_RANGULAR
					
				}
				break;	// end AFTER_RANGULAR
				
				case AFTER_ASTERIX: {
					switch(ch)	{
					case '*':{
						tokens.add(new Token(Kind.OP_POWER, startPos, 2));
						pos++;
						state = State.START;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_TIMES, startPos, 1));
						state = State.START;
					}
					}	// end switch inside AFTER_ASTERIX
				}
				break;	// end AFTER_ASTERIX
				
				case AFTER_COLON: {
					switch(ch) {
					case '=': {
						tokens.add(new Token(Kind.OP_ASSIGN, startPos, 2));
						pos++;
						state = State.START;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_COLON, startPos, 1));
						state = State.START;
					}
					}	// end switch inside AFTER_COLON
				}
				break;	// end AFTER_COLON
				
				case AFTER_EQUAL: {
					switch(ch) {
					case '=': {
						tokens.add(new Token(Kind.OP_EQ, startPos, 2));
						pos++;
						state = State.START;
					}
					break;
					default: {
						error(pos, line(pos), posInLine(pos), "illegal char =");
					}
					}	// end switch inside AFTER_EQUAL
				}
				break;	// end AFTER_EQUAL
				
				case AFTER_NOT: {
					switch(ch) {
					case '=': {
						tokens.add(new Token(Kind.OP_NEQ, startPos, 2));
						pos++;
						state = State.START;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_EXCLAMATION, startPos, 1));
						state = State.START;
					}
					
					}	// end switch inside AFTER_NOT
				}
				break;	// end AFTER_NOT
				
				case IDENT: {
					if(Character.isLetterOrDigit(ch)||ch == '_'||ch == '$')
					{
						tape.append((char)ch);
						pos++;
					}
					else
					{
						if(KeyWords.containsKey(tape.toString()))
						{
							tokens.add(new Token(KeyWords.get(tape.toString()), startPos, pos - startPos));
							state= State.START;
						}
						else
						{
							tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos));
							state = State.START;
						}
						tape.setLength(0);
					}
				}
				break;	// end IDENT
				
				case LITERAL: {
					if(Character.isDigit(ch))
					{
						tape.append(Character.getNumericValue(ch));
						pos++;
					}
					else
					{
						if(ch == '.')
						{
							tape.append(ch);
							state = State.AFTER_DOT;
							pos++;
						}
						else
						{
							try {
								Integer.parseInt(tape.toString());
								tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos));
							}
							catch(NumberFormatException e)
							{
								error(pos, line(pos), posInLine(pos),"Number outside Integer Range");
							}
							state = State.START;
							tape.setLength(0);
						}
						
					}
				}
				break;	//end LITERAL
				
				case AFTER_DOT: {
					
					if(Character.isDigit(ch))
					{
						tape.append(Character.getNumericValue(ch));
						pos++;
					}
					else
					{
						if(tape.length()>1)
						{

							if(Float.isFinite(Float.parseFloat(tape.toString())))
							{
								tokens.add(new Token(Kind.FLOAT_LITERAL, startPos, pos - startPos));
								state = State.START;
								tape.setLength(0);
								//pos++;
							}
							else
							{
								error(pos, line(pos), posInLine(pos),"Number outside Float Range");
							}
						}
						else
						{
							tokens.add(new Token(Kind.DOT, startPos, 1));
							state = State.START;
							tape.setLength(0);
						}
					}
					
				}
				break;	// end AFTER_DOT
				
				case AFTER_ZERO: {
					if(ch=='.')
					{
						state = State.AFTER_DOT;
						pos++;
						tape.append(ch);
					}
					else
					{
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos));
						state = State.START;
						tape.setLength(0);
					}
				}
				break;	//end AFTER_ZERO
				
				case AFTER_DIV: {
					if(ch=='*')
					{
						state = State.IN_COMMENT;
						pos++;
					}
					else
					{
						tokens.add(new Token(Kind.OP_DIV, startPos, 1));
						state = State.START;
					}
				}
				break;	//end AFTER_DIV
				
				case IN_COMMENT: {
					if(ch == '*')
					{
						pos++;
						if(chars[pos]=='/')
						{
							pos++;
							state = State.START;
						}
						else
						{
							//pos++;
							state = State.IN_COMMENT;
						}
					}
					else if(ch == EOFChar)
					{
						error(pos, line(pos), posInLine(pos),"Expected Close of Comment Missing. EOF Reached.");
					}
					else
					{
						pos++;
						state = State.IN_COMMENT;
					}
				}
				break;	// end IN_COMMENT
				
				default: {
					error(pos, 0, 0, "undefined state");
				}
			}// switch state
		} // while
			
		return this;
	}


	private void error(int pos, int line, int posInLine, String message) throws LexicalException {
		String m = (line + 1) + ":" + (posInLine + 1) + " " + message;
		throw new LexicalException(m, pos);
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens and line starts
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		sb.append("Line starts:\n");
		for (int i = 0; i < lineStarts.length; i++) {
			sb.append(i).append(' ').append(lineStarts[i]).append('\n');
		}
		return sb.toString();
	}

}
