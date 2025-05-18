/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package views;

import DB.DB;

import javax.swing.*;
import java.awt.*;

public class TelaSaida extends JPanel {

    public TelaSaida(JFrame mainFrame, Integer uid) {
        DB.inserirLog(8001,uid,null);
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
        JLabel mensagem = new JLabel("“Pressione o botão Encerrar Sessão ou o botão\n" +
                "Encerrar Sistema para confirmar.", SwingConstants.CENTER);
        add(mensagem);

        // Botões
        JPanel botoes = new JPanel(new FlowLayout());
        JButton btnEncerrarSessao = new JButton("Encerrar Sessão");
        JButton btnEncerrarSistema = new JButton("Encerrar Sistema");
        JButton btnVoltar = new JButton("Voltar ao Menu Principal");

        btnEncerrarSessao.addActionListener(e -> {
            DB.inserirLog(8002,uid,null);
            DB.inserirLog(1004,uid,null);
            mainFrame.setContentPane(new TelaLogin1(mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        btnEncerrarSistema.addActionListener(e -> {
            DB.inserirLog(8003,uid,null);
            DB.inserirLog(1002,null,null);
            System.exit(0);
        });

        btnVoltar.addActionListener(e -> {
            DB.inserirLog(8005,uid,null);
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
