
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
        System.out.println("Key32="+base32);
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
        System.out.printf("\nTOTPCodeHexOffset=%01x",offset);
        System.out.println("\nTOTPCodeByteOffset="+offset);
        System.out.println("TruncInitPos="+(offset*2));
        System.out.println("TruncFinalPos="+(offset*2+8));

        int p1 = (hash[offset] & 0x7F) << 24;      // hash[10] = 0xBD & 0x7F = 0x3D
        int p2 = (hash[offset + 1] & 0xFF) << 16;  // hash[11] = 0xB0
        int p3 = (hash[offset + 2] & 0xFF) << 8;   // hash[12] = 0x51
        int p4 = (hash[offset + 3] & 0xFF);        // hash[13] = 0x04

        int binary = p1 | p2 | p3 | p4;

        System.out.printf("TruncatedHash=%08x\n",binary);
        System.out.printf("BaseValueOfTOTPCode=%08d\n",binary);
        int otp = binary % 1_000_000;
        System.out.printf("FinalValueOfTOTPCode=%06d\n",otp);
        //System.out.println(String.format("%06d", otp));
        return String.format("%06d", otp);
    }

    private byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    private String TOTPCode(long timeInterval) {
        System.out.println("\n[+] Gerando código para timeInterval: " + timeInterval);

        byte[] counter = longToBytes(timeInterval);

        //System.out.print("[1] Contador (bytes): ");
        System.out.print("TimeInterval=");
        for (byte b : counter) {
            System.out.printf("%02x", b);
        }

        byte[] hash = HMAC_SHA1(counter, key);
        //System.out.print("\n[2] Hash HMAC-SHA1: ");
        System.out.print("\nHashString=");
        for (byte b : hash) {
            System.out.printf("%02x", b);
        }

        String otp = getTOTPCodeFromHash(hash);
        System.out.println("[3] Código gerado: " + otp);
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
        System.out.println("Key(hex)="+base32Secret);
        TOTP totp = new TOTP(base32Secret, 30);

        long now = System.currentTimeMillis() / 1000 / 30;
        long before = now - 30;
        long after = now + 30;

        System.out.println("\n========== Código para 30 segundos ANTES ==========");
        String pastCode = totp.generateCodeAt(before);

        System.out.println("\n========== Código para tempo atual ==========");
        String currentCode = totp.generateCodeAt(now);

        System.out.println("\n========== Código para 30 segundos DEPOIS ==========");
        String futureCode = totp.generateCodeAt(after);

        System.out.println("\nResumo:");
        System.out.println("Antes : " + pastCode);
        System.out.println("Atual : " + currentCode);
        System.out.println("Depois: " + futureCode);
    }
}