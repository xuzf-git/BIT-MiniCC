package bit.minisys.minicc.parser.antlr;

import bit.minisys.minicc.parser.ast.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.*;

public class MiniCCListener extends CBaseListener{
    private ParseTreeProperty astMap = new ParseTreeProperty();
    private ASTCompilationUnit compilationUnit;

    public ASTCompilationUnit getAST() {
        return this.compilationUnit;
    }

    @Override public void exitStart(CParser.StartContext ctx) {
        ASTCompilationUnit.Builder builder = new ASTCompilationUnit.Builder();
        if (ctx.translationUnit() != null) {
            builder.addNode(this.astMap.get(ctx.translationUnit()));
        }
        this.compilationUnit = builder.build();
        this.astMap.put(ctx, this.compilationUnit);
    }

    @Override public void exitTranslationUnit(CParser.TranslationUnitContext ctx) {
        List<ASTNode> ast = new LinkedList<>();
        if (ctx.translationUnit() != null) {
            ast.addAll((Collection<? extends ASTNode>) this.astMap.get(ctx.translationUnit()));
        }
        ast.add((ASTNode) this.astMap.get(ctx.externalDeclaration()));
        this.astMap.put(ctx, ast);
    }

    @Override public void exitExternalDeclaration(CParser.ExternalDeclarationContext ctx) {
        this.astMap.put(ctx, this.astMap.get(ctx.getChild(0)));
    }

