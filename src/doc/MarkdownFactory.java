package doc;

import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import doc.Doc.Document;
import doc.Doc.Italic;
import doc.Doc.Sequence;
import doc.Doc.Text;
import doc.Im.ImList;
import doc.markdown.MarkdownBaseListener;
import doc.markdown.MarkdownLexer;
import doc.markdown.MarkdownParser;
import doc.markdown.MarkdownParser.ItalicContext;
import doc.markdown.MarkdownParser.MarkdownContext;
import doc.markdown.MarkdownParser.NormalContext;
import doc.markdown.MarkdownParser.RootContext;

public class MarkdownFactory {

    public static Document parse(String html) {
        // Create a stream of tokens using the lexer.
        CharStream stream = new ANTLRInputStream(html);
        MarkdownLexer lexer = new MarkdownLexer(stream);
        lexer.reportErrorsAsExceptions();
        TokenStream tokens = new CommonTokenStream(lexer);
        
        // Feed the tokens into the parser.
        MarkdownParser parser = new MarkdownParser(tokens);
        parser.reportErrorsAsExceptions();
        
        // Generate the parse tree using the starter rule.
        ParseTree tree = parser.root(); // "root" is the starter rule.
       
        // debugging option #1: print the tree to the console
        // System.err.println(tree);

        // debugging option #2: show the tree in a window
        RuleContext rootContext = (RuleContext) tree;
        rootContext.inspect(parser);
        
        // Walk the tree with the listener.
        ParseTreeWalker walker = new ParseTreeWalker();
        MarkdownTreeListener listener = new MarkdownTreeListener();
        walker.walk(listener, tree);
        
        // return the markup value that the listener created
        return listener.getDocument();
    }
    
    private static class MarkdownTreeListener extends MarkdownBaseListener {
        private Stack<Document> stack = new Stack<Document>();
        
        @Override
        public void exitNormal(NormalContext ctx) {
            TerminalNode token = ctx.TEXT();
            String text = token.getText();
            Document node = new Text(text);
            stack.push(node);
        }
        
        @Override
        public void exitItalic(ItalicContext ctx) {
            TerminalNode token = ctx.TEXT();
            String text = token.getText();
            Document node = new Italic(new Text(text));
            stack.push(node);
        }
        
        @Override
        public void exitMarkdown(MarkdownContext ctx) {
            ImList<Document> list = new Im.Empty<Document>();
            int numChildren = ctx.getChildCount();
            for (int i = 0; i < numChildren; ++i) {
                list = list.cons(stack.pop());
            }
            Document node = new Sequence(list);
            stack.push(node);
        }
        
        @Override
        public void exitRoot(RootContext ctx) {
            // do nothing, because the top of the stack should have the node already in it
            assert stack.size() == 1;
        }
        
        public Document getDocument() {
            return stack.get(0);
        }
    }
    
}
