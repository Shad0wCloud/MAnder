package sample;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

class Aes256Class {
    SecretKey secretKey;
    public Aes256Class() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            this.secretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    public byte[] makeAes(byte[] rawMessage, int cipherMode){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, this.secretKey);
            byte [] output = cipher.doFinal(rawMessage);
            if(cipherMode == Cipher.DECRYPT_MODE){
                byte[]result = new byte[output.length-8];
                //Выбрасываем последние 8 байт
                System.arraycopy(output, 0, result, 0, output.length-8);
                return result;
            }
            return output;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}