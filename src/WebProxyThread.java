import sun.misc.BASE64Encoder;

import java.beans.Encoder;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by WangDongxu on 2015/9/21.
 */
public class WebProxyThread extends Thread{
    private Socket client;
    private ArrayList<String>censor_word;
    public WebProxyThread(Socket client,ArrayList<String>censor_word){
        this.client = client;
        this.censor_word = censor_word;
    }
    public void run(){
        byte[] requestBuffer = new byte[8192];
        String uri = "", host = "", request = "",method = "";
        String []tmp;
        int length, server_port, end;
        BufferedInputStream in;
        BufferedOutputStream out;
        try {
            in = new BufferedInputStream(client.getInputStream());
            out = new BufferedOutputStream(client.getOutputStream());
            length = in.read(requestBuffer);
            if (length != -1)
                request = new String(requestBuffer, 0, length);
            else return;
            System.out.print(request);
            end = request.indexOf("\n");
            tmp = request.substring(0,end-1).split(" ");
            host = getHost(request);
            method = tmp[0];
            uri = tmp[1];
            end = host.indexOf(":");
            if(end == -1)
                server_port = 80;
            else {
                server_port = Integer.parseInt(host.substring(end+1,host.length()));
                host = host.substring(0, end);
                System.out.println("host:"+host);
                System.out.println("port:" + server_port);
            }
            String encoded_uri = Coder.Digest(uri);
            /** Check cache if file exists **/
            File f = new File(encoded_uri);
            if (f.exists()) {
                /** Read the file **/
            System.out.print("file exists\n");
                byte[] fileArray;
                int index = request.indexOf("\r\n\r\n");
                if(method.equalsIgnoreCase("GET"))
                request = request.substring(0,index)+"\r\nIf-Modified-Since: "+getDate()+request.substring(index);
                System.out.println(request);
                Path file = Paths.get(encoded_uri);
                fileArray = Files.readAllBytes(file);
                boolean found = serverResponse(encoded_uri, host, request, server_port, out);
                if(found == false) {
                    out.write(fileArray);
                    String a = new String(fileArray);
                    System.out.println(new String(fileArray));
                }


                /** generate appropriate respond headers and send the file contents **/
            }
            else {
                System.out.println(request);
                /** Get response from server **/
                FileOutputStream fos = new FileOutputStream(encoded_uri);
                serverResponse(encoded_uri, host, request, server_port, out);

            }
            out.close();
            in.close();
           client.close();
        }
        catch (IOException e){
            System.out.println("Cannot get the requested message ");
        }
    }
    public static String getHost(String request){
        int start = request.indexOf("Host:");
        int end = request.indexOf("\r\n",start);
        String host = request.substring(start + 6, end);
        return host;
    }

    public boolean serverResponse(String encoded_uri, String host, String request,int server_port,BufferedOutputStream out) {
        int length = 0;
        FileOutputStream fos = null;
        byte[] responseBuffer = new byte[1048576];
        boolean statError = false;
        Socket clientSocket = null;
        try {
            /** connect to server and relay client's request **/
            clientSocket = new Socket(host, server_port);
        }
        catch (Exception e) {
            statError = true;
            /**  */
            System.out.println("You meet with a 502 error");
        }
        try{
            if(statError == true){
                out.write(statError().getBytes());
                System.out.println(statError());
            }
            else {
                System.out.println("host:" + host);
                PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedInputStream fromServer = new BufferedInputStream(clientSocket.getInputStream());
                toServer.println(request);
                int round = 0;
                boolean istext = false;
                while ((length = fromServer.read(responseBuffer, 0, 1048576)) != -1) {
                    System.out.println("Received from server: ");
                    String b = new String (responseBuffer,0,length);
                    System.out.println(b);
                   if(round == 0){
                        String a = new String (responseBuffer,0,length);
                       int index = a.indexOf("\r\n\r\n");
                       int index2 = a.indexOf("\r\n");
                       String head = a.substring(0, index);
                       String firstline = a.substring(0, index2);

                      // System.out.print("responsehead: "+head);
                       if(firstline.split(" ")[1].equals("304")){
                           System.out.println("cache hit\n");
                           return false;
                       }
                       else {
                           fos = new FileOutputStream(encoded_uri);
                       }
                       // if(head.contains("Content-Type: text/html")){//
                         //  istext = true;//
                           // for(int i = 0;i<censor_word.size();i++)//
                             //   a = a.replace(censor_word.get(i),"---");//
                            //out.write(a.getBytes(), 0, a.length());//
                            //fos.write(a.getBytes(), 0,a.length());//
                        //}//
                        //else{//
                          //  istext = false;
                            out.write(responseBuffer,0,length);
                            if(fos != null)fos.write(responseBuffer,0,length);
                        //}//

                    }
                    else{
                    //   if(istext == true){
                     //      String a = new String (responseBuffer,0,length,"utf-8");
                      //     for(int i = 0;i<censor_word.size();i++)
                      //         a= a.replace(censor_word.get(i),"---");
                      //     out.write(a.getBytes(), 0, a.length());
                       //    fos.write(a.getBytes(), 0,a.length());
                      // }
                      // else{
                           out.write(responseBuffer,0,length);
                           fos.write(responseBuffer,0,length);
                      // }
                   }


                    round++;
                }

                //if (fos != null) fos.write(CensoredBuffer, 0, a.length());
                fos.flush();
                fos.close();
                toServer.close();
                fromServer.close();
                clientSocket.close();
            }
            out.flush();
        }catch (IOException e) {
            System.out.println(host + "Error transferring message from server.");
        }
        return true;

    }
    public String statError(){
        return "HTTP/1.1 502 Bad Gateway\r\n" +
                "Date: "+getDate()+"\r\n" +
                "Proxy-Agent: HTTP Proxy Server\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n"+
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "    <head>\n" +
                "        <meta charset=\"utf-8\">      \n" +
                "        <title>HTTP Status: 502 Bad Gateway</title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "    <p1>HTTP Status: 502 Bad Gateway!</p1>"+
                "    </body>\n" +
                "</html>";
    }
    public String getDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);
        Date date = Calendar.getInstance().getTime();
        TimeZone srcTimeZone = TimeZone.getTimeZone("GMT+8");
        TimeZone destTimeZone = TimeZone.getTimeZone("GMT");
        Long targetTime = date.getTime() - srcTimeZone.getRawOffset() + destTimeZone.getRawOffset();
       return sdf.format(new Date(targetTime)).replace("CST","GMT");
        }
}
