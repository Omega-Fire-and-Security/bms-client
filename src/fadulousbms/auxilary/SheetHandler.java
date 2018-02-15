package fadulousbms.auxilary;

import jdk.internal.org.xml.sax.Attributes;
import jdk.internal.org.xml.sax.helpers.DefaultHandler;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * See org.xml.sax.helpers.DefaultHandler javadocs
 */
public class SheetHandler implements ContentHandler {
    private SharedStringsTable sst;
    private String lastContents;
    private boolean nextIsString;

    public SheetHandler(SharedStringsTable sst)
    {
        this.sst = sst;
    }

    public void startElement(String uri, String localName, String name, Attributes attributes)
    {
        // c => cell
        if(name.equals("c"))
        {
            // Print the cell reference
            System.out.print(attributes.getValue("r") + " - ");
            // Figure out if the value is an index in the SST
            String cellType = attributes.getValue("t");
            if(cellType != null && cellType.equals("s"))
            {
                nextIsString = true;
            } else
            {
                nextIsString = false;
            }
        }
        // Clear contents cache
        lastContents = "";
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {

    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) throws SAXException
    {

    }

    public void endElement(String uri, String localName, String name)
    {
        // Process the last contents as required.
        // Do now, as characters() may be called more than once
        if(nextIsString)
        {
            int idx = Integer.parseInt(lastContents);
            lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
            nextIsString = false;
        }

        // v => contents of a cell
        // Output after we've seen the string contents
        if(name.equals("v"))
        {
            System.out.println(lastContents);
        }
    }

    public void characters(char[] ch, int start, int length)
    {
        lastContents += new String(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }
}