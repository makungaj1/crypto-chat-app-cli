package com.jm.server;

import com.jm.utils.Constant;
import com.jm.utils.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;
import java.util.logging.Logger;

public class Server {

    protected static final Logger log = Logger.getAnonymousLogger();
    protected static final TreeMap<String, Client> connectedClient = new TreeMap<>();
    protected static KeyPair keyPair = null;

    public static void main(String[] args) {
        try {

            keyPair = Util.generateKeyPair(Constant.KEY_ALGO, Constant.KEY_SIZE);

            // Create server
            // Listens on Port: 8000
            ServerSocket serverSocket = new ServerSocket(Constant.SERVER_PORT);

            // Log on the console
            log.info("Server started, listening on: " + Constant.SERVER_IP + ":" + serverSocket.getLocalPort()
                    + "\nServer Private Key: " + Util.byteToHex(keyPair.getPrivate().getEncoded())
                    + "\nServer Public Key: " + Util.byteToHex(keyPair.getPublic().getEncoded())
                    + "\nWaiting for clients requests");

            // Listen to client
            Socket clientSocket;

            while (true) {
                clientSocket = serverSocket.accept();

                // Log
                log.info("Client connected");

                // Handle client in separate thread
                new Thread(new HandleClient(clientSocket)).start();
            }

        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | ClassNotFoundException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }
}
