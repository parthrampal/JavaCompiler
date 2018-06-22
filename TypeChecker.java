package cop5556sp18;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

public class TypeChecker implements ASTVisitor {


	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	public class SymbolTable
	{
		int currScope;
		int nextScope;
		HashMap <String, ArrayList<SymbolValues>> SymTab = new HashMap <String, ArrayList<SymbolValues>>();
		Stack <Integer> ScopeStack = new Stack<Integer>();
		
		
		public class SymbolValues
		{
			int scope;
			Declaration declaration;
			
			public SymbolValues(int sym, Declaration dec)
			{
				this.scope = sym;
				this.declaration = dec;
			}
			
			public int GetScope()
			{
				return scope;
			}
			public Declaration GetDeclaration()
			{
				return declaration;
			}
		}
		
		public void enterScope()
		{
			currScope = nextScope++;
			ScopeStack.push(currScope);
		}
		public void leaveScope()
		{
			ScopeStack.pop();
			currScope = ScopeStack.peek();
			
		}
		
		public SymbolTable()
		{
			this.currScope = 0;
			this.nextScope = 1;
			ScopeStack.push(0);
		}
		
		
		public boolean insert (String ident, Declaration decl)
		{
			ArrayList<SymbolValues> SVList = new ArrayList<SymbolValues>();
			SymbolValues SVal = new SymbolValues(currScope, decl);
			
			if(SymTab.containsKey(ident))
			{
				SVList = SymTab.get(ident);
				for(SymbolValues sv: SVList)
				{
					if(sv.GetScope()==currScope)
					{
						return false;
					}
				}
			}
			SVList.add(SVal);
			SymTab.put(ident, SVList);
			return true;
		}
		
		public Declaration lookup(String ident)
		{
			if(!SymTab.containsKey(ident))
			{
				return null;
			}
			
			Declaration decl = null;
			ArrayList<SymbolValues> SVList = SymTab.get(ident);
			for(int i=SVList.size()-1;i>=0;i--)
			{
				int scope = SVList.get(i).GetScope();
				if(ScopeStack.contains(scope))
				{
					decl = SVList.get(i).GetDeclaration();
					break;
				}
			}
				
			
			return decl;
		}
		
		
	}
	
	SymbolTable SymTab = new SymbolTable();
	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		System.out.println("Visiting Program");
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		System.out.println("Visiting Block");
			SymTab.enterScope();
			for(ASTNode decl:block.decsOrStatements)
			{
				decl.visit(this, arg);
			}
			SymTab.leaveScope();
		return null;
		
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		System.out.println("Visiting Declaration");
		Token t = declaration.firstToken;
		
		if(!SymTab.insert(declaration.name, declaration))
		{
			throw new SemanticException(t,"Duplicate declaration, Identifier already exists in current scope at position " + t.posInLine() + " and line " + t.line());
		}
		else
		{
			if(declaration.width != null && declaration.height != null)
			{
				if(Types.getType(declaration.type) != Type.IMAGE)
				{
					throw new SemanticException(t,"Declaration at position " + t.posInLine() + " and line " + t.line() + "must be of type IMAGE to have height and width arguments");
				}
				if((Type)declaration.width.visit(this, arg) != Type.INTEGER)
				{
					throw new SemanticException(t,"Declaration of Image at position " + t.posInLine() + " and line " + t.line() + "must have first argument width of type Integer");
				}
				if((Type)declaration.height.visit(this, arg) != Type.INTEGER)
				{
					throw new SemanticException(t,"Declaration of Image at position " + t.posInLine() + " and line " + t.line() + "must have second argument height of type Integer");
				}
			}
			else if((declaration.width == null && declaration.height != null) || (declaration.width != null && declaration.height == null))
			{
				throw new SemanticException(t,"Declaration at position " + t.posInLine() + " and line " + t.line() + "cannot contain only one argument. Only type Image accepts two args");
			}
		}
		
