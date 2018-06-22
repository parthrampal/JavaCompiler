/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import java.io.File;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slot =1;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		//this.slot = 1;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		Label BlockStart = new Label();
		Label BlockEnd = new Label();
		mv.visitLabel(BlockStart);
		System.out.println("Coding Block");
		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, null);
		}
		mv.visitLabel(BlockEnd);
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		System.out.println("coding BoolLit");
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		System.out.println("coding Declaration");
		declaration.setSlot(slot);
		//declaration.IncrementSlot();
		slot = slot + 1;
		Kind kd = declaration.type;
		//Type tp = Types.getType(kd);
		Expression exp0 = declaration.width;
		Expression exp1 = declaration.height;
		int dec = declaration.getSlot();
		
		if(kd == Kind.KW_image)
		{
			if(exp0 != null && exp1 != null)
			{
				exp0.visit(this, arg);
				exp1.visit(this, arg);
				//RuntimeImageSupport.makeImage(defaultHeight, defaultHeight);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeImage", RuntimeImageSupport.makeImageSig, false);
			}
			else if(exp0 == null && exp1 == null)
			{
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeImage", RuntimeImageSupport.makeImageSig, false);
			}
			mv.visitVarInsn(ASTORE,dec);
		}
		
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		/*
		 * The Binary Operators are Plus, Minus, Multiply, Divide, Mod, Power,
		 * [Logic] And, Or,
		 * [Comparison] Less Than, Greater Than, Less than eq, Greater Than eq, equal, not equal
		 */
		System.out.println("coding ExpBinary");
		
		Type left = expressionBinary.leftExpression.type;
		Type right = expressionBinary.rightExpression.type;
		Kind op = expressionBinary.op;
		Label whenTrue = new Label();
		Label whenFalse = new Label();
		
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);

		if(op == Kind.OP_PLUS)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				/*int x = 2;
				int y = 3;
				int z;
				z = x + y;*/
				
				mv.visitInsn(IADD);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				/*float x = 2;
				float y = 3;
				float z;
				z = x + y;*/
				
				mv.visitInsn(FADD);
			}
			else if(left == Type.FLOAT && right == Type.INTEGER)
			{
				/*float x = 2;
				int y = 3;
				float z;
				z = x + y;*/
				
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
			else if(left == Type.INTEGER && right == Type.FLOAT)
			{
				/*int x = 2;
				float y = 3;
				float z;
				z = x + y;*/
				
				mv.visitInsn(SWAP); // Need to swap the top two values on stack as top value is  Float and we need to convert Int to float
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
		}
		else if(op == Kind.OP_MINUS)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				/*int x = 2;
				int y = 3;
				int z = y - x;*/
				
				mv.visitInsn(ISUB);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				/*float x = 2;
				float y = 3;
				float z = y - x;*/
				
				mv.visitInsn(FSUB);
			}
			else if(left == Type.FLOAT && right == Type.INTEGER)
			{
				/*float x = 2;
				int y = 3;
				float z = x - y;*/
				mv.visitInsn(I2F);
				mv.visitInsn(FSUB);
				
			}
			else if(left == Type.INTEGER && right == Type.FLOAT)
			{
				/*float y = 3;
				int x = 2;
				float z = x - y;*/
				mv.visitInsn(SWAP);	// Swap to bring Int value on top of stack
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP); // Swap to maintain Subtraction order
				mv.visitInsn(FSUB);
			}
			
		}
		else if(op == Kind.OP_TIMES)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				/*int x = 2;
				int y = 3;
				int z;
				z = x * y;*/
				mv.visitInsn(IMUL);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				/*float x = 2;
				float y = 3;
				float z;
				z = x * y;*/
				mv.visitInsn(FMUL);
			}
			else if(left == Type.FLOAT && right == Type.INTEGER)
			{
				/*float x = 2;
				int y = 3;
				float z;
				z = x * y;*/
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
			else if(left == Type.INTEGER && right == Type.FLOAT)
			{
				/*int x = 2;
				float y = 3;
				float z;
				z = x * y;*/
				mv.visitInsn(SWAP); // Swap to bring Int to top to convert it to float
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
		}
		else if(op == Kind.OP_DIV)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				/*int x = 4;
				int y = 2;
				int z;
				z = x/y;*/
				mv.visitInsn(IDIV);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				/*float x = 4;
				float y = 2;
				float z;
				z = x/y;*/
				mv.visitInsn(FDIV);
			}
			else if(left == Type.FLOAT && right == Type.INTEGER)
			{
				/*float x = 4;
				int y = 2;
				float z;
				z = x/y;*/
				mv.visitInsn(I2F);
				mv.visitInsn(FDIV);
			}
			else if(left == Type.INTEGER && right == Type.FLOAT)
			{
				/*int x = 4;
				float y =2;
				float z;
				z = x/y;*/
				mv.visitInsn(SWAP);	//Bring Int to top
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP); // Rearrange order to ensure division is correct.
				mv.visitInsn(FDIV);
				
				
				
				
			}
		}
		else if(op == Kind.OP_MOD)	// Only accepts integer
		{
			/*int x = 5;
			int y = 2;
			int z;
			z = x%y;*/
			
			mv.visitInsn(IREM);
		}
		else if(op == Kind.OP_POWER)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				/*int x = 2;
				int y = 3;
				int z;
				z = (int)Math.pow((double)x, (double)y);*/				
				mv.visitInsn(I2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(I2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
				
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				mv.visitInsn(F2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(F2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else if(left == Type.FLOAT && right == Type.INTEGER)
			{
				mv.visitInsn(I2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(F2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else if(left == Type.INTEGER && right == Type.FLOAT)
			{
				mv.visitInsn(F2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(I2D);
				mv.visitVarInsn(DLOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
		}
		else if(op == Kind.OP_AND)
		{
			/*int x = 5;
			int y = 2;
			int z;
			z = x&y;*/
			mv.visitInsn(IAND);	// Works for both Bool and Int
		}
		else if(op == Kind.OP_OR)
		{
			/*int x = 5;
			int y = 2;
			int z;
			z = x|y;*/
			mv.visitInsn(IOR);	// Works for both Bool and Int
		}
		else if(op == Kind.OP_EQ)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				mv.visitJumpInsn(IF_ICMPEQ, whenTrue);
			}
			else if(left == Type.BOOLEAN && right == Type.BOOLEAN)
			{
				mv.visitJumpInsn(IF_ICMPEQ, whenTrue);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, whenTrue);
			}
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, whenFalse);
		}
		else if(op == Kind.OP_NEQ)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				mv.visitJumpInsn(IF_ICMPNE, whenTrue);
			}
			else if(left == Type.BOOLEAN && right == Type.BOOLEAN)
			{
				mv.visitJumpInsn(IF_ICMPNE, whenTrue);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFNE, whenTrue);
			}
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, whenFalse);
		}
		else if(op == Kind.OP_LT)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				mv.visitJumpInsn(IF_ICMPLT, whenTrue);
			}
			else if(left == Type.BOOLEAN && right == Type.BOOLEAN)
			{
				mv.visitJumpInsn(IF_ICMPLT, whenTrue);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLT, whenTrue);
			}
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, whenFalse);
		}
		else if(op == Kind.OP_GT)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				mv.visitJumpInsn(IF_ICMPGT, whenTrue);
			}
			else if(left == Type.BOOLEAN && right == Type.BOOLEAN)
			{
				mv.visitJumpInsn(IF_ICMPGT, whenTrue);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGT, whenTrue);
			}
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, whenFalse);
		}
		else if(op == Kind.OP_LE)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				mv.visitJumpInsn(IF_ICMPLE, whenTrue);
			}
			else if(left == Type.BOOLEAN && right == Type.BOOLEAN)
			{
				mv.visitJumpInsn(IF_ICMPLE, whenTrue);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLE, whenTrue);
			}
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, whenFalse);
		}
		else if(op == Kind.OP_GE)
		{
			if(left == Type.INTEGER && right == Type.INTEGER)
			{
				mv.visitJumpInsn(IF_ICMPGE, whenTrue);
			}
			else if(left == Type.BOOLEAN && right == Type.BOOLEAN)
			{
				mv.visitJumpInsn(IF_ICMPGE, whenTrue);
			}
			else if(left == Type.FLOAT && right == Type.FLOAT)
			{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGE, whenTrue);
			}
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, whenFalse);
		}
		mv.visitJumpInsn(GOTO, whenFalse);	// Needed because The non comparison operators come here
		mv.visitLabel(whenTrue);
		mv.visitLdcInsn(1);
		mv.visitLabel(whenFalse);
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		System.out.println("coding Exp Conditional");
		// TODO Auto-generated method stub
		Label whenFalse = new Label();
		Label endFalse = new Label();
		
		expressionConditional.guard.visit(this, arg);
		mv.visitJumpInsn(IFEQ, whenFalse);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, endFalse);
		mv.visitLabel(whenFalse);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitLabel(endFalse);
		
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		System.out.println("coding Float Literal");
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		System.out.println("coding expressionFunctionAppWithExpressionArg");
		// TODO Auto-generated method stub
		Kind kd = expressionFunctionAppWithExpressionArg.function;
		Type tp = expressionFunctionAppWithExpressionArg.e.type;
		
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		
		if(kd == Kind.KW_sin)
		{
			/*float x = 0;
			x = (float) Math.sin(x);*/
			
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(kd == Kind.KW_cos)
		{
			/*float x = 0;
			x = (float) Math.cos(x);*/
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(kd == Kind.KW_atan)
		{
			/*float x = 0;
			x = (float) Math.atan(x);*/
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(kd == Kind.KW_log)
		{
			/*float x = 0;
			x = (float) Math.log(x);*/
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(kd == Kind.KW_abs)
		{
			/*float x = 0;
			x = (float) Math.abs(x);*/ 
			if(tp == Type.FLOAT)
			{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
			}
			else if(tp == Type.INTEGER)
			{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
			}
		}
		else if(kd == Kind.KW_int && tp == Type.FLOAT)
		{
			mv.visitInsn(F2I);
		}
		else if(kd == Kind.KW_float && tp == Type.INTEGER)
		{
			mv.visitInsn(I2F);
		}
		else if(kd == Kind.KW_width)
		{
			//RuntimeImageSupport.getWidth(null);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getWidth", RuntimeImageSupport.getWidthSig, false);
		}
		else if(kd == Kind.KW_height)
		{
			//RuntimeImageSupport.getHeight(null);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getHeight",RuntimeImageSupport.getHeightSig , false);
		}
		else if(kd == Kind.KW_alpha)
		{
			//RuntimePixelOps.getAlpha(0);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getAlpha", RuntimePixelOps.getAlphaSig, false);
		}
		else if(kd == Kind.KW_red)
		{
			//RuntimePixelOps.getRed(0);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getRed", RuntimePixelOps.getRedSig, false);
		}
		else if(kd == Kind.KW_green)
		{
			//RuntimePixelOps.getGreen(0);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getGreen", RuntimePixelOps.getGreenSig, false);
		}
		else if(kd == Kind.KW_blue)
		{
			//RuntimePixelOps.getBlue(0);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getBlue", RuntimePixelOps.getBlueSig, false);
		}
		
		
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		System.out.println("coding expressionFunctionAppWithPixel");
		// TODO Auto-generated method stub
		Kind kd = expressionFunctionAppWithPixel.name;
		
		
		
		if(kd == Kind.KW_cart_x)
		{
			/*float x = 0;
			int result;
			float r = 2;
			result = (int) (r* (float)Math.cos((double)x));*/
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}
		else if(kd == Kind.KW_cart_y)
		{
			/*float x = 0;
			int result;
			float r = 2;
			result = (int) (r* (float)Math.sin((double)x));*/
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}
		else if(kd == Kind.KW_polar_a)
		{
			/*int x = 0;
			float result;
			int y = 2;
			result = (float) (Math.atan2((double)x,(double)y));*/
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			//mv.visitInsn(SWAP);
			//mv.visitVarInsn(DSTORE, 0);
			mv.visitInsn(I2D);
			//mv.visitVarInsn(DLOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
			mv.visitInsn(D2F);
			
			
		}
		else if(kd == Kind.KW_polar_r)
		{
			/*int x = 0;
			float result;
			int y = 2;
			result = (float) (Math.hypot((double)x,(double)y));*/
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);
			//mv.visitVarInsn(DSTORE, 0);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(I2D);
			//mv.visitVarInsn(DLOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
			mv.visitInsn(D2F);
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		System.out.println("coding expressionIdent");
		// TODO Auto-generated method stub
		Type tp = expressionIdent.type;
		int dec = expressionIdent.dec.getSlot();
		
		if(tp == Type.FLOAT)
		{
			mv.visitVarInsn(FLOAD, dec);
		}
		else if(tp == Type.BOOLEAN || tp == Type.INTEGER)
		{
			mv.visitVarInsn(ILOAD, dec);
		}
		else
		{
			mv.visitVarInsn(ALOAD, dec);
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		System.out.println("coding IntegerLiteral");
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("coding expressionPixel");
		int dec = expressionPixel.dec.getSlot();
		
		mv.visitVarInsn(ALOAD, dec);
		expressionPixel.pixelSelector.visit(this, arg);
		//RuntimeImageSupport.getPixel(null, dec, dec);
		//mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getPixel", "(Ljava/awt/image/BufferedImage;II)I", false);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getPixel",RuntimeImageSupport.getPixelSig , false);
		
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		System.out.println("coding expressionPixelConstructor");
		// TODO Auto-generated method stub
		
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		
		//RuntimePixelOps.makePixel(defaultHeight, defaultHeight, defaultHeight, defaultHeight);
		//mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "makePixel", "(IIII)I", false);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "makePixel",RuntimePixelOps.makePixelSig , false);
		
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		System.out.println("coding expressionPredefinedName");
		// TODO Auto-generated method stub
		Kind name = expressionPredefinedName.name;
		
		if(name == Kind.KW_Z)
		{
			mv.visitLdcInsn(Z);
		}
		else if(name == Kind.KW_default_height)
		{
			mv.visitLdcInsn(defaultHeight);
		}
		else if(name == Kind.KW_default_width)
		{
			mv.visitLdcInsn(defaultWidth);
		}
		
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		System.out.println("coding visitExpressionUnary");
		// TODO Auto-generated method stub
		Kind op = expressionUnary.op;
		Type tp = expressionUnary.expression.type;
		Label setFalse = new Label();
		Label endExcl = new Label();
		
		expressionUnary.expression.visit(this, arg);

		if(op == Kind.OP_MINUS)
		{
			if(tp == Type.FLOAT)
			{
				mv.visitInsn(FNEG);
			}
			else if(tp == Type.INTEGER)
			{
				mv.visitInsn(INEG);
			}
		}
		else if(op == Kind.OP_EXCLAMATION)
		{
			if(tp == Type.INTEGER)
			{
				mv.visitInsn(ICONST_M1);
				mv.visitInsn(IXOR);
			}
			else if(tp == Type.BOOLEAN)
			{
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(IF_ICMPEQ, setFalse);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, endExcl);
				mv.visitLabel(setFalse);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(endExcl);
			}
		}
		else if(op == Kind.OP_PLUS)
		{
			
		}
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		System.out.println("coding lhsIdent");
		// TODO Auto-generated method stub
		Type tp = lhsIdent.type;
		int dec = lhsIdent.dec.getSlot();
		
		if(tp == Type.IMAGE)
		{
			//RuntimeImageSupport.deepCopy(null);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "deepCopy", RuntimeImageSupport.deepCopySig, false);
			mv.visitVarInsn(ASTORE, dec);
		}
		else if(tp == Type.FLOAT)
		{
			mv.visitVarInsn(FSTORE, dec);
		}
		else if(tp == Type.BOOLEAN || tp == Type.INTEGER)
		{
			mv.visitVarInsn(ISTORE, dec);
		}
		else if(tp == Type.FILE)
		{
			mv.visitVarInsn(ASTORE, dec);
		}
		
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		System.out.println("coding LHSPixel");
		// TODO Auto-generated method stub
		int dec = lhsPixel.dec.getSlot();
		mv.visitVarInsn(ALOAD, dec);
		lhsPixel.pixelSelector.visit(this, arg);
		//RuntimeImageSupport.setPixel(dec, null, dec, dec);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "setPixel", RuntimeImageSupport.setPixelSig, false);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		System.out.println("coding LHSSample");
		// TODO Auto-generated method stub
		int dec = lhsSample.dec.getSlot();
		Kind col = lhsSample.color;
		mv.visitVarInsn(ALOAD, dec);
		lhsSample.pixelSelector.visit(this, arg);
		
		if(col == Kind.KW_alpha)
		{
			mv.visitLdcInsn(RuntimePixelOps.ALPHA);
		}
		else if(col == Kind.KW_red)
		{
			mv.visitLdcInsn(RuntimePixelOps.RED);
		}
		else if(col == Kind.KW_green)
		{
			mv.visitLdcInsn(RuntimePixelOps.GREEN);
		}
		else if(col == Kind.KW_blue)
		{
			mv.visitLdcInsn(RuntimePixelOps.BLUE);
		}
		//RuntimeImageSupport.updatePixelColor(dec, null, dec, dec, dec);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		System.out.println("coding LHSPixelSelector");
		// TODO Auto-generated method stub
		Type tpx = pixelSelector.ex.getType();
		Type tpy = pixelSelector.ey.getType();
		
		if(tpx == Type.FLOAT && tpy == Type.FLOAT)
		{
			pixelSelector.ex.visit(this, arg);
			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
			
			pixelSelector.ex.visit(this, arg);
			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}
		else
		{
			pixelSelector.ex.visit(this, arg);
			pixelSelector.ey.visit(this, arg);
		}
		
		
		
		return null;
		
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		System.out.println("coding Program");
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		System.out.println("coding StatementAssign");
		// TODO Auto-generated method stub
		
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		/*int x =0;
		if(x>0)
		{
			
		}*/
		
		// TODO Auto-generated method stub
		System.out.println("coding StatementIf");
		Label ifBegin = new Label();
		Label ifEnd = new Label();
		Label ifBody = new Label();
		
		statementIf.guard.visit(this, arg);
		mv.visitJumpInsn(IFEQ, ifBody);
		
		mv.visitLabel(ifBegin);
		statementIf.b.visit(this, arg);
		mv.visitLabel(ifEnd);
		
		mv.visitLabel(ifBody);
		
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		System.out.println("coding StatementInput");
		Kind kd = statementInput.dec.type;
		Type tp = Types.getType(kd);
		int dec = statementInput.dec.getSlot();
		Expression height = statementInput.dec.height;
		Expression width = statementInput.dec.width;
		
		if(kd == Kind.KW_boolean)
		{
			mv.visitVarInsn(ALOAD, 0);
			statementInput.e.visit(this, arg);
			mv.visitInsn(AALOAD);
			//Boolean.parseBoolean(classDesc);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ISTORE, dec);
		}
		else if(kd == Kind.KW_int)
		{
			mv.visitVarInsn(ALOAD, 0);
			statementInput.e.visit(this, arg);
			mv.visitInsn(AALOAD);
			//Integer.parseInt(classDesc);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitVarInsn(ISTORE, dec);
		}
		else if(kd == Kind.KW_float)
		{
			mv.visitVarInsn(ALOAD, 0);
			statementInput.e.visit(this, arg);
			mv.visitInsn(AALOAD);
			//Float.parseFloat(classDesc);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
			mv.visitVarInsn(FSTORE,dec);
		}
		else if(kd == Kind.KW_filename)
		{
			mv.visitVarInsn(ALOAD, 0);
			statementInput.e.visit(this, arg);
			mv.visitInsn(AALOAD);
			mv.visitVarInsn(ASTORE,dec);
			
		}
		else if(kd == Kind.KW_image)
		{
			mv.visitVarInsn(ALOAD, 0);
			statementInput.e.visit(this, arg);
			mv.visitInsn(AALOAD);
			
			if(height != null && width != null)
			{	// CHECK THIS!!!
				statementInput.dec.width.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
				
				statementInput.dec.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
				
			}
			else
			{
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			//RuntimeImageSupport.readImage(classDesc, dec, dec);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "readImage", RuntimeImageSupport.readImageSig, false);
			mv.visitVarInsn(ASTORE, dec);
		}
		
		
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		System.out.println("Coding Show Statement");
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				//CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
				
			}
			 break; 
			case FLOAT : {
				//CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
			}
			 break; 
			case IMAGE : {
				
				// TODO implement functionality
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				//mv.visitInsn(Opcodes.SWAP);
				//RuntimeImageSupport.makeFrame(null);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeFrame", RuntimeImageSupport.makeFrameSig, false);
				mv.visitInsn(POP);
			}
			break;
			case FILE: {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Ljava/lang/String)V", false);
			}
			break;

		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		System.out.println("coding StatementSleep");
		//int i = 1000;
		//Thread.sleep(i);
		// TODO Auto-generated method stub
		statementSleep.duration.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		System.out.println("coding StatementWhile");
		// TODO Auto-generated method stub
		
		Label guard = new Label();
		mv.visitJumpInsn(GOTO, guard);
		Label body = new Label();
		mv.visitLabel(body);
		statementWhile.b.visit(this, arg);
		mv.visitLabel(guard);
		statementWhile.guard.visit(this, arg);
		mv.visitJumpInsn(IFNE, body);
		
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		System.out.println("coding StatementWrite");
		// TODO Auto-generated method stub
		int sourceDec = statementWrite.sourceDec.getSlot();
		int destDec = statementWrite.destDec.getSlot();
		
		mv.visitVarInsn(ALOAD, sourceDec);
		mv.visitVarInsn(ALOAD, destDec);
		
		//RuntimeImageSupport.write(null, classDesc);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "write", RuntimeImageSupport.writeSig, false);

		
		return null;

	}

}
