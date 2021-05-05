package bit.minisys.minicc.parser;

import bit.minisys.minicc.MiniCCCfg;
import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.parser.antlr.CLexer;
import bit.minisys.minicc.parser.antlr.CParser;
import bit.minisys.minicc.parser.ast.ASTCompilationUnit;
import bit.minisys.minicc.parser.antlr.MiniCCListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class MiniCCParser implements IMiniCCParser {
    public MiniCCParser() {
    }

    public String run(String filename) {
        String outfile = MiniCCUtil.removeAllExt(filename) + MiniCCCfg.MINICC_PARSER_OUTPUT_EXT;
        CharStream antlrInfile = null;
        try {
            antlrInfile = CharStreams.fromFileName(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* 词法分析 */
        CLexer lexer = new CLexer(antlrInfile);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        /* 语法分析生成语法解析树 */
        CParser parser = new CParser(tokens);
        ParseTree root = parser.start();
        /* 遍历ANTLR生成的解析树构造AST */
        ParseTreeWalker walker = new ParseTreeWalker();
        MiniCCListener listener = new MiniCCListener();
        walker.walk(listener, root);
        ASTCompilationUnit ast = listener.getAST();
        ObjectMapper jsonMapper = new ObjectMapper();
        /* AST序列化为Json */
        try {
            jsonMapper.writeValue(new File(outfile), ast);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("3. Parsing Finished!");
        /* 可视化 */
        TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), root);
        viewer.open();
        return outfile;
    }
}
