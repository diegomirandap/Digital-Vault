package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
public class TelaCadastro extends JPanel {
    JTextField campoCert, campoChave, campoFrase;
    JComboBox<String> comboGrupo;
    JPasswordField campoSenha, campoConfirmacao;
    boolean fecharAposCadastro;

    public TelaCadastro(JFrame mainFrame, boolean fecharAposCadastro) {
        this.fecharAposCadastro = fecharAposCadastro;
        setLayout(new GridLayout(8, 2, 5, 5));

        campoCert = new JTextField(255);
        campoChave = new JTextField(255);
        campoFrase = new JTextField(255);
        campoSenha = new JPasswordField(10);
        campoConfirmacao = new JPasswordField(10);
        comboGrupo = new JComboBox<>(new String[]{"Administrador", "Usuário"});

        add(new JLabel("Caminho do Certificado Digital:")); add(campoCert);
        add(new JLabel("Caminho da Chave Privada:")); add(campoChave);
        add(new JLabel("Frase Secreta:")); add(campoFrase);
        add(new JLabel("Grupo:")); add(comboGrupo);
        add(new JLabel("Senha Pessoal:")); add(campoSenha);
        add(new JLabel("Confirmar Senha:")); add(campoConfirmacao);

        JButton botaoCadastrar = new JButton("Cadastrar");
        JButton botaoVoltar = new JButton("Voltar");

        botaoCadastrar.addActionListener(e -> {
            String senha = new String(campoSenha.getPassword());
            String confSenha = new String(campoConfirmacao.getPassword());

            if (!senha.equals(confSenha)) {
                JOptionPane.showMessageDialog(this, "As senhas não coincidem.");
                return;
            }
            if (senha.length() < 8 || senha.length() > 10 || temDigitosRepetidos(senha)) {
                JOptionPane.showMessageDialog(this, "Senha inválida: deve ter 8 a 10 dígitos sem dígitos consecutivos iguais.");
                return;
            }

            // Lógica de cadastro no banco seria chamada aqui...

            if (fecharAposCadastro) System.exit(0);
        });

        botaoVoltar.addActionListener(e -> System.exit(0));

        add(botaoCadastrar);
        add(botaoVoltar);
    }

    private boolean temDigitosRepetidos(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) return true;
        }
        return false;
    }
}
