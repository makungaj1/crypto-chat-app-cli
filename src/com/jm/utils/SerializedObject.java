package com.jm.utils;

import java.io.Serializable;

public class SerializedObject implements Serializable {
    private byte[] fromIP;
    private byte[] toIP;
    private byte[] originIP;
    private byte[] subject;
    private byte[] message;
    private byte[] ivRandom;

    public SerializedObject() {}

    public byte[] getFromIP() {
        return fromIP;
    }

    public void setFromIP(byte[] fromIP) {
        this.fromIP = fromIP;
    }

    public byte[] getToIP() {
        return toIP;
    }

    public void setToIP(byte[] toIP) {
        this.toIP = toIP;
    }

    public byte[] getOriginIP() {
        return originIP;
    }

    public void setOriginIP(byte[] originIP) {
        this.originIP = originIP;
    }

    public byte[] getSubject() {
        return subject;
    }

    public void setSubject(byte[] subject) {
        this.subject = subject;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public byte[] getIvRandom() {
        return ivRandom;
    }

    public void setIvRandom(byte[] ivRandom) {
        this.ivRandom = ivRandom;
    }

}
