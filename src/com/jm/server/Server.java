package com.jm.server;

import com.jm.utils.Constant;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;
import java.util.logging.Logger;

public class Server {

    protected static final Logger log = Logger.getAnonymousLogger();
    protected static final TreeMap<String, Client> connectedClient = new TreeMap<>();

    public static void main(String[] args) {
        try {

            // Create server
            // Listens on Port: 8000
            ServerSocket serverSocket = new ServerSocket(Constant.SERVER_PORT);

            // Log on the console
            log.info("Server started, listening on: " + Constant.SERVER_IP + ":" + serverSocket.getLocalPort());

            // Listen to client
            Socket clientSocket;

            while (true) {
                clientSocket = serverSocket.accept();

                // Log
                log.info("Client connected");

                // Handle client in separate thread
                new Thread(new HandleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
