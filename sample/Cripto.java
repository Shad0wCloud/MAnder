package sample;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class Cripto {
    public static byte[] Decript(byte[] bytes, Aes256Class crypto)  {
            byte[] src = crypto.makeAes(bytes, Cipher.DECRYPT_MODE);
            return src;
    }
    public static byte[] Crypt(String msg, Aes256Class crypto){
        byte[] salt = new byte[8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        //Преобразуем исходный текст в поток байт и добавим полученную соль
        byte[] srcMessage = msg.getBytes();
        byte[] fullsrcMessage = new byte[srcMessage.length + 8];
        System.arraycopy(srcMessage, 0, fullsrcMessage, 0, srcMessage.length);
        System.arraycopy(salt, 0, fullsrcMessage, srcMessage.length, salt.length);
        //Шифруем
        byte[] shifr = crypto.makeAes(fullsrcMessage, Cipher.ENCRYPT_MODE);
        return shifr;
    }
    static ByteBuffer Get_Decripted(ByteBuffer MainBuffer){
        MainBuffer.flip();
        ByteArrayOutputStream TempBaos = new ByteArrayOutputStream();
        while (MainBuffer.hasRemaining()){
            TempBaos.write(MainBuffer.get());
        }
        byte[] randomrandom = TempBaos.toByteArray();
        byte[] temp = Decript(randomrandom,Main.cripto);
        MainBuffer.clear();
        MainBuffer.put(new byte[4096]);
        MainBuffer.clear();
        MainBuffer.put(temp);
        MainBuffer.flip();
        MainBuffer.clear();
        return MainBuffer;
    }
    static byte[] Get_Cript(String msg){
        return Cripto.Crypt(msg,Main.cripto);
        //return msg.getBytes();
    }
}