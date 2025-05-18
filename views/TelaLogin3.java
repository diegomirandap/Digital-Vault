/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package views;

import DB.DB;
import Main.*;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;

public class TelaLogin3 extends JPanel {
    public TelaLogin3(JFrame mainFrame, int uid, String senhaAes) {
        DB.inserirLog(4001,uid,null);
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
                SecretKeySpec chaveAES = TOTPAux.gerarChaveAES(senhaAes);// alterar para a senha fornecida anteriormente
                String segredoBase32 = TOTPAux.descriptografarAES(segredoTOTP, chaveAES);

                // Valida o TOTP
                TOTP totp = new TOTP(segredoBase32, 30);
                boolean valido = totp.validateCode(codigo);

                if (valido) {
                    Main.tentativasFalhas.put(uid, 0);
                    DB.incrementarAcessos(uid);
                    JOptionPane.showMessageDialog(this, "Código validado com sucesso!");
                    DB.inserirLog(4003,uid,null);
                    DB.inserirLog(4002,uid,null);
                    mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame, uid));
                    DB.inserirLog(1003,uid,null);
                    mainFrame.revalidate();
                } else {
                    int tentativas = Main.tentativasFalhas.getOrDefault(uid, 0) + 1;
                    Main.tentativasFalhas.put(uid, tentativas);

                    if (tentativas >= 3) {
                        long tempoBloqueio = System.currentTimeMillis() + (2 * 60 * 1000); // 2 minutos
                        Main.bloqueios.put(uid, tempoBloqueio);
                        JOptionPane.showMessageDialog(this, "Usuário bloqueado por 2 minutos após 3 tentativas inválidas.");
                        DB.inserirLog(4006,uid,null);
                        DB.inserirLog(4007,uid,null);
                        DB.inserirLog(4002,uid,null);
                        mainFrame.setContentPane(new TelaLogin1(mainFrame));
                    } else {
                        JOptionPane.showMessageDialog(this, "Código TOTP incorreto. Tentativa " + tentativas + "/3.");
                        int mid = 4003 + tentativas;
                        DB.inserirLog(mid,uid,null);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao validar código TOTP: " + ex.getMessage());
            }
        });
    }
}
