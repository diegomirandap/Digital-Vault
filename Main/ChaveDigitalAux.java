package Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class ChaveDigitalAux {
    public static byte[] gerarBytesAleatorios(int tamanho) {
        SecureRandom random = new SecureRandom();
        byte[] dados = new byte[tamanho];
        random.nextBytes(dados);
        return dados;
    }

    public static PrivateKey carregarChavePrivada(String caminho, String fraseSecreta) throws Exception {
        // Lê os bytes da chave criptografada
        byte[] chaveCriptografada = Files.readAllBytes(Path.of(caminho));

        // Gera chave AES de 256 bits com SHA1PRNG
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(fraseSecreta.getBytes(StandardCharsets.UTF_8));
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, sr);
        SecretKey chaveAES = kg.generateKey();

        // Descriptografa com AES/ECB/PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] chaveDescriptografada = cipher.doFinal(chaveCriptografada);

        // Tenta processar PEM ou DER
        try {
            byte[] chaveDer;

            String conteudo = new String(chaveDescriptografada, StandardCharsets.UTF_8);
            if (conteudo.contains("-----BEGIN PRIVATE KEY-----")) {
                // Remove cabeçalhos e quebra de linha
                String pem = conteudo
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
                chaveDer = Base64.getDecoder().decode(pem);
            } else {
                // Assume que já está em formato DER puro
                chaveDer = chaveDescriptografada;
            }

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(chaveDer);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            Path path = Files.createTempFile("chave-decodificada", ".bin");
            Files.write(path, chaveDescriptografada);
            throw new IllegalArgumentException("Erro ao reconstruir a chave privada. Arquivo salvo em: " + path.toString(), e);
        }
    }

    public static X509Certificate carregarCertificado(String caminhoCertificado) throws Exception {
        try (InputStream in = new FileInputStream(caminhoCertificado)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(in);
        }
    }

    public static boolean validarChaveComCertificado(PrivateKey chavePrivada, X509Certificate certificado) throws Exception {
        byte[] dadosTeste = "teste".getBytes(StandardCharsets.UTF_8);
        byte[] assinatura = assinarComChavePrivada(dadosTeste, chavePrivada);
        return verificarAssinatura(dadosTeste, assinatura, certificado.getPublicKey());
    }

    public static byte[] assinarComChavePrivada(byte[] dados, PrivateKey chavePrivada) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(chavePrivada);
        sig.update(dados);
        return sig.sign();
    }

    public static boolean verificarAssinatura(byte[] dados, byte[] assinatura, PublicKey chavePublica) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(chavePublica);
        sig.update(dados);
        return sig.verify(assinatura);
    }

    public static String converterCertificadoParaPEM(X509Certificate cert) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN CERTIFICATE-----\n");
        sb.append(Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(cert.getEncoded()));
        sb.append("\n-----END CERTIFICATE-----");
        return sb.toString();
    }
}