		return declaration.type;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		System.out.println("Visiting StatementWrite");
		Token t = statementWrite.firstToken;
		String ident0 = statementWrite.sourceName;
		statementWrite.sourceDec = SymTab.lookup(ident0);
		if(statementWrite.sourceDec==null)
		{
			throw new SemanticException(t,"The Source Identifier in Write statement at position " + t.posInLine() + " and line " + t.line() + "is null");
		}
		
		String ident1 = statementWrite.destName;
		statementWrite.destDec = SymTab.lookup(ident1);
		
		if(statementWrite.destDec==null)
		{
			throw new SemanticException(t,"The Destination Identifier in Write statement at position " + t.posInLine() + " and line " + t.line() + "is null");
		}
		if(Types.getType(statementWrite.sourceDec.type) != Type.IMAGE)
		{
			throw new SemanticException(t,"The Source Identifier in Write statement at position " + t.posInLine() + " and line " + t.line() + " should be of type Image");
		}
		if(Types.getType(statementWrite.destDec.type) != Type.FILE)
		{
			throw new SemanticException(t,"The Destination Identifier in Write statement at position " + t.posInLine() + " and line " + t.line() + "should be of type File");
		}
		
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		System.out.println("Visiting StatementInput");
		Token t = statementInput.firstToken;
		String ident0 = statementInput.destName;
		statementInput.dec = SymTab.lookup(ident0);
		
		if(statementInput.dec == null)
		{
			throw new SemanticException(t,"The Identifier in Input statement at position " + t.posInLine() + " and line " + t.line() + "is null");
		}
		if((Type)statementInput.e.visit(this, arg) != Type.INTEGER)
		{
			throw new SemanticException(t,"The Expression in Input statement at position " + t.posInLine() + " and line " + t.line() + "should be of type Integer");
		}
		
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		System.out.println("Visiting PixelSelector");
		Token t = pixelSelector.firstToken;
		Type exp0 = (Type)pixelSelector.ex.visit(this, arg);
		Type exp1 = (Type)pixelSelector.ey.visit(this, arg);
		
		if(exp0 != Type.INTEGER && exp0 != Type.FLOAT)
		{
			throw new SemanticException(t,"The Pixels in Pixel Selector statement at position " + t.posInLine() + " and line " + t.line() + "must be either Integer or Float");
		}
		if(exp0 != exp1)
		{
			throw new SemanticException(t,"The X and Y pixels in the Pixel Selector statement at position " + t.posInLine() + " and line " + t.line() + "must be of the same type");
		}
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		System.out.println("Visiting expressionConditional");
		Token t = expressionConditional.firstToken;
		Type exp0 = (Type)expressionConditional.guard.visit(this, arg);
		Type exp1 = (Type)expressionConditional.trueExpression.visit(this, arg);
		Type exp2 = (Type)expressionConditional.falseExpression.visit(this, arg);
		
		if(exp0 != Type.BOOLEAN)
		{
			throw new SemanticException(t,"The Guard expression in the Conditional statement at position " + t.posInLine() + " and line " + t.line() + "must be of Boolean type");
		}
		if(exp1 != exp2)
		{
			throw new SemanticException(t,"The true and false expressions in the Conditional statement at position " + t.posInLine() + " and line " + t.line() + "must be of the same type");
		}
		
		expressionConditional.type = exp1;
		
		return expressionConditional.type;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		System.out.println("Visiting expressionBinary");
		Token t = expressionBinary.firstToken;
		Kind op = expressionBinary.op;
		Expression expr0 = expressionBinary.leftExpression;
		Expression expr1 = expressionBinary.rightExpression;
		
