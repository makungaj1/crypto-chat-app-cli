package com.jm.server;

import java.net.Socket;

public class HandleClient implements Runnable {
    private Socket socket;

    public HandleClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
