package com.jm.server;

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

    public HandleClient(Socket socket) throws NoSuchPaddingException, IOException, NoSuchAlgorithmException, InvalidKeyException, ClassNotFoundException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] random = Util.generateSecureRandom();
        client = new Client(socket, Server.keyPair.getPrivate(), random);

        // Get Client Public Key
        client.updatePublicKey();
        Server.log.info("Received Client public key: " + Util.byteToHex(client.getOtherPublicKey().getEncoded()));

        // Send to client the server's public key
        serializedObject = new SerializedObject();
        serializedObject.setPublicKey(Server.keyPair.getPublic());
        serializedObject.setFromIP(random);
        client.outPutObject(serializedObject);
        Server.log.info("Sent to client: server's public key and random iv.");

        // Add client to connected
        Server.connectedClient.put(client.getIp(), client);
        Server.log.info("client " + client.getIp() + " added to connected list");
    }

    @Override
    public void run() {

    }
}