		if((op == Scanner.Kind.OP_PLUS || op == Scanner.Kind.OP_MINUS || op == Scanner.Kind.OP_TIMES || op == Scanner.Kind.OP_DIV 
				|| op == Scanner.Kind.OP_MOD || op == Scanner.Kind.OP_POWER || op == Scanner.Kind.OP_AND || op == Scanner.Kind.OP_OR )
				&&  (Type)expr0.visit(this, arg) == Type.INTEGER && (Type)expr1.visit(this, arg) == Type.INTEGER)
		{
			expressionBinary.type = Type.INTEGER;
		}
		else if((op == Scanner.Kind.OP_PLUS || op == Scanner.Kind.OP_MINUS || op == Scanner.Kind.OP_TIMES || op == Scanner.Kind.OP_DIV 
				|| op == Scanner.Kind.OP_POWER)	&& (Type)expr0.visit(this, arg) == Type.FLOAT && (Type)expr1.visit(this, arg) == Type.FLOAT)
		{
			expressionBinary.type = Type.FLOAT;
		}
		else if((op == Scanner.Kind.OP_PLUS || op == Scanner.Kind.OP_MINUS || op == Scanner.Kind.OP_TIMES || op == Scanner.Kind.OP_DIV 
				|| op == Scanner.Kind.OP_POWER)	&& (Type)expr0.visit(this, arg) == Type.FLOAT && (Type)expr1.visit(this, arg) == Type.INTEGER)
		{
			expressionBinary.type = Type.FLOAT;
		}
		else if((op == Scanner.Kind.OP_PLUS || op == Scanner.Kind.OP_MINUS || op == Scanner.Kind.OP_TIMES || op == Scanner.Kind.OP_DIV 
				|| op == Scanner.Kind.OP_POWER)	&& (Type)expr0.visit(this, arg) == Type.INTEGER && (Type)expr1.visit(this, arg) == Type.FLOAT)
		{
			expressionBinary.type = Type.FLOAT;
		}
		else if((op == Scanner.Kind.OP_AND || op == Scanner.Kind.OP_OR)	&& (Type)expr0.visit(this, arg) == Type.BOOLEAN && (Type)expr1.visit(this, arg) == Type.BOOLEAN)
		{
			expressionBinary.type = Type.BOOLEAN;
		}
		else if((op == Scanner.Kind.OP_AND || op == Scanner.Kind.OP_OR)	&& (Type)expr0.visit(this, arg) == Type.INTEGER && (Type)expr1.visit(this, arg) == Type.INTEGER)
		{
			expressionBinary.type = Type.INTEGER;
		}
		else if((op == Scanner.Kind.OP_EQ || op == Scanner.Kind.OP_NEQ || op == Scanner.Kind.OP_GT || op == Scanner.Kind.OP_GE 
				|| op == Scanner.Kind.OP_LT || op == Scanner.Kind.OP_LE)	&& (Type)expr0.visit(this, arg) == Type.INTEGER && (Type)expr1.visit(this, arg) == Type.INTEGER)
		{
			expressionBinary.type = Type.BOOLEAN;
		}
		else if((op == Scanner.Kind.OP_EQ || op == Scanner.Kind.OP_NEQ || op == Scanner.Kind.OP_GT || op == Scanner.Kind.OP_GE 
				|| op == Scanner.Kind.OP_LT || op == Scanner.Kind.OP_LE)	&& (Type)expr0.visit(this, arg) == Type.FLOAT && (Type)expr1.visit(this, arg) == Type.FLOAT)
		{
			expressionBinary.type = Type.BOOLEAN;
		}
		else if((op == Scanner.Kind.OP_EQ || op == Scanner.Kind.OP_NEQ || op == Scanner.Kind.OP_GT || op == Scanner.Kind.OP_GE 
				|| op == Scanner.Kind.OP_LT || op == Scanner.Kind.OP_LE)	&& (Type)expr0.visit(this, arg) == Type.BOOLEAN && (Type)expr1.visit(this, arg) == Type.BOOLEAN)
		{
			expressionBinary.type = Type.BOOLEAN;
		}
		if(expressionBinary.type == null)
		{
			throw new SemanticException(t,"ExpressionBinary type at position " + t.posInLine() + " in line " +t.line()+" is null");
		}
		
