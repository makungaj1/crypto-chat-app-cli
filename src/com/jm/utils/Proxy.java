package com.jm.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;

public abstract class Proxy {
    private String ip;
    private int port;
    private PublicKey otherPublicKey;
    private final PrivateKey privateKey;
    private SecretKeySpec secretKey;
    private IvParameterSpec ivParameterSpec;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private final Cipher cipher;
    private byte[] random;

    public Proxy(Socket socket, PrivateKey privateKey, PublicKey otherPublicKey, byte[] random) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        if (socket != null) {
            this.ip = socket.getInetAddress().getHostAddress();
            this.port = socket.getPort();
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        } else {
            this.ip = null;
            this.port = 0;
            this.objectOutputStream = null;
            this.objectInputStream = null;
        }

        this.otherPublicKey = otherPublicKey;
        this.privateKey = privateKey;
        this.random = random;
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        if (this.otherPublicKey != null ) this.computeSecret();
        if (this.random != null) this.computeIvParameterSpec();
    }

    public void setRandom(byte[] random) {
        this.random = random;
        this.computeIvParameterSpec();
    }

    public byte[] encrypt(byte[] o) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, this.ivParameterSpec);
        return this.cipher.doFinal(o);
    }

    public byte[] decrypt(byte[] o) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey, this.ivParameterSpec);
        return this.cipher.doFinal(o);
    }

    public SecretKeySpec getSecretKey() {
        return this.secretKey;
    }

    public void outPutObject(SerializedObject serializedObject) throws IOException {
        objectOutputStream.writeObject(serializedObject);
        objectOutputStream.flush();
    }

    public SerializedObject inputObject() throws IOException, ClassNotFoundException {
        return (SerializedObject) objectInputStream.readObject();
    }

    public PublicKey getOtherPublicKey() {
        return otherPublicKey;
    }

    public void setOtherPublicKey(PublicKey otherPublicKey) throws NoSuchAlgorithmException, InvalidKeyException {
        this.otherPublicKey = otherPublicKey;
        this.computeSecret();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private byte[] shorten(byte[] o) {
        byte[] a = new byte[16];
        System.arraycopy(o, 0, a, 0, a.length);
        return a;
    }

    private void computeSecret() throws NoSuchAlgorithmException, InvalidKeyException {

        if (this.otherPublicKey == null) throw new InvalidKeyException("Other public key can't be null");

        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(this.otherPublicKey, true);
        byte[] keyByte = this.shorten(keyAgreement.generateSecret());
        this.secretKey = new SecretKeySpec(keyByte, "AES");
    }

    private void computeIvParameterSpec() {
        this.ivParameterSpec = new IvParameterSpec(this.random);
    }
}
