package MASSLWithWebsockets.client;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLClient {

    public static void main(String[] args) {
        try {
            // allow java debugger to log all
            System.setProperty("javax.net.debug", "all");

            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            String password = "password";
            String client_certL = "client/client-certificate.p12";
            String server_certL = "server/server-certificate.p12";

            // (1)step: load client cert to keystore
            // load cert from resources folder
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(client_certL);
            keyStore.load(inputStream, password.toCharArray());

            // (2)step: load server cert to truststore
            KeyStore trustStore = KeyStore.getInstance("PKCS12");

            //password we use the same here
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE"); // we are giving exactly what we wanted here.

            InputStream servercert = ClassLoader.getSystemClassLoader().getResourceAsStream(server_certL);
            trustStore.load(servercert, password.toCharArray());

            // (3)step: initialize tmf with truststore
            trustManagerFactory.init(trustStore);

            X509TrustManager x509TrustManager = null;

            // loop through the array tmf to get the x509 instance
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()
            ) {
                if (trustManager instanceof X509TrustManager) {
                    x509TrustManager = (X509TrustManager) trustManager;
                    break;
                }

            }

            if (x509TrustManager == null) throw new NullPointerException();

            // (4)step: load keymanager factory to get the keymanager instance
            // with both in hand we can only call ssl context to init

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
            keyManagerFactory.init(keyStore, password.toCharArray());
            X509KeyManager x509KeyManager = null;

            for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
                if (keyManager instanceof X509KeyManager) {
                    x509KeyManager = (X509KeyManager) keyManager;
                    break;
                }
            }
            if (x509KeyManager == null) throw new NullPointerException();

            // (5)step: Ready for using SSL handshake
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, null);

            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            //load host port
            SSLSocket kkSocket = (SSLSocket) socketFactory.createSocket("127.0.0.1", 8333);
            // set protocol (opt)
            kkSocket.setEnabledProtocols(new String[]{"TLSv1.2"});

            // send/receive data
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(kkSocket.getInputStream())
            );

            // get user input
            BufferedReader stdIn = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            String fromUser;
            String fromServer;

            while ((fromServer = in.readLine())!= null){
                //while server talking
                System.out.println("server:"+fromServer);
                if(fromServer.equalsIgnoreCase("Bye."))
                    break;

                fromUser = stdIn.readLine();
                if(fromUser != null){
                    System.out.println("cleint:"+fromUser);
                    out.println(fromUser);

                }
            }


        }catch (IOException e){
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