		return expressionBinary.type;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		System.out.println("Visiting expressionUnary");
		//Token t = expressionUnary.firstToken;
		/*
		 * 		Token t = expressionUnary.firstToken;
		Kind op = expressionUnary.op;
		expressionUnary.type = (Type)expressionUnary.expression.visit(this, arg);
		
		if((op == Kind.OP_EXCLAMATION && (expressionUnary.type != Type.INTEGER && expressionUnary.type != Type.BOOLEAN))
				||(op == Kind.OP_MINUS && (expressionUnary.type != Type.INTEGER && expressionUnary.type != Type.FLOAT))
				||(op == Kind.OP_PLUS && (expressionUnary.type != Type.INTEGER && expressionUnary.type != Type.FLOAT))	)
		{
			throw new SemanticException(t,"The Unary Exclamation expression position " + t.posInLine() + " in line " + t.line() + " must be of type Boolean or Integer Only");
		}
		 */
		expressionUnary.type = (Type)expressionUnary.expression.visit(this, arg);
		
		return expressionUnary.type;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		System.out.println("Visiting expressionIntegerLiteral");
		//Token t = expressionIntegerLiteral.firstToken;
		
		expressionIntegerLiteral.type = Type.INTEGER;
		
		return expressionIntegerLiteral.type;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		System.out.println("Visiting expressionBooleanLiteral");
		//Token t = expressionBooleanLiteral.firstToken;
		
		expressionBooleanLiteral.type = Type.BOOLEAN;		
		
		return expressionBooleanLiteral.type;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		System.out.println("Visiting expressionPredefinedName");
		//Token t = expressionPredefinedName.firstToken;
		
		expressionPredefinedName.type = Type.INTEGER;
		
		return expressionPredefinedName.type;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		System.out.println("Visiting expressionFloatLiteral");
		//Token t = expressionFloatLiteral.firstToken;
		
		expressionFloatLiteral.type = Type.FLOAT;
		
		return expressionFloatLiteral.type;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		System.out.println("Visiting expressionFunctionAppWithExpressionArg");
		Token t = expressionFunctionAppWithExpressionArg.firstToken;
		Kind func = expressionFunctionAppWithExpressionArg.function;
		Expression exp = expressionFunctionAppWithExpressionArg.e;
		
		if((func == Scanner.Kind.KW_abs ||func == Scanner.Kind.KW_red ||func == Scanner.Kind.KW_blue 
				||func == Scanner.Kind.KW_green ||func == Scanner.Kind.KW_alpha) && (Type)exp.visit(this, arg) == Type.INTEGER)
		{
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
		}
		else if((func == Scanner.Kind.KW_abs || func == Scanner.Kind.KW_sin ||func == Scanner.Kind.KW_cos 
				||func == Scanner.Kind.KW_atan ||func == Scanner.Kind.KW_log) && (Type)exp.visit(this, arg) == Type.FLOAT)
		{
			expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
		}
		else if((func == Scanner.Kind.KW_width || func == Scanner.Kind.KW_height) && (Type)exp.visit(this, arg) == Type.IMAGE)
		{
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
		}
		else if((func == Scanner.Kind.KW_float) && (Type)exp.visit(this, arg) == Type.INTEGER)
		{
			expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
		}
		else if((func == Scanner.Kind.KW_float) && (Type)exp.visit(this, arg) == Type.FLOAT)
		{
			expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
		}
		else if((func == Scanner.Kind.KW_int) && (Type)exp.visit(this, arg) == Type.INTEGER)
		{
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
		}
		else if((func == Scanner.Kind.KW_int) && (Type)exp.visit(this, arg) == Type.FLOAT)
		{
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
		}
		else if(expressionFunctionAppWithExpressionArg.type == null)
		{
			throw new SemanticException(t,"expressionFunctionAppWithExpressionArg type at position " +t.posInLine()+ " in line " +t.line()+" is null");
		}
		return expressionFunctionAppWithExpressionArg.type;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		System.out.println("Visiting ExpressionFunctionAppWithPixel");
		Token t = expressionFunctionAppWithPixel.firstToken;
		Kind name = expressionFunctionAppWithPixel.name;
		if(name ==  Scanner.Kind.KW_cart_x|| name == Scanner.Kind.KW_cart_y)
		{
			if(((Type)expressionFunctionAppWithPixel.e0.visit(this, arg) != Type.FLOAT)
					||((Type)expressionFunctionAppWithPixel.e1.visit(this, arg) != Type.FLOAT))
			{
				throw new SemanticException(t,"Expressions in ExpressionFunctionAppWithPixel at position " +t.posInLine()+ " in line " +t.line()+ "must both be of type Float");
			}
			expressionFunctionAppWithPixel.type = Type.INTEGER;
		}
		if(name == Scanner.Kind.KW_polar_a || name == Scanner.Kind.KW_polar_r)
		{
			if(((Type)expressionFunctionAppWithPixel.e0.visit(this, arg) != Type.INTEGER)
					||((Type)expressionFunctionAppWithPixel.e1.visit(this, arg) != Type.INTEGER))
			{
				throw new SemanticException(t,"Expressions in ExpressionFunctionAppWithPixel at position " +t.posInLine()+ " in line " +t.line()+ "must both be of type Integer");
			}
			expressionFunctionAppWithPixel.type = Type.FLOAT;
		}
		
		
		return expressionFunctionAppWithPixel.type;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		System.out.println("Visiting ExpressionPixelConstructor");
		Token t = expressionPixelConstructor.firstToken;
		Type exprA = (Type) expressionPixelConstructor.alpha.visit(this, arg);
		Type exprR = (Type) expressionPixelConstructor.red.visit(this, arg);
		Type exprG = (Type) expressionPixelConstructor.green.visit(this, arg);
		Type exprB = (Type) expressionPixelConstructor.blue.visit(this, arg);
		
