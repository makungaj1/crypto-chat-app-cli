package com.jm.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class Util {
    public static KeyPair generateKeyPair(String algo, int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algo);
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    public static String byteToHex(byte[] o) {
        return Base64.getEncoder().encodeToString(o);
    }

    public static byte[] generateSecureRandom() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] random = new byte[16];
        secureRandom.nextBytes(random);
        return random;
    }

    public static Map<String, String> getOtherClientIp() throws SocketException {
        List<String> list = new ArrayList<>();

        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = e.nextElement();
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = ee.nextElement();
                list.add(i.toString().replaceAll("/", ""));
            }
        }

        Map<String, String> info = new HashMap<>();

        if (list.contains(Constant.CLIENT_A_IP)) {
            info.put("IP", Constant.CLIENT_B_IP);
            info.put("NAME", "Client B");
        }
        else {
            info.put("IP", Constant.CLIENT_A_IP);
            info.put("NAME", "Client A");
        }

        return info;
    }
}
