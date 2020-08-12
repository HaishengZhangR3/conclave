package com.r3.conclave.sample.client;

import com.r3.conclave.client.EnclaveConstraint;
import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.mail.Curve25519KeyPairGenerator;
import com.r3.conclave.mail.EnclaveMail;
import com.r3.conclave.mail.MutableMail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.util.UUID;

public class Client {
    public static void main(String[] args) throws Exception {
        // This is the client that will upload secrets to the enclave for processing.
        //
        // In this simple hello world app, we just connect to a TCP socket, take the EnclaveInstanceInfo we're sent
        // and transmit an encrypted string. The enclave will reverse it and send it back. You can use this sample
        // as a basis for your own apps.


        // Connect to the host, it will send us a remote attestation (EnclaveInstanceInfo).
        DataInputStream fromHost;
        DataOutputStream toHost;
        while (true) {
            try {
                System.out.println("Attempting to connect to localhost:9999");
                Socket socket = new Socket("localhost", 9999);
                fromHost = new DataInputStream(socket.getInputStream());
                toHost = new DataOutputStream(socket.getOutputStream());
                break;
            } catch (Exception e) {
                Thread.sleep(2000);
            }
        }

        byte[] attestationBytes = new byte[fromHost.readInt()];
        fromHost.readFully(attestationBytes);
        EnclaveInstanceInfo attestation = EnclaveInstanceInfo.deserialize(attestationBytes);

        // Check it's the enclave we expect. This will throw InvalidEnclaveException if not valid.
        System.out.println("Connected to " + attestation);
        EnclaveConstraint.parse("S:4924CA3A9C8241A3C0AA1A24A407AA86401D2B79FA9FF84932DA798A942166D4 PROD:1 SEC:INSECURE").check(attestation);

        // Generate our own Curve25519 keypair so we can receive a response.
        KeyPair myKey = new Curve25519KeyPairGenerator().generateKeyPair();

        // Now we checked the enclave's identity and are satisfied it's the enclave from this project,
        // we can send mail to it. We will provide our own private key whilst encrypting, so the enclave
        // gets our public key and can encrypt a reply.
//        MutableMail mail = attestation.createMail(toReverse.getBytes(StandardCharsets.UTF_8));

        String algo;
        {
            String file = "/diabetes.txt";
            String dataSet = Utils.loadTextFromResource(file);

            // leading "T:" means: training
            MutableMail mail = attestation.createMail(("T:" + dataSet).getBytes());
            mail.setPrivateKey(myKey.getPrivate());
            // Set a random topic, so we can re-run this program against the same server.
            mail.setTopic(UUID.randomUUID().toString());
            byte[] encryptedMail = mail.encrypt();

            System.out.println("Sending the encrypted mail to the host.");

            toHost.writeInt(encryptedMail.length);
            toHost.write(encryptedMail);

            // Enclave will mail us back.
            byte[] encryptedReply = new byte[fromHost.readInt()];
            System.out.println("Reading reply mail of length " + encryptedReply.length + " bytes.");
            fromHost.readFully(encryptedReply);
            EnclaveMail reply = attestation.decryptMail(encryptedReply, myKey.getPrivate());
            algo = new String(reply.getBodyAsBytes());
            System.out.println("Enclave gave us the answer '" + algo + "'");
        }

        {
            String file = "/diabetes_2.txt";
            String dataSet = Utils.loadTextFromResource(file);

            // leading "T:" means: training
            MutableMail mail = attestation.createMail(("T:" + dataSet).getBytes());
            mail.setPrivateKey(myKey.getPrivate());
            // Set a random topic, so we can re-run this program against the same server.
            mail.setTopic(UUID.randomUUID().toString());
            byte[] encryptedMail = mail.encrypt();

            System.out.println("Sending the encrypted mail to the host.");

            toHost.writeInt(encryptedMail.length);
            toHost.write(encryptedMail);

            // Enclave will mail us back.
            byte[] encryptedReply = new byte[fromHost.readInt()];
            System.out.println("Reading reply mail of length " + encryptedReply.length + " bytes.");
            fromHost.readFully(encryptedReply);
            EnclaveMail reply = attestation.decryptMail(encryptedReply, myKey.getPrivate());
            algo = new String(reply.getBodyAsBytes());
            System.out.println("Enclave gave us the answer '" + algo + "'");
        }

        {
            // second round
            //10,139,80,0,0,27.1,1.441,57 -> 0
            //1,189,60,23,846,30.1,0.398,59 -> 1
            //1,89,66,23,94,28.1,0.167,21->0
            //0,137,40,35,168,43.1,2.288,33->1
            //5,116,74,0,0,25.6,0.201,30->0
            //3,78,50,32,88,31,0.248,26->1
            //10,115,0,0,0,35.3,0.134,29->0
            //2,197,70,45,543,30.5,0.158,53->1

            // leading "P:" means: predict

            String[] testDataList = new String[]{
                    "10,139,80,0,0,27.1,1.441,57",
                    "1,189,60,23,846,30.1,0.398,59",
                    "1,89,66,23,94,28.1,0.167,21",
                    "0,137,40,35,168,43.1,2.288,33",
                    "5,116,74,0,0,25.6,0.201,30",
                    "3,78,50,32,88,31,0.248,26",
                    "10,115,0,0,0,35.3,0.134,29",
                    "2,197,70,45,543,30.5,0.158,53"
            };
            toHost.writeInt(testDataList.length);

            for (String testData : testDataList) {
                // leading "T:" means: training
                MutableMail mail = attestation.createMail(("P:" + algo + testData + Utils.OBJECT_SEPARATER).getBytes());
                mail.setPrivateKey(myKey.getPrivate());
                // Set a random topic, so we can re-run this program against the same server.
                mail.setTopic(UUID.randomUUID().toString());
                byte[] encryptedMail = mail.encrypt();

                System.out.println("Sending the encrypted mail to the host.");

                toHost.writeInt(encryptedMail.length);
                toHost.write(encryptedMail);

                // Enclave will mail us back.
                byte[] encryptedReply = new byte[fromHost.readInt()];
                System.out.println("Reading reply mail of length " + encryptedReply.length + " bytes.");
                fromHost.readFully(encryptedReply);
                EnclaveMail reply = attestation.decryptMail(encryptedReply, myKey.getPrivate());
                System.out.println("Enclave gave us the answer '" + new String(reply.getBodyAsBytes()) + "'");
            }
        }

        toHost.close();
        fromHost.close();
    }
}
