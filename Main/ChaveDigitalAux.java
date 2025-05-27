/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package Main;

import DB.DB;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.*;

public class ChaveDigitalAux {
    public static byte[] gerarBytesAleatorios(int tamanho) {
        SecureRandom random = new SecureRandom();
        byte[] dados = new byte[tamanho];
        random.nextBytes(dados);
        return dados;
    }

    public static PrivateKey reconstruirChavePrivada(byte[] chaveCriptografada, String fraseSecreta) throws Exception {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(fraseSecreta.getBytes(StandardCharsets.UTF_8));
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, sr);
        SecretKey chaveAES = kg.generateKey();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] chaveDescriptografada = cipher.doFinal(chaveCriptografada);

        try {
            byte[] chaveDer;
            String conteudo = new String(chaveDescriptografada, StandardCharsets.UTF_8);

            if (conteudo.contains("-----BEGIN PRIVATE KEY-----")) {
                String pem = conteudo
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
                chaveDer = Base64.getDecoder().decode(pem);
            } else {
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

    public static PrivateKey carregarChavePrivadaDoBanco(int uid, String frase) throws Exception {
        byte[] chaveCriptografada = DB.buscarChavePrivada(uid);
        if (chaveCriptografada == null) throw new IllegalArgumentException("Chave privada não encontrada.");
        return reconstruirChavePrivada(chaveCriptografada, frase);
    }

    public static X509Certificate carregarCertificadoDoBanco(int uid) throws Exception {
        String pem = DB.buscarCertificadoPEM(uid);
        if (pem == null) throw new IllegalArgumentException("Certificado não encontrado.");

        String base64 = pem
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(base64);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
    }

    // Lê o índice de arquivos do diretório, valida a integridade/autenticidade, e retorna as linhas permitidas
    public static List<String[]> processarIndice(File indexEnc, File indexEnv, File indexAsd, int uid) throws Exception {
        // Carrega e valida chave e certificado do admin
        int uidAdm = DB.buscarUidAdm();
        PrivateKey privAdm = carregarChavePrivadaDoBanco(uidAdm, Main.fraseAdm);
        X509Certificate certAdm = carregarCertificadoDoBanco(uidAdm);

        // Decriptar semente com chave privada do admin
        byte[] semente = ChaveDigitalAux.desenvelopar(indexEnv, privAdm);

        // Gerar chave AES com SHA1PRNG e a semente
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(semente);
        kg.init(256, sr);
        SecretKey chaveAES = kg.generateKey();

        // Decriptar index.enc
        byte[] cifrado = Files.readAllBytes(indexEnc.toPath());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] plano = cipher.doFinal(cifrado);
        String conteudo = new String(plano, StandardCharsets.UTF_8);

        // Verificar assinatura digital do conteúdo
        byte[] assinatura = Files.readAllBytes(indexAsd.toPath());
        boolean verificado = verificarAssinatura(plano, assinatura, certAdm.getPublicKey());
        //if (!verificado) throw new SecurityException("Assinatura do índice inválida.");
        /// ///

        // Filtrar linhas que pertencem ao usuário (por dono ou grupo)
        String nomeUsuario = DB.buscarEmail(uid);
        String grupoUsuario = DB.buscarGrupo(uid).toString();
        List<String[]> resultado = new java.util.ArrayList<>();

        for (String linha : conteudo.split("\n")) {
            String[] partes = linha.trim().split(" ");
            if (partes.length == 4) {
                String dono = partes[2], grupo = partes[3];
                if (dono.equalsIgnoreCase(nomeUsuario) || grupo.equals(grupoUsuario)) {
                    resultado.add(partes);
                }
            }
        }

        return resultado;
    }

    // Decripta o arquivo secreto selecionado usando os arquivos .env e .asd
    public static void decriptarArquivoSecreto(File arqEnc, File arqEnv, File arqAsd, String nomeSaida, PrivateKey chavePrivada, PublicKey chavePublica) throws Exception {
        // Valida assinatura
        byte[] assinatura = Files.readAllBytes(arqAsd.toPath());
        byte[] conteudoCriptografado = Files.readAllBytes(arqEnc.toPath());

        // Recupera semente do envelope
        byte[] semente = ChaveDigitalAux.desenvelopar(arqEnv, chavePrivada);

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(semente);
        kg.init(256, sr);
        SecretKey chaveAES = kg.generateKey();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] plano = cipher.doFinal(conteudoCriptografado);

        // apos o decriptar apenas
        boolean ok = verificarAssinatura(plano, assinatura, chavePublica);
        System.out.println(Arrays.toString(assinatura));
        if (!ok) throw new SecurityException("Assinatura do arquivo inválida.");
        // Salva conteúdo em novo arquivo
        Path destino = Path.of(arqEnc.getParent(), nomeSaida);
        Files.write(destino, plano);
    }

    public static byte[] desenvelopar(File arquivoEnv, PrivateKey chavePrivada) throws Exception {
        byte[] envelope = Files.readAllBytes(arquivoEnv.toPath());

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, chavePrivada);
        return cipher.doFinal(envelope); // retorna a semente (usada para gerar a chave AES do índice)
    }

}
