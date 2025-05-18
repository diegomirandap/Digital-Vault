/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package Main;

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

        int p1 = (hash[offset] & 0x7F) << 24;      // hash[10] = 0xBD & 0x7F = 0x3D
        int p2 = (hash[offset + 1] & 0xFF) << 16;  // hash[11] = 0xB0
        int p3 = (hash[offset + 2] & 0xFF) << 8;   // hash[12] = 0x51
        int p4 = (hash[offset + 3] & 0xFF);        // hash[13] = 0x04

        int binary = p1 | p2 | p3 | p4;

        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }

    private String TOTPCode(long timeInterval) {

        byte[] counter = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counter[i] = (byte) (timeInterval & 0xFF);
            timeInterval >>= 8;
        }

        for (byte b : counter) {
            System.out.printf("%02x", b);
        }

        byte[] hash = HMAC_SHA1(counter, key);

        String otp = getTOTPCodeFromHash(hash);
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
}
