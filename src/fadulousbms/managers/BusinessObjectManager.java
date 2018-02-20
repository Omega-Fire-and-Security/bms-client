package fadulousbms.managers;

import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;

/**
 * Created by ghost on 2017/01/11.
 */
public abstract class BusinessObjectManager implements HSSFListener
{
    private BusinessObject selected;
    private long refresh_lock = 0;
    private SSTRecord sstrec;
    private static boolean found_match = false;
    private static Employee current_consultant = new Employee();

    public abstract void initialize();

    protected long getRefresh_lock()
    {
        return refresh_lock;
    }

    protected void setRefresh_lock(long refresh_lock)
    {
        this.refresh_lock = refresh_lock;
    }

    public boolean isSerialized(String path)
    {
        File file = new File(path);
        return file.exists();
    }

    public void forceSynchronise()
    {
        if(getRefresh_lock()<=0)//if there's no other thread synchronising the data-set, synchronise the data-set
            reloadDataFromServer(getSynchronisationCallback());
        else IO.log(getClass().getName(), IO.TAG_WARN, "can't synchronize "+getClass().getSimpleName()+" model's data-set, thread started at ["+getRefresh_lock()+"] is still busy.");
    }

    protected void synchroniseDataset()
    {
        boolean dataset_empty;
        if(getDataset()==null)
            dataset_empty=true;
        else dataset_empty=getDataset().isEmpty();

        if(dataset_empty)//TODO: improve data set timestamp checks before synchronization
            if(getRefresh_lock()<=0)//if there's no other thread synchronising the data-set, synchronise the data-set
                reloadDataFromServer(getSynchronisationCallback());
            else IO.log(getClass().getName(), IO.TAG_WARN, "can't synchronize "+getClass().getSimpleName()+" model's data-set, thread started at ["+getRefresh_lock()+"] is still busy.");
        else IO.log(getClass().getName(), IO.TAG_WARN, getClass().getSimpleName()+" model's data-set has already been set, not synchronizing.");
    }

    protected void reloadDataFromServer(Callback callback)
    {
        //set model refresh lock
        setRefresh_lock(System.currentTimeMillis());
        if(callback!=null)
        {
            callback.call(null);//execute anonymous method to reload model's data-set
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "no anonymous function was provided to be executed when reloading " + getClass().getName() + "'s data-set.");
        //unlock model's refresh lock
        setRefresh_lock(0);
    }

    abstract Callback getSynchronisationCallback();

    public abstract HashMap<String, ? extends BusinessObject> getDataset();

    public void setSelected(BusinessObject selected)
    {
        this.selected=selected;
    }

    public BusinessObject getSelected()
    {
        return this.selected;
    }

