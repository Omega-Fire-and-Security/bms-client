/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.auxilary;

import com.google.gson.GsonBuilder;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Email;
import com.mailjet.client.resource.Emailv31;
import fadulousbms.exceptions.LoginException;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.BusinessObject;
import fadulousbms.model.Employee;
import fadulousbms.model.FileMetadata;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class RemoteComms
{
    public static String host = "http://localhost:8080";//192.168.0.103//95.85.57.110
    public static final String TAG = "RemoteComms";
    public static String MAIL_SERVER_IP = "localhost";
    public static int MAIL_SERVER_PORT = 9000;
    public static int TTL = 60*60*2;//2 hours in sec

    public static Session auth(String usr, String pwd) throws Exception
    {
        /*ArrayList<AbstractMap.SimpleEntry<String,String>> data = new ArrayList<>();
        data.add(new AbstractMap.SimpleEntry<>("usr",usr));
        data.add(new AbstractMap.SimpleEntry<>("pwd",pwd));*/

        ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
        headers.add(new AbstractMap.SimpleEntry<>("usr", usr));
        headers.add(new AbstractMap.SimpleEntry<>("pwd", IO.getEncryptedHexString(pwd)));
        HttpURLConnection connObj = putJSONData("/auth", null, headers);
        
        if(connObj.getResponseCode()==200)
        {
            //String cookie = connObj.getHeaderField("Set-Cookie");
            String session_str = IO.readStream(connObj.getInputStream());
            Session session = new GsonBuilder().create().fromJson(session_str, Session.class);
            if(session!=null)
            {
                    IO.log("User Authenticator", IO.TAG_INFO, "successfully signed in.");
                    connObj.disconnect();
                    return session;
            } else
            {
                connObj.disconnect();
                IO.logAndAlert("Authentication Error", "Could not parse Session JSON object.", IO.TAG_ERROR);
               //throw new LoginException("Cookie object is not set.");
            }
        } else
        {
            connObj.disconnect();
            IO.log("User Authenticator", IO.TAG_ERROR, "could not sign in.");
            if(connObj.getResponseCode()==HttpURLConnection.HTTP_NOT_FOUND)
                IO.logAndAlert("Authentication Error", "Invalid credentials.\nPlease try again with valid credentials or reset your password if you have forgotten it.", IO.TAG_ERROR);
            /*if(connObj.getResponseCode()==404)
                throw new LoginException("Invalid credentials.");
            else
                throw new LoginException("Could not authenticate, server response code: " + connObj.getResponseCode());*/
        }
        return null;
    }

    public static void setHost(String h)
    {
        host = h;
    }

    public static boolean pingServer() throws IOException
    {
        URL urlConn = new URL(host);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();

        boolean response = (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK);
        httpConn.disconnect();
        return response;
    }

    public static String sendGetRequest(String url, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(TAG, IO.TAG_INFO, String.format("\nGET %s HTTP/1.1\nHost: %s", url, host));

        URL urlConn = new URL(host + url);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();
        for(AbstractMap.SimpleEntry<String,String> header:headers)
            httpConn.setRequestProperty(header.getKey() , header.getValue());
        
        String response = null;
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            response="";
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line="";
            int read=0;
            while ((line=in.readLine())!=null)
                response += line;
            //Log.d(TAG,response);
        }else
        {
            response="";
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String line="";
            int read=0;
            while ((line=in.readLine())!=null)
                response += line;
            IO.logAndAlert("Error", response, IO.TAG_ERROR);
        }

        IO.log(TAG, IO.TAG_INFO, "GET response> " + response + "\n");
        return response;
    }

    public static byte[] sendFileRequest(String file_url, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(TAG, IO.TAG_INFO, String.format("\nGET %s HTTP/1.1", file_url));

        URL urlConn = new URL(host + file_url);
        //URL urlConn = new URL("http://127.0.0.1:9000/api/file/inspection/3-demolition.pdf");
        try(InputStream in = urlConn.openStream())
        {
            //Files.copy(in, new File("download.pdf").toPath(), StandardCopyOption.REPLACE_EXISTING);
            //DataInputStream dataInputStream = new DataInputStream(in);


            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read=0;
            while ((read=in.read(buffer, 0, buffer.length))>0)
                outbytes.write(buffer, 0, read);
            outbytes.flush();
            in.close();
            IO.log(TAG, IO.TAG_INFO, "GET received file> " + file_url + " " + outbytes.toByteArray().length + " bytes.\n");
            return outbytes.toByteArray();
        }
        //URL urlConn = new URL(host);
        /*HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();

        for(AbstractMap.SimpleEntry<String,String> header:headers)
            httpConn.setRequestProperty(header.getKey() , header.getValue());


        String response = null;
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            response="";
            DataInputStream in = new DataInputStream(httpConn.getInputStream());

            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read=0;
            while ((read=in.read(buffer, 0, buffer.length))>0)
            {
                outbytes.write(buffer, 0, read);
            }
            outbytes.flush();
            in.close();
            IO.log(TAG, IO.TAG_INFO, "GET received file> " + filename + " " + outbytes.toByteArray().length + "bytes.\n");
            return outbytes.toByteArray();
        }else
        {
            IO.log(TAG, IO.TAG_ERROR, IO.readStream(httpConn.getErrorStream()));
            return null;
        }*/
    }
    
    public static HttpURLConnection postData(String function, ArrayList<AbstractMap.SimpleEntry<String,String>> params, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
                httpConn.setRequestProperty(header.getKey() , header.getValue());
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        //Encode body data in UTF-8 charset
        StringBuilder result = new StringBuilder();
        for(int i=0;i<params.size();i++)
        {
            AbstractMap.SimpleEntry<String,String> entry = params.get(i);
            if(entry!=null)
            {
                if(entry.getKey()!=null && entry.getValue()!=null)
                {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    result.append((i != params.size() - 1 ? "&" : ""));
                }else return null;
            }else return null;
        }

        IO.log(TAG, IO.TAG_INFO, String.format("POST %s HTTP/1.1\nHost: %s", function, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(result.toString());
        writer.flush();
        writer.close();
        os.close();

        //httpConn.connect();
        
        /*Scanner scn = new Scanner(new InputStreamReader(httpConn.getErrorStream()));
        String resp = "";
        while(scn.hasNext())
            resp+=scn.nextLine();
        System.err.println(resp);*
        String resp = httpConn.getHeaderField("Set-Cookie");
        System.err.println(resp);*/
        
        return httpConn;
    }

    public static HttpURLConnection putURLEncodedData(String function, ArrayList<AbstractMap.SimpleEntry<String,String>> params, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
                httpConn.setRequestProperty(header.getKey() , header.getValue());
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("PUT");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        //Encode body data in UTF-8 charset
        StringBuilder result = new StringBuilder();
        for(int i=0;i<params.size();i++)
        {
            AbstractMap.SimpleEntry<String,String> entry = params.get(i);
            if(entry!=null)
            {
                if(entry.getKey()!=null && entry.getValue()!=null)
                {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    result.append((i != params.size() - 1 ? "&" : ""));//add ampersand if not last param
                }else return null;
            }else return null;
        }

        IO.log(TAG, IO.TAG_INFO, String.format("%s %s HTTP/1.1\nHost: %s", httpConn.getRequestMethod(), function, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(result.toString());
        writer.flush();
        writer.close();
        os.close();

        //httpConn.connect();

        /*Scanner scn = new Scanner(new InputStreamReader(httpConn.getErrorStream()));
        String resp = "";
        while(scn.hasNext())
            resp+=scn.nextLine();
        System.err.println(resp);*
        String resp = httpConn.getHeaderField("Set-Cookie");
        System.err.println(resp);*/

        return httpConn;
    }

    public static HttpURLConnection putJSONData(String function, ArrayList<AbstractMap.SimpleEntry<String,String>> params, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        /*URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
            {
                IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "setting header: ["+header.getKey()+":"+header.getValue()+"]");
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("PUT");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);*/

        //Encode body data in UTF-8 charset
        StringBuilder result = new StringBuilder("{");
        if(params!=null)
        {
            for (int i = 0; i < params.size(); i++)
            {
                AbstractMap.SimpleEntry<String, String> entry = params.get(i);
                if (entry != null)
                {
                    if (entry.getKey() != null && entry.getValue() != null)
                    {
                        //if not first item, add commas
                        if(i>0)
                            result.append(",");
                        result.append("\"" + entry.getKey() + "\"");
                        result.append(':');
                        result.append("\"" + entry.getValue() + "\"");
                        //result.append((i != params.size() - 1 ? "," : ""));//add comma if not last param
                    } else
                    {
                        IO.log(RemoteComms.class.getName(), IO.TAG_ERROR, "invalid key-value pair for entry: [" + entry.getKey()+","+entry.getValue()+"]");
                        return null;
                    }
                } else
                {
                    IO.log(RemoteComms.class.getName(), IO.TAG_ERROR, "invalid entry");
                    return null;
                }
            }
        }
        result.append("}");
        /*IO.log(TAG, IO.TAG_INFO, String.format("%s %s HTTP/1.1\nHost: %s", httpConn.getRequestMethod(), function, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(result.toString());
        writer.flush();
        writer.close();
        os.close();

        //httpConn.connect();

        /*Scanner scn = new Scanner(new InputStreamReader(httpConn.getErrorStream()));
        String resp = "";
        while(scn.hasNext())
            resp+=scn.nextLine();
        System.err.println(resp);*
        String resp = httpConn.getHeaderField("Set-Cookie");
        System.err.println(resp);*

        return httpConn;*/
        return putJSON(function, result.toString(), headers);
    }

    public static HttpURLConnection patchJSON(String function, String object, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
            {
                IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "setting header: ["+header.getKey()+":"+header.getValue()+"]");
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("%s %s HTTP/1.1\nHost: %s", httpConn.getRequestMethod(), function, host));
        IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "PATCHing data: "+object);

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(object);
        writer.flush();
        writer.close();
        os.close();

        return httpConn;
    }

    public static HttpURLConnection postJSON(String function, String object, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
            {
                IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "setting header: ["+header.getKey()+":"+header.getValue()+"]");
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("%s %s HTTP/1.1\nHost: %s", httpConn.getRequestMethod(), function, host));
        IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "POSTting data: "+object);

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(object);
        writer.flush();
        writer.close();
        os.close();

        return httpConn;
    }

    public static HttpURLConnection putJSON(String function, String object, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
            {
                IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "setting header: ["+header.getKey()+":"+header.getValue()+"]");
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("PUT");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("%s %s HTTP/1.1\nHost: %s", httpConn.getRequestMethod(), function, host));
        IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "PUTting data: "+object);

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(object);
        writer.flush();
        writer.close();
        os.close();

        return httpConn;
    }

    public static HttpURLConnection postData(String function, String object, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
                httpConn.setRequestProperty(header.getKey() , header.getValue());
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("POST %s HTTP/1.1\nHost: %s", function, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(object);
        writer.flush();
        writer.close();
        os.close();

        return httpConn;
    }

    public static void updateBusinessObjectOnServer(BusinessObject bo, String property)
    {
        if(SessionManager.getInstance().getActive()!=null)
        {
            if(!SessionManager.getInstance().getActive().isExpired())
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                //String id = bo.get_id();

                if(bo!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                    headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
                    try
                    {
                        HttpURLConnection connection = RemoteComms.patchJSON(bo.apiEndpoint(), bo.getJSONString(), headers);
                        if(connection!=null)
                        {
                            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                                IO.log(TAG, IO.TAG_INFO, "Successfully updated BusinessObject{"+bo.getClass().getName()+"}'s '" + property + "' property to ["+bo.get(property)+"].");
                            else
                            {
                                String msg = IO.readStream(connection.getErrorStream());
                                IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                            }
                            connection.disconnect();
                        } else IO.logAndAlert("Error", "Connection to server was interrupted.", IO.TAG_ERROR);
                    } catch (IOException e)
                    {
                        IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                    }
                } else IO.log(TAG, IO.TAG_ERROR, "Invalid BusinessObject");
            } else IO.logAndAlert("Session expired", "No active sessions.", IO.TAG_ERROR);
        } else IO.logAndAlert("Error: Invalid Session", "Active Session is invalid.", IO.TAG_ERROR);
    }

    public static void uploadFile(String endpoint, ArrayList<AbstractMap.SimpleEntry<String,String>> headers, byte[] file) throws IOException
    {
        URL urlConn = new URL(host + endpoint);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
                httpConn.setRequestProperty(header.getKey() , header.getValue());

        httpConn.setRequestProperty("Content-Length", String.valueOf(file.length));
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("POST %s HTTP/1.1\nHost: %s", endpoint, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        //OutputStreamWriter writer = new OutputStreamWriter(os);
        os.write(file);
        os.flush();
        os.close();

        httpConn.connect();
        String desc = IO.readStream(httpConn.getInputStream());
        IO.log(RemoteComms.class.getName(), httpConn.getResponseCode() + ":\t" + desc, IO.TAG_INFO);
        httpConn.disconnect();
    }

    public static void bar(String sender_email, String recipient_email) throws MailjetSocketTimeoutException, MailjetException
    {
        IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "sending email from ["+sender_email+"] to ["+recipient_email+"]");
        MailjetClient client = new MailjetClient(System.getenv("f8d3d1d74c95250bb2119063b3697082"), System.getenv("8304b30da4245632c878bf48f1d65d92"));
        /*MailjetRequest email = new MailjetRequest(Email.resource)
                .property("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("From", new JSONObject()
                                        .put(Email.FROMEMAIL, sender_email)
                                        .put(Email.FROMNAME, "pandora"))
                                .put(Email.TO, new JSONArray()
                                        .put(new JSONObject()
                                            .put("Email", recipient_email)))
                                .put(Email.SUBJECT, )
                                .put(Email.TEXTPART, "Dear passenger, welcome to Mailjet! May the delivery force be with you!")
                                .put(Email.HTMLPART, "<h3>Dear passenger, welcome to Mailjet!</h3><br />May the delivery force be with you!")));*/
        MailjetRequest email = new MailjetRequest(Email.resource);
        email.property(Email.FROMEMAIL, sender_email);
        email.property(Email.FROMNAME, "some person");
        email.property(Email.TO, recipient_email);
        email.property(Email.SUBJECT, "Your email flight plan!");
        email.property(Email.TEXTPART, "Dear passenger, welcome to Mailjet! May the delivery force be with you!");
        email.property(Email.HTMLPART, "<h3>Dear passenger, welcome to Mailjet!</h3><br />May the delivery force be with you!");
        email.property(Email.ATTACHMENTS, "<h3>Dear passenger, welcome to Mailjet!</h3><br />May the delivery force be with you!");
        client.post(email);
        IO.logAndAlert(RemoteComms.class.getName(), "Sent email to ["+recipient_email+"]", IO.TAG_INFO);
    }

    public static void sendMail(String sender_email, String recipient_email) throws MailjetSocketTimeoutException, MailjetException
    {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient("f8d3d1d74c95250bb2119063b3697082", "8304b30da4245632c878bf48f1d65d92", new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", sender_email)
                                        .put("Name", "BMS"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", recipient_email)
                                                .put("Name", "Some Name")))
                                .put(Emailv31.Message.SUBJECT, "Your email flight plan!")
                                .put(Emailv31.Message.TEXTPART, "Dear passenger 1, welcome to Mailjet! May the delivery force be with you!")
                                .put(Emailv31.Message.HTMLPART, "<h3>Dear passenger 1, welcome to Mailjet!</h3><br />May the delivery force be with you!")));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }

    public static void emailAttachment(String subject, String message, Employee[] recipient_employees, FileMetadata[] fileMetadata) throws MailjetSocketTimeoutException, MailjetException
    {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        //setup recipients
        JSONArray recipients = new JSONArray();
        for(Employee recipient:recipient_employees)
                recipients.put(new JSONObject()
                        .put("Email", recipient.getEmail())
                        .put("Name", recipient.getFirstname()+" "+recipient.getLastname()));

        //setup files to be emailed
        JSONArray files = new JSONArray();
        for(FileMetadata file: fileMetadata)
            files.put(new JSONObject()
            .put("ContentType", file.getContent_type())
            .put("Filename", file.getLabel())
            .put("Base64Content", "VGhpcyBpcyB5b3VyIGF0dGFjaGVkIGZpbGUhISEK"));


        client = new MailjetClient("f8d3d1d74c95250bb2119063b3697082", "8304b30da4245632c878bf48f1d65d92", new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "bms@omegafs.co.za")
                                        .put("Name", "BMS"))
                                .put(Emailv31.Message.TO, recipients)
                                .put(Emailv31.Message.SUBJECT, subject)
                                //.put(Emailv31.Message.TEXTPART, "Dear passenger 1, welcome to Mailjet! May the delivery force be with you!")
                                .put(Emailv31.Message.HTMLPART, message)
                                .put(Emailv31.Message.ATTACHMENTS, files)));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }
}