		if(exprA != Type.INTEGER)
		{
			throw new SemanticException(t,"The alpha value of the PixelConstructor at position " + t.posInLine() + " in line " + t.line() + " should be of type integer");
		}
		if(exprR != Type.INTEGER)
		{
			throw new SemanticException(t,"The red value of the PixelConstructor at position " + t.posInLine() + " in line " + t.line() + " should be of type integer");
		}
		if(exprB != Type.INTEGER)
		{
			throw new SemanticException(t,"The blue value of the PixelConstructor at position " + t.posInLine() + " in line " + t.line() + " should be of type integer");
		}
		if(exprG != Type.INTEGER)
		{
			throw new SemanticException(t,"The green value of the PixelConstructor at position " + t.posInLine() + " in line " + t.line() + " should be of type integer");
		}
		
		expressionPixelConstructor.type = Type.INTEGER;
		
		return expressionPixelConstructor.type;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		System.out.println("Visiting StatementAssign");
		Token t = statementAssign.firstToken;
		
		if((Type)statementAssign.lhs.visit(this, arg) != (Type)statementAssign.e.visit(this, arg))
		{
			throw new SemanticException(t,"The types of LHS and expression in Assignment Statement at position " + t.posInLine() + " and line " + t.line() + "do not match. They should be same.");
		}
		
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		System.out.println("Visiting statementShow");
		Token t = statementShow.firstToken;
		
		if(((Type)statementShow.e.visit(this, arg) != Type.INTEGER) && ((Type)statementShow.e.visit(this, arg) != Type.BOOLEAN) 
				&& ((Type)statementShow.e.visit(this, arg) != Type.FLOAT) && ((Type)statementShow.e.visit(this, arg) != Type.IMAGE))
		{
			throw new SemanticException(t,"The Expression in show statement at position " + t.posInLine()+ " in line " + t.line() + " must be of type Integer, Boolean, Float or Image Only.");
		}
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		System.out.println("Visiting expressionPixel");
		Token t = expressionPixel.firstToken;
		String ident0 = expressionPixel.name;
		expressionPixel.dec = SymTab.lookup(ident0);
		
