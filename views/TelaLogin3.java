package views;

import DB.DB;
import Main.*;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;

public class TelaLogin3 extends JPanel {
    public TelaLogin3(JFrame mainFrame, int uid, String fraseSecreta) {
        setLayout(new GridLayout(3, 1, 10, 10));

        JLabel label = new JLabel("Digite o código TOTP gerado no Authenticator:");
        JTextField campoCodigo = new JTextField(6);
        JButton botaoVerificar = new JButton("Verificar");

        add(label);
        add(campoCodigo);
        add(botaoVerificar);

        botaoVerificar.addActionListener(e -> {
            String codigo = campoCodigo.getText().trim();

            if (!codigo.matches("\\d{6}")) {
                JOptionPane.showMessageDialog(this, "O código deve conter exatamente 6 dígitos numéricos.");
                return;
            }

            try {
                // Recupera e descriptografa o segredo
                byte[] segredoTOTP = DB.buscarSecretTotp(uid);
                System.out.println(segredoTOTP);
                SecretKeySpec chaveAES = TOTPAux.gerarChaveAES(fraseSecreta);
                String segredoBase32 = TOTPAux.descriptografarAES(segredoTOTP, chaveAES);

                // Valida o TOTP
                TOTP totp = new TOTP(segredoBase32, 30);
                boolean valido = totp.validateCode(codigo);

                if (valido) {
                    JOptionPane.showMessageDialog(this, "Código validado com sucesso!");
                    mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame));
                    mainFrame.revalidate();
                } else {
                    JOptionPane.showMessageDialog(this, "Código TOTP inválido. Tente novamente.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao validar código TOTP: " + ex.getMessage());
            }
        });
    }
}
