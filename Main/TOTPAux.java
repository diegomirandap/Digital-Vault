package Main;

import Main.Base32;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import static Main.ChaveDigitalAux.gerarBytesAleatorios;

public class TOTPAux {

    // Gera os 20 bytes aleatórios e codifica em BASE32
    public static String gerarTOTPBase32() {
        byte[] segredo = gerarBytesAleatorios(20);

        Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
        return base32.toString(segredo);
    }

    // Deriva chave AES de 256 bits a partir da senha
    public static SecretKeySpec gerarChaveAES(String senha) throws Exception {
        SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
        prng.setSeed(senha.getBytes("UTF-8"));
        byte[] chave = new byte[32];
        prng.nextBytes(chave);
        return new SecretKeySpec(chave, "AES");
    }

    // Criptografa um texto com AES/ECB/PKCS5Padding
    public static byte[] criptografarAES(String texto, SecretKeySpec chaveAES) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, chaveAES);
        return cipher.doFinal(texto.getBytes("UTF-8"));
    }

    // Descriptografa segredo
    public static String descriptografarAES(byte[] cifrado, SecretKeySpec chaveAES) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] texto = cipher.doFinal(cifrado);
        return new String(texto, "UTF-8");
    }
}
