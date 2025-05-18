import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;

public class DiagnosticoChave {
    public static void main(String[] args) throws Exception {
        String caminhoChaveCriptografada = "C:/Users/diegu/OneDrive/FACUL/INF1416-Segurança/Trabalho3/Pacote-T4/Keys/user01-pkcs8-aes.key";
        String fraseSecreta = "user01";

        // Lê o conteúdo criptografado
        byte[] dadosCriptografados = Files.readAllBytes(new File(caminhoChaveCriptografada).toPath());

        // Deriva a chave AES-256 com SHA1PRNG
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(fraseSecreta.getBytes(StandardCharsets.UTF_8));
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, sr);
        SecretKey chaveAES = kg.generateKey();

        // Descriptografa os dados
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] dadosDescriptografados = cipher.doFinal(dadosCriptografados);

        // Exibe os primeiros bytes da chave descriptografada
        String hexDump = HexFormat.of().formatHex(dadosDescriptografados, 0, Math.min(64, dadosDescriptografados.length));
        System.out.println("Hex (primeiros 64 bytes da chave descriptografada):");
        System.out.println(hexDump);

        // Salva para inspeção manual, se desejar
        Files.write(new File("chave-descriptografada.bin").toPath(), dadosDescriptografados);
        System.out.println("Arquivo salvo como chave-descriptografada.bin");
    }
}
