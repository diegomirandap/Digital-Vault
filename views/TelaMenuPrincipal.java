package views;

import javax.swing.*;
import java.awt.*;

public class TelaMenuPrincipal extends JPanel {

    public TelaMenuPrincipal(JFrame mainFrame) {
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel titulo = new JLabel("Menu Principal", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(titulo);

        JButton btnCadastrar = new JButton("1 - Cadastrar novo usuário");
        JButton btnConsultar = new JButton("2 - Consultar arquivos secretos");
        JButton btnSair = new JButton("3 - Sair do sistema");

        btnCadastrar.addActionListener(e -> {
            // Substituir pela tela de cadastro (sem fechar após)
            mainFrame.setContentPane(new TelaCadastro(mainFrame, false));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        btnConsultar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Abrindo pasta de arquivos secretos (simulado).");
            // Substituir por: mainFrame.setContentPane(new TelaArquivos(mainFrame));
        });

        btnSair.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Abrindo menu de saída do sistema (simulado).");
            // Substituir por: mainFrame.setContentPane(new TelaSaida(mainFrame));
            System.exit(0);
        });

        add(btnCadastrar);
        add(btnConsultar);
        add(btnSair);
    }
}

