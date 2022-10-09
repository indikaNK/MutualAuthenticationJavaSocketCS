package MASSLWithWebsockets.server;

import MASSLWithWebsockets.protocol.KnockKnockProtocol;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLServer {

    public static void main(String[] args) {

            try {
                // allow java debugger to log all
                System.setProperty("javax.net.debug", "all");

                KeyStore keyStore = KeyStore.getInstance("PKCS12");

                String password = "password";
                String client_certL = "client/client-certificate.p12";
                String server_certL = "server/server-certificate.p12";

                // (1)step: load server cert to keystore
                // load server cert from resources folder
                InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(server_certL);
                keyStore.load(inputStream, password.toCharArray());

                // (2)step: load client cert to truststore
                KeyStore trustStore = KeyStore.getInstance("PKCS12");
                //password we use the same here
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE"); // we are giving exactly what we wanted here.

                InputStream cleintcert = ClassLoader.getSystemClassLoader().getResourceAsStream(client_certL);
                trustStore.load(cleintcert, password.toCharArray());

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

                // (6)step: setup SSL server socket

                SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
                // host and port configure
                SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(8333);
                serverSocket.setNeedClientAuth(true);
                serverSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
                SSLSocket socket = (SSLSocket) serverSocket.accept();

                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String inputLine, outputLine;

                //conversation initiate with client and knock knock protocol :D

                KnockKnockProtocol kkp = new KnockKnockProtocol();

                outputLine = kkp.processInput(null);
                out.println(outputLine);

                while ((inputLine = in.readLine()) != null) {
                    outputLine = kkp.processInput(inputLine);
                    out.println(outputLine);
                    if (outputLine.equals("Bye."))
                        break;
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
