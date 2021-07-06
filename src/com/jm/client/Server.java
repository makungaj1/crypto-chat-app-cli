package com.jm.client;

import com.jm.utils.Proxy;
import com.jm.utils.SerializedObject;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Server extends Proxy {

    public Server(Socket socket, PrivateKey privateKey, PublicKey otherPublicKey, byte[] random) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        super(socket, privateKey, otherPublicKey, random);
    }

    public void updatePublicKeyAndRandom() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeyException {
        SerializedObject serializedObject = this.inputObject();
        this.setOtherPublicKey(serializedObject.getPublicKey());
        this.setRandom(serializedObject.getIvRandom());
    }
}
