package views;

import javax.swing.*;
import java.awt.*;

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
            JOptionPane.showMessageDialog(this, "E-mail reconhecido (simulado). Ir para próxima tela...");
            // mainFrame.setContentPane(new TelaSenha(mainFrame));
        });

        botaoSair.addActionListener(e -> System.exit(0));
    }
}
