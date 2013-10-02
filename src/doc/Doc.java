package doc;

import doc.Im.ImList;

public class Doc {

    /**
     *  Document is an immutable datatype representing a text document with italics in it.
     */
    public static interface Document {
        public boolean containsItalic();
        public String toHtml() throws CantRepresentException;
        public String toMarkdown() throws CantRepresentException;
    }
    
    public static class CantRepresentException extends Exception {
        public CantRepresentException(String msg) { super(msg); }
    }

	/**
	 * @param html is a string with HTML italic tags (and no other HTML markup) 
	 * @return Document value represented by html
	 */
    public static Document parseHtml(String html) {
        return HtmlFactory.parse(html);
    }
    
	/**
	 * @param html is a string with Markdown italic tags (and no other Markdown markup) 
	 * @return Document value represented by markdown
	 */
    public static Document parseMarkdown(String markdown) {
        return MarkdownFactory.parse(markdown);
    }

    /*
     * The internal representation of Document is a recursive datatype,
     * defined by the following datatype definition:
     * 
     *      Document = Italic(child:Document) 
     *               + Sequence(parts:ImList<Document>) 
     *               + Text(string:String)
     *
     * The Italic, Sequence, and Text classes are package-private, visible
     * only to other classes in this package.
     * 
     */

     static class Italic implements Document {
        private final Document child;
        
        public Italic(Document child) {
            this.child = child;
        }
        public boolean containsItalic() {
            return true;
        }
        public String toHtml() throws CantRepresentException {
            return "<i>" + child.toHtml() + "</i>";
        }
        public String toMarkdown() throws CantRepresentException{
            if (child.containsItalic()) throw new CantRepresentException("nested italics can't be represented in Markdown");
            return "_" + child.toMarkdown() + "_";
        }
    }
    
    static class Sequence implements Document {
        private final ImList<Document> parts;
        
        public Sequence(ImList<Document> parts) {
            this.parts = parts;
        }
        public boolean containsItalic() {
            for (ImList<Document> e = parts; !e.isEmpty(); e = e.rest()) {
                if (e.first().containsItalic()) return true;
            }
            return false;            
        }
        public String toHtml() throws CantRepresentException {
            StringBuilder sb = new StringBuilder();
            for (ImList<Document> e = parts; !e.isEmpty(); e = e.rest()) {
                sb.append(e.first().toHtml());
            }
            return sb.toString();
        }
        public String toMarkdown() throws CantRepresentException{
            StringBuilder sb = new StringBuilder();
            for (ImList<Document> e = parts; !e.isEmpty(); e = e.rest()) {
                sb.append(e.first().toMarkdown());
            }
            return sb.toString();
        }
    }
    
    static class Text implements Document {
        private final String string;
        
        public Text(String string) {
            this.string = string;
        }
        public boolean containsItalic() {
            return false;       
        }
        public String toHtml() throws CantRepresentException {
            return string;
        }
        public String toMarkdown() throws CantRepresentException{
            return string;
        }        
    }
    
}
