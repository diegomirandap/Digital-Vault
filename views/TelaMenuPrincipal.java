/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package views;

import DB.DB;

import javax.swing.*;
import java.awt.*;

public class TelaMenuPrincipal extends JPanel {

    public TelaMenuPrincipal(JFrame mainFrame, Integer uid) {
        DB.inserirLog(5001,uid,null);
        Integer grupo = DB.buscarGrupo(uid);

        setLayout(new GridLayout(8, 1, 5, 5));

        add(new JLabel("Login: " + DB.buscarEmail(uid)));
        add(new JLabel("Grupo: " + DB.buscarGrupo(uid)));
        add(new JLabel("Nome: " + DB.buscarNome(uid)));
        add(new JLabel("Total de acessos do usuário: " + DB.contarAcessos(uid)));

        JLabel titulo = new JLabel("Menu Principal", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(titulo);

        JButton btnCadastrar = new JButton("1 - Cadastrar novo usuário");
        if (grupo == 1){
            btnCadastrar.setEnabled(true);
        }
        else{
            btnCadastrar.setEnabled(false);
        }
        JButton btnConsultar = new JButton("2 - Consultar arquivos secretos");
        JButton btnSair = new JButton("3 - Sair do sistema");

        btnCadastrar.addActionListener(e -> {
            DB.inserirLog(5002,uid,null);
            mainFrame.setContentPane(new TelaCadastro(mainFrame, false, uid));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        btnConsultar.addActionListener(e -> {
            DB.inserirLog(5003,uid,null);
            mainFrame.setContentPane(new TelaConsulta(mainFrame, uid));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        btnSair.addActionListener(e -> {
            DB.inserirLog(5004,uid,null);
            mainFrame.setContentPane(new TelaSaida(mainFrame, uid));
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        add(btnCadastrar);
        add(btnConsultar);
        add(btnSair);
    }
}

