package fadulousbms.auxilary;

import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.util.Callback;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;
import javax.print.*;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ghost on 2017/02/10.
 */
public class PDF
{
    private static final String TAG = "PDF";
    private static final int LINE_HEIGHT=20;
    private static final int LINE_END = 500;
    private static final int TEXT_VERT_OFFSET=LINE_HEIGHT/4;
    //private static final int ROW_COUNT = 34;
    private static final Insets PAGE_MARGINS = new Insets(90,25,60,25);
    private static String logo_path = "images/logo.png";
    private static String header_path = "images/header.jpg";

    static class Border
    {
        private static final int BORDER_NONE = 0;
        private static final int BORDER_LEFT = 1;
        private static final int BORDER_TOP = 2;
        private static final int BORDER_RIGHT = 3;
        private static final int BORDER_BOTTOM = 4;
        private static final int BORDER_ALL = 5;
        int border = BORDER_ALL;
        double border_width = 1.0;
        Color border_colour = Color.BLACK;
        Insets insets = new Insets(0, 0, 0, 0);

        Border(int pos, Color border_colour, int border_width, Insets insets)
        {
            this.border = pos;
            this.border_colour = border_colour;
            this.border_width = border_width;
            if(insets!=null)
                this.insets = insets;
            else this.insets = new Insets(0,0,0,0);
        }
    }

    public static void printPDF(final byte[] byteStream) throws PrintException
    {
        PrinterJob printerJob = PrinterJob.getPrinterJob();

        PrintService printService=null;
        if(printerJob.printDialog())
        {
            printService = printerJob.getPrintService();
        }
        if(printService!=null)
        {
            DocFlavor docType = DocFlavor.INPUT_STREAM.AUTOSENSE;

            DocPrintJob printJob = printService.createPrintJob();
            Doc documentToBePrinted = new SimpleDoc(new ByteArrayInputStream(byteStream), docType, null);
            printJob.print(documentToBePrinted, null);
        } else
        {
            IO.logAndAlert("Print Job", "Print job cancelled.", IO.TAG_INFO);
        }
    }

    private static void drawHorzLines(PDPageContentStream contents, int y_start, int page_width, Insets offsets) throws IOException
    {
        contents.setStrokingColor(new Color(171, 170, 166));
        //horizontal top title underline
        contents.moveTo(offsets.left, y_start);
        contents.lineTo(page_width-offsets.right, y_start);
        contents.stroke();
        for(int i=y_start;i>offsets.bottom;i-=LINE_HEIGHT)
        {
            //horizontal underline
            contents.moveTo(offsets.left, i-LINE_HEIGHT);
            contents.lineTo(page_width-offsets.right, i-LINE_HEIGHT);
            contents.stroke();
            //line_pos-=LINE_HEIGHT;
        }
    }

    private static void drawVertLines(PDPageContentStream contents, int[] x_positions, int y_start) throws IOException
    {
        for(int x: x_positions)
        {
            contents.moveTo(x, y_start);
            contents.lineTo(x, PAGE_MARGINS.bottom);
            contents.stroke();
        }
    }

    public static void createDocumentIndex(String title, FileMetadata[] fileMetadata, String path) throws IOException
    {
        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);
        
        // Create a new document with an empty page.
        final PDDocument document = new PDDocument();
        final PDPage page = new PDPage(PDRectangle.A4);

        final float w = page.getBBox().getWidth();
        final float h = page.getBBox().getHeight();

        //Add page to document
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents_stream = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        contents_stream.drawImage(logo, (w/2)-80, 770, 160, logo_h);

        int line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** draw horizontal lines **/
        drawHorzLines(contents_stream, line_pos, (int)w, PAGE_MARGINS);
        /** draw vertical lines **/
        final int[] col_positions = {75, (int)((w / 2) + 100), (int)((w / 2) + 200)};
        drawVertLines(contents_stream, col_positions, line_pos-LINE_HEIGHT);
        line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** begin text from the top**/
        //contents.beginText();
        //contents.setFont(font, 12);
        line_pos-=10;
        //Heading text
        addTextToPageStream(document, title, 16,(int)(w/2)-70, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //Create column headings
        addTextToPageStream(document,"Index", 14,10, line_pos);
        addTextToPageStream(document,"Label", 14, col_positions[0]+10, line_pos);
        addTextToPageStream(document,"Required?", 14,col_positions[1]+10, line_pos);
        addTextToPageStream(document,"Available?", 14,col_positions[2]+10, line_pos);

        //contents.endText();
        line_pos-=LINE_HEIGHT;//next line

        //int pos = line_pos;
        for(FileMetadata metadata : fileMetadata)
        {
            //contents.beginText();
            //TODO:addTextToPageStream(contents, String.valueOf(metadata.getIndex()), 14, 20, line_pos);

            if(metadata.getLabel().length()>=105)
                addTextToPageStream(document, metadata.getLabel(), 6, 80, line_pos);
            else if(metadata.getLabel().length()>=85)
                addTextToPageStream(document, metadata.getLabel(), 8, 80, line_pos);
            else if(metadata.getLabel().length()>=45)
                addTextToPageStream(document, metadata.getLabel(), 11, 80, line_pos);
            else if(metadata.getLabel().length()<45)
                addTextToPageStream(document, metadata.getLabel(), 14, 80, line_pos);

            //TODO: addTextToPageStream(contents, String.valueOf(metadata.getRequired()), 14, (int) (w / 2)+120, line_pos);
            //contents.endText();

            //Availability field to be filled in by official
            line_pos-=LINE_HEIGHT;//next line

            //if reached bottom of page, add new page and reset cursor.
            /*if(line_pos<page_margins.bottom)
            {
                //contents.close();
                final PDPage new_page = new PDPage(PDRectangle.A4);
                contents = new PDPageContentStream(document, new_page);
                //Add page to document
                document.addPage(new_page);
                contents.setFont(font, 14);

                line_pos = (int)h-logo_h-20;
                IO.log(TAG, IO.TAG_INFO, "Added new page.");
                //draw horizontal lines
                drawHorzLines(contents, line_pos, (int)w, page_margins);
                //draw vertical lines
                drawVertLines(contents, col_positions, line_pos);
                line_pos -= 10;//move cursor down a bit for following
            }*/
        }

        //contents.close();
        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.doOpen(path);
        pdfViewer.setVisible(true);
    }

    public static void createBordersOnPage(PDPageContentStream contents, Insets insets, int page_w) throws IOException
    {
        if(insets==null)
        {
            IO.log(PDF.class.getName(), IO.TAG_ERROR, "page insets are invalid");
            return;
        }
        //top border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(insets.left, insets.top);
        contents.lineTo(page_w-insets.right, insets.top);
        contents.stroke();

        //left border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(insets.left, insets.top);
        contents.lineTo(insets.left, insets.bottom-LINE_HEIGHT);
        contents.stroke();

        //right border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(page_w-insets.left, insets.top);
        contents.lineTo(page_w-insets.left, insets.bottom-LINE_HEIGHT);
        contents.stroke();

        //bottom border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(insets.left, insets.bottom-LINE_HEIGHT);
        contents.lineTo(page_w-insets.left, insets.bottom-LINE_HEIGHT);
        contents.stroke();
    }

    public static void createLinesAndBordersOnPage(PDPageContentStream contents, Insets insets, int page_w) throws IOException
    {
        //draw borders
        createBordersOnPage(contents, insets, page_w);
        //draw horizontal lines
        int line_pos=insets.top;
        //for(int i=0;i<ROW_COUNT;i++)//35 rows
        while(line_pos>insets.bottom)
        {
            //horizontal underline
            contents.setStrokingColor(new Color(171, 170, 166));
            contents.moveTo(insets.left, line_pos-LINE_HEIGHT);
            contents.lineTo(page_w-insets.left, line_pos-LINE_HEIGHT);
            contents.stroke();
            line_pos-=LINE_HEIGHT;
        }
        /*if(isTextMode)
            contents.beginText();*/
    }

    public static String createRequisitionPDF(Requisition requisition) throws IOException
    {
        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);

        // Create a new document with an empty page.
        final PDDocument document = new PDDocument();
        final PDPage page = new PDPage(PDRectangle.A4);

        final float w = page.getBBox().getWidth();
        final float h = page.getBBox().getHeight();

        //Add page to document
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents_stream = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        contents_stream.drawImage(logo, (w/2)-80, 770, 160, logo_h);

        int line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** draw horizontal lines **/
        drawHorzLines(contents_stream, line_pos, (int)w, PAGE_MARGINS);
        /** draw vertical lines **/
        //final int[] col_positions = {(int)((w / 2)), (int)((w / 2) + 100), (int)((w / 2) + 200)};
        //drawVertLines(contents, col_positions, line_pos-LINE_HEIGHT);
        line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** begin text from the top**/
        //contents.beginText();
        //contents.setFont(font, 12);
        line_pos-=10;
        //Heading text
        addTextToPageStream(document, "Requisition", 16,(int)(w/2)-70, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "Client: "+ requisition.getClient().getClient_name(), 16 ,10, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "Description: "+ requisition.getDescription(), PDType1Font.TIMES_ITALIC, 16 ,10, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "Requisition Type: "+ requisition.getType(), 16 ,10, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "Responsible Person: "+ requisition.getResponsible_person().getFirstname()+" "+requisition.getResponsible_person().getLastname(), 16 ,10, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "Logged By: "+ requisition.getCreatorEmployee().getFirstname()+" "+requisition.getCreatorEmployee().getLastname(), 16 ,10, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "Date Logged: "+(new SimpleDateFormat("yyyy-MM-dd").format(requisition.getDate_logged()*1000)), 16,10, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        line_pos-=LINE_HEIGHT*2;//next 2nd line
        String status = "N/A";
        switch (requisition.getStatus())
        {
            case BusinessObject.STATUS_PENDING:
                status="PENDING";
                break;
            case BusinessObject.STATUS_APPROVED:
                status="GRANTED";
                break;
            case BusinessObject.STATUS_ARCHIVED:
                status="ARCHIVED";
                break;
        }
        addTextToPageStream(document, "STATUS: ", 14,10, line_pos);
        addTextToPageStream(document, status, 14,100, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        if(requisition.getOther()!=null)
            addTextToPageStream(document, "Extra: "+ requisition.getOther(), 16, 15, line_pos);

        /*line_pos-=LINE_HEIGHT*3;//next 3rd line
        addTextToPageStream(contents, "Applicant's Signature", 16,10, line_pos);
        addTextToPageStream(contents, "Manager Signature", 16, 200, line_pos);*/

        //contents.endText();

        String path = "out/pdf/requisition_" + requisition.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/requisition_" + requisition.get_id() + "." + i + ".pdf";
            i++;
        }

        contents_stream.close();
        document.save(path);
        document.close();

        return path;
    }

    public static String createLeaveApplicationPDF(Leave leave) throws IOException
    {
        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);

        // Create a new document with an empty page.
        final PDDocument document = new PDDocument();
        final PDPage page = new PDPage(PDRectangle.A4);

        final float w = page.getBBox().getWidth();
        final float h = page.getBBox().getHeight();

        //Add page to document
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents_stream = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        contents_stream.drawImage(logo, (w/2)-80, 770, 160, logo_h);

        int line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** draw horizontal lines **/
        drawHorzLines(contents_stream, line_pos, (int)w, PAGE_MARGINS);
        /** draw vertical lines **/
        final int[] col_positions = {(int)((w / 2)), (int)((w / 2) + 100), (int)((w / 2) + 200)};
        drawVertLines(contents_stream, col_positions, line_pos-LINE_HEIGHT);
        line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** begin text from the top**/
        //contents.beginText();
        //contents.setFont(font, 12);
        line_pos-=10;
        //Heading text
        addTextToPageStream(document, "Leave Application", 16,(int)(w/2)-70, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "DATE LOGGED: ", 16,10, line_pos);
        addTextToPageStream(document, (new SimpleDateFormat("yyyy-MM-dd").format(leave.getDate_logged())), 16,(int)w/2+100, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "I "+leave.getEmployee().getName() + " hereby wish to apply for leave as indicated below.", PDType1Font.TIMES_ITALIC, 16 ,10, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        //Create column headings
        addTextToPageStream(document,"Type", 14,10, line_pos);
        addTextToPageStream(document,"From", 14, col_positions[0]+10, line_pos);
        addTextToPageStream(document,"Till", 14,col_positions[1]+10, line_pos);
        addTextToPageStream(document,"Total Days", 14,col_positions[2]+10, line_pos);

        //contents.endText();
        line_pos-=LINE_HEIGHT;//next line

        //int pos = line_pos;
        //contents.beginText();
        addTextToPageStream(document, String.valueOf(leave.getType()), 14, 10, line_pos);

        if(leave.getStart_date()>0)
            addTextToPageStream(document, (new SimpleDateFormat("yyyy-MM-dd").format(leave.getStart_date())), 12, col_positions[0]+5, line_pos);
        else addTextToPageStream(document, "N/A", 12, col_positions[0]+5, line_pos);
        if(leave.getEnd_date()>0)
            addTextToPageStream(document, (new SimpleDateFormat("yyyy-MM-dd").format(leave.getEnd_date())), 12, col_positions[1]+5, line_pos);
        else addTextToPageStream(document, "N/A", 12, col_positions[1]+5, line_pos);

        long diff_ms = leave.getEnd_date()-leave.getStart_date();//in epoch milliseconds
        long days = diff_ms/1000/60/60/24;
        addTextToPageStream(document, String.valueOf(days), 12, col_positions[2]+5, line_pos);

        line_pos-=LINE_HEIGHT*2;//next 2nd line
        String status = "N/A";
        switch (leave.getStatus())
        {
            case Leave.STATUS_PENDING:
                status="PENDING";
                break;
            case Leave.STATUS_APPROVED:
                status="GRANTED";
                break;
            case Leave.STATUS_ARCHIVED:
                status="ARCHIVED";
                break;
        }
        addTextToPageStream(document, "STATUS: ", 14,10, line_pos);
        addTextToPageStream(document, status, 14,100, line_pos);
        line_pos-=LINE_HEIGHT*2;//next 2nd line

        addTextToPageStream(document, "IF DENIED, REASON WILL BE STATED BELOW: ", 14,10, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        if(leave.getOther()!=null)
            addTextToPageStream(document, leave.getOther(), 16, 15, line_pos);

        line_pos-=LINE_HEIGHT*3;//next 3rd line
        addTextToPageStream(document, "Applicant's Signature", 16,10, line_pos);
        addTextToPageStream(document, "Manager Signature", 16, 200, line_pos);
        //contents.endText();

        //draw first signature line
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos+LINE_HEIGHT+5);
        contents_stream.lineTo(120, line_pos+LINE_HEIGHT+5);
        contents_stream.stroke();
        //draw second signature line
        contents_stream.moveTo(200, line_pos+LINE_HEIGHT+5);
        contents_stream.lineTo(320, line_pos+LINE_HEIGHT+5);
        contents_stream.stroke();

        String path = "out/pdf/leave_" + leave.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/leave_" + leave.get_id() + "." + i + ".pdf";
            i++;
        }

        contents_stream.close();
        document.save(path);
        document.close();

