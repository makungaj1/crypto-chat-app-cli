package com.jm.server;

import com.jm.utils.Proxy;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client extends Proxy {

    public Client(Socket socket, PrivateKey privateKey, PublicKey otherPublicKey, byte[] random) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        super(socket, privateKey, otherPublicKey, random);
    }

}
