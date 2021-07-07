package com.jm.client;

import com.jm.server.Client;
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
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getAnonymousLogger();
    private static SerializedObject serializedObject = new SerializedObject();
    private static Client myFriend = null;
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        try {

            // Get other Non-Server IP
            Map<String, String> MY_FRIEND = Util.getOtherClientIp();
            String myIP = MY_FRIEND.get("IP").equalsIgnoreCase(Constant.CLIENT_A_IP) ? Constant.CLIENT_B_IP : Constant.CLIENT_A_IP;

            // Generate Key Pair
            KeyPair keyPair = Util.generateKeyPair(Constant.KEY_ALGO, Constant.KEY_SIZE);
            log.info("Private Key: " + Util.byteToHex(keyPair.getPrivate().getEncoded())
                    + "\nPublic Key: " + Util.byteToHex(keyPair.getPublic().getEncoded()));

            // Connect to the server
            Socket socket = new Socket(Constant.SERVER_IP, Constant.SERVER_PORT);
            Server server = new Server(socket, keyPair.getPrivate(), null, null);

            log.info("Connected to server: " + server.getIp() + ":" + server.getPort()
                    + "\nExchanging key");

            // Send public key to server
            serializedObject.setPublicKey(keyPair.getPublic());
            server.outPutObject(serializedObject);

            // Get server's public key and random iv
            server.updatePublicKeyAndRandom();

            log.info("Server's Public key: " + Util.byteToHex(server.getOtherPublicKey().getEncoded())
                    + "\nShared secret: " + Util.byteToHex(server.getSecretKey().getEncoded())
                    + "\nMy friend's IP: " + MY_FRIEND.get("IP") + "; My Friend's ID: " + MY_FRIEND.get("NAME"));

            boolean isActive = true;
            boolean initiateChat = MY_FRIEND.get("NAME").equalsIgnoreCase("Client B");

            while (isActive) {

                serializedObject = new SerializedObject();

                // Subject: Initiate
                if (initiateChat) {
                    // request my friend's public key and random iv from server
                    log.info("Sending the initial request");
                    serializedObject.setFromIP(server.encrypt(Constant.CLIENT_A_IP.getBytes()));
                    serializedObject.setOriginIP(server.encrypt(Constant.CLIENT_A_IP.getBytes()));
                    serializedObject.setToIP(server.encrypt(MY_FRIEND.get("IP").getBytes()));
                    serializedObject.setSubject(server.encrypt(Constant.INITIAL.getBytes()));

                    server.outPutObject(serializedObject);
                    log.info("Initial request sent");
                }

                serializedObject = server.inputObject();
                String fromIP = new String(server.decrypt(serializedObject.getFromIP()));
                String toIP = new String(server.decrypt(serializedObject.getToIP()));
                String originIP = new String(server.decrypt(serializedObject.getOriginIP()));
                String subject = new String(server.decrypt(serializedObject.getSubject()));

                byte[] fromIPB;
                byte[] toIPB;
                byte[] originIPB;
                byte[] subjectB;

                log.info("reading request from server\nFrom IP: " + fromIP
                        + "\nTo IP: " + toIP + "\nOrigin IP: " + originIP + "\nSubject: " + subject);

                if (myFriend == null && subject.equalsIgnoreCase(Constant.ACTIVE)) {
                    log.info("Creating my friend object");
                    myFriend = new Client(null, keyPair.getPrivate(), serializedObject.getIvRandom());
                    myFriend.setOtherPublicKey(serializedObject.getPublicKey());
                    myFriend.setIp(MY_FRIEND.get("IP"));
                    myFriend.setPort(1111);
                    log.info("My Friend Public Key: " + Util.byteToHex(myFriend.getOtherPublicKey().getEncoded())
                            + "\nSecret key with my friend: " + Util.byteToHex(myFriend.getSecretKey().getEncoded()));
                }

                // If initiateChat && subject is Active
                // Else if subject is Active and !initiateChat
                //      respond with "I am available to chat!" message with subject insta-chat
                if (subject.equalsIgnoreCase(Constant.ACTIVE)) {
                    if (!initiateChat) {
                        log.info("Responding to an initial request from " + originIP);

                        fromIPB = server.encrypt(myIP.getBytes());
                        toIPB = server.encrypt(myFriend.getIp().getBytes());
                        originIPB = server.encrypt(myIP.getBytes());
                        subjectB = server.encrypt(Constant.INSTA_CHAT.getBytes());

                        log.info("Encrypted data\nFrom: " + Util.byteToHex(fromIPB) + "\nTo: " + Util.byteToHex(toIPB)
                                + "\nOrigin: " + Util.byteToHex(originIPB) + "\nSubject: " + Util.byteToHex(subjectB));

                        serializedObject.setFromIP(fromIPB);
                        serializedObject.setOriginIP(originIPB);
                        serializedObject.setToIP(toIPB);
                        serializedObject.setSubject(subjectB);

                        // encrypt the message with the secret key shared only with the other end
                        // End-to-End Encryption
                        // The server does not have the key to decrypt this message
                        serializedObject.setMessage(myFriend.encrypt("I am available to chat!".getBytes()));
                        server.outPutObject(serializedObject);

                        log.info("Sent the first insta-chat request as a reply to initial request from " + originIP);
                    } else {
                        log.info("My Friend is Active, waiting for them to kick off the chat");
                        initiateChat = false;
                    }
                }
                else if (subject.equalsIgnoreCase(Constant.LOGOUT) || subject.equalsIgnoreCase(Constant.INACTIVE)) {
                    isActive = false;
                    serializedObject.setFromIP(server.encrypt(myIP.getBytes()));
                    serializedObject.setOriginIP(server.encrypt(myIP.getBytes()));
                    serializedObject.setToIP(server.encrypt(server.getIp().getBytes()));
                    serializedObject.setSubject(server.encrypt(Constant.LOGOUT.getBytes()));
                    log.info("Subject is either Log out or Inactive\nSent log out signal to server");
                }
                else if (subject.equalsIgnoreCase(Constant.INSTA_CHAT)) {
                    assert myFriend != null;
                    String msg = new String(myFriend.decrypt(serializedObject.getMessage()));

                    log.info("Reading Message\nEncrypted: " + Util.byteToHex(serializedObject.getMessage())
                            + "\nDecrypted: " + msg);

                    System.out.print("you: ");
                    msg = input.nextLine();

                    serializedObject.setMessage(myFriend.encrypt(msg.getBytes())); // End to End Encryption
                    serializedObject.setFromIP(server.encrypt(myIP.getBytes()));
                    serializedObject.setOriginIP(server.encrypt(myIP.getBytes()));
                    serializedObject.setToIP(server.encrypt(myFriend.getIp().getBytes()));
                    server.outPutObject(serializedObject);
                    log.info("reply sent");

                }
                else {
                    log.info("Unsupported Subject: " + subject);
                    break;
                }

            }

            // Close connection
            socket.close();
            log.info("Connection closed.");

        } catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidKeyException | ClassNotFoundException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }
}
