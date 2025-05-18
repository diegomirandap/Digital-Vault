package Main;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Certificado {
    public static String[] extrairNomeEEmail(X509Certificate cert) throws Exception {
        String subject = cert.getSubjectX500Principal().getName(X500Principal.RFC1779);
        System.out.println("Subject completo: " + subject);

        String nome = extrairCampo(subject, "CN");
        String email = extrairValorPorOID(subject, "1.2.840.113549.1.9.1"); // OID do email

        return new String[]{nome, email};
    }

    private static String extrairCampo(String subject, String campo) {
        Pattern p = Pattern.compile(campo + "=([^,]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(subject);
        if (m.find()) return m.group(1).trim();
        return "";
    }

    public static String extrairValorPorOID(String subject, String oid) {
        Pattern pattern = Pattern.compile("(OID\\.)?" + Pattern.quote(oid) + "=([^,]+)");
        Matcher matcher = pattern.matcher(subject);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        return "";
    }
}