    public void serialize(String file_path, Object obj) throws IOException
    {
        String path = file_path.substring(0, file_path.lastIndexOf("/")+1);
        File fpath = new File(path);
        if(!fpath.exists())
        {
            if (fpath.mkdirs())
            {
                IO.log(getClass().getName(), IO.TAG_INFO,"successfully created new directory["+path+"].");
            } else {
                IO.log(getClass().getName(), IO.TAG_ERROR,"could not create new directory["+path+"].");
                return;
            }
        }else IO.log(getClass().getName(), IO.TAG_INFO, "directory["+path+"] already exists.");

        File file = new File(file_path);
        if(!file.exists())
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
            out.writeObject(obj);
            out.flush();
            out.close();
            IO.log(getClass().getName(), IO.TAG_INFO, "successfully serialized file["+file.getAbsolutePath()+"] to disk.");
        }else IO.log(getClass().getName(), IO.TAG_INFO, "file["+file.getAbsolutePath()+"] is already up-to-date.");
    }

    public Object deserialize(String path) throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(path)));
        Object obj = in.readObject();
        in.close();
        IO.log(getClass().getName(), IO.TAG_INFO, "successfully deserialized file ["+path+"].");
        return obj;
    }

    public void emailBusinessObject(BusinessObject businessObject, String pdf_path, Callback callback) throws IOException
    {
        if(businessObject==null)
        {
            IO.logAndAlert("Error", "Invalid "+businessObject.getClass().getName(), IO.TAG_ERROR);
            return;
        }
        if(EmployeeManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert("Error", "Could not find any employees in the system.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error: Invalid Session", "Could not find any valid sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Error: Session Expired", "The active session has expired.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActiveEmployee()==null)
        {
            IO.logAndAlert("Error: Invalid Employee Session", "Could not find any active employee sessions.", IO.TAG_ERROR);
            return;
        }
        String base64_obj = null;
        if(pdf_path!=null)
        {
            File f = new File(pdf_path);
            if (f != null)
            {
                if (f.exists())
                {
                    FileInputStream in = new FileInputStream(f);
                    byte[] buffer =new byte[(int) f.length()];
                    in.read(buffer, 0, buffer.length);
                    in.close();
                    base64_obj = Base64.getEncoder().encodeToString(buffer);
                } else
                {
                    IO.logAndAlert(QuoteManager.class.getName(), "File [" + pdf_path + "] not found.", IO.TAG_ERROR);
                }
            } else
            {
                IO.log(QuoteManager.class.getName(), "File [" + pdf_path + "] object is null.", IO.TAG_ERROR);
            }
        } else IO.log(QuoteManager.class.getName(), "Could not get valid path for created "+businessObject.getClass().getName()+" pdf.", IO.TAG_ERROR);
        final String finalBase64_obj = base64_obj;

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail "+businessObject.getClass().getSimpleName()+" ["+businessObject.get_id()+"]");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_destination = new TextField();
        txt_destination.setMinWidth(200);
        txt_destination.setMaxWidth(Double.MAX_VALUE);
        txt_destination.setPromptText("Type in recipient eMail addresses.");
        HBox destination = CustomTableViewControls.getLabelledNode("Recipient/s: ", 200, txt_destination);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText(businessObject.getClass().getSimpleName()+" ["+businessObject.get_id()+"]");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        //set default message
        Employee sender = SessionManager.getInstance().getActiveEmployee();
        String title = sender.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";;
        String def_msg = "Good day,\n\n<<insert message here>>\n\nThank you.\n\nBest Regards,\n"
                + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
        txt_message.setText(def_msg);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            if(!Validators.isValidNode(txt_destination, txt_destination.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));//multipart/form-data
                headers.add(new AbstractMap.SimpleEntry<>("_id", businessObject.get_id()));
                headers.add(new AbstractMap.SimpleEntry<>("destination", txt_destination.getText()));
                headers.add(new AbstractMap.SimpleEntry<>("message", msg));
                headers.add(new AbstractMap.SimpleEntry<>("subject", txt_subject.getText()));

                if(SessionManager.getInstance().getActive()!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive().getSession_id()));
                    headers.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().getName()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                FileMetadata fileMetadata = new FileMetadata(businessObject.getClass().getSimpleName().toLowerCase()+"_"+businessObject.get_id()+".pdf","application/pdf");
                fileMetadata.setCreator(SessionManager.getInstance().getActive().getUsr());
                fileMetadata.setFile(finalBase64_obj);
                HttpURLConnection connection = RemoteComms.postJSON(businessObject.apiEndpoint()+"/mailto", fileMetadata.getJSONString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully emailed "+businessObject.getClass().getSimpleName()+"!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    } else {
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        vbox.getChildren().add(message);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    public static void parsePDF(String path, Callback callback)
    {
        if(path==null)
        {
            IO.logAndAlert("Error", "Invalid PDF path.", IO.TAG_ERROR);
            return;
        }
        File file = new File(path);//C:/my.pdf
        try
        {
            PDDocument doc = PDDocument.load(file);
            String doc_text = new PDFTextStripper().getText(doc);
            if(doc_text!=null)
            {
                String balance_regex = "^(R\\s*(\\-|\\+)*\\s*\\d+\\,{0,1}\\d*\\.{0,1}\\d{0,2})";
                String tel_num_regex = "(\\d{3,}\\s+\\d{3,}\\s+\\d{4,})";
                String contact_regex = "(\\w+\\s*\\w*)";
                String active_regex = "(Yes|No)";
                String category_regex = "(\\w+\\s*\\w*)";
                String name_regex = "(\\w+\\s*\\w*)";

                String[] lines = doc_text.split("\n");
                if(lines==null)
                {
                    IO.logAndAlert("Error","No lines were found.\n*Must be new-line char delimited.", IO.TAG_ERROR);
                    return;
                }
                for (int i=0;i<lines.length;i++)
                {
                    String line=lines[i];
                    String response = IO.showConfirm("Continue?", "Continue with new object ["+(i+1)+" of "+lines.length+"]: ["+ line+"]?", IO.YES, IO.NO, IO.CANCEL);
                    if(response.equals(IO.YES))
                    {
                        String balance="", tel="", contact="", category="", org = "";
                        boolean active=false;

                        System.out.println("\n\n");
                        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "***********Parsing Line: " + line);
                        if(line.toLowerCase().contains("yes") || line.toLowerCase().contains("no"))
                        {
                            Matcher matcher = Validators.matchRegex(balance_regex+tel_num_regex+contact_regex+active_regex+category_regex+name_regex, line);//check balance
                            if(matcher.find())
                            {
                                IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tmatches main regex, group count: " + (matcher.groupCount()));

                                balance = matcher.group(0);
                                tel = matcher.group(1);
                                contact = matcher.group(2);
                                active = matcher.group(3).toLowerCase().equals("yes");
                                category = matcher.group(4);
                                name_regex = matcher.group(5);
                            } else//line does not have all the data
                            {
                                //filter out the missing fields
                                IO.log(PDF.class
                                        .getName(), IO.TAG_VERBOSE, "\tdoes not match main regex, parsing given fields..");

                                int index = 0;
                                //check balance
                                matcher = Validators.matchRegex(balance_regex, line);
                                if (matcher.find())
                                {
                                    balance = matcher.group(0);
                                    index = matcher.end();//move char cursor for next search
                                }

                                //check telephone
                                matcher = Validators.matchRegex(tel_num_regex, line);
                                if (matcher.find(index))
                                {
                                    tel = matcher.group(0);
                                    index = matcher.end();//move char cursor for next search
                                }

                                //the remaining fields need special parsing
                                String new_line = line.substring(index);
                                String[] contact_cat_org_arr = new String[0];
                                if(line.contains("Yes"))
                                {
                                    contact_cat_org_arr = new_line.split("Yes");
                                    active=true;
                                }
                                if(line.contains("Yes0"))
                                {
                                    contact_cat_org_arr = new_line.split("Yes0");//get rid of leading zero
                                    active=true;
                                }
                                if(line.contains("No"))
                                {
                                    contact_cat_org_arr = new_line.split("No");
                                    active=false;
                                }
                                if(line.contains("No0"))
                                {
                                    contact_cat_org_arr = new_line.split("No0");//get rid of leading zero
                                    active=false;
                                }

                                //for (String s : new_line.split("(?=\\p{Upper})"))
                                if(contact_cat_org_arr!=null)
                                {
                                    if(contact_cat_org_arr.length>0)
                                    {
                                        contact = contact_cat_org_arr[0];//.isEmpty()?contact_cat_org_arr[1]:contact_cat_org_arr[0];

                                        if(contact_cat_org_arr.length>1)//if arr not empty, use elem [1] as category & org name
                                        {
                                            if(contact.isEmpty())
                                                contact=contact_cat_org_arr[1];//use elem [1] as contact if elem [0] is empty
                                            category = contact_cat_org_arr[1];
                                            org=contact_cat_org_arr[1];
                                        } else //if no elem [1], use same value as contact
                                        {
                                            category=contact_cat_org_arr[0];
                                            org=contact_cat_org_arr[0];
                                        }

                                    /*if(contact_cat_org_arr.length>2)//if arr len>2 use elem 2 as organisation
                                        org=contact_cat_org_arr[2];
                                    else if(contact_cat_org_arr.length>1)//if arr len>1 use elem 1 as organisation
                                        org=contact_cat_org_arr[1];
                                    else org=contact_cat_org_arr[0];//else just default to using the same value as contact*/
                                    }
                                }
                            }
                            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tBalance: " + balance);
                            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tTel: " + tel);
                            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tContact: " + contact);
                            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tActive: " + active);
                            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tCategory: " + category);
                            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tOrganisation: " + org);

                            if(callback!=null)
                                callback.call(new String[]{balance, org, contact, category, String.valueOf(active), tel});
                        } else IO.log(PDF.class.getName(), IO.TAG_WARN, "invalid object, can't tell if active or not.");
                    } else if(response.equals(IO.NO))
                        continue;
                    else if(response.equals(IO.CANCEL))return;
                }
            }
            doc.close();
        } catch (IOException e)
        {
            IO.log(PDF.class.getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public static void parseXLSX(String path)
    {
        //client info
        //String company, contact_person,tel,cell_num="",fax,email;
        HashMap<String, String> quote_info = new HashMap<>();
        HashMap<String, Employee> sale_consultants = new HashMap<>();

        try
        {
            //String contact_person_regex = "(\\.*contact(\\s+|\\t+)person\\s*\\:\\.*)(\\.*)";
            HashMap<String, String> regular_expressions = new HashMap<>();

            regular_expressions.put("Contact Person", "((Contact(\\s+|\\t+)Person)\\s*\\:\\s*([a-zA-Z0-9\\s]*))");//
            regular_expressions.put("Company", "(Company\\s*\\:\\s*([a-zA-Z0-9\\s]+))");
            regular_expressions.put("Sales Consultant", "((Sales(\\s+|\\t+)Consultant)\\s*\\:\\s*([a-zA-Z0-9\\s]*))");//+
            regular_expressions.put("Sale Consultant", "(([a-zA-Z0-9\\s]+)\\s*\\:\\s*([0-9\\s]{10,}))");
            regular_expressions.put("Cell", "((Cell|CeII|cell|ceII)\\s*\\:\\s*([0-9\\s]*))");//{10,}
            regular_expressions.put("Email", "((email|Email|e-mail|eMail|e-Mail)\\s*\\:\\s*(\\.*))");//[a-zA-Z0-9@.-/]*
            /*regular_expressions.putIfAbsent("contact_person", "((Contact(\\s+|\\t+)Person)\\s*\\:\\s*([a-zA-Z0-9\\s]+))");
            regular_expressions.putIfAbsent("contact_person", "((Contact(\\s+|\\t+)Person)\\s*\\:\\s*([a-zA-Z0-9\\s]+))");
            regular_expressions.putIfAbsent("contact_person", "((Contact(\\s+|\\t+)Person)\\s*\\:\\s*([a-zA-Z0-9\\s]+))");
            regular_expressions.putIfAbsent("contact_person", "((Contact(\\s+|\\t+)Person)\\s*\\:\\s*([a-zA-Z0-9\\s]+))");
            regular_expressions.putIfAbsent("contact_person", "((Contact(\\s+|\\t+)Person)\\s*\\:\\s*([a-zA-Z0-9\\s]+))");*/

            FileInputStream excelFile = new FileInputStream(new File(path));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            while (iterator.hasNext())
            {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                //int cell=0;
                while (cellIterator.hasNext())
                {
                    Cell currentCell = cellIterator.next();
                    if (currentCell.getCellTypeEnum() == CellType.STRING)
                    {
                        IO.log(BusinessObjectManager.class.getName(), IO.TAG_VERBOSE, "processing line: [" + currentCell.getStringCellValue()+"].");
                        //final Employee curr_consultant = current_consultant;
                        found_match = false;
                        regular_expressions.forEach((key, regex) ->
                        {
                            Matcher matcher = Validators.matchRegex(regex, currentCell.getStringCellValue());
                            found_match=matcher.find();
                            if (found_match)
                            {
                                System.out.println("Found " + key + ": " + matcher.group(1).split("\\:")[1].trim());

                                //parse cell numbers
                                if(key.toLowerCase().equals("cell")||key.toLowerCase().equals("ceII"))
                                {
                                    //check if company cell or sale consultant cell
                                    if(quote_info.get("Cell")==null)
                                        quote_info.put("Cell", matcher.group(1).split("\\:")[1].trim());
                                    else //company cell has been set, append to sale reps
                                    {
                                        //if(curr_consultant.getCell()==null)//if current consultant's cell if not set, set it
                                        current_consultant.setCell(matcher.group(1).split("\\:")[1].trim());
                                    }
                                    IO.log(BusinessObjectManager.class.getName(), IO.TAG_VERBOSE, "current sale consultant: [" + current_consultant.getJSONString()+"].");
                                }
                                //parse email addresses
                                if(key.toLowerCase().equals("email")||key.toLowerCase().equals("e-mail"))
                                {
                                    //check if company eMail or sale consultant eMail
                                    /*if(quote_info.get("eMail")==null)
                                        quote_info.put("eMail", matcher.group(1).split("\\:")[1].trim());
                                    else //company cell has been set, append to sale reps
                                    {
                                        //if(curr_consultant.getCell()==null)//if current consultant's cell if not set, set it
                                        curr_consultant.setEmail(matcher.group(1).split("\\:")[1].trim());
                                    }*/
                                    if(currentCell.getColumnIndex()==0)
                                        quote_info.put("eMail", matcher.group(1).split("\\:")[1].trim());
                                    else current_consultant.setEmail(matcher.group(1).split("\\:")[1].trim());

                                    IO.log(BusinessObjectManager.class.getName(), IO.TAG_VERBOSE, "current sale consultant: [" + current_consultant.getJSONString()+"].");
                                }
                                //parse consultant names
                                if(key.toLowerCase().equals("sales consultant"))
                                {
                                    String full_name = matcher.group(1).split("\\:")[1].trim();
                                    if(full_name.contains(" "))
                                    {
                                        current_consultant.setFirstname(full_name.split(" ")[0]);
                                        current_consultant.setLastname(full_name.split(" ")[1]);
                                    } else
                                    {
                                        current_consultant.setFirstname(full_name);
                                        current_consultant.setLastname("");
                                    }
                                    IO.log(BusinessObjectManager.class.getName(), IO.TAG_VERBOSE, "current sale consultant: [" + current_consultant.getJSONString()+"].");
                                }
                            }
                        });
                        if(!found_match)//check if line matches additional consultant name regex
                        {
                            //parse additional consultant name & cell
                            if(currentCell.getColumnIndex()>0)//fields on the far left are client info
                            {
                                Matcher matcher = Validators.matchRegex(regular_expressions.get("Sale Consultant"), currentCell.getStringCellValue());
                                if(matcher.find())
                                {
                                    String[] consult_info = matcher.group(1).split("\\:");
                                    if(consult_info!=null) {
                                        if (consult_info.length > 1) {
                                            if (!consult_info[0].trim().isEmpty() && !consult_info[1].trim().isEmpty()) {
                                                IO.log(BusinessObjectManager.class.getName(), IO.TAG_VERBOSE, "found additional sale consultant: [" + matcher.group(1) + "].");

                                                String full_name = matcher.group(1).split("\\:")[0].trim();
                                                String cell = matcher.group(1).split("\\:")[1].trim();
                                                current_consultant.setCell(cell);
                                                if (full_name.contains(" ")) {
                                                    current_consultant.setFirstname(full_name.split(" ")[0]);
                                                    current_consultant.setLastname(full_name.split(" ")[1]);
                                                } else {
                                                    current_consultant.setFirstname(full_name);
                                                    current_consultant.setLastname("");
                                                }
                                            }
                                        }
                                        IO.log(BusinessObjectManager.class.getName(), IO.TAG_VERBOSE, "current sale consultant: [" + current_consultant.getJSONString() + "].");
                                    }
                                }
                            }
                        } else IO.log(BusinessObjectManager.class.getName(), IO.TAG_WARN, "no usable fields were found on line: " +currentCell.getStringCellValue());
                    } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC)
                    {
                        IO.logAndAlert("Error", "Unexpected data-type found [Numeric]: " + currentCell.getNumericCellValue(), IO.TAG_WARN);
                        //TODO: handle this better
                    }
                    //is the current sale consultant complete? i.e. cell, email & name have been set
                    if(current_consultant.getCell()!=null && current_consultant.getEmail() !=null && current_consultant.getName()!=null)
                    if(!current_consultant.getCell().isEmpty() && !current_consultant.getEmail().isEmpty() && !current_consultant.getName().isEmpty())
                    {
                        //got all the data for the current consultant
                        sale_consultants.putIfAbsent(current_consultant.getName(), current_consultant);

                        IO.log(BusinessObjectManager.class.getName(), IO.TAG_INFO, "committed current sale consultant: [" + current_consultant.getJSONString()+"] to map.");

                        current_consultant = new Employee();
                    } else IO.log(BusinessObjectManager.class.getName(), IO.TAG_WARN, "current sale consultant: [" + current_consultant.getJSONString()+"] is not yet complete.");

                }
                System.out.println();//print new line
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            IO.logAndAlert("Error", e.getMessage(), IO.TAG_WARN);
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.logAndAlert("Error", e.getMessage(), IO.TAG_WARN);
        }
    }

    public void writeXLSX(String path)
    {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Datatypes in Java");
        Object[][] datatypes = {
                {"Datatype", "Type", "Size(in bytes)"},
                {"int", "Primitive", 2},
                {"float", "Primitive", 4},
                {"double", "Primitive", 8},
                {"char", "Primitive", 1},
                {"String", "Non-Primitive", "No fixed size"}
        };

        int rowNum = 0;
        System.out.println("Creating excel");

        for (Object[] datatype : datatypes) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object field : datatype) {
                Cell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }

        try
        {
            FileOutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Done");
    }
    /**
     * This method listens for incoming records and handles them as required.
     * @param record    The record that was found while reading.
     */
    public void processRecord(Record record)
    {
        switch (record.getSid())
        {
            // the BOFRecord can represent either the beginning of a sheet or the workbook
            case BOFRecord.sid:
                BOFRecord bof = (BOFRecord) record;
                if (bof.getType() == bof.TYPE_WORKBOOK)
                {
                    System.out.println("Encountered workbook");
                    // assigned to the class level member
                } else if (bof.getType() == bof.TYPE_WORKSHEET)
                {
                    System.out.println("Encountered sheet reference");
                }
                break;
            case BoundSheetRecord.sid:
                BoundSheetRecord bsr = (BoundSheetRecord) record;
                System.out.println("New sheet named: " + bsr.getSheetname());
                break;
            case RowRecord.sid:
                RowRecord rowrec = (RowRecord) record;
                System.out.println("Row found, first column at "
                        + rowrec.getFirstCol() + " last column at " + rowrec.getLastCol());
                break;
            case NumberRecord.sid:
                NumberRecord numrec = (NumberRecord) record;
                System.out.println("Cell found with value " + numrec.getValue()
                        + " at row " + numrec.getRow() + " and column " + numrec.getColumn());
                break;
            // SSTRecords store a array of unique strings used in Excel.
            case SSTRecord.sid:
                sstrec = (SSTRecord) record;
                for (int k = 0; k < sstrec.getNumUniqueStrings(); k++)
                {
                    System.out.println("String table value " + k + " = " + sstrec.getString(k));
                }
                break;
            case LabelSSTRecord.sid:
                LabelSSTRecord lrec = (LabelSSTRecord) record;
                System.out.println("String cell found with value "
                        + sstrec.getString(lrec.getSSTIndex()));
                break;
        }
    }

    /**
     * Read an excel file and spit out what we find.
     *
     * @param file_path  the file path of the file to be read.
     * @throws IOException  When there is an error processing the file.
     */
    public void readExcelSpreadsheet(String file_path) throws IOException
    {
        File file = new File(file_path);
        if(file.exists())
        {
            // create a new file input stream with the input file specified
            FileInputStream fin = new FileInputStream(file);

            // create a new org.apache.poi.poifs.filesystem.Filesystem
            POIFSFileSystem poifs = new POIFSFileSystem(fin);
            // get the Workbook (excel part) stream in a InputStream
            InputStream din = poifs.createDocumentInputStream("Workbook");
            // construct out HSSFRequest object
            HSSFRequest req = new HSSFRequest();
            // lazy listen for ALL records with the listener shown above
            req.addListenerForAllRecords(this);
            // create our event factory
            HSSFEventFactory factory = new HSSFEventFactory();
            // process our events based on the document input stream
            factory.processEvents(req, din);
            // once all the events are processed close our file input stream
            fin.close();
            // and our document input stream (don't want to leak these!)
            din.close();
            System.out.println("done.");
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "file ["+file_path+"] does not exist.");
    }
}
