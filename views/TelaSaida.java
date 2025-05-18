package views;

import DB.DB;

import javax.swing.*;
import java.awt.*;

public class TelaSaida extends JPanel {

    public TelaSaida(JFrame mainFrame, Integer uid) {
        setLayout(new GridLayout(8, 1, 5, 5));

        // Informações do usuário
        add(new JLabel("Login: " + DB.buscarEmail(uid)));
        add(new JLabel("Grupo: " + DB.buscarGrupo(uid)));
        add(new JLabel("Nome: " + DB.buscarNome(uid)));
        add(new JLabel("Total de acessos do usuário: " + DB.contarAcessos(uid)));

        // Título
        JLabel titulo = new JLabel("Saída do sistema:", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(titulo);

        // Mensagem
        JLabel mensagem = new JLabel("Mensagem de saída.", SwingConstants.CENTER);
        add(mensagem);

        // Botões
        JPanel botoes = new JPanel(new FlowLayout());
        JButton btnEncerrarSessao = new JButton("Encerrar Sessão");
        JButton btnEncerrarSistema = new JButton("Encerrar Sistema");
        JButton btnVoltar = new JButton("Voltar ao Menu Principal");

        btnEncerrarSessao.addActionListener(e -> {
            mainFrame.setContentPane(new TelaLogin1(mainFrame)); // Assumindo que TelaLogin1 é a tela inicial
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        btnEncerrarSistema.addActionListener(e -> {
            System.exit(0);
        });

        btnVoltar.addActionListener(e -> {
            mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame, uid));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        botoes.add(btnEncerrarSessao);
        botoes.add(btnEncerrarSistema);
        botoes.add(btnVoltar);

        add(botoes);
    }
}
