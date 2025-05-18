package views;

import javax.swing.*;
import java.awt.*;
import DB.*;
import Main.*;

public class TelaFraseAdm extends JPanel {
    public TelaFraseAdm(JFrame mainFrame) {
        setLayout(new GridLayout(3, 1, 10, 10));

        JTextField campoEmail = new JTextField(255);
        JButton botaoEntrar = new JButton("Entrar");
        JButton botaoSair = new JButton("Sair");

        add(new JLabel("Digite a frase secreta do administrador:"));
        add(campoEmail);
        JPanel botoes = new JPanel();
        botoes.add(botaoEntrar);
        botoes.add(botaoSair);
        add(botoes);

        botaoEntrar.addActionListener(e -> {
            String frase = campoEmail.getText().trim();
            Main.fraseAdm = frase;
            if (frase.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Frase não pode estar vazia.");
                return;
            }

            mainFrame.setContentPane(new TelaLogin1(mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        botaoSair.addActionListener(e -> System.exit(0));
    }
}
