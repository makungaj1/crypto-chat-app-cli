package com.jm.server;

import com.jm.utils.Constant;
import com.jm.utils.SerializedObject;
import com.jm.utils.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HandleClient implements Runnable {
    private final Client client;
    private SerializedObject serializedObject;
    private byte[] random;
    private final Socket socket;

    public HandleClient(Socket socket) throws NoSuchPaddingException, IOException, NoSuchAlgorithmException, InvalidKeyException, ClassNotFoundException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        random = Util.generateSecureRandom();
        this.socket = socket;

        client = new Client(this.socket, Server.keyPair.getPrivate(), random);

        Server.log.info("Client: " + client.getIp() + ":" + client.getPort());

        // Get Client Public Key
        client.updatePublicKey();
        Server.log.info("Received Client public key: " + Util.byteToHex(client.getOtherPublicKey().getEncoded()));
        Server.log.info("Shared secret: " + Util.byteToHex(client.getSecretKey().getEncoded()));

        // Send to client the server's public key
        serializedObject = new SerializedObject();
        serializedObject.setPublicKey(Server.keyPair.getPublic());
        serializedObject.setFromIP(client.encrypt(Constant.SERVER_IP.getBytes()));
        serializedObject.setToIP(client.encrypt(client.getIp().getBytes()));
        serializedObject.setOriginIP(client.encrypt(Constant.SERVER_IP.getBytes()));

        // TODO: Encrypt
        serializedObject.setIvRandom(random);

        client.outPutObject(serializedObject);
        Server.log.info("Sent to client: server's public key and random iv(" + Util.byteToHex(random) + ").");

        // Add client to connected
        Server.connectedClient.put(client.getIp(), client);
        Server.log.info("client " + client.getIp() + " added to connected list");
        Server.log.info(Server.connectedClient.toString());
    }

    @Override
    public void run() {

        // Client can request another user's public key to kick off chat (subject: initial)
        // The other user will respond back with an initial message (subject: insta-chat)
        // if the other user is inactive, the user will respond to the requester (subject: inactive)

        try {

            boolean log_out = false;

            while (!log_out) {

                this.serializedObject = client.inputObject();

                String subject = new String(client.decrypt(serializedObject.getSubject()));
                String fromIP = new String(client.decrypt(serializedObject.getFromIP()));
                String toIP = new String(client.decrypt(serializedObject.getToIP()));
                String msg = serializedObject.getMessage() == null ? "null" :Util.byteToHex(serializedObject.getMessage());

                // Log
                Server.log.info("received a(n) " + subject + " request from " + fromIP + " going to " + toIP
                        + "\nmsg: " + msg);

                if (subject.equalsIgnoreCase(Constant.INITIAL) || subject.equalsIgnoreCase(Constant.INSTA_CHAT)) {

                    if (Server.connectedClient.containsKey(toIP)) {
                        // Other user is active
                        Client targetClient = Server.connectedClient.get(toIP);

                        Server.log.info("Targeted user is active");

                        // If subject is insta-chat and the target user is active, transfer the message
                        if (subject.equalsIgnoreCase(Constant.INSTA_CHAT)) {

                            serializedObject.setFromIP(targetClient.encrypt(Constant.SERVER_IP.getBytes()));
                            serializedObject.setOriginIP(targetClient.encrypt(fromIP.getBytes()));
                            serializedObject.setToIP(targetClient.encrypt(toIP.getBytes()));
                            serializedObject.setSubject(targetClient.encrypt(subject.getBytes()));

                            // TODO: check that the message body is not null

                            targetClient.outPutObject(serializedObject);

                            Server.log.info("Message transferred to " + targetClient.getIp());
                        }

                        // If subject is initial and the target user is active:
                        //      Send:
                        //              subject: Active
                        //              to requester: receiver's public key
                        //              to receiver: requesters public key
                        //              to both: an unique secure random
                        else {

                            random = Util.generateSecureRandom();

                            // To the requester
                            serializedObject.setSubject(client.encrypt(Constant.ACTIVE.getBytes()));
                            serializedObject.setFromIP(client.encrypt(Constant.SERVER_IP.getBytes()));
                            serializedObject.setToIP(client.encrypt(fromIP.getBytes()));
                            serializedObject.setOriginIP(client.encrypt(Constant.SERVER_IP.getBytes()));
                            serializedObject.setPublicKey(targetClient.getOtherPublicKey());

                            // TODO: Encrypt random before sending it over the network
                            serializedObject.setIvRandom(random);

                            client.outPutObject(serializedObject);
                            Server.log.info("Sent the target user ( " + targetClient.getIp() + " ) Public key to " + client.getIp());


                            // To the receiver
                            serializedObject.setSubject(targetClient.encrypt(Constant.ACTIVE.getBytes()));
                            serializedObject.setFromIP(targetClient.encrypt(Constant.SERVER_IP.getBytes()));
                            serializedObject.setToIP(targetClient.encrypt(toIP.getBytes()));
                            serializedObject.setOriginIP(targetClient.encrypt(fromIP.getBytes()));
                            serializedObject.setPublicKey(client.getOtherPublicKey());

                            targetClient.outPutObject(serializedObject);
                            Server.log.info("Sent the current user's ( " + client.getIp() + " ) Public key to " + targetClient.getIp());
                        }
                        // If subject is log out: remove the client in the connected list, terminate both the socket and thread
                    } else {
                        // Other user is inactive
                        Server.log.info("Targeted user is inactive");

                        // Otherwise, send back to the requester an inactive subject
                        serializedObject.setSubject(client.encrypt(Constant.INACTIVE.getBytes()));
                        serializedObject.setFromIP(client.encrypt(Constant.SERVER_IP.getBytes()));
                        serializedObject.setToIP(client.encrypt(fromIP.getBytes()));
                        serializedObject.setOriginIP(client.encrypt(Constant.SERVER_IP.getBytes()));

                        client.outPutObject(serializedObject);
                        Server.log.info("Sent inactive subject to " + client.getIp());
                        break;
                    }

                }
                else if (subject.equalsIgnoreCase(Constant.LOGOUT)) {
                    log_out = true;
                }
                // Unsupported subject
            }
            terminate();

        } catch (IOException | ClassNotFoundException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println(e.getClass());
        } finally {
            terminate();
        }

    }

    private void terminate()  {
        // Close thread/Socket
        Server.log.info("terminating thread " + Thread.currentThread().getName()
                + "\nTerminating the current socket " + this.socket.getInetAddress().getHostAddress());
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.connectedClient.remove(client.getIp());
        Thread.currentThread().interrupt();
    }
}
