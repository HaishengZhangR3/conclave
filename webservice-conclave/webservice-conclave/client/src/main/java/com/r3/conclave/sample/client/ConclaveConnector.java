package com.r3.conclave.sample.client;

import com.r3.conclave.client.EnclaveConstraint;
import com.r3.conclave.client.InvalidEnclaveException;
import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.mail.Curve25519KeyPairGenerator;
import com.r3.conclave.mail.EnclaveMail;
import com.r3.conclave.mail.MutableMail;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.KeyPair;

public class ConclaveConnector {

    public static String enclaveHost = "http://localhost:9999";
    public static String infoPoint = "/enclaveinstance";
    public static String trainingPoint = "/training";
    public static String predictPoint = "/predict";
    public static String algorithmPoint = "/algorithm";

    public static byte[] postData(String endpoint, String topic, int sequenceNumber, byte[] data) throws IOException {
        // Generate our own Curve25519 keypair so we can receive a response.
        KeyPair myKey = new Curve25519KeyPairGenerator().generateKeyPair();

        // Send a GET request to retrieve the remote attestation
        EnclaveInstanceInfo enclave = getEnclave();

        // Create a mail object with the bid as a byte[]
        MutableMail mail = enclave.createMail(data);
        mail.setSequenceNumber(sequenceNumber);
        mail.setPrivateKey(myKey.getPrivate());
        mail.setTopic(topic);

        // Encrypt the mail
        byte[] encryptedMail = mail.encrypt();

        System.out.println("Sending the encrypted mail to the host.");

        // Create a POST request to send the encrypted byte[] to Host server
        HttpURLConnection postConn;
        URL url = new URL(enclaveHost + endpoint);
        postConn = (HttpURLConnection) url.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "image/jpeg");
        postConn.setDoOutput(true);

        try (OutputStream os = postConn.getOutputStream()) {
            os.write(encryptedMail, 0, encryptedMail.length);
        }

        byte[] response = new byte[0];
        try {
            // Read the enclave's response given by the server
            byte[] encryptedReply = new byte[postConn.getInputStream().available()];
            postConn.getInputStream().read(encryptedReply);

            // Try to decrypt the response. If it is a proper MAIL object then it should work
            // else, we simply let the client know in a catch block that their bid was received.
            EnclaveMail replyMail = enclave.decryptMail(encryptedReply, myKey.getPrivate());
            response = replyMail.getBodyAsBytes();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            postConn.disconnect();
        }

        return response;
    }

    public static EnclaveInstanceInfo getEnclave() throws IOException {
        URL url = new URL(enclaveHost + infoPoint);
        HttpURLConnection getConn = (HttpURLConnection) url.openConnection();
        getConn.setRequestMethod("GET");

        EnclaveInstanceInfo attestation = null;
        try {
            //check attestation
            byte[] buf = new byte[getConn.getInputStream().available()];
            getConn.getInputStream().read(buf);
            attestation = EnclaveInstanceInfo.deserialize(buf);
            EnclaveConstraint.parse("S:4924CA3A9C8241A3C0AA1A24A407AA86401D2B79FA9FF84932DA798A942166D4 PROD:1 SEC:INSECURE").check(attestation);

        } catch (IOException | InvalidEnclaveException e) {
            e.printStackTrace();
        } finally {
            getConn.disconnect();
        }
        return attestation;
    }
}
