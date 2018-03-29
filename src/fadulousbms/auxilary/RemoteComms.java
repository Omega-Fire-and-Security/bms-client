/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.auxilary;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.managers.SessionManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/01/11.
 * @author ghost
 */
public class RemoteComms
{
    public static String host = "http://localhost:8080";//192.168.0.103//95.85.57.110
    public static final String TAG = "RemoteComms";
    public static String MAIL_SERVER_IP = "localhost";
    public static int MAIL_SERVER_PORT = 9000;
    public static int TTL = 60*60*2;//2 hours in seconds

    public static Session auth(String usr, String pwd) throws Exception
    {
        URL urlConn = new URL(host + "/session");

        IO.log(RemoteComms.class.getName() + ">auth() ", IO.TAG_VERBOSE, "authenticating: ["+usr + "]");//["+IO.getEncryptedHexString(pwd)+"]"

        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();

        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("usr", usr);
        httpConn.setRequestProperty("pwd", IO.getEncryptedHexString(pwd));
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("PUT");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        //Write to server
        /*OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(creds);
        writer.flush();
        writer.close();
        os.close();*/

        if(httpConn!=null)
        {
            if (httpConn.getResponseCode() == 200)
            {
                //String cookie = connObj.getHeaderField("Set-Cookie");
                String session_str = IO.readStream(httpConn.getInputStream());
                try
                {
                    IO.log(RemoteComms.class.getName(),IO.TAG_INFO, "auth() response> "+session_str);
                    Session session = new GsonBuilder().create().fromJson(session_str, Session.class);
                    if (session != null)
                    {
                        IO.log(RemoteComms.class.getName() + ">auth()", IO.TAG_INFO, "user [" + usr + "] successfully signed in.");
                        httpConn.disconnect();
                        return session;
                    }
                    else
                    {
                        httpConn.disconnect();
                        IO.logAndAlert("Authentication Error", "Could not parse Session JSON object.", IO.TAG_ERROR);
                        //throw new LoginException("Cookie object is not set.");
                    }
                }catch (JsonSyntaxException e)
                {
                    IO.logAndAlert("Error", "Invalid server response.", IO.TAG_ERROR);
                    e.printStackTrace();
                }
            } else
            {
                httpConn.disconnect();
                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                    IO.logAndAlert("Authentication Error", "Invalid credentials.\nPlease try again with valid credentials or reset your password if you have forgotten it.", IO.TAG_ERROR);
                /*if(connObj.getResponseCode()==404)
                    throw new LoginException("Invalid credentials.");
                else
                    throw new LoginException("Could not authenticate, server response code: " + connObj.getResponseCode());*/
            }
        } else IO.log(RemoteComms.class.getName(), IO.TAG_ERROR, "auth()> Could not get a valid response from the server.");
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
        httpConn.setConnectTimeout(5000);
        httpConn.setReadTimeout(5000);

        boolean response = (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK);
        httpConn.disconnect();
        return response;
    }

    public static byte[] sendFileRequest(String file_url, ArrayList<AbstractMap.SimpleEntry<String, String>> headers) throws IOException
    {
        IO.log(TAG, IO.TAG_INFO, String.format("\nGET %s HTTP/1.1", file_url));

        URL urlConn = new URL(host + file_url);
        //set session_id
        if (SessionManager.getInstance().getActive() != null)
            headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive()
                    .getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return null;
        }
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

    public static String get(String url, ArrayList<AbstractMap.SimpleEntry<String, String>> headers) throws IOException
    {
        IO.log(TAG, IO.TAG_VERBOSE, String.format("\nGET %s HTTP/1.1\nHost: %s", url, host));

        URL urlConn = new URL(host + url);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();
        for(AbstractMap.SimpleEntry<String,String> header:headers)
        {
            IO.log(RemoteComms.class.getName(), IO.TAG_VERBOSE, "setting header: ["+header.getKey()+":"+header.getValue()+"]");
            httpConn.setRequestProperty(header.getKey(), header.getValue());
        }
        
        String response = "";
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line;
            while ((line=in.readLine())!=null)
                response += line;
        } else
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String line;
            while ((line=in.readLine())!=null)
                response += line;
            //IO.logAndAlert("Error", response, IO.TAG_ERROR);
        }

        IO.log(TAG, IO.TAG_INFO, "GET response> " + response + "\n");
        return response;
    }

    public static HttpURLConnection patch(String function, String object, ArrayList<AbstractMap.SimpleEntry<String, String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        //set session_id
        if (SessionManager.getInstance().getActive() != null)
            headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive()
                    .getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return null;
        }

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

    public static HttpURLConnection post(String function, String object, ArrayList<AbstractMap.SimpleEntry<String, String>> headers) throws IOException
    {
        //set session_id
        if (SessionManager.getInstance().getActive() != null)
            headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive()
                    .getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return null;
        }

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

    public static HttpURLConnection put(String function, String object, ArrayList<AbstractMap.SimpleEntry<String, String>> headers) throws IOException
    {
        //set session_id
        if (SessionManager.getInstance().getActive() != null)
            headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive()
                    .getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return null;
        }

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

    public static HttpURLConnection delete(String function, String object_id, ArrayList<AbstractMap.SimpleEntry<String, String>> headers) throws IOException
    {
        //set session_id
        if (SessionManager.getInstance().getActive() != null)
            headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive()
                    .getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return null;
        }

        URL urlConn = new URL(host + function + "/" + object_id);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
            {
                IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "setting header: ["+header.getKey()+": "+header.getValue()+"]");
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("DELETE");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("%s %s HTTP/1.1\nHost: %s", httpConn.getRequestMethod(), function, host));
        IO.log(RemoteComms.class.getName(), IO.TAG_INFO, "DELETE object: "+object_id);

        return httpConn;
    }

    //TODO: fix this here and on server
    public static void uploadFile(String endpoint, ArrayList<AbstractMap.SimpleEntry<String, String>> headers, byte[] file) throws IOException
    {
        //set session_id
        if (SessionManager.getInstance().getActive() != null)
            headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive()
                    .getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }

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
}
