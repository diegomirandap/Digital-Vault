/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package views;

import javax.swing.*;
import java.awt.*;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import Main.*;
import DB.DB;

public class TelaFraseAdm extends JPanel {
    public TelaFraseAdm(JFrame mainFrame) {
        setLayout(new GridLayout(3, 1, 10, 10));

        JTextField campoFrase = new JTextField(255);
        JButton botaoEntrar = new JButton("Entrar");
        JButton botaoSair = new JButton("Sair");

        add(new JLabel("Digite a frase secreta do administrador:"));
        add(campoFrase);
        JPanel botoes = new JPanel();
        botoes.add(botaoEntrar);
        botoes.add(botaoSair);
        add(botoes);

        botaoEntrar.addActionListener(e -> {
            String frase = campoFrase.getText().trim();
            if (frase.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Frase não pode estar vazia.");
                return;
            }
            Main.fraseAdm = frase;

            try {
                Integer uid = DB.buscarUidAdm();
                PrivateKey chave = ChaveDigitalAux.carregarChavePrivadaDoBanco(uid, frase);
                X509Certificate cert = ChaveDigitalAux.carregarCertificadoDoBanco(uid);

                boolean ok = ChaveDigitalAux.validarChaveComCertificado(chave, cert);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Frase inválida. A chave não pôde ser validada.");
                    return;
                }

                JOptionPane.showMessageDialog(this, "Frase correta. Acesso autorizado.");
                // Aqui pode redirecionar, se necessário, para a tela principal do sistema:
                mainFrame.setContentPane(new TelaLogin1(mainFrame));
                mainFrame.revalidate();
                mainFrame.repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao validar: " + ex.getMessage());
            }
        });

        botaoSair.addActionListener(e -> System.exit(0));
    }
}
