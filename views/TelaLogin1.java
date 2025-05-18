/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package views;

import javax.swing.*;
import java.awt.*;
import DB.*;
import Main.*;

public class TelaLogin1 extends JPanel {
    public TelaLogin1(JFrame mainFrame) {
        DB.inserirLog(2001,null,null);
        setLayout(new GridLayout(3, 1, 10, 10));

        JTextField campoEmail = new JTextField(255);
        JButton botaoEntrar = new JButton("Entrar");
        JButton botaoSair = new JButton("Sair");

        add(new JLabel("Digite seu e-mail:"));
        add(campoEmail);
        JPanel botoes = new JPanel();
        botoes.add(botaoEntrar);
        botoes.add(botaoSair);
        add(botoes);

        botaoEntrar.addActionListener(e -> {
            String email = campoEmail.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "E-mail não pode estar vazio.");
                return;
            }

            int uid = DB.buscarUid(email);
            if (uid == -1) {
                DB.inserirLog(2005,uid,null);
                JOptionPane.showMessageDialog(this, "E-mail não encontrado.");
                return;
            }

            // Verifica se o usuário está bloqueado
            Long tempoDesbloqueio = Main.bloqueios.get(uid);
            if (tempoDesbloqueio != null && System.currentTimeMillis() < tempoDesbloqueio) {
                long segundosRestantes = (tempoDesbloqueio - System.currentTimeMillis()) / 1000;
                DB.inserirLog(2004,uid,null);
                JOptionPane.showMessageDialog(this, "Usuário bloqueado. Tente novamente em " + segundosRestantes + " segundos.");
                return;
            }
            String hash = DB.buscarSenhaHash(uid);
            DB.inserirLog(2003,uid,null);
            DB.inserirLog(2002,null,null);
            mainFrame.setContentPane(new TelaLogin2(mainFrame, hash, uid));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        botaoSair.addActionListener(e -> System.exit(0));
    }
}
