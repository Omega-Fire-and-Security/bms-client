/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.auxilary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.exceptions.LoginException;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.BusinessObject;
import fadulousbms.model.Error;
import sun.net.www.http.HttpClient;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class RemoteComms
{
    public static String host = "http://95.85.57.110:9000";//192.168.0.103
    public static final String TAG = "RemoteComms";

    public static Session auth(String usr, String pwd) throws IOException, LoginException
    {
        ArrayList<AbstractMap.SimpleEntry<String,String>> data = new ArrayList<>();
        data.add(new AbstractMap.SimpleEntry<>("usr",usr));
        data.add(new AbstractMap.SimpleEntry<>("pwd",pwd));
        HttpURLConnection connObj = postData("/api/auth", data, null);
        
        if(connObj.getResponseCode()==200)
        {
            String cookie = connObj.getHeaderField("Set-Cookie");
            if(cookie!=null)
            {
                String[] cookie_attrs = cookie.split(";");
                if(cookie_attrs.length>=3)
                {
                    String session_id="";//cookie_attrs[0];
                    int date=0, ttl=0;
                    for(String attr: cookie_attrs)
                    {
                        if(attr.contains("="))
                        {
                            String key = attr.split("=")[0];
                            String val = attr.split("=")[1];
                            switch(key.toUpperCase())
                            {
                                case "SESSION":
                                    session_id = val;
                                    break;
                                case "DATE":
                                    date = (int)Double.parseDouble(val);
                                    break;
                                case "TTL":
                                    ttl = Integer.parseInt(val);
                                    break;
                                default:
                                    IO.log("User Authenticator", IO.TAG_ERROR, "Unknown cookie attribute: " + key);
                                    break;
                            }
                        }else{
                            //throw new LoginException("Cookie attributes are invalid. Missing '='.");
                            IO.logAndAlert("Authentication Error", "Cookie attributes are invalid. Missing '='.", IO.TAG_ERROR);
                        }
                    }

                    IO.log("User Authenticator", IO.TAG_INFO, "successfully signed in.");

                    Session session = new Session(usr, session_id, date, ttl);
                    connObj.disconnect();
                    return session;
                }else{
                    connObj.disconnect();
                    IO.logAndAlert("Authentication Error", "Cookie attributes are invalid. Not enough attributes. Must be >= 3.", IO.TAG_ERROR);
                    //throw new LoginException("Cookie attributes are invalid. Not enough attributes. Must be >= 3.");
                }
            }else{
                connObj.disconnect();
                IO.logAndAlert("Authentication Error", "Cookie object is not set.", IO.TAG_ERROR);
               //throw new LoginException("Cookie object is not set.");
            }
        }else{
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

    public static void updateBusinessObjectOnServer(BusinessObject bo, String api_method, String property)
    {
        if(SessionManager.getInstance().getActive()!=null)
        {
            if(!SessionManager.getInstance().getActive().isExpired())
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                String id = bo.get_id();

                if(id!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                    try
                    {
                        HttpURLConnection connection = RemoteComms.postData(api_method + "/update/" + id, bo.asUTFEncodedString(), headers);
                        if(connection!=null)
                        {
                            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                                IO.log(TAG, IO.TAG_INFO, "Successfully updated BusinessObject's '" + property + "' property.");
                            else
                            {
                                String msg = IO.readStream(connection.getErrorStream());
                                /*Gson gson = new GsonBuilder().create();
                                Error error = gson.fromJson(msg, Error.class);*/
                                IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                            }
                            connection.disconnect();
                        }else IO.logAndAlert("Error", "Connection to server was interrupted.", IO.TAG_ERROR);
                    } catch (IOException e)
                    {
                        IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                    }
                } else IO.log(TAG, IO.TAG_ERROR, "Invalid BusinessObject ID");
            }else{
                IO.logAndAlert("Session expired", "No active sessions.", IO.TAG_ERROR);
            }
        }else{
            IO.logAndAlert("Error", "Connection to server was interrupted.", IO.TAG_ERROR);
            JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
        }
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
}
