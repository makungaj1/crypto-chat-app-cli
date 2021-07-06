package com.jm.client;

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
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getAnonymousLogger();
    private static SerializedObject serializedObject = new SerializedObject();

    public static void main(String[] args) {
        try {

            // Get other Non-Server IP
            Map<String, String> MY_FRIEND = Util.getOtherClientIp();

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

                // If initiateChat && subject is Active
                //      create other user object
                // Else if subject is Active and !initiateChat
                //      create other user object
                //      respond with "I am available to chat!" message
                // if subject is insta-chat
                //      Read message
                //      Type reply answer and send
                // Else: set isActive to false

            }

            // Close connection
            socket.close();
            log.info("Connection closed.");

        } catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidKeyException | ClassNotFoundException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }
}
