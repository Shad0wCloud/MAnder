package Server;

import javax.crypto.Cipher;
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
}