        return path;
    }

    public static String createPurchaseOrderPdf(PurchaseOrder purchaseOrder, Callback callback) throws IOException
    {
        if(purchaseOrder==null)
        {
            IO.logAndAlert("PDF Viewer", "PurchaseOrder object passed is null.", IO.TAG_ERROR);
            return null;
        }
        //Prepare PDF data from database.
        //Load PurchaseOrder Supplier
        Supplier supplier = purchaseOrder.getSupplier();
        if(supplier==null)
        {
            IO.logAndAlert("PDF Viewer Error", "PurchaseOrder has no Supplier assigned to it.", IO.TAG_ERROR);
            return null;
        }
        //Load PurchaseOrder contact person
        Employee employee = purchaseOrder.getContact_person();
        if(employee==null)
        {
            IO.logAndAlert("PDF Viewer Error", "PurchaseOrder has no contact person assigned to it.", IO.TAG_ERROR);
            return null;
        }
        //Load PurchaseOrder contact person
        PurchaseOrderItem[] purchaseOrderItems = purchaseOrder.getItems();
        if(purchaseOrderItems==null)
        {
            IO.logAndAlert("PDF Viewer Error", "PurchaseOrder has no PurchaseOrderItems assigned to it.", IO.TAG_ERROR);
            return null;
        }

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        /*File font_file = new File(FadulousBMS.class.getResource("fonts/Ubuntu-L.ttf").getFile());
        if(font_file==null)
        {
            IO.log("Purchase Order PDF generator Error", IO.TAG_ERROR, "Could not find default system font file [fonts/Raleway-Light.ttf]");
            return null;
        }
        PDFont font = PDType0Font.load(document, font_file);*/
        PDFont font = PDType1Font.COURIER;
        PDResources resources = new PDResources();
        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents_stream = new PDPageContentStream(document, page);
        int logo_h = 60;
        //PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        //contents.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-20;//(int)h-logo_h-20;
        int digit_font_size=9;

        /**Draw lines**/
        int center_vert_line_start = line_pos;
        //int bottom_line = (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT;
        createLinesAndBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, PAGE_MARGINS.top, PAGE_MARGINS.right), (int)w);

        /** begin text from the top**/
        //contents.beginText();
        //contents.setFont(font, 12);
        line_pos-=LINE_HEIGHT/2;

        int temp_pos = line_pos;
        int header_h = 90;
        //PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        PDImageXObject header = PDImageXObject.createFromFile(header_path, document);
        //right text
        //addTextToPageStream(contents,"PURCHASE ORDER", PDType1Font.COURIER_BOLD_OBLIQUE, 17, (int)(w/2)+20, line_pos);
        //line_pos-=LINE_HEIGHT;//next line
        //contents.endText();
        //System.out.println(">>>>>>"+new File("images/logo.png").getPath());
        //System.out.println(">>>>>>"+(new File(logo_path).exists()));
        //PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        contents_stream.drawImage(header, 0, 760, w, header_h);
        //contents.drawImage(logo, (int)(w/2)+ 20, line_pos-logo_h, 150, logo_h);
        //contents.beginText();

        //line_pos=temp_pos;//revert to original line
        //line_pos-=LINE_HEIGHT;//next line

        //left text
        addTextToPageStream(document,"Purchase Order #" + purchaseOrder.getObject_number(), PDType1Font.COURIER_BOLD_OBLIQUE, 17,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()))), 12,20, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(document,"Date Logged:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(purchaseOrder.getDate_logged()))), 12, 20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"Overall Discount:  " + purchaseOrder.discountProperty().get(), 12,20 , line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"VAT:  " + purchaseOrder.getVat() + "%", 12,20 , line_pos);

        //line_pos=temp_pos;//revert to original line
        line_pos-=LINE_HEIGHT;//next line
        temp_pos=line_pos;

        //horizontal solid line after purchase order details
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents_stream.stroke();

        //horizontal solid line after from/to labels
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();

        //horizontal solid line after company details
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT-LINE_HEIGHT/2);
        contents_stream.stroke();
        //contents.beginText();

        //Company Info
        //Left Text: From
        addTextToPageStream(document,"FROM", PDType1Font.HELVETICA_BOLD, 15,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,Globals.COMPANY.getValue(), PDType1Font.HELVETICA_BOLD, 16,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"VAT No.: #########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"POSTAL ADDRESS: ##########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"CITY: ##########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"POSTAL CODE: ####", 12,20, line_pos);

        line_pos-=LINE_HEIGHT*2;//next 2ND line

        addTextToPageStream(document,"PHYSICAL ADDRESS: ########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"CITY: ########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"PROVINCE/STATE: #######", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"POSTAL CODE: ####", 12,20, line_pos);

        line_pos=temp_pos;//revert to original line
        //line_pos-=LINE_HEIGHT;//next line
        //temp_pos=line_pos;

        //Right Text: To
        int supplier_text_x = (int)(w/2)+5;
        addTextToPageStream(document,"TO", PDType1Font.HELVETICA_BOLD, 15,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document, purchaseOrder.getSupplier().getSupplier_name(), PDType1Font.HELVETICA_BOLD, 16, supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"VAT No.: "+purchaseOrder.getSupplier().getVat_number(), 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        //addTextToPageStream(contents,"POSTAL ADDRESS: " + purchaseOrder.getSupplier().getPostal_address(), 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"CITY: ##########", 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"POSTAL CODE: ####", 12,supplier_text_x, line_pos);

        line_pos-=LINE_HEIGHT*2;//next 2ND line

        //addTextToPageStream(contents, String.format("PHYSICAL ADDRESS: %s", purchaseOrder.getSupplier().getPhysical_address()), 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"CITY: ########", 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"PROVINCE/STATE: #######", 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"POSTAL CODE: ####", 12,supplier_text_x, line_pos);

        //horizontal solid line after company details
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();
        contents_stream.beginText();

        line_pos-=LINE_HEIGHT;//next line
        temp_pos=line_pos;//backup current position

        //left text
        addTextToPageStream(document,"Creator: " + purchaseOrder.getCreator(), PDType1Font.COURIER_BOLD_OBLIQUE, 12, 20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"Tel    :  " + purchaseOrder.getCreatorEmployee().getTel(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"Cell   :  " + purchaseOrder.getCreatorEmployee().getCell(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"eMail :  " + purchaseOrder.getCreatorEmployee().getEmail(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);

        line_pos=temp_pos;//revert line pos

        //right text
        addTextToPageStream(document,"Supplier Contact: " + purchaseOrder.getContact_person(), PDType1Font.COURIER_BOLD_OBLIQUE, 12, (int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"Tel    :  " + purchaseOrder.getContact_person().getTel(), PDType1Font.HELVETICA_BOLD, 12,(int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"Cell   :  " + purchaseOrder.getContact_person().getCell(), PDType1Font.HELVETICA_BOLD, 12,(int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(document,"eMail :  " + purchaseOrder.getContact_person().getEmail(), PDType1Font.HELVETICA_BOLD, 12,(int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //horizontal solid line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents_stream.stroke();
        //contents.beginText();

        //horizontal solid line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();
        //contents.beginText();

        //contents.endText();

        //horizontal solid line after reps
        /*contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //horizontal solid line after request
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();
        //solid horizontal line after site location, before quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(w-10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();*/

        int col_divider_start = line_pos-LINE_HEIGHT;

        //vertical line going through center of page
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo((w/2), center_vert_line_start);
        contents_stream.lineTo((w/2),(col_divider_start-LINE_HEIGHT*2+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.stroke();
        //
        contents_stream.moveTo((w/2), (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.lineTo((w/2),(col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.stroke();

        //contents.beginText();

        //Column headings
        int col_pos = 10;
        addTextToPageStream(document,"Item No.", PDType1Font.COURIER_BOLD,14,15, line_pos);
        col_pos += 80;
        addTextToPageStream(document,"Item description", PDType1Font.COURIER_BOLD,14,col_pos+20, line_pos);
        col_pos = (int)(w/2);
        String[] cols = {"Unit", "Qty", "Cost", "Discount", "Total"};
        for(int i=0;i<5;i++)//7 cols in total
            addTextToPageStream(document, cols[i], PDType1Font.COURIER_BOLD, 12,col_pos+(55*i)+2, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //Purchase Order Items
        col_pos = 10;
        double sub_total = 0;
        for(PurchaseOrderItem item: purchaseOrderItems)
        {
            //quote content column dividers
            //contents.endText();
            //#1
            contents_stream.moveTo(80, (col_divider_start+LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents_stream.lineTo(80, line_pos-LINE_HEIGHT/2);
            contents_stream.stroke();
            //vertical line going through center of page
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents_stream.lineTo((w/2),line_pos-LINE_HEIGHT/2);
            contents_stream.stroke();
            //#3+
            for(int i=1;i<5;i++)//7 cols in total
            {
                contents_stream.moveTo((w/2)+55*i, (col_divider_start+LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
                contents_stream.lineTo((w/2)+55*i,line_pos-LINE_HEIGHT/2);
                contents_stream.stroke();
            }
            //contents.beginText();
            //end draw columns

            //if the page can't hold another 4 lines[current item, blank, sub-total, vat] add a new page
            /*if(line_pos-LINE_HEIGHT<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
            {
                addTextToPageStream(document, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
                //add new page
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                //TODO: setup page, i.e. draw lines and stuff
                //contents.close();
                contents = new PDPageContentStream(document, page);
                contents.beginText();
                line_pos = (int)h-logo_h;
                col_divider_start = line_pos+LINE_HEIGHT;
                createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
                quote_page_count++;
            }*/

            col_pos =0;//first column
            //Item # col
            addTextToPageStream(document, item.getItem_number(), 12,col_pos+30, line_pos);
            col_pos += 80;//next column
            //Description col
            //addTextToPageStream(contents, item.getItem_description(), 12,col_pos+5, line_pos);
            addTextToPageStream(document, item.getItem_description() , font, 12, col_pos+5, line_pos);
            col_pos = (int)w/2;//next column - starts at middle of page
            //Unit col
            addTextToPageStream(document,item.getUnit(), 12,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Quantity col
            addTextToPageStream(document,item.getQuantity(), digit_font_size,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Cost col
            addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getCostValue())), digit_font_size,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Discount col
            addTextToPageStream(document, String.valueOf(item.getDiscount()), digit_font_size,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Total col
            sub_total+=item.getTotal();
            addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getTotal())), digit_font_size,col_pos+5, line_pos);

            line_pos -= LINE_HEIGHT;//next line
        }
        IO.log(TAG, IO.TAG_INFO, "successfully created Purchase Order PDF.");
        //col_pos = 0;
        //line_pos -= LINE_HEIGHT;//skip another line
        /*if the page can't hold another 2 lines add a new page
        if(line_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT) || temp_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            //TODO: setup page, i.e. draw lines and stuff
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            col_divider_start = line_pos+LINE_HEIGHT;
        }*/
        //solid horizontal line
        int col_divider_end= line_pos;

        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents_stream.stroke();

        //contents.beginText();
        addTextToPageStream(document, "Sub-Total [Excl. VAT]: ", PDType1Font.COURIER, 14,20, line_pos);
        addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total)), PDType1Font.COURIER, 14,(int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents_stream.stroke();

        double vat = sub_total*(purchaseOrder.getVatVal()/100);
        //contents.beginText();
        addTextToPageStream(document, "VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,20, line_pos);
        addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents_stream.stroke();

        //contents.beginText();
        addTextToPageStream(document, "Total [Incl. VAT]: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,20, line_pos);
        addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total + vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int)(5+(w/2)), line_pos);
        //contents.endText();
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents_stream.stroke();

        //int col_divider_end = line_pos;
        line_pos -= LINE_HEIGHT*3;//next 3rd line
        /*solid horizontal lines after quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();*/

        //quote content column dividers
        //#1
        contents_stream.moveTo(80, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.lineTo(80, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents_stream.stroke();
        //vertical line going through center of page again
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.lineTo((w/2), col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        //contents.lineTo((w/2),col_divider_end-LINE_HEIGHT/2);
        contents_stream.stroke();
        //#3+
        for(int i=1;i<5;i++)//7 cols in total
        {
            contents_stream.moveTo((w/2)+55*i, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents_stream.lineTo((w/2)+55*i,col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
            contents_stream.stroke();
        }

        //contents.beginText();

        if(purchaseOrder.getOther()!=null)
            addTextToPageStream(document, "P.S. "+purchaseOrder.getOther(), PDType1Font.TIMES_ITALIC, 14,col_pos+5, line_pos);

        line_pos -= LINE_HEIGHT;//next line
        //if the page can't hold another 9 lines add a new page
        /*if(line_pos-(LINE_HEIGHT*4)<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
            quote_page_count++;
        }*/

        addTextToPageStream(document, "Page "+document.getNumberOfPages(), PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
        //contents.endText();

        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);
        
        String path = "out/pdf/purchase_order_" + purchaseOrder.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/purchase_order_" + purchaseOrder.get_id() + "." + i + ".pdf";
            i++;
        }

        //Files.delete(Paths.get(path));//delete previous versions

        contents_stream.close();
        document.save(path);
        document.close();

        if(callback!=null)
            callback.call(path);

        return path;
    }

    public static String createQuotePdf(Quote quote) throws IOException
    {
        if(quote==null)
        {
            IO.logAndAlert("PDF Generator", "Quote object passed is null.", IO.TAG_ERROR);
            return null;
        }
        //Load Quote Client
        Client client = quote.getClient();
        if(client==null)
        {
            IO.logAndAlert("PDF Generator Error", "Quote has no client assigned to it.", IO.TAG_ERROR);
            return null;
        }
        Employee contact = quote.getContact_person();
        if(contact==null)
        {
            IO.logAndAlert("PDF Generator Error", "Quote has no contact person assigned to it.", IO.TAG_ERROR);
            return null;
        }

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();

        PDPageContentStream contents_stream = new PDPageContentStream(document, page);
        //int logo_h = 60;
        //PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        int header_h = 90;

        PDImageXObject header = PDImageXObject.createFromFile(header_path, document);
        contents_stream.drawImage(header, 0, 760, w, header_h);

        int text_offset = 5;
        int line_pos = (int)h-header_h-0;
        int digit_font_size=9;
        int page_content_max_width = (int)(w-PAGE_MARGINS.right-PAGE_MARGINS.left);
        Border no_border = new Border(Border.BORDER_NONE, Color.BLACK, 0, new Insets(0, 0, 0, 0));

        /**Draw lines**/
        //int bottom_line = (int)h-header_h-(ROW_COUNT+1)*LINE_HEIGHT;
        createLinesAndBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, PAGE_MARGINS.bottom, PAGE_MARGINS.right), (int)w);

        /** begin text from the top**/
        line_pos-=LINE_HEIGHT/2;
        //left text
        line_pos = addTextToPageStream(document, "Client Details", PDType1Font.COURIER, 15, page_content_max_width, (int)((w/2)/4), line_pos, no_border, null);
        //right text
        line_pos = addTextToPageStream(document, "Quotation No.: " + quote.quoteProperty().getValue(), PDType1Font.COURIER, 11, page_content_max_width, (int)(w/2)+text_offset, line_pos,no_border, null);
        line_pos-=LINE_HEIGHT;//next line

        //left text
        line_pos = addTextToPageStream(document, "Company: " + client.getClient_name(), PDType1Font.COURIER, 12, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        //right text
        //addTextToPageStream(contents,"Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()))), 12,(int)(w/2)+5, line_pos);
        //line_pos-=LINE_HEIGHT;
        line_pos = addTextToPageStream(document,"Date Logged:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(quote.getDate_logged()))), PDType1Font.COURIER, 12, page_content_max_width,(int)(w/2)+text_offset, line_pos,no_border, null);
        line_pos-=LINE_HEIGHT;//next line

        //left text
        line_pos = addTextToPageStream(document,"Company Tel: " + client.getTel(), PDType1Font.COURIER, 12, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        //right text
        //line_pos = addTextToPageStream(document,"Sale Consultant(s): ", PDType1Font.COURIER, 16, page_content_max_width, (int)((w/2)+((w/2)/4)), line_pos, no_border, null);

        //horizontal solid line after company details
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-PAGE_MARGINS.right, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();

        line_pos-=LINE_HEIGHT;//next line

        int temp_pos = line_pos;
        //left text
        line_pos = addTextToPageStream(document,"Contact Person:  " + contact.getName(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos-=LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document,"Cell   :  " + contact.getCell(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width,PAGE_MARGINS.left+text_offset*2, line_pos,no_border, null);
        line_pos-=LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document,"Tel    :  " + contact.getTel(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width,PAGE_MARGINS.left+text_offset*2, line_pos,no_border, null);
        line_pos-=LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document,"eMail :  " + contact.getEmail(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width,PAGE_MARGINS.left+text_offset*2, line_pos,no_border, null);

        line_pos-=LINE_HEIGHT;//next line (for external consultants)

        //render Quote creator
        temp_pos = addTextToPageStream(document, "Creator:  " + quote.getCreatorEmployee().getFirstname()+" "+quote.getCreatorEmployee().getLastname(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width, (int) (w / 2) + text_offset, temp_pos,no_border, null);
        temp_pos -= LINE_HEIGHT;//next line
        temp_pos = addTextToPageStream(document, "Tel    :  " + quote.getCreatorEmployee().getTel(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width, (int) (w / 2) + text_offset*2, temp_pos,no_border, null);
        temp_pos -= LINE_HEIGHT;//next line
        temp_pos = addTextToPageStream(document, "Cell   :  " + quote.getCreatorEmployee().getCell(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width, (int) (w / 2) + text_offset*2, temp_pos,no_border, null);
        temp_pos -= LINE_HEIGHT;//next line
        temp_pos = addTextToPageStream(document, "eMail :  " + quote.getCreatorEmployee().getEmail(), PDType1Font.TIMES_ROMAN, 12, page_content_max_width, (int) (w / 2) + text_offset*2, temp_pos,no_border, null);
        temp_pos -= LINE_HEIGHT;//next line

        //set the cursor to the line after the sale/client rep info
        line_pos = line_pos<temp_pos?line_pos:temp_pos;

        //horizontal solid line after sale rep info
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(PAGE_MARGINS.left, line_pos+LINE_HEIGHT/2);
        contents_stream.lineTo(w-PAGE_MARGINS.right, line_pos+LINE_HEIGHT/2);
        contents_stream.stroke();

        //solid horizontal line after site location, before quote_items
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(PAGE_MARGINS.left, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.lineTo(w-PAGE_MARGINS.right, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.stroke();

        //vertical line going through page centre
        contents_stream.moveTo((int)(w/2), h-header_h);// + LINE_HEIGHT / 2
        contents_stream.lineTo((int)(w/2), line_pos + LINE_HEIGHT / 2);// - LINE_HEIGHT / 2
        contents_stream.stroke();

        line_pos = addTextToPageStream(document,"Site: " + quote.getSitename(),PDType1Font.COURIER, 13, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
        line_pos-=LINE_HEIGHT;//next line

        //define column headings
        String[] cols = {"Item", "Description", "Unit", "Qty", "Rate", "Total"};
        //column heading positions
        int[] col_positions = {PAGE_MARGINS.left, PAGE_MARGINS.left+35, (int) (w/2), (int) (w/2)+55, (int) (w/2)+85, (int) (w/2)+170};

        //render column headings
        for(int i=0;i<cols.length;i++)//6 cols in total
            line_pos = addTextToPageStream(document, cols[i], PDType1Font.COURIER_BOLD, 12, page_content_max_width,col_positions[i]+text_offset, line_pos, no_border, col_positions);

        line_pos-=LINE_HEIGHT;//next line

        //horizontal bold line after column headings
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(col_positions[0], (line_pos+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.lineTo(w-PAGE_MARGINS.left, (line_pos+(int)Math.ceil(LINE_HEIGHT/2)));
        contents_stream.stroke();

        //add blank line
        line_pos = addTextToPageStream(document, "", PDType1Font.COURIER, 12, 10, PAGE_MARGINS.left, line_pos, no_border, col_positions);
        line_pos -= LINE_HEIGHT;//next line

        //render quote request
        line_pos = addTextToPageStream(document, quote.getRequest(), PDType1Font.COURIER, 11, col_positions[2]-col_positions[1], col_positions[1]+text_offset, line_pos,no_border, col_positions);
        line_pos -= LINE_HEIGHT;//next line

        //Actual quote information
        //col_pos = col_positions[0];
        double sub_total = 0;
        int item_index = 0;
        if(quote.getResources()!=null)
        {
            //group quote materials by category
            HashMap<String, ArrayList<QuoteItem>> quoteItemsCategoriesMap = new HashMap<>();
            for(QuoteItem item: quote.getResources())
            {
                if(quoteItemsCategoriesMap.get(item.getCategory())==null)//if material category is first of its type (i.e. has no bucket of materials)
                    quoteItemsCategoriesMap.put(item.getCategory(), new ArrayList<>());//instantiate the category's bucket
                ArrayList cat_list = quoteItemsCategoriesMap.get(item.getCategory());//get current material's respective category's bucket of materials
                cat_list.add(item);//add current material to respective category's bucket of materials.
            }
            if(quoteItemsCategoriesMap.isEmpty())
            {
                IO.log(PDF.class.getName(), IO.TAG_ERROR, "quote ["+quote.get_id()+"] has no resources.");
                return null;
            }
            //for each category, list that category's QuoteItems (materials/resources)
            for(ArrayList<QuoteItem> items: quoteItemsCategoriesMap.values())//go through all categories
            {
                if(items==null)
                    continue;
                if(items.isEmpty())
                    continue;

                item_index++;//increment major index

                //add blank line
                line_pos = addTextToPageStream(document, "", PDType1Font.COURIER, 12, 10, PAGE_MARGINS.left, line_pos, no_border, col_positions);
                line_pos -= LINE_HEIGHT;//next line

                //render category's index
                line_pos = addTextToPageStream(document, String.valueOf(item_index), PDType1Font.COURIER_BOLD, 12, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos,no_border, col_positions);
                //render category name
                if(items.get(0).getCategory()!=null)
                    line_pos = addTextToPageStream(document,items.get(0).getCategory(), PDType1Font.COURIER_BOLD,11, page_content_max_width,col_positions[1]+text_offset, line_pos, no_border, col_positions);

                line_pos -= LINE_HEIGHT;//next line

                int item_index_minor =0;//reset minor index
                for (QuoteItem item : items)
                {
                    item_index_minor++;

                    /**begin rendering actual quote material information**/
                    //first column, Item number
                    line_pos = addTextToPageStream(document, String.valueOf(item_index)+"."+String.valueOf(item_index_minor), PDType1Font.COURIER, 11, page_content_max_width, col_positions[0]+text_offset, line_pos,no_border, col_positions);//item.getItem_number()

                    //Description col
                    line_pos = addTextToPageStream(document, item.getResource().getResource_description(), PDType1Font.COURIER, 9, col_positions[2]-col_positions[1], col_positions[1] + text_offset, line_pos,no_border, col_positions);

                    //Unit col
                    line_pos = addTextToPageStream(document, item.getUnit(), PDType1Font.COURIER, 9, col_positions[3]-col_positions[2], col_positions[2]+text_offset, line_pos,no_border, col_positions);

                    //Quantity col
                    line_pos = addTextToPageStream(document, item.getQuantity(), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[3]+text_offset, line_pos,no_border, col_positions);

                    //Rate col
                    line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getRate())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[4]+text_offset, line_pos,no_border, col_positions);

                    //Total col
                    sub_total += item.getTotal();
                    line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getTotal())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[5]+text_offset, line_pos,no_border, col_positions);

                    line_pos -= LINE_HEIGHT;//next line
                }
            }
            IO.log(TAG, IO.TAG_VERBOSE, "successfully processed quote resources.");
        } else IO.log(TAG, IO.TAG_WARN, "quote ["+quote.get_id()+"] has no resources.");

        if(quote.getServices()!=null)
        {
            /** Render quote services **/
            for (QuoteService service : quote.getServices())
            {
                item_index++;//increment major index
                //render service items

                //add blank line
                line_pos = addTextToPageStream(document, "", PDType1Font.COURIER, 9, 10, PAGE_MARGINS.left, line_pos, no_border, col_positions);
                line_pos -= LINE_HEIGHT;//next line

                //render service index
                line_pos = addTextToPageStream(document, String.valueOf(item_index), PDType1Font.COURIER_BOLD, 12, page_content_max_width, col_positions[0]+text_offset, line_pos, no_border, col_positions);

                //render service title
                line_pos = addTextToPageStream(document, service.getService().getService_title(), PDType1Font.COURIER_BOLD, 11, col_positions[2]-col_positions[1], col_positions[1] + text_offset, line_pos, no_border, col_positions);

                line_pos -= LINE_HEIGHT;//next line

                if (service.getService() == null) {
                    IO.log(PDF.class.getName(), IO.TAG_WARN, "quote service [" + service.get_id() + "]'s service object for quote [" + quote.get_id() + "] is invalid.");
                    continue;
                }
                if (service.getService().getServiceItemsMap() == null) {
                    IO.log(PDF.class.getName(), IO.TAG_WARN, "quote service [" + service.get_id() + "] for quote [" + quote.get_id() + "] services are invalid.");
                    continue;
                }
                if (service.getService().getServiceItemsMap().isEmpty()) {
                    IO.log(PDF.class.getName(), IO.TAG_WARN, "quote service [" + service.get_id() + "] for quote [" + quote.get_id() + "] has no services.");
                    continue;
                }

                int item_index_minor = 0;//reset minor index
                for (ServiceItem service_item : service.getService().getServiceItemsMap().values())
                {
                    item_index_minor += 1;//increment minor item index
                    //render service items

                    /**begin rendering actual quote service information**/
                    //Border quote_items_border = new Border(Border.BORDER_RIGHT, Color.BLACK, 1, new Insets(0, 0, 0, text_offset));

                    //first column, Item number
                    line_pos = addTextToPageStream(document, String.valueOf(item_index) + "." + String.valueOf(item_index_minor), PDType1Font.COURIER, 12, page_content_max_width, col_positions[0]+text_offset, line_pos, no_border, col_positions);

                    //Description col //int new_line_pos
                    line_pos = addTextToPageStream(document, service_item.getItem_name(), PDType1Font.COURIER, 9, col_positions[2]-col_positions[1], col_positions[1] + text_offset, line_pos, no_border, col_positions);
                    System.out.println("current line position: "+ line_pos);

                    //Unit col
                    line_pos = addTextToPageStream(document, service_item.getUnit(), PDType1Font.COURIER, 8, col_positions[3]-col_positions[2], col_positions[2]+text_offset, line_pos, no_border, col_positions);

                    //Quantity col
                    line_pos = addTextToPageStream(document, String.valueOf(service_item.getQuantity()), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[3]+text_offset, line_pos, no_border, col_positions);

                    //Rate col
                    line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(service_item.getItem_rate())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[4]+text_offset, line_pos, no_border, col_positions);

                    //Total col
                    sub_total += service_item.getTotal();
                    line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(service_item.getTotal())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[5]+text_offset, line_pos, no_border, col_positions);

                    line_pos -= LINE_HEIGHT;//next line
                }
            }
        } else IO.log(PDF.class.getName(), IO.TAG_WARN,"quote ["+quote.get_id()+"] has no services.");

        //render notes
        if(quote.getOther()!=null)
        {
            Border border = new Border(Border.BORDER_ALL, Color.CYAN, 2, new Insets(-1, -1*text_offset, -1, -1*text_offset));

            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "Notes: ", PDType1Font.TIMES_BOLD, 12, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            String[] other_lines = quote.getOther().split(";");
            for(String line: other_lines)
            {
                line_pos = addTextToPageStream(document, line, PDType1Font.TIMES_ROMAN, 11, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
                //line_pos = addTextToPageStream(document, line, PDType1Font.TIMES_ITALIC, 11, page_content_max_width, PAGE_MARGINS.left + text_offset * 2, line_pos, border, null);
                line_pos -= LINE_HEIGHT;//next line
            }
            line_pos -= LINE_HEIGHT*2;//next 2nd line
        }

        Border border = new Border(Border.BORDER_ALL, new Color(40,40,40), 2, new Insets(-1, -1*text_offset, -1, -1*text_offset));

        line_pos = addTextToPageStream(document, "Sub-Total Excl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 12, (int)(w-PAGE_MARGINS.right-PAGE_MARGINS.left)/2,PAGE_MARGINS.left+text_offset, line_pos, border, null);
        line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, page_content_max_width/2, (int)(text_offset+(w/2)), line_pos, border, null);
        line_pos -= LINE_HEIGHT;//next line

        double vat = sub_total*(quote.getVat()/100);

        line_pos = addTextToPageStream(document, "VAT [" + quote.getVat() + "%]: ", PDType1Font.COURIER_BOLD_OBLIQUE, 12, page_content_max_width/2,PAGE_MARGINS.left+text_offset, line_pos, border, null);
        line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, page_content_max_width/2, (int)(text_offset+(w/2)), line_pos, border, null);
        line_pos -= LINE_HEIGHT;//next line

        line_pos = addTextToPageStream(document, "Total Incl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 12, page_content_max_width/2, PAGE_MARGINS.left+text_offset, line_pos, border, null);
        line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total + vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, page_content_max_width/2, (int)(text_offset+(w/2)), line_pos, border, null);

        line_pos -= LINE_HEIGHT*2;//next 2nd line

        line_pos = addTextToPageStream(document, "TERMS AND CONDITIONS OF SALE", PDType1Font.HELVETICA_BOLD, 12, page_content_max_width,(int)(w/2)-130, line_pos,no_border, null);

        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "*Validity: Quote valid for 24 Hours.", PDType1Font.HELVETICA, 11, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "*Payment Terms: COD / 30 Days on approved accounts.", PDType1Font.HELVETICA, 11, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "*Delivery: 1 - 6 Weeks, subject to stock availability.", PDType1Font.HELVETICA, 11, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "*All pricing quoted, is subject to Rate of Exchange USD=R.", PDType1Font.HELVETICA_BOLD, 11, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "*All goods / equipment remain the property of " + Globals.COMPANY.getValue()+ " until paid for completely.", PDType1Font.HELVETICA, 11, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "*" + Globals.COMPANY.getValue() + " reserves the right to retake possession of all equipment not paid for completely", PDType1Font.HELVETICA, 10, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "  Within the payment term set out above.", PDType1Font.HELVETICA, 11, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "*E & O E", PDType1Font.HELVETICA, 12, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);

        line_pos -= LINE_HEIGHT*2;//next 2nd line
        line_pos = addTextToPageStream(document, "Acceptance (Full Name):______________________", PDType1Font.HELVETICA, 12, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos = addTextToPageStream(document, "Signature :_____________________", PDType1Font.HELVETICA, 12, page_content_max_width,(int) (w/2)+15, line_pos,no_border, null);
        line_pos -= LINE_HEIGHT;//next line
        line_pos = addTextToPageStream(document, "Order / Reference No.:________________________", PDType1Font.HELVETICA, 12, page_content_max_width,PAGE_MARGINS.left+text_offset, line_pos,no_border, null);
        line_pos = addTextToPageStream(document, "Date :_________________________", PDType1Font.HELVETICA, 12, page_content_max_width,(int) (w/2)+15, line_pos,no_border, null);

        //draw page numbers
        for(int i=0;i<document.getNumberOfPages();i++)
        {
            PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(i), PDPageContentStream.AppendMode.APPEND, true);
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, w/2, 20));
            contentStream.showText("Page " + (i+1) + " of " + document.getNumberOfPages());
            contentStream.endText();
            //close current page's stream writer
            contentStream.close();
        }

        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);
        
        String path = "out/pdf/quote_" + quote.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/quote_" + quote.get_id() + "." + i + ".pdf";
            i++;
        }

        contents_stream.close();
        page.getContents().close();

        document.save(path);
        document.close();

        return path;
    }

    public static String createInvoicePdf(Invoice invoice) throws IOException//, HashMap<String, Quote> quote_revisions
    {
        if(invoice==null)
        {
            IO.logAndAlert("PDF Viewer", "Invoice object passed is null.", IO.TAG_ERROR);
            return null;
        }
        if(invoice.quoteRevisions()==null)
        {
            IO.logAndAlert("PDF Viewer", "Invalid Quote revisions map.", IO.TAG_ERROR);
            return null;
        }
        if(invoice.getJob()==null)
        {
            IO.logAndAlert("PDF Viewer", "Invoice->Job object passed is null.", IO.TAG_ERROR);
            return null;
        }
        if(invoice.getJob().getQuote()==null)
        {
            IO.logAndAlert("PDF Viewer", "Invoice->Quote object passed is null.", IO.TAG_ERROR);
            return null;
        }
        Job job = invoice.getJob();
        // Create a new document with an empty page.
        PDDocument document = new PDDocument();

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        for(Quote quote: invoice.quoteRevisions().values())
        {
            //Quote quote = invoice.getQuote();
            //Quote[] quotes = invoice.getJob().getQuote().getSortedSiblings("revision");
            //Prepare PDF data from database.
            //Load Invoice Client
            Client client = quote.getClient();
            if (client == null)
            {
                IO.logAndAlert("PDF Viewer Error", "Quote[" + quote
                        .get_id() + "] has no client assigned to it.", IO.TAG_ERROR);
                return null;
            }
            Employee contact = quote.getContact_person();
            if (contact == null)
            {
                IO.logAndAlert("PDF Viewer Error", "Quote has no client contact person assigned to it.", IO.TAG_ERROR);
                return null;
            }

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contents_stream = new PDPageContentStream(document, page);

            Border no_border = new Border(Border.BORDER_NONE, Color.BLACK, 0, new Insets(0, 0, 0, 0));

            int header_h = 90;
            float w = page.getBBox().getWidth();
            float h = page.getBBox().getHeight();
            int line_pos = (int) h - 20;//(int)h-logo_h-20;
            int digit_font_size = 9;
            int col_text_offset = 5;
            int page_content_max_width=(int)(w-PAGE_MARGINS.right-PAGE_MARGINS.left);

            /**Draw lines**/
            //int bottom_line = (int) h - header_h - (ROW_COUNT + 1) * LINE_HEIGHT;
            createLinesAndBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, PAGE_MARGINS.bottom, PAGE_MARGINS.right), (int)w);

            /** begin text from the top**/
            line_pos -= LINE_HEIGHT / 2;

            //left text
            //contents.beginText();
            int temp_pos = line_pos;
            addTextToPageStream(document, "Invoice ID: " + invoice
                    .get_id(), PDType1Font.COURIER_BOLD_OBLIQUE, 15, 20, line_pos);
            line_pos -= LINE_HEIGHT;
            int center_vert_line_start = line_pos;
            addTextToPageStream(document, "Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date(System.currentTimeMillis()))), 12, 20, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Date Logged:  " + (new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date(job.getDate_logged()))), 12, 20, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Invoice Creator:  " + invoice.getCreatorEmployee().getName(), 12, 20, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Invoice ID: " + invoice.get_id(), 14, 20, line_pos);
            addTextToPageStream(document, "Quote ID: " + quote.get_id(), 14, (int)(w/2)+10, line_pos);

            line_pos = temp_pos;

            //right content
            PDImageXObject header = PDImageXObject.createFromFile(header_path, document);
            //right text
            //addTextToPageStream(contents,"PURCHASE ORDER", PDType1Font.COURIER_BOLD_OBLIQUE, 17, (int)(w/2)+20, line_pos);
            //line_pos-=LINE_HEIGHT;//next line
            //System.out.println(">>>>>>"+new File("images/logo.png").getPath());
            //System.out.println(">>>>>>"+(new File(logo_path).exists()));
            //PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
            contents_stream.drawImage(header, 0, 760, w, header_h);
            //contents.drawImage(logo, (int) (w / 2) + 20, line_pos - logo_h - 10, 150, logo_h);

            line_pos -= LINE_HEIGHT * 5;
            temp_pos = line_pos;

            //horizontal solid line after company logo
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos + LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos + LINE_HEIGHT / 2);
            contents_stream.stroke();

            //horizontal solid line after consultants heading
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos - LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos - LINE_HEIGHT / 2);
            contents_stream.stroke();
            //contents.beginText();

            //left text
            addTextToPageStream(document, "Client Information", PDType1Font.COURIER_BOLD_OBLIQUE, 15, 20, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Company: " + client.getClient_name(), 12, 20, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Company Tel: " + client.getTel(), 12, 20, line_pos);

            line_pos = temp_pos;

            //right content
            addTextToPageStream(document, "Job Number #" + job.getObject_number(),
                                    PDType1Font.COURIER_BOLD_OBLIQUE, 15, (int) (w / 2) + 5, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date(job.getDate_logged()))), 12, (int) (w / 2) + 5, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Date Started:  " + (new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date(job.getDate_started()))), 12, (int) (w / 2) + 5, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Date Completed:  " + (new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date(job.getDate_completed()))), 12, (int) (w / 2) + 5, line_pos);
            line_pos -= LINE_HEIGHT;
            addTextToPageStream(document, "Job Creator:  " + job.getCreatorEmployee().getName(), 12, (int) (w / 2) + 5, line_pos);
            //contents.endText();
            //PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
            //contents.drawImage(logo, (int)(w/2)+ 20, line_pos-logo_h, 150, logo_h);

            line_pos -= LINE_HEIGHT;

            //horizontal solid line after job details
            //contents.endText();
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos - LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos - LINE_HEIGHT / 2);
            contents_stream.stroke();
            //contents.beginText();

            //horizontal solid line after company details
            //contents.endText();
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos - LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos - LINE_HEIGHT / 2);
            contents_stream.stroke();
            //contents.beginText();

            line_pos -= LINE_HEIGHT;//next line

            temp_pos = line_pos;
            //left text
            addTextToPageStream(document, "Contact Person:  " + contact
                    .getName(), PDType1Font.HELVETICA_BOLD, 12, 20, line_pos);
            line_pos -= LINE_HEIGHT;//next line
            addTextToPageStream(document, "Tel    :  " + contact
                    .getTel(), PDType1Font.HELVETICA_BOLD, 12, 30, line_pos);
            line_pos -= LINE_HEIGHT;//next line
            addTextToPageStream(document, "Cell   :  " + contact
                    .getCell(), PDType1Font.HELVETICA_BOLD, 12, 30, line_pos);
            line_pos -= LINE_HEIGHT;//next line
            addTextToPageStream(document, "eMail :  " + contact
                    .getEmail(), PDType1Font.HELVETICA_BOLD, 12, 30, line_pos);

            //horizontal solid line
            //contents.endText();
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos - LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos - LINE_HEIGHT / 2);
            contents_stream.stroke();
            //contents.beginText();

            line_pos -= LINE_HEIGHT;//next line (for external consultants)
            //temp_pos-=LINE_HEIGHT;//next line (for internal consultants)
            //Render sale representatives
            int int_rep_count = 0;
            if(invoice.getJob().getQuote().getCreatorEmployee()!=null)
            {
                addTextToPageStream(document, "Invoice Created By:  " + invoice.getJob().getQuote().getCreatorEmployee().getFirstname()
                        +" "+invoice.getJob().getQuote().getCreatorEmployee().getLastname(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 5, temp_pos);
                temp_pos -= LINE_HEIGHT;//next line
                addTextToPageStream(document, "Tel    :  " + invoice.getJob().getQuote().getCreatorEmployee()
                        .getTel(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 5, temp_pos);
                temp_pos -= LINE_HEIGHT;//next line
                addTextToPageStream(document, "Cell   :  " + invoice.getJob().getQuote().getCreatorEmployee()
                        .getCell(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 5, temp_pos);
                temp_pos -= LINE_HEIGHT;//next line
                addTextToPageStream(document, "eMail :  " + invoice.getJob().getQuote().getCreatorEmployee()
                        .getEmail(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 5, temp_pos);
                temp_pos -= LINE_HEIGHT;//next line
            }
            //set the cursor to the line after the sale/client rep info
            line_pos = line_pos < temp_pos ? line_pos : temp_pos;
            addTextToPageStream(document, "Request: " + quote.getRequest(), PDType1Font.HELVETICA, 13, 20, line_pos);
            line_pos -= LINE_HEIGHT;//next line

            //horizontal solid line after reps
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos + LINE_HEIGHT + LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos + LINE_HEIGHT + LINE_HEIGHT / 2);
            contents_stream.stroke();
            //horizontal solid line after request
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos + LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos + LINE_HEIGHT / 2);
            contents_stream.stroke();
            //solid horizontal line after site location, before quote_items
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, (line_pos - LINE_HEIGHT + (int) Math.ceil(LINE_HEIGHT / 2)));
            contents_stream.lineTo(w - 10, (line_pos - LINE_HEIGHT + (int) Math.ceil(LINE_HEIGHT / 2)));
            contents_stream.stroke();

            int col_divider_start = line_pos - LINE_HEIGHT;

            //vertical line going through center of page
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo((w / 2), center_vert_line_start + LINE_HEIGHT / 2);
            contents_stream.lineTo((w / 2), (col_divider_start + LINE_HEIGHT * 2 + (int) Math.ceil(LINE_HEIGHT / 2)));
            contents_stream.stroke();
            //
            contents_stream.moveTo((w / 2), (col_divider_start + (int) Math.ceil(LINE_HEIGHT / 2)));
            contents_stream.lineTo((w / 2), (col_divider_start - LINE_HEIGHT + (int) Math.ceil(LINE_HEIGHT / 2)));
            contents_stream.stroke();

            //contents.beginText();
            addTextToPageStream(document, "Site Location: " + quote
                    .getSitename(), PDType1Font.HELVETICA, 13, 20, line_pos);
            line_pos -= LINE_HEIGHT;//next line

            //contents.endText();
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, (line_pos - LINE_HEIGHT + (int) Math.ceil(LINE_HEIGHT / 2)));
            contents_stream.lineTo(w - 10, (line_pos - LINE_HEIGHT + (int) Math.ceil(LINE_HEIGHT / 2)));
            contents_stream.stroke();

            //define column headings
            String[] cols = {"Item", "Description", "Unit", "Qty", "Rate", "Total"};
            //column heading positions
            int[] col_positions = {10, 10+35, (int) (w/2), (int) (w/2)+55, (int) (w/2)+85, (int) (w/2)+170};

            //render column headings
            for(int i=0;i<cols.length;i++)//6 cols in total
                line_pos = addTextToPageStream(document, cols[i], PDType1Font.COURIER_BOLD, 12, page_content_max_width,col_positions[i]+col_text_offset, line_pos, no_border, col_positions);
            line_pos-=LINE_HEIGHT;//next line

            //horizontal bold line after column headings
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(col_positions[0], (line_pos+(int)Math.ceil(LINE_HEIGHT/2)));
            contents_stream.lineTo(w-10, (line_pos+(int)Math.ceil(LINE_HEIGHT/2)));
            contents_stream.stroke();

            //add blank line
            line_pos = addTextToPageStream(document, "", PDType1Font.COURIER, 12, 10, 10, line_pos, no_border, col_positions);
            line_pos -= LINE_HEIGHT;//next line

            //render quote request
            line_pos = addTextToPageStream(document, quote.getRequest(), PDType1Font.COURIER, 12, col_positions[2]-col_positions[1], col_positions[1]+col_text_offset, line_pos,no_border, col_positions);
            line_pos -= LINE_HEIGHT;//next line

            //Actual quote information
            //col_pos = col_positions[0];
            double sub_total = 0;
            int item_index = 0;
            if(quote.getResources()!=null)
            {
                //group quote materials by category
                HashMap<String, ArrayList<QuoteItem>> quoteItemsCategoriesMap = new HashMap<>();
                for(QuoteItem item: quote.getResources())
                {
                    if(quoteItemsCategoriesMap.get(item.getCategory())==null)//if material category is first of its type (i.e. has no bucket of materials)
                        quoteItemsCategoriesMap.put(item.getCategory(), new ArrayList<>());//instantiate the category's bucket
                    ArrayList cat_list = quoteItemsCategoriesMap.get(item.getCategory());//get current material's respective category's bucket of materials
                    cat_list.add(item);//add current material to respective category's bucket of materials.
                }
                if(quoteItemsCategoriesMap.isEmpty())
                {
                    IO.log(PDF.class.getName(), IO.TAG_ERROR, "quote ["+quote.get_id()+"] has no resources.");
                    return null;
                }
                //for each category, list that category's QuoteItems (materials/resources)
                for(ArrayList<QuoteItem> items: quoteItemsCategoriesMap.values())//go through all categories
                {
                    if(items==null)
                        continue;
                    if(items.isEmpty())
                        continue;

                    item_index++;//increment major index

                    //add blank line
                    line_pos = addTextToPageStream(document, "", PDType1Font.COURIER, 12, 10, 10, line_pos, no_border, col_positions);
                    line_pos -= LINE_HEIGHT;//next line

                    //render category's index
                    line_pos = addTextToPageStream(document, String.valueOf(item_index), PDType1Font.COURIER_BOLD, 14, page_content_max_width, 15, line_pos,no_border, null);
                    //render category name
                    if(items.get(0).getCategory()!=null)
                        line_pos = addTextToPageStream(document,items.get(0).getCategory(), PDType1Font.COURIER_BOLD,14, page_content_max_width,50, line_pos,no_border, null);

                    line_pos -= LINE_HEIGHT;//next line

                    int item_index_minor =0;//reset minor index
                    for (QuoteItem item : items)
                    {
                        item_index_minor++;

                        /**begin rendering actual quote material information**/
                        //first column, Item number
                        line_pos = addTextToPageStream(document, String.valueOf(item_index)+"."+String.valueOf(item_index_minor), PDType1Font.COURIER, 12, page_content_max_width, col_positions[0]+col_text_offset, line_pos,no_border, null);//item.getItem_number()

                        //Description col
                        line_pos = addTextToPageStream(document, item.getResource().getResource_description(), PDType1Font.COURIER, 12, col_positions[2]-col_positions[1], col_positions[1] + col_text_offset, line_pos,no_border, null);

                        //Unit col
                        line_pos = addTextToPageStream(document, item.getUnit(), PDType1Font.COURIER, 12, col_positions[3]-col_positions[2], col_positions[2]+col_text_offset, line_pos,no_border, null);//col_pos + 5

                        //Quantity col
                        line_pos = addTextToPageStream(document, item.getQuantity(), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[3]+col_text_offset, line_pos,no_border, null);

                        //Rate col
                        line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getRate())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[4]+col_text_offset, line_pos,no_border, null);

                        //Total col
                        sub_total += item.getTotal();
                        line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getTotal())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[5]+col_text_offset, line_pos,no_border, null);

                        line_pos -= LINE_HEIGHT;//next line
                    }
                }
                IO.log(TAG, IO.TAG_VERBOSE, "successfully processed quote resources.");
            } else IO.log(TAG, IO.TAG_INFO, "quote has no resources.");

            if(quote.getServices()!=null)
            {
                /** Render quote services **/
                for (QuoteService service : quote.getServices())
                {
                    item_index++;//increment major index
                    //render service items

                    //add blank line
                    line_pos = addTextToPageStream(document, "", PDType1Font.COURIER, 12, 10, 10, line_pos, no_border, col_positions);
                    line_pos -= LINE_HEIGHT;//next line

                    //render service index
                    line_pos = addTextToPageStream(document, String.valueOf(item_index), PDType1Font.COURIER_BOLD, 14, page_content_max_width, col_positions[0]+col_text_offset, line_pos, no_border, col_positions);

                    //render service title
                    line_pos = addTextToPageStream(document, service.getService().getService_title(), PDType1Font.COURIER_BOLD, 14, col_positions[2]-col_positions[1], col_positions[1] + col_text_offset, line_pos, new Border(Border.BORDER_RIGHT, Color.RED, 2, new Insets(0, 0, 0, col_text_offset)), col_positions);

                    line_pos -= LINE_HEIGHT;//next line

                    if (service.getService() == null) {
                        IO.log(PDF.class.getName(), IO.TAG_WARN, "quote service [" + service.get_id() + "]'s service object for quote [" + quote.get_id() + "] is invalid.");
                        continue;
                    }
                    if (service.getService().getServiceItemsMap() == null) {
                        IO.log(PDF.class.getName(), IO.TAG_WARN, "quote service [" + service.get_id() + "] for quote [" + quote.get_id() + "] services are invalid.");
                        continue;
                    }
                    if (service.getService().getServiceItemsMap().isEmpty()) {
                        IO.log(PDF.class.getName(), IO.TAG_WARN, "quote service [" + service.get_id() + "] for quote [" + quote.get_id() + "] has no services.");
                        continue;
                    }

                    int item_index_minor = 0;//reset minor index
                    for (ServiceItem service_item : service.getService().getServiceItemsMap().values())
                    {
                        item_index_minor += 1;//increment minor item index
                        //render service items

                        /**begin rendering actual quote service information**/
                        Border quote_items_border = new Border(Border.BORDER_RIGHT, Color.BLACK, 1, new Insets(0, 0, 0, col_text_offset));

                        //first column, Item number
                        line_pos = addTextToPageStream(document, String.valueOf(item_index) + "." + String.valueOf(item_index_minor), PDType1Font.COURIER, 12, page_content_max_width, col_positions[0]+col_text_offset, line_pos,no_border, col_positions);

                        //Description col //int new_line_pos
                        line_pos = addTextToPageStream(document, service_item.getItem_name(), PDType1Font.COURIER, 12, col_positions[2]-col_positions[1], col_positions[1] + col_text_offset, line_pos, no_border, col_positions);
                        System.out.println("current line position: "+ line_pos);

                        //Unit col
                        line_pos = addTextToPageStream(document, service_item.getUnit(), PDType1Font.COURIER, 8, col_positions[3]-col_positions[2], col_positions[2]+col_text_offset, line_pos, no_border, col_positions);

                        //Quantity col
                        line_pos = addTextToPageStream(document, String.valueOf(service_item.getQuantity()), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[3]+col_text_offset, line_pos, no_border, col_positions);

                        //Rate col
                        line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(service_item.getItem_rate())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[4]+col_text_offset, line_pos,no_border, col_positions);

                        //Total col
                        sub_total += service_item.getTotal();
                        line_pos = addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance().format(service_item.getTotal())), PDType1Font.COURIER, digit_font_size, page_content_max_width, col_positions[5]+col_text_offset, line_pos,no_border, col_positions);

                        line_pos -= LINE_HEIGHT;//next line
                    }
                }
            } else IO.log(PDF.class.getName(), IO.TAG_WARN,"quote ["+quote.get_id()+"] has no services.");

            //render notes
            if(quote.getOther()!=null)
            {
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, "Notes: ", PDType1Font.TIMES_BOLD, 14, page_content_max_width, 15, line_pos,no_border, null);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, quote.getOther(), PDType1Font.TIMES_ITALIC, 12, page_content_max_width,15, line_pos,no_border, null);
                line_pos -= LINE_HEIGHT*2;//next 2nd line
            }
            int col_pos = 0;

            //solid horizontal line
            int col_divider_end = line_pos;

            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos + LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos + LINE_HEIGHT / 2);
            contents_stream.stroke();

            addTextToPageStream(document, "Sub-Total Excl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14, col_pos + 30, line_pos);
            addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance()
                    .format(sub_total)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int) (5 + (w / 2)), line_pos);
            line_pos -= LINE_HEIGHT;//next line

            //solid horizontal line
            //contents.endText();
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos + LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos + LINE_HEIGHT / 2);
            contents_stream.stroke();

            double vat = sub_total * (quote.getVat() / 100);

            addTextToPageStream(document, "VAT[" + quote
                    .getVat() + "%]: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14, col_pos + 30, line_pos);
            addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance()
                    .format(vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int) (5 + (w / 2)), line_pos);
            line_pos -= LINE_HEIGHT;//next line

            //solid horizontal line
            //contents.endText();
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos + LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos + LINE_HEIGHT / 2);
            contents_stream.stroke();


            addTextToPageStream(document, "Total Incl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14, col_pos + 30, line_pos);
            addTextToPageStream(document, String.valueOf(DecimalFormat.getCurrencyInstance()
                    .format(sub_total + vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int) (5 + (w / 2)), line_pos);

            line_pos -= LINE_HEIGHT;//next line

            //solid horizontal line
            contents_stream.setStrokingColor(Color.BLACK);
            contents_stream.moveTo(10, line_pos + LINE_HEIGHT / 2);
            contents_stream.lineTo(w - 10, line_pos + LINE_HEIGHT / 2);
            contents_stream.stroke();

            //int col_divider_end = line_pos;
            line_pos -= LINE_HEIGHT * 3;//next 3rd line

            if (quote.getOther() != null)
                addTextToPageStream(document, "P.S. " + quote
                        .getOther(), PDType1Font.TIMES_ITALIC, 14, col_pos + 5, line_pos);

            line_pos -= LINE_HEIGHT;//next line

            line_pos = addTextToPageStream(document, "TERMS AND CONDITIONS OF SALE", PDType1Font.HELVETICA_BOLD, 14, page_content_max_width,(int)(w/2)-130, line_pos,no_border, null);

            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "*Validity: Quote valid for 24 Hours.", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "*Payment Terms: COD / 30 Days on approved accounts. ", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "*Delivery: 1 - 6 Weeks, subject to stock availability.", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "*All pricing quoted, is subject to Rate of Exchange USD=R.", PDType1Font.HELVETICA_BOLD, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "*All goods / equipment remain the property of " + Globals.COMPANY.getValue()+ " until paid for completely. ", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "*" + Globals.COMPANY.getValue() + " reserves the right to retake possession of all equipment not paid for completely", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "  Within the payment term set out above.", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "*E & O E", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);

            line_pos -= LINE_HEIGHT*2;//next 2nd line
            line_pos = addTextToPageStream(document, "Acceptance (Full Name):______________________", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos = addTextToPageStream(document, "Signature :_____________________", PDType1Font.HELVETICA, 12, page_content_max_width,(int) (w/2)+col_text_offset, line_pos,no_border, null);
            line_pos -= LINE_HEIGHT;//next line
            line_pos = addTextToPageStream(document, "Order / Reference No.:________________________", PDType1Font.HELVETICA, 12, page_content_max_width,15, line_pos,no_border, null);
            line_pos = addTextToPageStream(document, "Date :_________________________", PDType1Font.HELVETICA, 12, page_content_max_width,(int) (w/2)+col_text_offset, line_pos,no_border, null);

            if(contents_stream!=null)
                contents_stream.close();
        }

        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);

        String path = "out/pdf/invoice_" + job.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/invoice_" + job.get_id() + "." + i + ".pdf";
            i++;
        }

        document.save(path);
        document.close();

        return path;
    }

    public static String createJobCardPdf(Job job) throws IOException
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert(TAG, "Active session object is null.", IO.TAG_ERROR);
            return null;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert(TAG, "Active session has expired.", IO.TAG_ERROR);
            return null;
        }
        if(job==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "Job object passed is null.");
            return null;
        }
        if(job.getQuote()==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "Job's Quote object is null.");
            return null;
        }
        if(job.getQuote().getClient()==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "Job Quote's Client object is null.");
            return null;
        }
        if(job.getAssigned_employees()==null)
        {
            IO.logAndAlert("Error", "Job[#"+job.getObject_number()+"] has not been assigned any employees, please fix this and try again.", IO.TAG_ERROR);
            return null;
        }
        if(job.getAssigned_employees().length<=0)
        {
            IO.logAndAlert("Error", "Job[#"+job.getObject_number()+"] has not been assigned any employees, please fix this and try again.", IO.TAG_ERROR);
            return null;
        }

        //ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        //headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
        //Client client;
        //String client_json = null;//RemoteComms.sendGetRequest("/api/client/" + job.getClient_id(), headers);
        //client = new GsonBuilder().create().fromJson(client_json, Client.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        //PDPage page = new PDPage(PDRectangle.A4);
        //document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        int header_h = 90;

        PDPageContentStream contents_stream = null;// = new PDPageContentStream(document, page);

        if(job.getAssigned_employees()!=null)
        {
            PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);

            for (Employee employee : job.getAssigned_employees())
            {
                //contents.close();
                final PDPage new_page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
                //Add page to document
                document.addPage(new_page);
                contents_stream = new PDPageContentStream(document, new_page);
                //contents.setFont(font, 14);

                //line_pos = (int)h-logo_h-20;
                IO.log("Job PDF Exporter", IO.TAG_INFO, "added new page.");
                int logo_h = 60;
                float w = new_page.getBBox().getWidth();
                float h = new_page.getBBox().getHeight();
                int line_pos = (int) h - logo_h - LINE_HEIGHT;
                final int VERT_LINE_START = line_pos;
                int text_offset = 5;
                int digit_font_size = 9;

                PDImageXObject header = PDImageXObject.createFromFile(header_path, document);
                contents_stream.drawImage(header, PAGE_MARGINS.left, h-header_h+10, w-PAGE_MARGINS.right-PAGE_MARGINS.left, header_h);//new_page.getBBox().getHeight()
                //contents.drawImage(logo, (int) (w / 2) - 80, 770, 160, logo_h);
                int page_content_max_width = (int)(w-PAGE_MARGINS.right-PAGE_MARGINS.left);
                Border no_border = new Border(Border.BORDER_NONE, Color.BLACK, 0, new Insets(0, 0, 0, 0));

                /**Draw lines**/
                //createLinesAndBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT, PAGE_MARGINS.right), (int)w);
                //createBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT, PAGE_MARGINS.right), (int)w);
                //createLinesAndBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, PAGE_MARGINS.top, PAGE_MARGINS.right), (int)w);
                //createBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, PAGE_MARGINS.top, PAGE_MARGINS.right), (int)w);
                createLinesAndBordersOnPage(contents_stream, new Insets(line_pos, PAGE_MARGINS.left, PAGE_MARGINS.bottom+35, PAGE_MARGINS.right), (int)w);

                /** begin text from the top**/
                //contents.beginText();
                //contents.setFont(font, 12);
                line_pos -= LINE_HEIGHT/2;
                int title_font_size = 14;
                String str_job_card = job.getQuote().getClient().getClient_name() + ": "
                        + job.getQuote().getSitename() + " JOB CARD";

                float str_w = (PDType1Font.COURIER.getStringWidth(str_job_card)/1000)*title_font_size;

                line_pos = addTextToPageStream(document, str_job_card, PDType1Font.HELVETICA, title_font_size, page_content_max_width, (int) ((w/2)-(str_w/2)), line_pos, no_border, null);
                line_pos -= LINE_HEIGHT * 2;//next line

                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos+LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos+LINE_HEIGHT/2);
                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);

                line_pos = addTextToPageStream(document, "ISO 9001:2008", PDType1Font.HELVETICA, 12, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos, no_border, null);

                line_pos = addTextToPageStream(document, "Effective Date: " + formatter.format(new Date(System.currentTimeMillis())), PDType1Font.HELVETICA, 12, page_content_max_width, (int) (w/2)-160, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);

                line_pos = addTextToPageStream(document, "Authorized By: ", PDType1Font.HELVETICA, 12, page_content_max_width, (int) (w/2)+40, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);

                line_pos -= LINE_HEIGHT;//next line
                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);

                line_pos = addTextToPageStream(document, "JOB NUMBER: " + job.getObject_number(), PDType1Font.HELVETICA, 11, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, "DATE LOGGED: " + (job.getDate_logged()>0?LocalDate.parse(formatter.format(new Date(job.getDate_logged()))):"N/A"), PDType1Font.HELVETICA, 11, page_content_max_width, (int)(w/2)-70, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);

                line_pos -= LINE_HEIGHT;//next line
                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);

                //(int) (w-PAGE_MARGINS.right-(w/2)-70)
                line_pos = addTextToPageStream(document, "CUSTOMER: " + job.getQuote().getClient().getClient_name(), PDType1Font.HELVETICA, 11, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, "ADDRESS: " + job.getQuote().getClient().getPhysical_address(), PDType1Font.HELVETICA, 11, page_content_max_width/2, (int)(w/2)-70, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);

                line_pos -= LINE_HEIGHT;//next line
                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);

                line_pos = addTextToPageStream(document, "SITE: " + job.getQuote().getSitename(), PDType1Font.HELVETICA, 11, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, "CONTACT: " + job.getQuote().getContact_person(), PDType1Font.HELVETICA, 11, page_content_max_width, (int)(w/2)-70, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);
                line_pos = addTextToPageStream(document, "TEL: " + job.getQuote().getContact_person().getTel(), PDType1Font.HELVETICA, 11, page_content_max_width, (int)w-170, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);

                line_pos -= LINE_HEIGHT;//next line
                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);

                line_pos = addTextToPageStream(document, "REQUEST: " + job.getQuote().getRequest(), PDType1Font.HELVETICA, 11, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos, no_border, null);

                line_pos -= LINE_HEIGHT;//next line
                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);

                /*line_pos = addTextToPageStream(document, "START DATE: " + (job.getDate_started()>0?LocalDate.parse(formatter.format(new Date(job.getDate_started()))):"N/A"), PDType1Font.HELVETICA, 11, page_content_max_width, (int)(w/2)-70, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);
                line_pos = addTextToPageStream(document, "COMP DATE: " + (job.isJob_completed()?LocalDate.parse(formatter.format(new Date(job.getDate_completed()))):"N/A"), PDType1Font.HELVETICA, 11, page_content_max_width, (int)w-170, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);*/
                line_pos = addTextToPageStream(document, "TECHNICIAN: ", PDType1Font.HELVETICA, 11, page_content_max_width, PAGE_MARGINS.left+text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, "START DATE: ", PDType1Font.HELVETICA, 11, page_content_max_width, (int)(w/2)-70, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);
                line_pos = addTextToPageStream(document, "COMP DATE: ", PDType1Font.HELVETICA, 11, page_content_max_width, (int)w-170, line_pos, new Border(Border.BORDER_LEFT, Color.BLACK, 1, new Insets(0,text_offset,0,0)), null);

                line_pos -= LINE_HEIGHT;//next line
                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);


                //addTextToPageStream(document, "STATUS: " + (job.isJob_completed()?"completed":"pending"), 12, (int)(w/2)+30, line_pos);

                //draw horizontal line
                createBordersOnPage(contents_stream, new Insets(line_pos+LINE_HEIGHT/2, PAGE_MARGINS.left, line_pos+LINE_HEIGHT/2, PAGE_MARGINS.right), (int)w);//line_pos-LINE_HEIGHT/2

                int[] col_positions = new int[]{PAGE_MARGINS.left+text_offset+60,
                                                PAGE_MARGINS.left+text_offset+130,
                                                PAGE_MARGINS.left+text_offset+200,
                                                (int) (w-PAGE_MARGINS.right-350),//PAGE_MARGINS.left+text_offset+400 //(int) (w/2)
                                                (int) (w-PAGE_MARGINS.right-200),//PAGE_MARGINS.left+text_offset+520
                                                (int) (w-PAGE_MARGINS.right-70)};//PAGE_MARGINS.left+text_offset+600

                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "DATE ", PDType1Font.HELVETICA_BOLD, 11, col_positions[0] - PAGE_MARGINS.left - text_offset, PAGE_MARGINS.left + text_offset, line_pos, no_border, col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "TIME IN ", PDType1Font.HELVETICA_BOLD, 11, col_positions[1] - col_positions[0] - text_offset, col_positions[0] + text_offset, line_pos, no_border, col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "TIME OUT ", PDType1Font.HELVETICA_BOLD, 11, col_positions[2] - col_positions[1] - text_offset, col_positions[1] + text_offset, line_pos, no_border, col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "DESCRIPTION OF WORK DONE ", PDType1Font.HELVETICA_BOLD, 10, col_positions[3] - col_positions[2] - text_offset, col_positions[2] + text_offset, line_pos, no_border, col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Materials Used", PDType1Font.HELVETICA_BOLD, 11, col_positions[4] - col_positions[3] - text_offset, col_positions[3] + text_offset, line_pos, no_border, col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Model/Serial", PDType1Font.HELVETICA_BOLD, 11, col_positions[5] - col_positions[4] - text_offset, col_positions[4] + text_offset, line_pos, no_border, col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Quantity", PDType1Font.HELVETICA_BOLD, 11, (int) (w - PAGE_MARGINS.right - col_positions[5] - text_offset), col_positions[5] + text_offset, line_pos, no_border, col_positions);

                //render 5 blank lines
                for(int i=0;i<5;i++)
                {
                    line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), " ", PDType1Font.HELVETICA_BOLD, 11, col_positions[0] - PAGE_MARGINS.left - text_offset, PAGE_MARGINS.left + text_offset, line_pos, no_border, col_positions);
                    line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), " ", PDType1Font.HELVETICA_BOLD, 11, col_positions[1] - col_positions[0] - text_offset, col_positions[0] + text_offset, line_pos, no_border, col_positions);
                    line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), " ", PDType1Font.HELVETICA_BOLD, 11, col_positions[2] - col_positions[1] - text_offset, col_positions[1] + text_offset, line_pos, no_border, col_positions);
                    line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), " ", PDType1Font.HELVETICA_BOLD, 10, col_positions[3] - col_positions[2] - text_offset, col_positions[2] + text_offset, line_pos, no_border, col_positions);
                    line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), " ", PDType1Font.HELVETICA_BOLD, 11, col_positions[4] - col_positions[3] - text_offset, col_positions[3] + text_offset, line_pos, no_border, col_positions);
                    line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), " ", PDType1Font.HELVETICA_BOLD, 11, col_positions[5] - col_positions[4] - text_offset, col_positions[4] + text_offset, line_pos, no_border, col_positions);
                    line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), " ", PDType1Font.HELVETICA_BOLD, 11, (int) (w - PAGE_MARGINS.right - col_positions[5] - text_offset), col_positions[5] + text_offset, line_pos, no_border, col_positions);
                    line_pos -= LINE_HEIGHT;//next line
                }
                //line_pos = LINE_END - LINE_HEIGHT/2;//(int) h - logo_h - LINE_HEIGHT - (LINE_HEIGHT*30) - LINE_HEIGHT/2;
                //line_pos -= LINE_HEIGHT*10;//skip 10 lines

                //render PnGs
                int[] png_col_positions = new int[]
                        {
                            PAGE_MARGINS.left+text_offset+90,
                            PAGE_MARGINS.left+text_offset+180,
                            PAGE_MARGINS.left+text_offset+250,
                            (int) (w-PAGE_MARGINS.right-350)
                        };

                createBordersOnPage(contents_stream, new Insets(line_pos+LINE_HEIGHT/2, PAGE_MARGINS.left, line_pos+LINE_HEIGHT/2, PAGE_MARGINS.right), (int)w);//line_pos-LINE_HEIGHT/2

                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Labour Hours ", PDType1Font.HELVETICA_BOLD, 11, png_col_positions[0] - PAGE_MARGINS.left - text_offset, PAGE_MARGINS.left + text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Travel Hours", PDType1Font.HELVETICA_BOLD, 11, png_col_positions[1] - png_col_positions[0] - text_offset, png_col_positions[0] + text_offset, line_pos, no_border, png_col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Kilometres", PDType1Font.HELVETICA_BOLD, 11, png_col_positions[2] - png_col_positions[1] - text_offset, png_col_positions[1] + text_offset, line_pos, no_border, png_col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Other Staff ", PDType1Font.HELVETICA_BOLD, 10, png_col_positions[3] - png_col_positions[2] - text_offset, png_col_positions[2] + text_offset, line_pos, no_border, png_col_positions);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "PO", PDType1Font.HELVETICA_BOLD, 11, (int) (w - PAGE_MARGINS.right - png_col_positions[3] - text_offset), png_col_positions[3] + text_offset, line_pos, no_border, png_col_positions);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Quote", PDType1Font.HELVETICA_BOLD, 11, (int) (w - PAGE_MARGINS.right - png_col_positions[3] - text_offset), png_col_positions[3] + text_offset, line_pos, no_border, png_col_positions);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Client PO", PDType1Font.HELVETICA_BOLD, 11, (int) (w - PAGE_MARGINS.right - png_col_positions[3] - text_offset), png_col_positions[3] + text_offset, line_pos, no_border, png_col_positions);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "Invoice", PDType1Font.HELVETICA_BOLD, 11, (int) (w - PAGE_MARGINS.right - png_col_positions[3] - text_offset), png_col_positions[3] + text_offset, line_pos, no_border, png_col_positions);

                drawLine(contents_stream, Color.BLACK, PAGE_MARGINS.left, line_pos-LINE_HEIGHT/2, (int) (w-PAGE_MARGINS.right), line_pos-LINE_HEIGHT/2);

                line_pos -= LINE_HEIGHT;//next line

                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "CUSTOMER NOTE: _______________________________________________________________________________________________________________", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + text_offset, line_pos, no_border, null);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "________________________________________________________________________________________________________________________________", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + text_offset, line_pos, no_border, null);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "The authorised signatory agrees that the detailed task above have been performed and completed to the client's satisfaction and " +
                        "acknowledges that all equipment installed remains property of " + Globals.COMPANY.getValue() + " until final payment has been received.", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + text_offset, line_pos, no_border, null);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "CUSTOMER NAME: __________________________", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "CUSTOMER SIGNATURE: ____________________", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + 275 + text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "DESIGNATION: _____________________", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + 540 + text_offset, line_pos, no_border, null);
                line_pos -= LINE_HEIGHT;//next line
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "TECHNICIAN SIGNATURE: ____________________", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + text_offset, line_pos, no_border, null);
                line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), "DATE: ____________________________", PDType1Font.HELVETICA, 11, page_content_max_width-text_offset*2, PAGE_MARGINS.left + 540 + text_offset, line_pos, no_border, null);

                //line_pos -= LINE_HEIGHT*2;//next 2nd line
                //render quote materials
                /*if(job.getQuote().getResources()!=null)
                {
                    for (QuoteItem item : job.getQuote().getResources())
                    {
                        /*addTextToPageStream(document, item.getResource().getResource_description(), 14, 20, line_pos);
                        addTextToPageStream(document, item.getResource().getResource_code(), 14, (int) (w / 2) + 20, line_pos);
                        addTextToPageStream(document, item.getQuantity(), 14, (int) w - 80, line_pos);*
                        line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), item.getResource().getResource_description(), PDType1Font.HELVETICA, 11, (int) (w / 2) + PAGE_MARGINS.left-PAGE_MARGINS.right, PAGE_MARGINS.left+text_offset, line_pos, no_border, col_positions);
                        line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), item.getResource().getResource_code(), PDType1Font.HELVETICA, 11, (int) w - 80 - (int) (w / 2) + PAGE_MARGINS.left + text_offset, col_positions[0] + text_offset, line_pos, no_border, col_positions);
                        line_pos = addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), item.getQuantity(), PDType1Font.HELVETICA, 11, (int) (w - 80), col_positions[1] + text_offset, line_pos, no_border, col_positions);
                        line_pos -= LINE_HEIGHT;//next line
                    }
                }*/

                //render quote services
                /*if(job.getQuote().getServices()!=null)
                {
                    for (QuoteService service : job.getQuote().getServices())
                    {
                        addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), service.getService().getService_title(), 14, 20, line_pos);
                        addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), service.getService().getService_description()!=null?service.getService().getService_description():"N/A", 14, (int) (w / 2) + 20, line_pos);
                        int service_qty = 0;
                        if(service.getService().getServiceItemsMap()!=null)
                            for(ServiceItem serviceItem : service.getService().getServiceItemsMap().values())
                                service_qty+=serviceItem.getQuantity();
                        addTextToPageStream(document, new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()), String.valueOf(service_qty), 14, (int) w - 80, line_pos);
                        line_pos -= LINE_HEIGHT;//next line
                    }
                }*/
                //createBordersOnPage(contents_stream, new Insets(BORDER_START+LINE_HEIGHT/2, PAGE_MARGINS.left, BORDER_START-LINE_HEIGHT/2, PAGE_MARGINS.right), (int)w);
                //createBordersOnPage(contents_stream, new Insets(BORDER_START+LINE_HEIGHT/2, PAGE_MARGINS.left, line_pos+BORDER_START+LINE_HEIGHT/2, PAGE_MARGINS.right), (int)w);

                //contents.close();
            }
        } else
        {
            IO.logAndAlert(TAG, "job " + job.get_id() + " has no assigned employees.", IO.TAG_ERROR);
            return null;
        }

        //draw page numbers & footer text
        for(int i=0;i<document.getNumberOfPages();i++)
        {
            float w = document.getPage(i).getBBox().getWidth();
            PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(i), PDPageContentStream.AppendMode.APPEND, true);
            contentStream.beginText();
            contentStream.setFont(font, 10);

            /* draw footer text */
            //company info
            contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, PAGE_MARGINS.left + 5, 27));
            contentStream.showText(Globals.COMPANY.getValue() + " " + Globals.COMPANY_SECTOR.getValue());
            //center text
            contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, centerText("Form 20: Job Card", 10, (int) w, font), 27));
            contentStream.showText("Form 20: Job Card");
            //date & revision
            String str = "Revision 1.0 : " + formatter.format(new Date(System.currentTimeMillis()));
            contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, (int) (w - PAGE_MARGINS.right - 5 - (PDType1Font.HELVETICA.getStringWidth(str)/1000)*11), 27));
            contentStream.showText(str);

            //draw page number
            str = "Page " + (i+1) + " of " + document.getNumberOfPages();
            contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, centerText(str, 10, (int) w, font), 10));
            contentStream.showText(str);

            contentStream.endText();

            createBordersOnPage(contentStream, new Insets(30+LINE_HEIGHT/2, PAGE_MARGINS.left, 30+LINE_HEIGHT/2, PAGE_MARGINS.right), (int)w);

            //close current page's stream writer
            contentStream.close();
        }


        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);

        //TODO: fix this hack
        String path = "out/pdf/job_card_" + job.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/job_card_" + job.get_id() + "." + i + ".pdf";
            i++;
        }

        if(contents_stream!=null)
            contents_stream.close();
        document.save(path);
        document.close();
        return path;
    }

    public static void createGeneralJournalPdf(long start, long end) throws IOException
    {
        //Init managers and load data to memory
        AssetManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        ExpenseManager.getInstance().initialize();
        InvoiceManager.getInstance().initialize();

        ArrayList<Transaction> transactions = new ArrayList<>();

        //Load assets
        if(AssetManager.getInstance().getDataset()!=null)
            for(Asset asset : AssetManager.getInstance().getDataset().values())
                transactions.add(new Transaction(asset.get_id(), asset.getDate_acquired(), asset));

        //Load Resources/Stock
        if(ResourceManager.getInstance().getDataset()!=null)
            for(Resource resource : ResourceManager.getInstance().getDataset().values())
                transactions.add(new Transaction(resource.get_id(), resource.getDate_acquired(), resource));

        //Load additional Expenses
        if(ExpenseManager.getInstance().getDataset()!=null)
            for(Expense expense: ExpenseManager.getInstance().getDataset().values())
                transactions.add(new Transaction(expense.get_id(), expense.getDate_logged(), expense));

        //Load Service income (Invoices)
        if(InvoiceManager.getInstance().getDataset()!=null)
            for(Invoice invoice: InvoiceManager.getInstance().getDataset().values())
                transactions.add(new Transaction(invoice.get_id(), invoice.getDate_logged(), invoice));

        if(transactions==null)
        {
            IO.logAndAlert("Error", "No transactions could be found.", IO.TAG_ERROR);
            return;
        }

        if(transactions.size()<=0)
        {
            IO.logAndAlert("Error", "No transactions could be found.", IO.TAG_ERROR);
            return;
        }

        Transaction[] transactions_arr = new Transaction[transactions.size()];
        transactions.toArray(transactions_arr);
        long start_ms = System.currentTimeMillis();
        IO.getInstance().quickSort(transactions_arr, 0, transactions.size()-1, "date");
        //Arrays.sort(transactions_arr);
        //transactions_arr = selectionSort(transactions_arr);
        IO.log("PDF Creator> generateGeneralJournal",IO.TAG_INFO, "Sorted in: "+ (System.currentTimeMillis()-start_ms) + "ms");
        IO.log("PDF Creator> generateGeneralJournal",IO.TAG_INFO, "Transaction count: "+ transactions_arr.length);

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents_stream = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        contents_stream.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-logo_h-20;
        int digit_font_size=9;

        /**Draw lines**/
        int center_vert_line_start = line_pos;
        //int bottom_line = (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT;
        //createLinesAndBordersOnPage(contents, (int)w, line_pos, bottom_line);
        drawHorzLines(contents_stream, line_pos, (int) w, new Insets(0,0,80,0));

        /** begin text from the top**/
        //contents.beginText();
        //contents.setFont(font, 12);
        line_pos-=LINE_HEIGHT/2;

        String gj_title = "General Journal";
        String date_start = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(start * 1000)));
        String date_end = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(end * 1000)));
        String date_generated = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(System.currentTimeMillis())));
        addTextToPageStream(document, gj_title, PDType1Font.COURIER_BOLD, 16,(int)(w-250), 830);
        addTextToPageStream(document, "Company: " + Globals.COMPANY.getValue(), PDType1Font.COURIER, 12,(int)(w-250), 815);
        addTextToPageStream(document, "Period: "+date_start+" - " + date_end, PDType1Font.COURIER, 12,(int)(w-200), 800);
        addTextToPageStream(document, "Generated: " + date_generated, PDType1Font.COURIER, 12,(int)(w-200), 785);

        //horizontal solid line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();
        //contents.beginText();
        line_pos-=LINE_HEIGHT;

        addTextToPageStream(document,"Date", PDType1Font.COURIER_BOLD_OBLIQUE, 15,10, line_pos);
        addTextToPageStream(document,"Account", PDType1Font.COURIER_BOLD_OBLIQUE, 15,150, line_pos);
        addTextToPageStream(document,"Debit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-180, line_pos);
        addTextToPageStream(document,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);

        //horizontal solid line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();
        //contents.beginText();

        line_pos-=LINE_HEIGHT;//next line

        int lines=0;
        int pages = 1;
        for(Transaction t : transactions_arr)
        {
            if(t.getDate()>=start && t.getDate()<=end)
            {
                if (lines > 15)
                {
                    //line_pos-=LINE_HEIGHT;
                    addTextToPageStream(document, "page " + pages, PDType1Font.HELVETICA, 18, (int) (w / 2) - 50, 30);
                    //contents.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contents_stream = new PDPageContentStream(document, page);
                    contents_stream.drawImage(logo, 10, 770, 160, logo_h);
                    line_pos = (int) h - logo_h - 20;
                    //line_pos = 700;
                    drawHorzLines(contents_stream, line_pos, (int) w, new Insets(0, 0, 80, 0));
                    line_pos -= LINE_HEIGHT / 2;
                    //contents.beginText();

                    addTextToPageStream(document, gj_title, PDType1Font.COURIER_BOLD, 16, (int) (w - 250), 830);
                    addTextToPageStream(document, "Company: " + Globals.COMPANY.getValue(), PDType1Font.COURIER, 12, (int) (w - 250), 815);
                    addTextToPageStream(document, "Period: "+date_start+" - " + date_end, PDType1Font.COURIER, 12,(int)(w-200), 800);
                    addTextToPageStream(document, "Generated: " + date_generated, PDType1Font.COURIER, 12,(int)(w-200), 785);
                    line_pos -= LINE_HEIGHT;
                    pages++;
                    lines = 0;
                }
                addTextToPageStream(document,
                        (new SimpleDateFormat("yyyy-MM-dd").format(
                                new Date(t.getDate() * 1000))),
                        PDType1Font.HELVETICA, 15, 10, line_pos);

                if (t.getBusinessObject() instanceof Invoice)
                {
                    //title
                    String title = ((Invoice) t.getBusinessObject()).getAccount();
                    if (title.length() <= 50)
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    double difference = 0;
                    if (((Invoice) t.getBusinessObject()).getJob() != null)
                        if (((Invoice) t.getBusinessObject()).getJob().getQuote() != null)
                            difference = ((Invoice) t.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) t.getBusinessObject()).getReceivable();
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(difference), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    addTextToPageStream(document, "Accounts Receivable", PDType1Font.HELVETICA, 15, 105, line_pos);
                    //debit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Invoice) t.getBusinessObject()).getReceivable()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    addTextToPageStream(document, "Service Revenue", PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Invoice) t.getBusinessObject()).getTotal()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                } else if (t.getBusinessObject() instanceof Expense)
                {
                    //title
                    String title = ((Expense) t.getBusinessObject()).getExpense_title();
                    if (title.length() <= 50)
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) t.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    addTextToPageStream(document, ((Expense) t.getBusinessObject()).getAccount(), PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) t.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                } else if (t.getBusinessObject() instanceof Asset)
                {
                    //title
                    String title = ((Asset) t.getBusinessObject()).getAsset_name();
                    if (title.length() <= 50)
                        addTextToPageStream(document, "Purchased asset: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(document, "Purchased asset: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(document, "Purchased asset: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) t.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    //addTextToPageStream(contents, ((Asset) t.getBusinessObject()).getAccount_name(), PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) t.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                } else if (t.getBusinessObject() instanceof Resource)
                {
                    //title
                    String title = ((Resource) t.getBusinessObject()).getResource_description();
                    if (title.length() <= 50)
                        addTextToPageStream(document, "Purchased stock: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(document, "Purchased stock: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(document, "Purchased stock: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) t.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    //addTextToPageStream(contents, ((Resource) t.getBusinessObject()).getAccount_name(), PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) t.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                }
                //addTextToPageStream(contents,"Account", PDType1Font.COURIER_BOLD_OBLIQUE, 15,100, line_pos);
                //addTextToPageStream(contents,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);
                lines++;
                line_pos -= LINE_HEIGHT;//next line
            }
        }
        addTextToPageStream(document, "page " + pages, PDType1Font.HELVETICA, 18, (int)(w/2)-50, 30);

        //horizontal solid line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(0, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();

        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);
        
        String path = "out/pdf/general_journal.pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/general_journal" + "." + i + ".pdf";
            i++;
        }

        if(contents_stream!=null)
            contents_stream.close();
        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);
        pdfViewer.doOpen(path);
    }

    private static PDPage initLedgerPage(PDDocument document,String account, long start, long end) throws IOException
    {
        // Create a new document with an empty page.
        //PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents_stream = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile(logo_path, document);
        contents_stream.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-logo_h-20;

        /**Draw lines**/
        drawHorzLines(contents_stream, line_pos, (int) w, new Insets(0,0,80,0));

        /** begin text from the top**/
        //contents.beginText();
        //contents.setFont(font, 12);
        line_pos-=LINE_HEIGHT/2;

        String ledger_title = account.substring(0,1).toUpperCase() + account.substring(1) + " General Ledger";
        String date_start = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(start * 1000)));
        String date_end = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(end * 1000)));
        String date_generated = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(System.currentTimeMillis())));
        addTextToPageStream(document, ledger_title, PDType1Font.COURIER_BOLD, 16,(int)(w/2-100), 830);
        addTextToPageStream(document, "Company: " + Globals.COMPANY.getValue(), PDType1Font.COURIER, 12,(int)(w/2-100), 815);
        addTextToPageStream(document, "Period: "+date_start+" - " + date_end, PDType1Font.COURIER, 12,(int)(w/2-100), 800);
        addTextToPageStream(document, "Generated: " + date_generated, PDType1Font.COURIER, 12,(int)(w/2-100), 785);

        //horizontal solid line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents_stream.stroke();
        //contents.beginText();
        line_pos-=LINE_HEIGHT;

        addTextToPageStream(document,"Date", PDType1Font.COURIER_BOLD_OBLIQUE, 15,10, line_pos);
        addTextToPageStream(document,"Description", PDType1Font.COURIER_BOLD_OBLIQUE, 15,150, line_pos);
        addTextToPageStream(document,"Debit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-180, line_pos);
        addTextToPageStream(document,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);

        //horizontal solid line
        //contents.endText();
        contents_stream.setStrokingColor(Color.BLACK);
        contents_stream.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents_stream.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        //contents.stroke();
        //contents.beginText();

        return page;
    }

    public static void createGeneralLedgerPdf(long start, long end) throws IOException
    {
        //Init managers and load data to memory
        AssetManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        ExpenseManager.getInstance().initialize();
        InvoiceManager.getInstance().initialize();
        RevenueManager.getInstance().initialize();

        ArrayList<Transaction> transactions = new ArrayList<>();
        //Load assets
        for(Asset asset : AssetManager.getInstance().getDataset().values())
            transactions.add(new Transaction(asset.get_id(), asset.getDate_acquired(), asset));
        //Load Resources/Stock
        for(Resource resource : ResourceManager.getInstance().getDataset().values())
            transactions.add(new Transaction(resource.get_id(), resource.getDate_acquired(), resource));
        //Load additional Expenses
        for(Expense expense: ExpenseManager.getInstance().getDataset().values())
            transactions.add(new Transaction(expense.get_id(), expense.getDate_logged(), expense));
        //Load Service revenue (Invoices)
        for(Invoice invoice: InvoiceManager.getInstance().getDataset().values())
            transactions.add(new Transaction(invoice.get_id(), invoice.getDate_logged(), invoice));
        //Load Additional income/revenue
        for(Revenue revenue: RevenueManager.getInstance().getDataset().values())
            transactions.add(new Transaction(revenue.get_id(), revenue.getDate_logged(), revenue));

        Transaction[] transactions_arr = new Transaction[transactions.size()];
        transactions.toArray(transactions_arr);
        long start_ms = System.currentTimeMillis();
        IO.getInstance().quickSort(transactions_arr, 0, transactions.size()-1, "date");
        //Arrays.sort(transactions_arr);
        //transactions_arr = selectionSort(transactions_arr);
        System.out.println("Sorted in: "+ (System.currentTimeMillis()-start_ms) + "ms");
        System.out.println("Transaction count: "+ transactions_arr.length);

        HashMap<String, Account> accounts_map = new HashMap<>();
        for(Transaction t : transactions_arr)
        {
            if(t.getBusinessObject() instanceof Expense)
            {
                Expense expense = ((Expense)t.getBusinessObject());
                String str_acc = expense.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), expense.getExpense_value(), 0);
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its debit
                    acc.setDebit(acc.getDebit()+expense.getExpense_value());
                    acc.addTransaction(t);
                }
            }else if(t.getBusinessObject() instanceof Asset)
            {
                Asset asset = ((Asset)t.getBusinessObject());
                String str_acc = "%ACCOUNT";//asset.getAccount_name();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), asset.getAsset_value(), 0);
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its debit
                    acc.setDebit(acc.getDebit()+asset.getAsset_value());
                    acc.addTransaction(t);
                }
            }else if(t.getBusinessObject() instanceof Resource)
            {
                Resource resource = ((Resource)t.getBusinessObject());
                String str_acc = "%ACCOUNT";//resource.getAccount_name();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), resource.getResource_value(), 0);
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its debit
                    acc.setDebit(acc.getDebit()+resource.getResource_value());
                    acc.addTransaction(t);
                }
            }else if(t.getBusinessObject() instanceof Invoice)
            {
                Invoice invoice = ((Invoice)t.getBusinessObject());
                String str_acc = invoice.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), 0, invoice.getTotal());
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+invoice.getTotal());
                    acc.addTransaction(t);
                }
                //Update service revenue
                acc = accounts_map.get("service revenue");
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account("service revenue", 0, invoice.getTotal());
                    acc.addTransaction(t);
                    accounts_map.put("service revenue", acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+invoice.getTotal());
                    acc.addTransaction(t);
                }
                //check difference between received and sale total
                double difference = 0;
                if (((Invoice) t.getBusinessObject()).getJob() != null)
                    if (((Invoice) t.getBusinessObject()).getJob().getQuote() != null)
                        difference = ((Invoice) t.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) t.getBusinessObject()).getReceivable();
                if(difference>0)
                {
                    //customer paid on credit, add to accounts receivable
                    //Update accounts receivable
                    acc = accounts_map.get("accounts receivable");
                    if(acc==null)
                    {
                        //account not in map yet create it.
                        acc = new Account("accounts receivable",difference, 0);
                        acc.addTransaction(t);
                        accounts_map.put("accounts receivable", acc);
                    }else{
                        //account in map, update its credit
                        acc.setDebit(acc.getDebit()+difference);
                        acc.addTransaction(t);
                    }
                }
            }else if(t.getBusinessObject() instanceof Revenue)
            {
                Revenue revenue = ((Revenue)t.getBusinessObject());
                String str_acc = revenue.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), 0, revenue.getRevenue_value());
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+revenue.getRevenue_value());
                    acc.addTransaction(t);
                }
                //Update additional revenue
                acc = accounts_map.get("additional revenue");
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account("additional revenue", 0, revenue.getRevenue_value());
                    acc.addTransaction(t);
                    accounts_map.put("additional revenue", acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+revenue.getRevenue_value());
                    acc.addTransaction(t);
                }
            }
        }

        PDDocument document = new PDDocument();
        PDPageContentStream contents = null;
        int logo_h = 60, pages= 1, line_pos= 0, w=0, h=0;

        for(Account account : accounts_map.values())
        {
            System.out.println("account: " + account.getAccount_name() + ", debit: "+ account.getDebit() +
                                ", credit: " + account.getCredit()+", transactions: "+ account.getTransactions().size());

            if(contents!=null)
            {
                contents.endText();
                contents.close();
            }
            PDPage page = initLedgerPage(document, account.getAccount_name(), start, end);
            //contents.beginText();
            h = (int)document.getPage(0).getBBox().getHeight();
            w = (int)document.getPage(0).getBBox().getWidth();
            line_pos = h-logo_h-LINE_HEIGHT*3-LINE_HEIGHT/2;
            pages++;
            int lines=0;
            double credit=0;
            double debit=0;

            //Generate ledger for each account
            for(Transaction transaction : account.getTransactions())
            {
                if(transaction.getDate()>=start && transaction.getDate()<=end)
                {
                    if (lines > 32)
                    {
                        //line_pos-=LINE_HEIGHT;
                        addTextToPageStream(document, "page " + pages, PDType1Font.HELVETICA, 18, (int) (w / 2) - 50, 30);
                        //contents.close();
                        page = initLedgerPage(document, account.getAccount_name(), start, end);
                        contents.beginText();
                        pages++;
                        lines = 0;
                        line_pos = h-logo_h-LINE_HEIGHT*3-LINE_HEIGHT/2;
                    }
                    addTextToPageStream(document,
                            (new SimpleDateFormat("yyyy-MM-dd").format(
                                    new Date(transaction.getDate() * 1000))),
                            PDType1Font.HELVETICA, 15, 10, line_pos);

                    if (transaction.getBusinessObject() instanceof Revenue)
                    {
                        System.out.println(((Revenue) transaction.getBusinessObject()).getRevenue_title() + " in Account: " + account.getAccount_name());
                        //title
                        String title = ((Revenue) transaction.getBusinessObject()).getRevenue_title();
                        //TODO: add to Additional Revenue account
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 15, 105, line_pos);
                        //debit
                        /*double difference = 0;
                        if (((Invoice) transaction.getBusinessObject()).getJob() != null)
                            if (((Invoice) transaction.getBusinessObject()).getJob().getQuote() != null)
                                difference = ((Invoice) transaction.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) t.getBusinessObject()).getReceivable();*/
                        if(!account.getAccount_name().toLowerCase().equals("additional revenue") && !account.getAccount_name().toLowerCase().equals("accounts receivable"))
                        {
                            //debit
                            addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Revenue) transaction.getBusinessObject()).getRevenue_value()), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                            debit += ((Revenue) transaction.getBusinessObject()).getRevenue_value();
                        }else
                        {
                            //credit
                            addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Revenue) transaction.getBusinessObject()).getRevenue_value()), PDType1Font.HELVETICA, 15, w - 100, line_pos);
                            credit += ((Revenue) transaction.getBusinessObject()).getRevenue_value();
                        }
                    } else if (transaction.getBusinessObject() instanceof Invoice)
                    {
                        //title
                        String title = "Service Revenue";
                        //TODO: add to Service Revenue account if diff>0 add to accounts receivable
                        addTextToPageStream(document, title, PDType1Font.HELVETICA, 15, 105, line_pos);
                        //debit
                        double difference = 0;
                        if (((Invoice) transaction.getBusinessObject()).getJob() != null)
                            if (((Invoice) transaction.getBusinessObject()).getJob().getQuote() != null)
                                difference = ((Invoice) transaction.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) transaction.getBusinessObject()).getReceivable();
                        double invoice_total = ((Invoice) transaction.getBusinessObject()).getJob().getQuote().getTotal();
                        if(!account.getAccount_name().toLowerCase().equals("service revenue"))
                        {
                            if(!account.getAccount_name().toLowerCase().equals("accounts receivable"))
                            {
                                //debit
                                addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(invoice_total), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                                debit += invoice_total;
                            } else{
                                //purchased on credit, debit accounts receivable
                                addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(difference), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                                debit += difference;
                            }
                        } else
                        {
                            //credit
                            addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(invoice_total), PDType1Font.HELVETICA, 15, w - 100, line_pos);
                            credit += invoice_total;
                        }
                    } else if (transaction.getBusinessObject() instanceof Expense)
                    {
                        //title
                        String title = ((Expense) transaction.getBusinessObject()).getExpense_title();
                        if (title.length() <= 50)
                            addTextToPageStream(document, title, PDType1Font.HELVETICA, 14, 105, line_pos);
                        if (title.length() > 50 && title.length() <= 60)
                            addTextToPageStream(document, title, PDType1Font.HELVETICA, 10, 105, line_pos);
                        if (title.length() > 60)
                            addTextToPageStream(document, title, PDType1Font.HELVETICA, 8, 105, line_pos);
                        //debit
                        //addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) transaction.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                        //credit
                        addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) transaction.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15,  w - 100, line_pos);
                        credit+=((Expense) transaction.getBusinessObject()).getExpense_value();
                    } else if (transaction.getBusinessObject() instanceof Asset)
                    {
                        //title
                        String title = ((Asset) transaction.getBusinessObject()).getAsset_name();
                        if (title.length() <= 50)
                            addTextToPageStream(document, "Purchased asset: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                        if (title.length() > 50 && title.length() <= 60)
                            addTextToPageStream(document, "Purchased asset: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                        if (title.length() > 60)
                            addTextToPageStream(document, "Purchased asset: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                        //debit
                        addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) transaction.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15,w - 180, line_pos);
                        debit+=((Asset) transaction.getBusinessObject()).getAsset_value();
                        //credit
                        //addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) transaction.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                    } else if (transaction.getBusinessObject() instanceof Resource)
                    {
                        //title
                        String title = ((Resource) transaction.getBusinessObject()).getResource_description();
                        if (title.length() <= 50)
                            addTextToPageStream(document, "Purchased stock: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                        if (title.length() > 50 && title.length() <= 60)
                            addTextToPageStream(document, "Purchased stock: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                        if (title.length() > 60)
                            addTextToPageStream(document, "Purchased stock: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                        //debit
                        addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) transaction.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                        debit+=((Resource) transaction.getBusinessObject()).getResource_value();
                        //credit
                        //addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) transaction.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                    }
                    //addTextToPageStream(contents,"Account", PDType1Font.COURIER_BOLD_OBLIQUE, 15,100, line_pos);
                    //addTextToPageStream(contents,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);
                    lines++;
                    line_pos -= LINE_HEIGHT;//next line
                }
            }
            //horizontal solid line
            contents.endText();
            contents.setStrokingColor(Color.BLACK);
            contents.moveTo(0, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.lineTo(w, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.stroke();
            contents.beginText();

            addTextToPageStream(document, "Totals", PDType1Font.COURIER_BOLD_OBLIQUE, 16, 105, line_pos);
            addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(debit), PDType1Font.HELVETICA, 14, w - 200, line_pos);
            addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(credit), PDType1Font.HELVETICA, 14, w - 100, line_pos);
            line_pos-=LINE_HEIGHT;

            //horizontal solid line
            contents.endText();
            contents.setStrokingColor(Color.BLACK);
            contents.moveTo(0, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.lineTo(w, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.stroke();
            contents.beginText();

            addTextToPageStream(document, "Closing Balance", PDType1Font.COURIER_BOLD_OBLIQUE, 16, 105, line_pos);
            double balance = debit-credit;
            if(balance>0)
                addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(Math.abs(balance)), PDType1Font.HELVETICA, 14, w - 200, line_pos);
            else addTextToPageStream(document, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(Math.abs(balance)), PDType1Font.HELVETICA, 14, w - 100, line_pos);

            //horizontal solid line
            contents.endText();
            contents.setStrokingColor(Color.BLACK);
            contents.moveTo(0, line_pos-LINE_HEIGHT/2);
            contents.lineTo(w, line_pos-LINE_HEIGHT/2);
            contents.stroke();

            contents.beginText();
            addTextToPageStream(document, "page " + pages, PDType1Font.HELVETICA, 18, (w/2)-50, 30);
        }

        //create PDF output directory
        if(new File("out/pdf/").mkdirs())
            IO.log(PDF.class.getName(), "successfully created PDF output directory [out/pdf/]", IO.TAG_INFO);
        
        String path = "out/pdf/general_ledger.pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "out/pdf/general_ledger" + "." + i + ".pdf";
            i++;
        }

        contents.close();
        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);
        pdfViewer.doOpen(path);
    }

    private static void addTextToPageStream(PDDocument document, String text, int font_size, int x, int y) throws IOException
    {
        try
        {
            addTextToPageStream(document, text, PDType1Font.HELVETICA, font_size, x, y);
        }catch (IllegalArgumentException e)
        {
            IO.log("PDF creator", IO.TAG_ERROR, e.getMessage());
        }
    }

    private static void addTextToPageStream(PDDocument document, String text, PDFont font,int font_size, int x, int y) throws IOException
    {
        PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(document.getNumberOfPages()-1), PDPageContentStream.AppendMode.PREPEND, true);
        contentStream.beginText();

        contentStream.setFont(font, font_size);
        contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, x, y-TEXT_VERT_OFFSET));

        char[] text_arr = text.toCharArray();
        StringBuilder str_builder = new StringBuilder();
        //PDType0Font.
        Encoding e = org.apache.pdfbox.pdmodel.font.encoding.Encoding.getInstance(COSName.WIN_ANSI_ENCODING);// EncodingManager.INSTANCE.getEncoding(COSName.WIN_ANSI_ENCODING);
        //Encoding e = EncodingManager.INSTANCE.getEncoding(COSName.WIN_ANSI_ENCODING);

        System.out.println("\n\n::::::::::::::::::::Processing Text: [" + text + "]::::::::::::::::::::");
        System.out.println("Encoding Name: " + e.getEncodingName());
        System.out.println("Encoding Name to Code Map: " + e.getNameToCodeMap());
        //String toPDF = String.valueOf(Character.toChars(e.getCode(e.getNameFromCharacter(symbol))));

        for (int i = 0; i < text_arr.length; i++)
        {
            Character c = text_arr[i];
            int code = 0;
            System.out.println(String.format("Character [%s] has codename: [%s] and code [%s]", c, e.getName(c), String.valueOf(e.getNameToCodeMap().get(c))));
            if(e.getName(c).toLowerCase().equals(".notdef") || e.getName(c).toLowerCase().equals("nbspace"))
                str_builder.append("[?]");
            else str_builder.append(c);
            /*if(Character.isWhitespace(c))
            {
                code = e.getNameToCodeMap().get("space");
            }else{
                String toPDF = String.valueOf(Character.toChars(e.getCodeToNameMap().get(e.getName(symbol))));
                code = e.getNameToCodeMap(e.getName(c));
            }
            str_builder.appendCodePoint(code);*/
        }
        contentStream.showText(str_builder.toString().replaceAll("\u00A0","").trim());

        contentStream.endText();
        contentStream.close();
    }

    private static void drawLine(PDPageContentStream contentStream, Color color, int start_x, int start_y, int end_x, int end_y) throws IOException {
        contentStream.setStrokingColor(color);
        contentStream.moveTo(start_x, start_y);
        contentStream.lineTo(end_x, end_y);
        contentStream.stroke();
    }

    public static int centerText(String text, int font_size, int page_w, PDFont font) throws IOException
    {
        float str_w = (font.getStringWidth(text)/1000)*font_size;
        return (int) ((page_w/2)-(str_w/2));
    }

    public static int addTextToPageStream(PDDocument document, String text, PDFont font, int font_size, int text_block_width, int x, int y, Border border, int[] col_positions) throws IOException
    {
        return addTextToPageStream(document, PDRectangle.A4, text, font, font_size, text_block_width, x, y, border, col_positions);
    }
    /**
     * Adds text to a page with text-wrapping
     * @param text
     * @param font
     * @param font_size
     * @param text_block_width
     * @param x
     * @param y
     * @throws IOException
     */
    public static int addTextToPageStream(PDDocument document, PDRectangle page_size, String text, PDFont font, int font_size, int text_block_width, int x, int y, Border border, int[] col_positions) throws IOException
    {
        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\n\n::::::::::::::::::::Processing Text: [" + text + "]::::::::::::::::::::");

        float w = document.getPage(document.getNumberOfPages()-1).getBBox().getWidth();//page width
        //open stream on last page
        PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(document.getNumberOfPages()-1), PDPageContentStream.AppendMode.PREPEND, true);

        //draw column dividers if they exist
        if(col_positions!=null)
        {
            contentStream.setStrokingColor(Color.BLACK);
            for (int i = 0; i < col_positions.length; i++)//6 cols in total
            {
                if(y > PAGE_MARGINS.bottom)
                {
                    contentStream.moveTo(col_positions[i], y + LINE_HEIGHT / 2);
                    contentStream.lineTo(col_positions[i], y - LINE_HEIGHT / 2);
                    contentStream.stroke();
                }
            }
        }

        char[] text_arr = text.toCharArray();
        StringBuilder str_builder = new StringBuilder();
        Encoding e = org.apache.pdfbox.pdmodel.font.encoding.Encoding.getInstance(COSName.WIN_ANSI_ENCODING);// EncodingManager.INSTANCE.getEncoding(COSName.WIN_ANSI_ENCODING);

        //remove unsupported characters
        for (int i = 0; i < text_arr.length; i++)
        {
            Character c = text_arr[i];
            //IO.log(PDF.class.getName(), IO.TAG_VERBOSE, String.format("Character [%s] has codename: [%s] and code [%s]", c, e.getName(c), String.valueOf(e.getNameToCodeMap().get(c))));
            if(e.getName(c).toLowerCase().equals(".notdef") || e.getName(c).toLowerCase().equals("nbspace"))
                str_builder.append("[?]");
            else str_builder.append(c);
        }
        String new_text = str_builder.toString().replaceAll("\u00A0","").trim();

        //if adding one more line makes the line cursor move out the page, add a new page
        if (y < PAGE_MARGINS.bottom)//page.getBBox().getLowerLeftY()//- LINE_HEIGHT
        {
            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "##########line position: [" + y + "] is out of page bounds, adding new page.");

            //close current page's stream writer
            contentStream.close();

            //create new page
            PDPage new_page = new PDPage(page_size);
            IO.log(PDF.class.getName(), IO.TAG_INFO, "new page size w: " + page_size.getWidth() + ", h: " + page_size.getHeight());
            //add new page to document
            document.addPage(new_page);
            //create new stream writer using new page
            contentStream = new PDPageContentStream(document, new_page);
            //update line cursor to point to top of new page
            y = (int)new_page.getBBox().getHeight()-LINE_HEIGHT-LINE_HEIGHT/2;

            //draw lines and borders on new page
            //createLinesAndBordersOnPage(contentStream, new Insets(y, PAGE_MARGINS.left, 35, PAGE_MARGINS.right), (int)new_page.getBBox().getWidth());
            createLinesAndBordersOnPage(contentStream, new Insets(y, PAGE_MARGINS.left, PAGE_MARGINS.bottom+35, (int)new_page.getBBox().getWidth()-PAGE_MARGINS.right), (int)w);
            //return addTextToPageStream(new_document, contentStream, text, font, font_size, text_block_width, x, (int)new_page.getBBox().getHeight()-100);
            y-=LINE_HEIGHT/2;
        }

        //draw border
        switch (border.border)
        {
            case Border.BORDER_LEFT:
                drawLine(contentStream, border.border_colour, x-border.insets.left, (y+LINE_HEIGHT/2)+border.insets.top, x-border.insets.left, (y-LINE_HEIGHT+LINE_HEIGHT/2)-border.insets.bottom);
                break;
            case Border.BORDER_TOP:
                drawLine(contentStream, border.border_colour, x+border.insets.left, y+LINE_HEIGHT/2+border.insets.top, x+border.insets.right+text_block_width, y+LINE_HEIGHT/2+border.insets.top);
                break;
            case Border.BORDER_RIGHT:
                drawLine(contentStream, border.border_colour, x+text_block_width+border.insets.right, (y+LINE_HEIGHT/2)+border.insets.top, x+text_block_width+border.insets.right, (y-LINE_HEIGHT+LINE_HEIGHT/2)-border.insets.bottom);
                break;
            case Border.BORDER_BOTTOM:
                drawLine(contentStream, border.border_colour, x+border.insets.left, y-LINE_HEIGHT+LINE_HEIGHT/2-border.insets.bottom, x+border.insets.right+text_block_width, y-LINE_HEIGHT+LINE_HEIGHT/2-border.insets.bottom);
                break;
            case Border.BORDER_ALL:
                //left
                drawLine(contentStream, border.border_colour, x-border.insets.left, (y+LINE_HEIGHT/2)+border.insets.top, x-border.insets.left, (y-LINE_HEIGHT+LINE_HEIGHT/2)-border.insets.bottom);
                //top
                drawLine(contentStream, border.border_colour, x+border.insets.left, y+LINE_HEIGHT/2+border.insets.top, x+border.insets.right+text_block_width, y+LINE_HEIGHT/2+border.insets.top);
                //right
                drawLine(contentStream, border.border_colour, x+text_block_width+border.insets.right, (y+LINE_HEIGHT/2)+border.insets.top, x+text_block_width+border.insets.right, (y-LINE_HEIGHT+LINE_HEIGHT/2)-border.insets.bottom);
                //bottom
                drawLine(contentStream, border.border_colour, x+border.insets.left, y-LINE_HEIGHT+LINE_HEIGHT/2-border.insets.bottom, x+border.insets.right+text_block_width, y-LINE_HEIGHT+LINE_HEIGHT/2-border.insets.bottom);
                break;
            case Border.BORDER_NONE:
                break;
            default:
                IO.log(PDF.class.getName(), IO.TAG_WARN, "invalid border position.");
                break;
        }

        contentStream.beginText();
        contentStream.setFont(font, font_size);
        contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, x, y-TEXT_VERT_OFFSET));


        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "horizontal space for text: " + (font.getStringWidth(new_text)/1000)*font_size + ", space width: " + font.getSpaceWidth() + ", average width: " + font.getAverageFontWidth() + ", block max width: " + text_block_width);

        float char_w = (font.getStringWidth("A")/1000)*(float)font_size;//(int)(font_size/1.6);

        //if the length (in px) of the text is greater than the allowed width [col_width]
        if(((font.getStringWidth(new_text)/1000)*font_size)-char_w >= text_block_width)//if text is too long
        {
            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "text is too long, splitting..");
            String block_text = new_text.substring(0, (int) (text_block_width/char_w)-1);//copy all chars that can fit in the text block

            //TODO: split the text at last index of space
            //String[] lines = text.lastIndexOf(" ");
            char last_char = block_text.trim().length()!=0?block_text.charAt(block_text.length()-1):' ';

            contentStream.showText(last_char!=' '?block_text+'-':block_text);
            contentStream.endText();
            contentStream.close();

            y-=LINE_HEIGHT;//go to next line
            //draw remainder of text on next line
            return addTextToPageStream(document, new_text.substring((int) (text_block_width/char_w)-1), font, font_size, text_block_width, x, y, border, col_positions);
        } else contentStream.showText(new_text);

        contentStream.endText();
        contentStream.close();

        return y;
    }
}
