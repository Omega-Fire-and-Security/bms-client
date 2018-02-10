package fadulousbms.auxilary;

import fadulousbms.controllers.ScreenController;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.BusinessObject;
import fadulousbms.model.FileMetadata;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.Notifications;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.print.PrintException;
import java.awt.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by ghost on 2017/01/28.
 */
public class IO<T extends BusinessObject>
{
    public static final String TAG_VERBOSE = "verbose";
    public static final String TAG_INFO = "info";
    public static final String TAG_WARN = "warning";
    public static final String TAG_ERROR = "error";
    private static final String TAG = "IO";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String OK = "OK";
    public static final String CANCEL = "Cancel";
    public static final String STYLES_ROOT_PATH= "styles/";//FadulousBMS.class.getResource("styles/").getPath();
    private static IO io = new IO();
    private static ScreenManager screenManager;


    private IO()
    {
    }

    public static IO getInstance(){return io;}

    public void quickSort(T arr[], int left, int right, String comparator)
    {
        int index = partition(arr, left, right, comparator);
        if (left < index - 1)
            quickSort(arr, left, index - 1, comparator);
        if (index < right)
            quickSort(arr, index, right, comparator);
    }

    public int partition(T arr[], int left, int right, String comparator) throws ClassCastException
    {
        int i = left, j = right;
        T tmp;
        double pivot = (Double) arr[(left + right) / 2].get(comparator);

        while (i <= j)
        {
            while ((Double) arr[i].get(comparator) < pivot)
                i++;
            while ((Double) arr[j].get(comparator) > pivot)
                j--;
            if (i <= j)
            {
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }
        return i;
    }

    public static String generateRandomString(int len)
    {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String str="";
        for(int i=0;i<len;i++)
            str+=chars.charAt((int)(Math.floor(Math.random()*chars.length())));
        return str;
    }

    public void init(ScreenManager screenManager)
    {
        this.screenManager = screenManager;
    }

    public static void log(String src, String tag, String msg)
    {
        if(screenManager!=null)
        {
            ScreenController current_screen = screenManager.getFocused();
            if(current_screen!=null)
            {
                if (src.contains("."))
                    current_screen.refreshStatusBar(src.substring(src.lastIndexOf(".") + 1) + "> " + tag + ":: " + msg.replaceAll("\n",""));
                else current_screen.refreshStatusBar(src + "> " + tag + ":: " + msg.replaceAll("\n",""));
            }else System.err.println(getInstance().getClass().getName() + "> error: focused screen is null.");
        }
        switch (tag.toLowerCase())
        {
            case TAG_INFO:
                if (Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                    System.out.println(String.format("%s> %s: %s", src, tag, msg));
                break;
            case TAG_VERBOSE:
                if (Globals.DEBUG_VERBOSE.getValue().toLowerCase().equals("on"))
                    System.out.println(String.format("%s> %s: %s", src, tag, msg));
                break;
            case TAG_WARN:
                if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                    System.out.println(String.format("%s> %s: %s", src, tag, msg));
                break;
            case TAG_ERROR:
                if (Globals.DEBUG_ERRORS.getValue().toLowerCase().equals("on"))
                    System.err.println(String.format("%s> %s: %s", src, tag, msg));
                break;
            default://fallback for custom tags
                System.out.println(String.format("%s> %s: %s", src, tag, msg));
                break;
        }
    }

    public static String showConfirm(String title, String message, String... options)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Choose an option");
        alert.setHeaderText(title);
        alert.setContentText(message);

        //To make enter key press the actual focused button, not the first one. Just like pressing "space".
        alert.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {
            if (event.getCode().equals(KeyCode.ENTER))
            {
                event.consume();
                try
                {
                    Robot r = new Robot();
                    r.keyPress(java.awt.event.KeyEvent.VK_SPACE);
                    r.keyRelease(java.awt.event.KeyEvent.VK_SPACE);
                } catch (Exception e)
                {
                    IO.log(IO.class.getName(), IO.TAG_ERROR, e.getMessage());
                }
            }
        });

        if (options == null || options.length == 0)
        {
            options = new String[]{OK, CANCEL};
        }

        ArrayList<ButtonType> buttons = new ArrayList<>();
        for (String option : options)
        {
            buttons.add(new ButtonType(option));
        }

        alert.getButtonTypes().setAll(buttons);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent())
        {
            return CANCEL;
        } else
        {
            return result.get().getText();
        }
    }

    public static void showMessage(String title, String msg, String type)
    {
        NotificationPane notificationPane = new NotificationPane();
        Platform.runLater(() ->
        {
            switch (type.toLowerCase())
            {
                case TAG_INFO:
                    //notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
                    //notificationPane.setText(msg);
                    //notificationPane.setGraphic(new Button("a button"));
                    //notificationPane.show();
                    Notifications.create()
                            .title(title)
                            .text(msg)
                            .hideAfter(Duration.seconds(15))
                            .position(Pos.BOTTOM_LEFT)
                            .owner(ScreenManager.getInstance())
                            .showInformation();
                    break;
                case TAG_WARN:
                    //notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
                    //notificationPane.setShowFromTop(true);
                    //notificationPane.setText(msg);
                    //notificationPane.setGraphic(new Button("a button"));
                    //notificationPane.show();
                    Notifications.create()
                            .title(title)
                            .text(msg)
                            .hideAfter(Duration.seconds(10))
                            .owner(ScreenManager.getInstance())
                            .showWarning();
                    break;
                case TAG_ERROR:
                    //notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
                    //notificationPane.setText(msg);
                    //notificationPane.setGraphic(new Button("a button"));
                    //notificationPane.show();
                    Notifications.create()
                            .title(title)
                            .text(msg)
                            .hideAfter(Duration.INDEFINITE)
                            .position(Pos.CENTER)
                            .owner(ScreenManager.getInstance())
                            .showError();
                    break;
                default:
                    IO.log(IO.class.getName(), IO.TAG_ERROR, "unknown message type '" + type + "'");
                    //Notifications.create().title(title).text(msg).showWarning();
                    break;
            }
        });
    }

   /* public static void showMessage(String title, String msg, String type)
    {
        Platform.runLater(() ->
        {
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setResizable(false);
            stage.setAlwaysOnTop(true);
            stage.centerOnScreen();

            Label label = new Label(msg);
            Button btn = new Button("Confirm");

            BorderPane borderPane= new BorderPane();
            borderPane.setTop(label);
            borderPane.setCenter(btn);
            //VBox vBox = new VBox(label, btn);
            stage.setScene(new Scene(borderPane));

            stage.show();

            btn.setOnAction(event -> stage.close());

            /*switch (type.toLowerCase())
            {
                case TAG_INFO:
                    JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
                    break;
                case TAG_WARN:
                    JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
                    break;
                case TAG_ERROR:
                    JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    System.err.println("IO> unknown message type '" + type + "'");
                    JOptionPane.showMessageDialog(null, msg, title, JOptionPane.PLAIN_MESSAGE);
                    break;
            }*
        });
    }*/

    public static void logAndAlert(String title, String msg, String type)
    {
        log(title, type, msg);
        showMessage(title, msg, type);
    }

    public static String readStream(InputStream stream) throws IOException
    {
        if(stream!=null)
        {
            //Get message from input stream
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            if (in != null)
            {
                StringBuilder msg = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null)
                {
                    msg.append(line + "\n");
                }
                in.close();

                return msg.toString();
            } else IO.logAndAlert(TAG, "Could not read error stream from server response.", IO.TAG_ERROR);
        } else IO.logAndAlert(TAG, "Could not read response from server.", IO.TAG_ERROR);
        return null;
    }

    public static void viewIndexPage(String title, FileMetadata[] documents, String path)
    {
        try
        {
            PDF.createDocumentIndex(title, documents, path);
        } catch (IOException e)
        {
            IO.logAndAlert("Error","Could not successfully generate index page: " + e.getMessage(), IO.TAG_ERROR);
        }
    }

    public static void printIndexPage(String path)
    {
        IO.log(TAG, IO.TAG_INFO, "printing index page.");
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    //Prepare headers
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                    File file= new File(path);
                    if(file.exists())
                    {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[512];
                        int read;
                        while ((read = fis.read(buffer)) > 0)
                        {
                            IO.log(TAG, IO.TAG_INFO, "read " + read + " bytes.");
                            outputStream.write(buffer, 0, read);
                        }
                        outputStream.flush();
                        outputStream.close();
                        fis.close();

                        IO.log(TAG, IO.TAG_INFO, "PDF total size: " + outputStream.toByteArray().length + " bytes. Ready for printing.");

                        PDF.printPDF(outputStream.toByteArray());
                        IO.log(TAG, IO.TAG_INFO, "Printing: Safety Index Page ]");
                    }else{
                        IO.logAndAlert("Error", "File 'bin/safety_index.pdf' was not found - have you generated the index page?", IO.TAG_ERROR);
                    }
                }catch (PrintException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void printSelectedDocuments(FileMetadata[] documents)
    {
        IO.log(TAG, IO.TAG_INFO, "printing selected documents.");
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    for (FileMetadata fileMetadata : documents)
                    {
                        if (fileMetadata.isMarked())
                        {
                            //Prepare headers
                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                            long start = System.currentTimeMillis();
                            byte[] doc = RemoteComms.sendFileRequest(fileMetadata.getPath(), headers);
                            long ellapsed = System.currentTimeMillis() - start;
                            IO.log(TAG, IO.TAG_INFO, "File [" + fileMetadata.getLabel() + "] download complete, size: " + doc.length + " bytes in " + ellapsed + "msec.");

                            PDF.printPDF(doc);
                            IO.log(TAG, IO.TAG_INFO, "Printing: " + fileMetadata.getLabel() + " ["+fileMetadata.get_id()+"]");
                        }
                    }
                }catch (PrintException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void printAllDocuments(FileMetadata[] documents)
    {
        IO.log(TAG, IO.TAG_INFO, "initiating atch printing of all documents...");
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    for (FileMetadata fileMetadata : documents)
                    {
                        //Prepare headers
                        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                        headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                        long start = System.currentTimeMillis();
                        byte[] doc = RemoteComms.sendFileRequest(fileMetadata.getPath(), headers);
                        long ellapsed = System.currentTimeMillis() - start;
                        IO.log(TAG, IO.TAG_INFO, "File [" + fileMetadata.getLabel() + "] download complete, size: " + doc.length + " bytes in " + ellapsed + "msec.");

                        PDF.printPDF(doc);
                        IO.log(TAG, IO.TAG_INFO, "Printing: " + fileMetadata.getLabel() + " ["+fileMetadata.get_id()+"]");
                    }
                }catch (PrintException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static String getEncryptedHexString(String message) throws Exception
    {
        StringBuilder str = new StringBuilder();
        for(byte b: hash(message))
            str.append(Integer.toHexString(0xFF & b));
        return str.toString();
    }

    public static byte[] hash(String plaintext) throws Exception
    {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(plaintext.getBytes());
        return m.digest();
    }

    public static byte[] encrypt(String digest, String message) throws Exception
    {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(digest.getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        final byte[] plainTextBytes = message.getBytes("utf-8");
        final byte[] cipherText = cipher.doFinal(plainTextBytes);

        return cipherText;
    }

    public static String decrypt(String digest, byte[] message) throws Exception
    {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(digest.getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;)
        {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        decipher.init(Cipher.DECRYPT_MODE, key, iv);

        final byte[] plainText = decipher.doFinal(message);

        return new String(plainText, "UTF-8");
    }

    public static void writeAttributeToConfig(String key, String value) throws IOException
    {
        //TO_Consider: add meta data for [key,value] to meta records.
        File f = new File("config.cfg");
        StringBuilder result = new StringBuilder();
        boolean rec_found=false;
        if(f.exists())
        {
            String s = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            int line_read_count=0;
            while ((s = in.readLine())!=null)
            {
                if(s.contains("="))
                {
                    String k = s.split("=")[0];
                    String val = s.split("=")[1];
                    //If the record exists, change it
                    if(k.equals(key))
                    {
                        val = value;//Update record value
                        rec_found=true;
                    }
                    result.append(k+"="+val+"\n");//Append existing record.
                    line_read_count++;
                } else IO.log(TAG, IO.TAG_ERROR, "Config file may be corrupted.");
            }
            if(!rec_found)//File exists but no key was found - write new line.
                result.append(key+"="+value+"\n");
            /*if(in!=null)
                in.close();*/
        } else result.append(key+"="+value+"\n");//File DNE - write new line.

        IO.log(TAG, IO.TAG_INFO, "writing attribute to config: " + key + "=" + value);

        /*if(!rec_found)//File exists but record doesn't exist - create new record
            result.append(key+"="+value+"\n");*/

        //Write to disk.
        PrintWriter out = new PrintWriter(f);
        out.print(result);
        out.flush();
        out.close();
    }

    public static String readAttributeFromConfig(String key) throws IOException
    {
        File f = new File("config.cfg");
        if(f.exists())
        {
            String s = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while ((s = in.readLine())!=null)
            {
                if(s.contains("="))
                {
                    String var = s.split("=")[0];
                    String val = s.split("=")[1];
                    if(var.equals(key))
                    {
                        /*if(in!=null)
                            in.close();*/
                        return val;
                    }
                }
            }
        }
        return null;
    }
}
