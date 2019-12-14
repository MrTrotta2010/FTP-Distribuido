package client;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private static final byte[] key = "0123456789abcdef".getBytes();
    private byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private final IvParameterSpec ivspec = new IvParameterSpec(iv);
    
    public AES(){}
    
    public String Encrypt(String msg) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), ivspec);
        byte[] encrypted = cipher.doFinal(msg.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public String Decrypt(String msg) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivspec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(msg));
        return new String(decrypted);
    }
}