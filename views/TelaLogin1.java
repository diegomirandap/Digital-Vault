package views;

import javax.swing.*;
import java.awt.*;
import DB.*;
import Main.*;

public class TelaLogin1 extends JPanel {
    public TelaLogin1(JFrame mainFrame) {
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

            // Aqui você poderia validar se o e-mail existe no banco
            //JOptionPane.showMessageDialog(this, "E-mail reconhecido (simulado). Ir para próxima tela...");

            // ainda tem as telas 2 e 3


            //TEMPORARIOOOOOOOOOOOOO!!!!!!!!!!!

            int uid = DB.buscarUid(email);
            if (uid == -1) {
                JOptionPane.showMessageDialog(this, "E-mail não encontrado.");
                return;
            }

            // Verifica se o usuário está bloqueado
            Long tempoDesbloqueio = Main.bloqueios.get(uid);
            if (tempoDesbloqueio != null && System.currentTimeMillis() < tempoDesbloqueio) {
                long segundosRestantes = (tempoDesbloqueio - System.currentTimeMillis()) / 1000;
                JOptionPane.showMessageDialog(this, "Usuário bloqueado. Tente novamente em " + segundosRestantes + " segundos.");
                return;
            }
            DB.incrementarAcessos(uid);
            /*
            mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();*/
            String hash = DB.buscarSenhaHash(uid);
            mainFrame.setContentPane(new TelaLogin2(mainFrame, hash, uid));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        botaoSair.addActionListener(e -> System.exit(0));
    }
}
