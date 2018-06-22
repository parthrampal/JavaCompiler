package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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


import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;
import cop5556sp18.AST.*;


public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program prog = null;
		prog = program();
		matchEOF();
		return prog;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token first = t;
		Block b = null;
		Token name = t;
		match(IDENTIFIER);
		b = block();
		return new Program(first,name,b);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, KW_while, KW_if, KW_SLEEP, KW_show, 
			KW_red, KW_green, KW_blue, KW_alpha, IDENTIFIER };

	public Block block() throws SyntaxException {
		Token first = t;
		Declaration dec = null;
		Statement stat = null;
		List<ASTNode> list = new ArrayList<ASTNode>();
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			dec = declaration();
			list.add(dec);
		} else if (isKind(firstStatement)) {
			stat = statement();
			list.add(stat);
		}
			match(SEMI);
		}
		match(RBRACE);
		return new Block(first,list);
	}
	
	public Declaration declaration() throws SyntaxException {	/* Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ] */
		Declaration d = null;
			d = Type();
		return d;
	//		throw new UnsupportedOperationException();
	}
	
	private Declaration Type() throws SyntaxException {	/* Type ::= int | float | boolean | image | filename	*/
		Token first = t;
		Token type = null;
		Token name = null;
		Expression e0 = null;
		Expression e1 = null;
		if(isKind(KW_int)||isKind(KW_float)||isKind(KW_boolean)||isKind(KW_filename))
		{		/*	 Type IDENTIFIER	*/
			type = t;
			consume();
			name = t;
			match(IDENTIFIER);
		}
		else if(isKind(KW_image))
		{						/*	image IDENTIFIER [ Expression , Expression ]	*/
				type = t;
				consume();
				name = t;
				match(IDENTIFIER);
				if(isKind(LSQUARE))
				{
					consume();
					e0 = expression();
					match(COMMA);
					e1 = expression();
					match(RSQUARE);
				}
				
		}
		
		return new Declaration(first,type,name,e0,e1);
			//throw new UnsupportedOperationException();
		
	}


	Expression expression() throws SyntaxException {
		/*	Expression ::=  OrExpression  ?  Expression  :  Expression|   OrExpression
	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		Expression e2 = null;
		
		e0 = OrExpression();
		if(isKind(OP_QUESTION))
		{
			consume();	//match(OP_QUESTION);
			e1 = expression();
			match(OP_COLON);
			e2 = expression();
			return new ExpressionConditional(first,e0,e1,e2);
		}
		else
		{
			return e0;
		}
		
		
	}


	private Expression OrExpression() throws SyntaxException	{
		/*	OrExpression  ::=  AndExpression   (  |  AndExpression ) *	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = AndExpression();
		while(isKind(OP_OR))
		{
			Token op = t;
			consume();
			e1 = AndExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
		}
		return e0;
	}


	private Expression AndExpression() throws SyntaxException	{
		/*	AndExpression ::=  EqExpression ( & EqExpression )*	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = EqExpression();
		while(isKind(OP_AND))
		{
			Token op = t;
			consume();
			e1 = EqExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
		}
		return e0;
	}


	private Expression EqExpression() throws SyntaxException	{
		/*	EqExpression ::=  RelExpression  (  (== | != )  RelExpression )*	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = RelExpression();
		while(isKind(OP_EQ)||isKind(OP_NEQ))
		{
			Token op = t;
			consume();
			e1 = RelExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
		}
		return e0;
	}


	private Expression RelExpression() throws SyntaxException	{
		/*	RelExpression ::= AddExpression (  (<  | > |  <=  | >= )   AddExpression)*	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = AddExpression();
		while(isKind(OP_LT)||isKind(OP_GT)||isKind(OP_LE)||isKind(OP_GE))
		{
			Token op = t;
			consume();
			e1 = AddExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
		}
		return e0;
	}


	private Expression AddExpression() throws SyntaxException	{
		/*	AddExpression ::= MultExpression   (  ( + | - ) MultExpression )*	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = MultExpression();
		while(isKind(OP_PLUS)||isKind(OP_MINUS))
		{
			Token op = t;
			consume();
			e1 = MultExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
		}
		return e0;
	}


	private Expression MultExpression() throws SyntaxException	{
		/*	MultExpression := PowerExpression ( ( * | /  | % ) PowerExpression )*	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = PowerExpression();
		while(isKind(OP_TIMES)||isKind(OP_DIV)||isKind(OP_MOD))
		{
			Token op = t;
			consume();
			e1 = PowerExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
		}
		return e0;
	}


	private Expression PowerExpression() throws SyntaxException	{
		/*	PowerExpression := UnaryExpression  (** PowerExpression | ε)	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		e0 = UnaryExpression();
		if(isKind(OP_POWER))
		{
			Token op = t;
			consume();
			e1 = PowerExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
		}
		return e0;	
	}


	private Expression UnaryExpression() throws SyntaxException	{
		/*	+ UnaryExpression | - UnaryExpression | UnaryExpressionNotPlusMinus	*/
		Token first = t;
		Expression e0 = null;
		
		
		if(isKind(OP_PLUS)||isKind(OP_MINUS))
		{
			Token op = t;
			consume();
			e0 = UnaryExpression();
			e0 = new ExpressionUnary(first,op,e0);
		}
		else
		{
			e0 = UnaryExpressionNotPlusMinus();
		}
		return e0;
	}


	private Expression UnaryExpressionNotPlusMinus() throws SyntaxException	{
		/*	UnaryExpressionNotPlusMinus ::=  ! UnaryExpression  | Primary 	*/
		Token first = t;
		Expression e0 = null;
		if(isKind(OP_EXCLAMATION))
		{
			Token op = t;
			consume();
			e0 = UnaryExpression();
			e0 = new ExpressionUnary(first,op,e0);
		}
		else
		{
			e0 = Primary();
		}
		return e0;
	}


	private Expression Primary() throws SyntaxException	{
		/*	Primary ::= INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | 
                ( Expression ) | FunctionApplication  | IDENTIFIER | PixelExpression | 
                 PredefinedName | PixelConstructor
	*/
		Token first = t;
		Token type = null;
		Expression e0 = null;
		Kind kind = t.kind;
		
		switch(kind)
		{
		case INTEGER_LITERAL: {
			type = t;
			consume();
			e0 = new ExpressionIntegerLiteral(first,type);
			return e0;
			}
		case BOOLEAN_LITERAL: {
			type = t;
			consume();
			e0 = new ExpressionBooleanLiteral(first,type);
			return e0;
			}
		case FLOAT_LITERAL:	{
			type = t;
			consume();
			e0 = new ExpressionFloatLiteral(first,type);
			return e0;
			}
		case LPAREN:	{
			consume();
			e0 = expression();
			match(RPAREN);
			return e0;
		}
		case IDENTIFIER:	{
			{
				e0 = PixelExpression();
				return e0;
			}
			
		}
		case KW_sin:
		case KW_cos:
		case KW_atan:
		case KW_abs:
		case KW_log:
		case KW_cart_x:
		case KW_cart_y:
		case KW_polar_a:
		case KW_polar_r:
		case KW_int:
		case KW_float:
		case KW_width:
		case KW_height:
		case KW_red:
		case KW_green:
		case KW_blue:
		case KW_alpha:	{
			//type = t;
			e0 = FunctionApplication();
			return e0;
		}
		case KW_Z:
		case KW_default_height:
		case KW_default_width:	{
			type = PredefinedName();
			e0 = new ExpressionPredefinedName(first,type);;
			return e0;
		}
		case LPIXEL:	{
			e0 = PixelConstructor();
			return e0;
		}
		default:
				throw new SyntaxException(t, null);
		}

	}


	private Expression PixelConstructor() throws SyntaxException {
		/*	PixelConstructor ::=  <<  Expression , Expression , Expression , Expression  >> 	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		Expression e2 = null;
		Expression e3 = null;
		
		consume();
		e0 = expression();
		match(COMMA);
		e1 = expression();
		match(COMMA);
		e2 = expression();
		match(COMMA);
		e3 = expression();
		match(RPIXEL);
		return new ExpressionPixelConstructor(first,e0,e1,e2,e3);
	}


	private Token PredefinedName() throws SyntaxException {
		/*	PredefinedName ::= Z | default_height | default_width	*/
		
		if(isKind(KW_Z)||isKind(KW_default_height)||isKind(KW_default_width))
		{
			Token name = t;
			consume();
			return name;
		}
		else
		{
			throw new SyntaxException(t,"Invalid token in Predefined Name.");
		}
		
	}


	private Expression FunctionApplication() throws SyntaxException {
		/* FunctionApplication ::= FunctionName ( Expression )  | FunctionName  [ Expression , Expression ] 	*/
		Token first = t;
		Token name = null;
		Expression e0 = null;
		Expression e1 = null;
		
		name = FunctionName();
		if(isKind(LPAREN))
		{
			consume();
			e0 = expression();
			match(RPAREN);
			return new ExpressionFunctionAppWithExpressionArg(first,name,e0);
		}
		else if(isKind(LSQUARE))
		{
			consume();
			e0 = expression();
			match(COMMA);
			e1 = expression();
			match(RSQUARE);
			return new ExpressionFunctionAppWithPixel(first,name,e0,e1);
		}
		else
		{
			throw new SyntaxException(t, "Unidentified token in FunctionApplication");
		}
		
	}


	private Token FunctionName() throws SyntaxException	{
		/*	FunctionName ::= sin | cos | atan | abs | log | cart_x | cart_y | polar_a | polar_r 
	    int | float | width | height | Color	*/
		Token name = null;
		if(isKind(KW_sin)||isKind(KW_cos)||isKind(KW_atan)||isKind(KW_abs)||isKind(KW_log)||isKind(KW_cart_x)
				||isKind(KW_cart_y)||isKind(KW_polar_a)||isKind(KW_polar_r)||isKind(KW_int)||isKind(KW_float)
				||isKind(KW_width)||isKind(KW_height))
		{
			name = t;
			consume();
		}
		else	{
			name = color();
		}
		return name;
	}


	private Expression PixelExpression() throws SyntaxException {
		/*	PixelExpression ::= IDENTIFIER PixelSelector	*/
		Token first = t;
		Token name = t;
		Expression e0 = null;
		consume();
		if(isKind(LSQUARE))
		{
			PixelSelector pix = null;
			pix = PixelSelector();
			e0 = new ExpressionPixel(first,name,pix);
			
		}
		else
		{
			e0 = new ExpressionIdent(first,name); 
		}
		return e0;
	}


	public Statement statement() throws SyntaxException	{	
		/* StatementInput | StatementWrite | StatementAssignment | 
		 * StatementWhile | StatementIf | StatementShow | StatementSleep	*/
		//TODO
		Statement stat = null;
		
		Kind kind = t.kind;
		switch(kind)
		{
		case KW_input:	{
			stat = StatementInput();
		}break;
		case KW_write:	{
			stat = StatementWrite();
		}break;
		case KW_while: {
			stat = StatementWhile();
		}break;
		case KW_if: {
			stat = StatementIf();
		}break;
		case KW_show: {
			stat = StatementShow();
		}break;
		case KW_SLEEP: {
			stat = StatementSleep();
		}break;
		case IDENTIFIER:
		default:
			stat = StatementAssignment();
		}
		return stat;
		
		//throw new UnsupportedOperationException();
	}

	private StatementAssign StatementAssignment() throws SyntaxException	{
		/*	LHS := Expression	*/
		Token first = t;
		LHS lhs = null;
		Expression e0 = null;
		lhs = LHS();
		match(OP_ASSIGN);
		e0 = expression();
		return new StatementAssign(first,lhs,e0);
	}


	private LHS LHS() throws SyntaxException	{
		/*	LHS ::=  IDENTIFIER | IDENTIFIER PixelSelector | Color ( IDENTIFIER PixelSelector )	*/
		Token first = t;
		if(isKind(IDENTIFIER))
		{
			Token ident = t;
			consume();
			if(isKind(LSQUARE))
			{
				PixelSelector pix = null;
				pix = PixelSelector();
				return new LHSPixel(first,ident,pix);
			}
			else
			{
				return new LHSIdent(first,ident);
			}
		}
		else
		{
			PixelSelector pix = null;
			Token clr = null;
			clr = color();
			match(LPAREN);
			Token ident = t;
			match(IDENTIFIER);
			pix = PixelSelector();
			match(RPAREN);
			return new LHSSample(first,ident,pix,clr);
		}
	}


	private Token color() throws SyntaxException	{
		/*	Color ::= red | green | blue | alpha	*/
		if(isKind(KW_red)||isKind(KW_green)||isKind(KW_blue)||isKind(KW_alpha))
		{
			Token name = t;
			consume();
			return name;
		}
		else
		{
			throw new SyntaxException(t,"Undefined token in Color");
		}
		
	}


	private PixelSelector PixelSelector() throws SyntaxException	{
		/*	PixelSelector ::= [ Expression , Expression ]	*/
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		
		match(LSQUARE);
		e0 = expression();
		match(COMMA);
		e1 = expression();
		match(RSQUARE);
		return new PixelSelector(first,e0,e1);
		
	}


	private StatementSleep StatementSleep() throws SyntaxException	{
		/*	StatementSleep ::=  sleep Expression	*/
		Token first = t;
		Expression e0 = null;
		match(KW_SLEEP);
		e0 = expression();
		return new StatementSleep(first,e0);
	}


	private StatementShow StatementShow() throws SyntaxException	{
		/*	StatementShow ::=  show Expression	*/
		Token first = t;
		Expression e0 = null;
		match(KW_show);
		e0 = expression();
		return new StatementShow(first,e0);
	}


	private StatementIf StatementIf() throws SyntaxException	{
		/*	StatementIf ::=  if ( Expression ) Block	*/
		Token first = t;
		Expression e0 = null;
		Block b = null;
		match(KW_if);
		match(LPAREN);
		e0 = expression();
		match(RPAREN);
		b = block();
		return new StatementIf(first,e0,b);
	}


	private StatementWhile StatementWhile() throws SyntaxException	{
		/*	StatementWhile ::=  while (Expression ) Block	*/
		Token first = t;
		Expression e0 = null;
		Block b = null;
		match(KW_while);
		match(LPAREN);
		e0 = expression();
		match(RPAREN);
		b = block();
		return new StatementWhile(first,e0,b);
	}


	private StatementWrite StatementWrite() throws SyntaxException	{
		/*	StatementWrite ::= write IDENTIFIER to IDENTIFIER	*/
		Token first = t;
		match(KW_write);
		Token from = t;
		match(IDENTIFIER);
		match(KW_to);
		Token to = t;
		match(IDENTIFIER);
		return new StatementWrite(first,from,to);
	}


	private StatementInput StatementInput() throws SyntaxException	{	
		/*	StatementInput ::= input IDENTIFIER from @ Expression	*/
		Token first = t;
		Expression e0 = null;
		match(KW_input);
		Token name = t;
		match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		e0 = expression();
		return new StatementInput(first,name,e0);
	}


	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Syntax Error. Mismatched token."); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error. Unexpected EOF."); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	

}