    @Override public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        ASTFunctionDefine.Builder builder = new ASTFunctionDefine.Builder();
        if (ctx.declarationSpecifierList() != null) {
            builder.addSpecifiers(this.astMap.get(ctx.declarationSpecifierList()));
        }
        builder.setDeclarator((ASTDeclarator)this.astMap.get(ctx.declarator()));
        builder.setBody((ASTCompoundStatement)this.astMap.get(ctx.compoundStatement()));
        this.astMap.put(ctx, builder.build());
    }

    @Override public void exitDeclarationSpecifierList(CParser.DeclarationSpecifierListContext ctx) {
        List<ASTNode> ast = new LinkedList<>();
        ast.add((ASTNode) this.astMap.get(ctx.getChild(0)));
        if (ctx.declarationSpecifierList() != null) {
            ast.addAll((Collection<? extends ASTNode>) this.astMap.get(ctx.declarationSpecifierList()));
        }
        this.astMap.put(ctx, ast);
    }

    @Override public void exitDeclaration(CParser.DeclarationContext ctx) {
        ASTDeclaration.Builder builder = new ASTDeclaration.Builder();
        builder.addSpecfiers(this.astMap.get(ctx.declarationSpecifierList()));
        if (ctx.initDeclaratorList() != null) {
            builder.addInitList(this.astMap.get(ctx.initDeclaratorList()));
        }
        this.astMap.put(ctx, builder.build());
    }

    @Override public void exitDeclarator(CParser.DeclaratorContext ctx) {
        this.astMap.put(ctx, this.astMap.get(ctx.directDeclarator()));
    }

    @Override public void exitStorageClassSpecifier(CParser.StorageClassSpecifierContext ctx) {
        if (ctx.Typedef() != null){
            this.astMap.put(ctx, new ASTToken("typedef", ctx.Typedef().getSymbol().getTokenIndex()));
        } else if (ctx.Extern() != null){
            this.astMap.put(ctx, new ASTToken("extern", ctx.Extern().getSymbol().getTokenIndex()));
        } else if (ctx.Static() != null){
            this.astMap.put(ctx, new ASTToken("static", ctx.Static().getSymbol().getTokenIndex()));
        } else if (ctx.Auto() != null){
            this.astMap.put(ctx, new ASTToken("auto", ctx.Auto().getSymbol().getTokenIndex()));
        }
    }

    @Override public void exitTypeSpecifier(CParser.TypeSpecifierContext ctx) {
        if (ctx.Void() != null){
            this.astMap.put(ctx, new ASTToken("void", ctx.Void().getSymbol().getTokenIndex()));
        } else if (ctx.Char() != null){
            this.astMap.put(ctx, new ASTToken("char", ctx.Char().getSymbol().getTokenIndex()));
        } else if (ctx.Short() != null){
            this.astMap.put(ctx, new ASTToken("short", ctx.Short().getSymbol().getTokenIndex()));
        } else if (ctx.Int() != null){
            this.astMap.put(ctx, new ASTToken("int", ctx.Int().getSymbol().getTokenIndex()));
        } else if (ctx.Long() != null){
            this.astMap.put(ctx, new ASTToken("long", ctx.Long().getSymbol().getTokenIndex()));
        } else if (ctx.Float() != null){
            this.astMap.put(ctx, new ASTToken("float", ctx.Float().getSymbol().getTokenIndex()));
        } else if (ctx.Double() != null){
            this.astMap.put(ctx, new ASTToken("double", ctx.Double().getSymbol().getTokenIndex()));
        } else if (ctx.Signed() != null){
            this.astMap.put(ctx, new ASTToken("signed", ctx.Signed().getSymbol().getTokenIndex()));
        } else if (ctx.Unsigned() != null){
            this.astMap.put(ctx, new ASTToken("unsigned", ctx.Unsigned().getSymbol().getTokenIndex()));
        }
        // TODO: remove typedef
    }

    @Override public void exitTypeQualifier(CParser.TypeQualifierContext ctx) {
        if (ctx.Const() != null){
            this.astMap.put(ctx, new ASTToken("const", ctx.Const().getSymbol().getTokenIndex()));
        }
        else if (ctx.Restrict() != null){
            this.astMap.put(ctx, new ASTToken("restrict", ctx.Restrict().getSymbol().getTokenIndex()));
        }
        else if (ctx.Volatile() != null){
            this.astMap.put(ctx, new ASTToken("volatile", ctx.Volatile().getSymbol().getTokenIndex()));
        }
    }

    @Override public void exitFunctionSpecifier(CParser.FunctionSpecifierContext ctx) {
        this.astMap.put(ctx, new ASTToken("inline", ctx.Inline().getSymbol().getTokenIndex()));
    }

    @Override public void exitDirectDeclarator(CParser.DirectDeclaratorContext ctx) {
        CParser.DirectDeclaratorContext nestedNode = ctx.directDeclarator();
        /* 变量声明 */
        if (nestedNode == null){
            ASTIdentifier id = new ASTIdentifier(ctx.Identifier(0).getText(), ctx.Identifier(0).getSymbol().getTokenIndex());
            this.astMap.put(ctx, new ASTVariableDeclarator(id));
        }
        /* 函数声明 */
        else if (ctx.getChild(1).getText().equals("(")){
            ASTFunctionDeclarator.Builder funcBuilder = new ASTFunctionDeclarator.Builder();
            funcBuilder.setDecl((ASTDeclarator) this.astMap.get(nestedNode));
            if (ctx.parameterTypeList() != null){
                funcBuilder.addParams(this.astMap.get(ctx.parameterTypeList()));
            }
            this.astMap.put(ctx, funcBuilder.build());
        }
        /* 数组声明 */
        else if (ctx.getChild(1).getText().equals("[")) {
            ASTArrayDeclarator.Builder arrayBuilder = new ASTArrayDeclarator.Builder();
            arrayBuilder.setDecl((ASTDeclarator) this.astMap.get(nestedNode));
            if (ctx.assignmentExpression() != null) {
                arrayBuilder.setExprs(this.astMap.get(ctx.assignmentExpression()));
            }
            this.astMap.put(ctx, arrayBuilder.build());
        }
    }

    @Override public void exitParameterTypeList(CParser.ParameterTypeListContext ctx) {
        this.astMap.put(ctx, this.astMap.get(ctx.parameterList()));
    }

    @Override public void exitParameterList(CParser.ParameterListContext ctx) {
        List<ASTNode> ast = new LinkedList<>();
        if (ctx.parameterList() != null) {
            ast.addAll((Collection<? extends ASTNode>) this.astMap.get(ctx.parameterList()));
        }
        ast.add((ASTNode) this.astMap.get(ctx.parameterDeclaration()));
        this.astMap.put(ctx, ast);
    }

    @Override public void exitParameterDeclaration(CParser.ParameterDeclarationContext ctx) {
        ASTParamsDeclarator.Builder builder = new ASTParamsDeclarator.Builder();
        builder.addSpecfiers(this.astMap.get(ctx.declarationSpecifierList()));
        builder.setDeclarator((ASTDeclarator)this.astMap.get(ctx.declarator()));
        this.astMap.put(ctx, builder.build());
    }

    @Override public void exitInitDeclaratorList(CParser.InitDeclaratorListContext ctx) {
        List<ASTNode> ast = new LinkedList<>();
        if (ctx.initDeclaratorList() != null) {
            ast.addAll((Collection<? extends ASTNode>) this.astMap.get(ctx.initDeclaratorList()));
        }
        ast.add((ASTNode) this.astMap.get(ctx.initDeclarator()));
        this.astMap.put(ctx, ast);
    }

    @Override public void exitInitDeclarator(CParser.InitDeclaratorContext ctx) {
        ASTInitList.Builder builder = new ASTInitList.Builder();
        builder.setDeclarator((ASTDeclarator) this.astMap.get(ctx.declarator()));
        if (ctx.initializer() != null) {
            builder.addInitialize(this.astMap.get(ctx.initializer()));
        }
        this.astMap.put(ctx, builder.build());
    }

    @Override public void exitInitializer(CParser.InitializerContext ctx) {
        if (ctx.assignmentExpression() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.assignmentExpression()));
        }
        else {
            this.astMap.put(ctx, this.astMap.get(ctx.initializerList()));
        }
    }

    @Override public void exitInitializerList(CParser.InitializerListContext ctx) {
        List<ASTExpression> ast = new LinkedList<>();
        if (ctx.initializerList() != null) {
            ast.addAll((Collection<? extends ASTExpression>) this.astMap.get(ctx.initializerList()));
        }
        ast.add((ASTExpression) this.astMap.get(ctx.initializer()));
        this.astMap.put(ctx, ast);
    }

    @Override public void exitStatement(CParser.StatementContext ctx) {
        if (ctx.labeledStatement() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.labeledStatement()));
        } else if (ctx.compoundStatement() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.compoundStatement()));
        } else if (ctx.expressionStatement() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.expressionStatement()));
        } else if (ctx.selectionStatement() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.selectionStatement()));
        } else if (ctx.iterationStatement() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.iterationStatement()));
        } else if (ctx.jumpStatement() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.jumpStatement()));
        }
    }

    @Override public void exitLabeledStatement(CParser.LabeledStatementContext ctx) {
        ASTLabeledStatement node;
        ASTIdentifier label = new ASTIdentifier(ctx.Identifier().getText(), ctx.Identifier().getSymbol().getTokenIndex());
        ASTStatement stat = (ASTStatement) this.astMap.get(ctx.statement());
        node = new ASTLabeledStatement(label, stat);
        this.astMap.put(ctx, node);
    }

    @Override public void exitCompoundStatement(CParser.CompoundStatementContext ctx) {
        ASTCompoundStatement node;
        if (ctx.blockItemList() != null) {
            node = new ASTCompoundStatement((List<ASTNode>) this.astMap.get(ctx.blockItemList()));
        } else {
            node = new ASTCompoundStatement();
        }
        this.astMap.put(ctx, node);
    }

    @Override public void exitBlockItemList(CParser.BlockItemListContext ctx) {
        List<ASTNode> ast = new LinkedList<>();
        if (ctx.blockItemList() != null) {
            ast.addAll((Collection<? extends ASTNode>) this.astMap.get(ctx.blockItemList()));
        }
        ast.add((ASTNode) this.astMap.get(ctx.blockItem()));
        this.astMap.put(ctx, ast);
    }

    @Override public void exitBlockItem(CParser.BlockItemContext ctx) {
        if (ctx.statement() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.statement()));
        } else {
            this.astMap.put(ctx, this.astMap.get(ctx.declaration()));
        }
    }

    @Override public void exitExpressionStatement(CParser.ExpressionStatementContext ctx) {
        ASTExpressionStatement node = null;
        if (ctx.expression() != null) {
            node = new ASTExpressionStatement((List) this.astMap.get(ctx.expression()));
        }
        this.astMap.put(ctx, node);
    }

    @Override public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        ASTSelectionStatement node;
        LinkedList<ASTExpression> cond = (LinkedList<ASTExpression>) this.astMap.get(ctx.expression());
        ASTStatement then = (ASTStatement) this.astMap.get(ctx.statement(0));
        ASTStatement otherwise = null;
        if (ctx.Else() != null) {
            otherwise = (ASTStatement) this.astMap.get(ctx.statement(1));
        }
        node = new ASTSelectionStatement(cond, then, otherwise);
        this.astMap.put(ctx, node);
    }

    @Override public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        LinkedList<ASTExpression> cond = null;
        LinkedList<ASTExpression> step = null;
        ASTStatement stat = null;
        if (ctx.declaration() != null) {
            ASTIterationDeclaredStatement node;
            ASTDeclaration init = (ASTDeclaration) this.astMap.get(ctx.declaration());

            switch (ctx.getChildCount()) {
                case 8:
                    cond = (LinkedList) this.astMap.get(ctx.expression(0));
                    step = (LinkedList) this.astMap.get(ctx.expression(1));
                    break;
                case 7:
                    if (!ctx.getChild(3).getText().equals(";")) {
                        step = (LinkedList) this.astMap.get(ctx.expression(0));
                    } else {
                        cond = (LinkedList) this.astMap.get(ctx.expression(0));
                    }
                    break;
            }
            stat = (ASTStatement) this.astMap.get(ctx.statement());
            node = new ASTIterationDeclaredStatement(init, cond, step, stat);
            this.astMap.put(ctx, node);
        } else {
            ASTIterationStatement node;
            LinkedList<ASTExpression> init = null;

            switch (ctx.getChildCount()) {
                case 9:
                    init = (LinkedList) this.astMap.get(ctx.expression(0));
                    cond = (LinkedList) this.astMap.get(ctx.expression(1));
                    step = (LinkedList) this.astMap.get(ctx.expression(2));
                    break;
                case 8:
                    if (ctx.getChild(2).getText().equals(";")) {
                        cond = (LinkedList) this.astMap.get(ctx.expression(0));
                        step = (LinkedList) this.astMap.get(ctx.expression(1));
                    } else if (ctx.getChild(4).getText().equals(";")) {
                        init = (LinkedList) this.astMap.get(ctx.expression(0));
                        step = (LinkedList) this.astMap.get(ctx.expression(1));
                    } else {
                        init = (LinkedList) this.astMap.get(ctx.expression(0));
                        cond = (LinkedList) this.astMap.get(ctx.expression(1));
                    }
                    break;
                case 7:
                    if (!ctx.getChild(2).getText().equals(";")) {
                        init = (LinkedList) this.astMap.get(ctx.expression(0));
                    } else if (!ctx.getChild(3).getText().equals(";")) {
                        cond = (LinkedList) this.astMap.get(ctx.expression(0));
                    } else {
                        step = (LinkedList) this.astMap.get(ctx.expression(0));
                    }
                    break;
            }
            stat = (ASTStatement) this.astMap.get(ctx.statement());
            node = new ASTIterationStatement(init, cond, step, stat);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitJumpStatement(CParser.JumpStatementContext ctx) {
        if (ctx.Goto() != null) {
            ASTIdentifier label = new ASTIdentifier(ctx.Identifier().getText(), ctx.Identifier().getSymbol().getTokenIndex());
            ASTGotoStatement node = new ASTGotoStatement(label);
            this.astMap.put(ctx, node);
        } else if (ctx.Continue() != null) {
            ASTContinueStatement node = new ASTContinueStatement();
            this.astMap.put(ctx, node);
        } else if (ctx.Break() != null) {
            ASTBreakStatement node = new ASTBreakStatement();
            this.astMap.put(ctx, node);
        } else if (ctx.Return() != null) {
            ASTReturnStatement node;
            LinkedList<ASTExpression> exprs = null;
            if (ctx.expression() != null) {
                 exprs = (LinkedList)this.astMap.get(ctx.expression());
            }
            node = new ASTReturnStatement(exprs);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitPrimaryExpression(CParser.PrimaryExpressionContext ctx) {
        if (ctx.Identifier() != null) {
            ASTIdentifier node = new ASTIdentifier(ctx.Identifier().getText(), ctx.Identifier().getSymbol().getTokenIndex());
            this.astMap.put(ctx, node);
        } else if (ctx.IntConstant() != null) {
            Integer value = Integer.parseInt(ctx.IntConstant().getText());
            Integer tokenId = ctx.IntConstant().getSymbol().getTokenIndex();
            ASTIntegerConstant node = new ASTIntegerConstant(value, tokenId);
            this.astMap.put(ctx, node);
        } else if (ctx.FloatConstant() != null) {
            Double value = Double.parseDouble(ctx.FloatConstant().getText());
            Integer tokenId = ctx.FloatConstant().getSymbol().getTokenIndex();
            ASTFloatConstant node = new ASTFloatConstant(value, tokenId);
            this.astMap.put(ctx, node);
        } else if (ctx.CharConstant() != null) {
            Integer tokenId = ctx.CharConstant().getSymbol().getTokenIndex();
            ASTCharConstant node = new ASTCharConstant(ctx.CharConstant().getText(), tokenId);
            this.astMap.put(ctx, node);
        } else if (ctx.StringLiteral(0) != null) {
            Integer tokenId = ctx.StringLiteral(0).getSymbol().getTokenIndex();
            ASTStringConstant node = new ASTStringConstant(ctx.StringLiteral(0).getText(), tokenId);
            this.astMap.put(ctx, node);
        } else {
            this.astMap.put(ctx, this.astMap.get(ctx.expression()));
        }
    }

    @Override public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        if (ctx.primaryExpression() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.primaryExpression()));
        } else if (ctx.postfixExpression() != null) {
            switch (ctx.getChild(1).getText()) {
                case "[":
                    ASTArrayAccess.Builder arrayCallBuilder = new ASTArrayAccess.Builder();
                    arrayCallBuilder.setArrayName((ASTExpression) this.astMap.get(ctx.postfixExpression()));
                    arrayCallBuilder.addElement(this.astMap.get(ctx.expression()));
                    this.astMap.put(ctx, arrayCallBuilder.build());
                    break;
                case "(":
                    ASTFunctionCall.Builder funcCallBuilder = new ASTFunctionCall.Builder();
                    funcCallBuilder.setName((ASTExpression) this.astMap.get(ctx.postfixExpression()));
                    if (ctx.argumentExpressionList() != null) {
                        funcCallBuilder.addArg(this.astMap.get(ctx.argumentExpressionList()));
                    }
                    this.astMap.put(ctx, funcCallBuilder.build());
                    break;
                case ".":
                    ASTMemberAccess dotNode;
                    ASTToken dot = new ASTToken(".", ctx.Dot().getSymbol().getTokenIndex());
                    ASTExpression dotMaster = (ASTExpression) this.astMap.get(ctx.postfixExpression());
                    ASTIdentifier dotMember = new ASTIdentifier(ctx.Identifier().getText(), ctx.Identifier().getSymbol().getTokenIndex());
                    dotNode = new ASTMemberAccess(dotMaster, dot, dotMember);
                    this.astMap.put(ctx, dotNode);
                    break;
                case "->":
                    ASTMemberAccess arrowNode;
                    ASTToken arrow = new ASTToken("->", ctx.Arrow().getSymbol().getTokenIndex());
                    ASTExpression arrowMaster = (ASTExpression) this.astMap.get(ctx.postfixExpression());
                    ASTIdentifier arrowMember = new ASTIdentifier(ctx.Identifier().getText(), ctx.Identifier().getSymbol().getTokenIndex());
                    arrowNode = new ASTMemberAccess(arrowMaster, arrow, arrowMember);
                    this.astMap.put(ctx, arrowNode);
                    break;
                case "++":
                    ASTPostfixExpression incPostNode;
                    ASTExpression incPostExpr = (ASTExpression) this.astMap.get(ctx.postfixExpression());
                    ASTToken inc = new ASTToken("++", ctx.PlusPlus().getSymbol().getTokenIndex());
                    incPostNode = new ASTPostfixExpression(incPostExpr, inc);
                    this.astMap.put(ctx, incPostNode);
                    break;
                case "--":
                    ASTPostfixExpression decPostNode;
                    ASTExpression decPostExpr = (ASTExpression) this.astMap.get(ctx.postfixExpression());
                    ASTToken dec = new ASTToken("--", ctx.MinusMinus().getSymbol().getTokenIndex());
                    decPostNode = new ASTPostfixExpression(decPostExpr, dec);
                    this.astMap.put(ctx, decPostNode);
                    break;
            }
        }
    }

    @Override public void exitArgumentExpressionList(CParser.ArgumentExpressionListContext ctx) {
        List<ASTNode> ast = new LinkedList<>();
        if (ctx.argumentExpressionList() != null) {
            ast.addAll((Collection<? extends ASTNode>) this.astMap.get(ctx.argumentExpressionList()));
        }
        ast.add((ASTNode) this.astMap.get(ctx.assignmentExpression()));
        this.astMap.put(ctx, ast);
    }

    @Override public void exitUnaryExpression(CParser.UnaryExpressionContext ctx) {
        ASTUnaryExpression node;
        ASTToken op = null;
        ASTExpression expr = null;

        if (ctx.PlusPlus() != null) {
            op = new ASTToken("++", ctx.PlusPlus().getSymbol().getTokenIndex());
            expr = (ASTExpression) this.astMap.get(ctx.unaryExpression());
        } else if (ctx.MinusMinus() != null) {
            op = new ASTToken("--", ctx.MinusMinus().getSymbol().getTokenIndex());
            expr = (ASTExpression) this.astMap.get(ctx.unaryExpression());
        } else if (ctx.unaryOpeartor() != null) {
            op = (ASTToken) this.astMap.get(ctx.unaryOpeartor());
            expr = (ASTExpression) this.astMap.get(ctx.castExpression());
        } else if (ctx.Sizeof() != null && ctx.typeName() == null) {
            op = new ASTToken("sizeof", ctx.Sizeof().getSymbol().getTokenIndex());
            expr = (ASTExpression) this.astMap.get(ctx.unaryExpression());
        }

        if (ctx.postfixExpression() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.postfixExpression()));
        } else if (ctx.typeName() == null) {
            node = new ASTUnaryExpression(op, expr);
            this.astMap.put(ctx, node);
        } else {
            op = new ASTToken("sizeof", ctx.Sizeof().getSymbol().getTokenIndex());
            ASTTypename typename = (ASTTypename) this.astMap.get(ctx.typeName());
            ASTUnaryTypename unaryTypename = new ASTUnaryTypename(op, typename);
            this.astMap.put(ctx, unaryTypename);
        }
    }

    @Override public void exitUnaryOpeartor(CParser.UnaryOpeartorContext ctx) {
        ASTToken op = null;
        switch (ctx.getText()) {
            case "&":
                op = new ASTToken("&", ctx.And().getSymbol().getTokenIndex());
                break;
            case "*":
                op = new ASTToken("*", ctx.Star().getSymbol().getTokenIndex());
                break;
            case "+":
                op = new ASTToken("+", ctx.Plus().getSymbol().getTokenIndex());
                break;
            case "-":
                op = new ASTToken("-", ctx.Minus().getSymbol().getTokenIndex());
                break;
            case "~":
                op = new ASTToken("~", ctx.Tilde().getSymbol().getTokenIndex());
                break;
            case "!":
                op = new ASTToken("!", ctx.Not().getSymbol().getTokenIndex());
                break;
        }
        this.astMap.put(ctx, op);
    }

    @Override public void exitCastExpression(CParser.CastExpressionContext ctx) {
        if (ctx.unaryExpression() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.unaryExpression()));
        } else {
            ASTCastExpression node;
            ASTTypename typename = (ASTTypename) this.astMap.get(ctx.typeName());
            ASTExpression expr = (ASTExpression) this.astMap.get(ctx.castExpression());
            node = new ASTCastExpression(typename, expr);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitMultiplicationExpression(CParser.MultiplicationExpressionContext ctx) {
        if (ctx.multiplicationExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.castExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = null;
            if (ctx.Star() != null) {
                op = new ASTToken("*", ctx.Star().getSymbol().getTokenIndex());
            } else if (ctx.Div() != null) {
                op = new ASTToken("/", ctx.Div().getSymbol().getTokenIndex());
            } else if (ctx.Mod() != null) {
                op = new ASTToken("%", ctx.Mod().getSymbol().getTokenIndex());
            }
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.multiplicationExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.castExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitAddExpression(CParser.AddExpressionContext ctx) {
        if (ctx.addExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.multiplicationExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = null;
            if (ctx.Plus() != null) {
                op = new ASTToken("+", ctx.Plus().getSymbol().getTokenIndex());
            } else if (ctx.Minus() != null) {
                op = new ASTToken("-", ctx.Minus().getSymbol().getTokenIndex());
            }
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.addExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.multiplicationExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitShiftExpression(CParser.ShiftExpressionContext ctx) {
        if (ctx.shiftExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.addExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = null;
            if (ctx.LeftShift() != null) {
                op = new ASTToken("<<", ctx.LeftShift().getSymbol().getTokenIndex());
            } else if (ctx.RightShift() != null) {
                op = new ASTToken(">>", ctx.RightShift().getSymbol().getTokenIndex());
            }
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.shiftExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.addExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitRelationExpression(CParser.RelationExpressionContext ctx) {
        if (ctx.relationExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.shiftExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = null;
            if (ctx.Less() != null) {
                op = new ASTToken("<", ctx.Less().getSymbol().getTokenIndex());
            } else if (ctx.Greater() != null) {
                op = new ASTToken(">", ctx.Greater().getSymbol().getTokenIndex());
            } else if (ctx.LessEqual() != null) {
                op = new ASTToken("<=", ctx.LessEqual().getSymbol().getTokenIndex());
            } else if (ctx.GreaterEqual() != null) {
                op = new ASTToken(">=", ctx.GreaterEqual().getSymbol().getTokenIndex());
            }
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.relationExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.shiftExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitEqualExpression(CParser.EqualExpressionContext ctx) {
        if (ctx.equalExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.relationExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = null;
            if (ctx.Equal() != null) {
                op = new ASTToken("==", ctx.Equal().getSymbol().getTokenIndex());
            } else if (ctx.NotEqual() != null) {
                op = new ASTToken("!=", ctx.NotEqual().getSymbol().getTokenIndex());
            }
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.equalExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.relationExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitAndExpression(CParser.AndExpressionContext ctx) {
        if (ctx.andExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.equalExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = new ASTToken("&", ctx.And().getSymbol().getTokenIndex());
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.andExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.equalExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitXorExpression(CParser.XorExpressionContext ctx) {
        if (ctx.xorExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.andExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = new ASTToken("^", ctx.Caret().getSymbol().getTokenIndex());
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.xorExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.andExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitInOrExpression(CParser.InOrExpressionContext ctx) {
        if (ctx.inOrExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.xorExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = new ASTToken("|", ctx.Or().getSymbol().getTokenIndex());
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.inOrExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.xorExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitLogicalAndExpression(CParser.LogicalAndExpressionContext ctx) {
        if (ctx.logicalAndExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.inOrExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = new ASTToken("&&", ctx.AndAnd().getSymbol().getTokenIndex());
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.logicalAndExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.inOrExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitLogicalOrExpression(CParser.LogicalOrExpressionContext ctx) {
        if (ctx.logicalOrExpression() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.logicalAndExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = new ASTToken("||", ctx.OrOr().getSymbol().getTokenIndex());
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.logicalOrExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.logicalAndExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitConditionExpression(CParser.ConditionExpressionContext ctx) {
        if (ctx.Question() == null) {
            this.astMap.put(ctx, this.astMap.get(ctx.logicalOrExpression()));
        } else {
            ASTConditionExpression node;
            ASTExpression condExpr = (ASTExpression) this.astMap.get(ctx.logicalOrExpression());
            LinkedList<ASTExpression> trueExpr = (LinkedList<ASTExpression>) this.astMap.get(ctx.expression());
            ASTExpression falseExpr = (ASTExpression) this.astMap.get(ctx.conditionExpression());
            node = new ASTConditionExpression(condExpr, trueExpr, falseExpr);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        if (ctx.conditionExpression() != null) {
            this.astMap.put(ctx, this.astMap.get(ctx.conditionExpression()));
        } else {
            ASTBinaryExpression node;
            ASTToken op = (ASTToken) this.astMap.get(ctx.assignmentOpeartor());
            ASTExpression expr1 = (ASTExpression) this.astMap.get(ctx.unaryExpression());
            ASTExpression expr2 = (ASTExpression) this.astMap.get(ctx.assignmentExpression());
            node = new ASTBinaryExpression(op, expr1, expr2);
            this.astMap.put(ctx, node);
        }
    }

    @Override public void exitAssignmentOpeartor(CParser.AssignmentOpeartorContext ctx) {
        ASTToken op = null;
        if (ctx.Assign() != null) {
            op = new ASTToken("=", ctx.Assign().getSymbol().getTokenIndex());
        } else if (ctx.StarAssign() != null) {
            op = new ASTToken("*=", ctx.StarAssign().getSymbol().getTokenIndex());
        } else if (ctx.DivAssign() != null) {
            op = new ASTToken("/=", ctx.DivAssign().getSymbol().getTokenIndex());
        } else if (ctx.ModAssign() != null) {
            op = new ASTToken("%=", ctx.ModAssign().getSymbol().getTokenIndex());
        } else if (ctx.PlusAssign() != null) {
            op = new ASTToken("+=", ctx.PlusAssign().getSymbol().getTokenIndex());
        } else if (ctx.MinusAssign() != null) {
            op = new ASTToken("-=", ctx.MinusAssign().getSymbol().getTokenIndex());
        } else if (ctx.LeftShiftAssign() != null) {
            op = new ASTToken("<<=", ctx.LeftShiftAssign().getSymbol().getTokenIndex());
        } else if (ctx.RightShiftAssign() != null) {
            op = new ASTToken(">>=", ctx.RightShiftAssign().getSymbol().getTokenIndex());
        } else if (ctx.AndAssign() != null) {
            op = new ASTToken("&=", ctx.AndAssign().getSymbol().getTokenIndex());
        } else if (ctx.XorAssign() != null) {
            op = new ASTToken("^=", ctx.XorAssign().getSymbol().getTokenIndex());
        } else if (ctx.OrAssign() != null) {
            op = new ASTToken("|=", ctx.OrAssign().getSymbol().getTokenIndex());
        }
        this.astMap.put(ctx, op);
    }

    @Override public void exitExpression(CParser.ExpressionContext ctx) {
        List<ASTExpression> expression = new LinkedList<>();
        if (ctx.expression() != null) {
            expression.addAll((Collection<? extends ASTExpression>) this.astMap.get(ctx.expression()));
        }
        expression.add((ASTExpression) this.astMap.get(ctx.assignmentExpression()));
        this.astMap.put(ctx, expression);
    }

    @Override public void exitTypeName(CParser.TypeNameContext ctx) {
        ASTTypename node;
        List<ASTToken> specfiers = new LinkedList<>();
        for (ParseTree parseTree : ctx.children) {
            ASTToken token = (ASTToken) this.astMap.get(parseTree);
            specfiers.add(token);
        }
        node = new ASTTypename(specfiers, null);
        this.astMap.put(ctx, node);
    }

    @Override public void exitTypedefName(CParser.TypedefNameContext ctx) {
        ASTToken node = new ASTToken(ctx.Identifier().getText(), ctx.Identifier().getSymbol().getTokenIndex());
        this.astMap.put(ctx, node);
    }

}
