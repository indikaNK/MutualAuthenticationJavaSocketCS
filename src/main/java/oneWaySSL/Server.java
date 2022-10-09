package oneWaySSL;

import com.sun.net.ssl.internal.ssl.Provider;

import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.Security;


public class Server {
    private static int port = 35786;

    public static void main(String[] args) throws Exception{

        String servercert = "/home/indika/Documents/MY/JavaSocketCS/src/main/java/oneWaySSL/serverteststore.jks";
        String clientcert = "/home/indika/Documents/MY/JavaSocketCS/src/main/java/oneWaySSL/keystore.jks";

        char[] cleint_password = "password".toCharArray();
        char[] server_password = "password".toCharArray();

        // client used to communicate with the server
        System.setProperty("javax.net.ssl.keyStore", servercert);
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.debug","all");

        // JSSE provider to enforce the security protocols
//        Security.addProvider(new Provider());


        try{
            //implementing SSL context via SSLSocketFactory
            SSLServerSocketFactory sssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

            //Create SSLServerSocket using SSLServerSocketFactory established ssl context and connnects to the port
            SSLServerSocket sslServerSocket = (SSLServerSocket) sssf.createServerSocket(port);
            System.out.println("Server started ....");
            System.out.println("Ready to accept client connections  ....");

            System.out.println("Echo Server Started & Ready to accept Client Connection");

            // authentication needed for mutual ssl
//            sslServerSocket.setNeedClientAuth(true);


            //Wait for the SSL client send req to server (blocking call)
            SSLSocket sslSocket = (SSLSocket)sslServerSocket.accept();
            //Create InputStream to recive messages send by the client
            DataInputStream inputStream = new DataInputStream(sslSocket.getInputStream());
            //Create OutputStream to send message to client
            DataOutputStream outputStream = new DataOutputStream(sslSocket.getOutputStream());
            outputStream.writeUTF("Hello Client, Say Something!");
            //Keep sending the client the message you recive unless he sends the word "close"
            while(true)
            {
                String recivedMessage = inputStream.readUTF();
                System.out.println("Client Said : " + recivedMessage);
                if(recivedMessage.equals("close"))
                {
                    outputStream.writeUTF("Bye");
                    outputStream.close();
                    inputStream.close();
                    sslSocket.close();
                    sslServerSocket.close();
                    break;
                }
                else
                {
                    outputStream.writeUTF("You Said : "+recivedMessage);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }



    }
}
