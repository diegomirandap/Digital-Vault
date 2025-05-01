/*import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

public class TOTP {
    private byte [] key = null;
    private long timeStepInSeconds = 30;
    // Construtor da classe. Recebe a chave secreta em BASE32 e o intervalo
    // de tempo a ser adotado (default = 30 segundos). Deve decodificar a
    // chave secreta e armazenar em key. Em caso de erro, gera Exception.
    public TOTP(String base32EncodedSecret, long timeStepInSeconds) throws Exception {
        this.timeStepInSeconds = timeStepInSeconds;
        Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
        this.key = base32.fromString(base32EncodedSecret);
        if (this.key == null) throw new Exception("Chave secreta inválida");

    }
    // Recebe o HASH HMAC-SHA1 e determina o código TOTP de 6 algarismos
    // decimais, prefixado com zeros quando necessário.
    private String getTOTPCodeFromHash(byte[] hash) {
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }
    // Recebe o contador e a chave secreta para produzir o hash HMAC-SHA1.
    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        try {
            SecretKeySpec signKey = new SecretKeySpec(keyByteArray, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            return mac.doFinal(counter);
        } catch (Exception e) {
            return null;
        }
    }
    // Recebe o intervalo de tempo e executa o algoritmo TOTP para produzir
    // o código TOTP. Usa os métodos auxiliares getTOTPCodeFromHash e HMAC_SHA1.
    private String TOTPCode(long timeInterval) {
        byte[] counter = longToBytes(timeInterval);
        byte[] hash = HMAC_SHA1(counter, key);
        return getTOTPCodeFromHash(hash);
    }

    // Método que é utilizado para solicitar a geração do código TOTP.
    public String generateCode() {
        long time = (new Date().getTime()) / 1000;
        long timeInterval = time / timeStepInSeconds;
        return TOTPCode(timeInterval);
    }
    // Método que é utilizado para validar um código TOTP (inputTOTP).
    // Deve considerar um atraso ou adiantamento de 30 segundos no
    // relógio da máquina que gerou o código TOTP.
    public boolean validateCode(String inputTOTP) {


        return true;
    }
}
*/

//import java.util.Date;
import java.nio.ByteBuffer;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
//import de.taimos.oss.crypto.Base32;

public class TOTP {
    private final byte[] key;
    private final long timeStepInSeconds;

    public TOTP(String base32EncodedSecret, long timeStepInSeconds) throws Exception {
        this.timeStepInSeconds = timeStepInSeconds;
        Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
        this.key = base32.fromString(base32EncodedSecret);
        if (this.key == null) {
            throw new Exception("Chave secreta inválida");
        }
    }

    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        try {
            SecretKeySpec signKey = new SecretKeySpec(keyByteArray, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            return mac.doFinal(counter);
        } catch (Exception e) {
            return null;
        }
    }

    private String getTOTPCodeFromHash(byte[] hash) {
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }

    private String TOTPCode(long timeInterval) {
        System.out.println("\n[+] Gerando código para timeInterval: " + timeInterval);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(timeInterval);
        byte[] counter = buffer.array();

        System.out.print("[1] Contador (bytes): ");
        for (byte b : counter) {
            System.out.printf("%02X ", b);
        }

        byte[] hash = HMAC_SHA1(counter, key);
        System.out.print("\n[2] Hash HMAC-SHA1: ");
        for (byte b : hash) {
            System.out.printf("%02X ", b);
        }

        String otp = getTOTPCodeFromHash(hash);
        System.out.println("\n[3] Código gerado: " + otp);
        return otp;
    }

    public String generateCodeAt(long timeInSeconds) {
        long timeInterval = timeInSeconds / timeStepInSeconds;
        return TOTPCode(timeInterval);
    }

    public boolean validateCode(String inputTOTP) {
        long time = System.currentTimeMillis() / 1000;
        long timeInterval = time / timeStepInSeconds;

        for (long i = -1; i <= 1; i++) {
            if (TOTPCode(timeInterval + i).equals(inputTOTP)) {
                return true;
            }
        }
        return false;
    }

    // --- Exemplo de execução detalhada ---
    public static void main(String[] args) throws Exception {
        String base32Secret = "JBSWY3DPEHPK3PXP"; // mesma chave que você configura no Authenticator
        TOTP totp = new TOTP(base32Secret, 30);

        long now = System.currentTimeMillis() / 1000;
        long before = now - 30;
        long after = now + 30;

        System.out.println("\n========== Código para tempo atual ==========");
        String currentCode = totp.generateCodeAt(now);

        System.out.println("\n========== Código para 30 segundos ANTES ==========");
        String pastCode = totp.generateCodeAt(before);

        System.out.println("\n========== Código para 30 segundos DEPOIS ==========");
        String futureCode = totp.generateCodeAt(after);

        System.out.println("\nResumo:");
        System.out.println("Atual : " + currentCode);
        System.out.println("Antes : " + pastCode);
        System.out.println("Depois: " + futureCode);
    }
}

