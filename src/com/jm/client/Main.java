package com.jm.client;

import com.jm.utils.Constant;
import com.jm.utils.SerializedObject;
import com.jm.utils.Util;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getAnonymousLogger();
    private static SerializedObject serializedObject = new SerializedObject();

    public static void main(String[] args) {
        try {

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
                    + "\nShared secret: " + Util.byteToHex(server.getSecretKey().getEncoded()));

        } catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidKeyException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