		if(expressionPixel.dec == null)
		{
			throw new SemanticException(t,"The identity for the Pixel expression at position " + t.posInLine() + " in line" + t.line() + "is null");
		}
		if(Types.getType((Kind)expressionPixel.dec.type) != Type.IMAGE)
		{
			throw new SemanticException(t,"The declaration for the pixel expression at position " + t.posInLine() + " in line" + t.line() + "must be of type Image");
		}
		expressionPixel.pixelSelector.visit(this, arg);
		expressionPixel.type = Type.INTEGER;
		
		
		return expressionPixel.type;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		System.out.println("Visiting expressionIdent");
		Token t = expressionIdent.firstToken;
		expressionIdent.dec = SymTab.lookup(expressionIdent.name);
		
		if(expressionIdent.dec == null)
		{
			throw new SemanticException(t,"The identity for the expression at position " + t.posInLine() + " in line" + t.line() + "is null");
		}
		expressionIdent.type = Types.getType(expressionIdent.dec.type);
		
		return expressionIdent.type;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		System.out.println("Visiting lhsSample");
		Token t = lhsSample.firstToken;
		String ident0 = lhsSample.name;
		lhsSample.dec = SymTab.lookup(ident0);
		
		if(lhsSample.dec == null)
		{
			throw new SemanticException(t,"The lhs Sample identifier at position " + t.posInLine() + " in line " + t.line() + " is null");
		}
		
		if(Types.getType((Kind)lhsSample.dec.type) != Type.IMAGE)
		{
			throw new SemanticException(t,"The lhs Sample declaration at position " + t.posInLine() + " in line " + t.line() + " must be of type Image");
		}
		lhsSample.pixelSelector.visit(this, arg);
		lhsSample.type = Type.INTEGER;
		
		return lhsSample.type;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		System.out.println("Visiting lhsPixel");
		Token t = lhsPixel.firstToken;
		String ident0 = lhsPixel.name;
		lhsPixel.dec = SymTab.lookup(ident0);
		
		if(lhsPixel.dec == null)
		{
			throw new SemanticException(t,"The lhs Pixel identifier at position " + t.posInLine() + " in line " + t.line() + " is null");
		}
		if(Types.getType((Kind)lhsPixel.dec.type) != Type.IMAGE)
		{
			throw new SemanticException(t,"The lhs Pixel declaration at position " + t.posInLine() + " in line " + t.line() + " must be of type Image");
		}
		
		lhsPixel.pixelSelector.visit(this, arg);
		lhsPixel.type = Type.INTEGER;
		
		return lhsPixel.type;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		System.out.println("Visiting lhsIdent");
		Token t = lhsIdent.firstToken;
		String ident0 = lhsIdent.name;
		lhsIdent.dec = SymTab.lookup(ident0);
		
		if(lhsIdent.dec == null)
		{
			throw new SemanticException(t,"The lhs Identifier at position " + t.posInLine() + " in line " + t.line() + " is null");
		}
		
		lhsIdent.type = Types.getType(lhsIdent.dec.type);		
		
		return lhsIdent.type;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		System.out.println("Visiting statementIf");
		Token t = statementIf.firstToken;
		
		if((Type)statementIf.guard.visit(this, arg) != Type.BOOLEAN)
		{
			throw new SemanticException(t,"The expression in if statement at position " + t.posInLine() + " in line " + t.line() + " must be of type Boolean");
		}
		statementIf.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		System.out.println("Visiting statementWhile");
		Token t = statementWhile.firstToken;
		if((Type)statementWhile.guard.visit(this, arg) != Type.BOOLEAN)
		{
			throw new SemanticException(t,"The expression in while statement at position " + t.posInLine() + " in line " + t.line() + " must be of type Boolean");
		}
		statementWhile.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		System.out.println("Visiting statementSleep");
		Token t = statementSleep.firstToken;
		
		if((Type)statementSleep.duration.visit(this, arg) != Type.INTEGER)
		{
			throw new SemanticException(t,"The Sleep Statement at position " + t.posInLine() + " in line " + t.line() + " must have an expression of type Integer only.");
		}
		
		return null;
	}


}
