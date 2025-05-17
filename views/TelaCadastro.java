package views;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.SecureRandom;
import Main.*;
import DB.*;

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
        if (fecharAposCadastro){
            comboGrupo = new JComboBox<>(new String[]{"Administrador"});
        }
        else{
            comboGrupo = new JComboBox<>(new String[]{"Administrador", "Usuário"});
        }

        add(new JLabel("Caminho do Certificado Digital:")); add(campoCert);
        add(new JLabel("Caminho da Chave Privada:")); add(campoChave);
        add(new JLabel("Frase Secreta:")); add(campoFrase);
        add(new JLabel("Grupo:")); add(comboGrupo);
        add(new JLabel("Senha Pessoal:")); add(campoSenha);
        add(new JLabel("Confirmar Senha:")); add(campoConfirmacao);

        JButton botaoCadastrar = new JButton("Cadastrar");
        JButton botaoVoltar = new JButton("Voltar");

        botaoCadastrar.addActionListener(e -> {
            String frase = new String(campoFrase.getText());
            String grupo = new String(String.valueOf(comboGrupo.getSelectedItem()));
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

            String hashSenha = TecladoVirtualSeguro.criarSenha(senha);

            //login e nome vem do certificado digital
            int grupoid;
            if (grupo.equals("Administrador")){
                grupoid = 1;
            }else {
                grupoid = 2;
            }
            try {
                // 1. Gera segredo em base32
                String segredoBase32 = TOTPAux.gerarTOTPBase32();

                // 2. Gera chave AES a partir da senha
                SecretKeySpec chaveAES = TOTPAux.gerarChaveAES(frase);

                // 3. Criptografa o segredo base32
                byte[] totp = TOTPAux.criptografarAES(segredoBase32, chaveAES);

                // 4. Exibe segredo (ou URI TOTP) ao usuário
                String email = "mail2@mail.com"; // ajuste conforme necessário
                String uri = "otpauth://totp/Cofre%20Digital:" + email +
                        "?secret=" + segredoBase32 + "&issuer=Cofre%20Digital";

                JOptionPane.showMessageDialog(this,
                        "Cadastre no Authenticator com este código:\n" + segredoBase32 + "\nOu utilize o QRCode a seguir:");

                QRCode.mostrarQRCode(uri);

                // 5. Salva no banco junto com outros dados
                boolean res = DB.inserirUsuario("mail2@mail.com","nome2",hashSenha,totp,grupoid, -1);
                if (!res){
                    JOptionPane.showMessageDialog(this, "Erro ao inserir usuário. Tente novamente");
                }
                if (fecharAposCadastro){
                    System.exit(0);
                } else{
                    mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame));
                    mainFrame.revalidate();
                    mainFrame.repaint();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao gerar chave TOTP: " + ex.getMessage());
            }
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
