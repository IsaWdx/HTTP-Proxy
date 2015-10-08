import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by WangDongxu on 2015/9/21.
 */
public class WebProxy {
    private static int port;
    public static void main(String[] args) {
        port = Integer.parseInt(args[0]);
        start();
    }
    public static void start() {
        try {
          //  FileInputStream fis;
            ArrayList<String> censor_word = new ArrayList<String>();
          //  fis = new FileInputStream("censor.txt");
          //  InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
         //   BufferedReader in = new BufferedReader(isr);
        //    String line="";
       //     while ((line=in.readLine())!=null) {
       //         censor_word.add(line);
      //          System.out.println(line);
       //     }

       //     in.close();
      //      isr.close();
      //      fis.close();


            ServerSocket socket = new ServerSocket(port);
            Socket client = null;
            while (true) {
                client = socket.accept();
                System.out.println("'Received a connection from: " + client);
                WebProxyThread wpt = new WebProxyThread(client,censor_word);
                wpt.start();
            }
        }catch (IOException e) {
            System.out.println("Catch an IOException in the main loooop!");
        }
    }
}

