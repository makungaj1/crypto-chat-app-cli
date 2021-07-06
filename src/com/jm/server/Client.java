package com.jm.server;

import com.jm.utils.Proxy;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class Client extends Proxy {

    public Client(Socket socket, PrivateKey privateKey, byte[] random) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        super(socket, privateKey, null, random);
    }

}
