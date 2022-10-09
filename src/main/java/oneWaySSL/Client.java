package oneWaySSL;

import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {

        int port = 35786;
        char[] cleint_password = "password".toCharArray();
        char[] server_password = "password".toCharArray();

        System.setProperty("javax.net.debug","all");


// configure SSL for client authentication
        KeyStore clientKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
        clientKeystore.load(
                Files.newInputStream(Paths.get("/home/indika/Documents/MY/JavaSocketCS/src/main/java/oneWaySSL/keystore.jks")),
                cleint_password);

        //client key manager factory where to store client certificates
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        //initialize with client keystore
        kmf.init(clientKeystore, cleint_password);

        KeyManager[] keyManagers = kmf.getKeyManagers();

        //configure the client to trust the server
        KeyStore serverKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
        serverKeystore.load(
                new FileInputStream("/home/indika/Documents/MY/JavaSocketCS/src/main/java/oneWaySSL/serverteststore.jks"),server_password
        );

        //setup trust-manager factory to see weather server is trusted
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(serverKeystore);
        TrustManager[] trustManagers = tmf.getTrustManagers();

        //setup SSL context to and parse the retrived keymanagers and trustmanagers
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers,trustManagers,null);

        //Create SSLSocketFactory and establish the connection
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        SSLSocket  sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost",port);


        // send data to server
        DataOutputStream outputStream = new DataOutputStream(sslSocket.getOutputStream());
        //Create InputStream to read messages send by the server
        DataInputStream inputStream = new DataInputStream(sslSocket.getInputStream());
        //read the first message send by the server after being connected
//        System.out.println(inputStream.readUTF());
        //Keep  sending the server the message entered by the client unless the it is "close"

        Scanner scanner = new Scanner(System.in);


        while (true)
        {
            System.out.println("Write a Message : ");
            String messageToSend = scanner.next();
            outputStream.writeUTF(messageToSend);
            System.err.println(inputStream.readUTF());
            if(messageToSend.equals("close"))
            {
                break;
            }
        }

    }
}
