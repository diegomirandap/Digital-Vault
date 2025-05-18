package views;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import DB.*;
import Main.*;

public class TelaCadastro extends JPanel {
    JTextField campoCert, campoChave, campoFrase;
    JComboBox<String> comboGrupo;
    JPasswordField campoSenha, campoConfirmacao;
    boolean fecharAposCadastro;

    public TelaCadastro(JFrame mainFrame, boolean fecharAposCadastro, Integer uidLog) {
        this.fecharAposCadastro = fecharAposCadastro;
        if (fecharAposCadastro)
            setLayout(new GridLayout(8, 2, 5, 5));
        else{
            setLayout(new GridLayout(12, 2, 5, 5));
            add(new JLabel("Login: " + DB.buscarEmail(uidLog)));
            add(new JLabel("Grupo: " + DB.buscarGrupo(uidLog)));
            add(new JLabel("Nome: " + DB.buscarNome(uidLog)));
            add(new JLabel("Total de acessos do usuário: " + DB.contarAcessos(uidLog)));
        }

        campoCert = new JTextField(255);
        campoChave = new JTextField(255);
        campoFrase = new JTextField(255);
        campoSenha = new JPasswordField(10);
        campoConfirmacao = new JPasswordField(10);

        comboGrupo = fecharAposCadastro
                ? new JComboBox<>(new String[]{"Administrador"})
                : new JComboBox<>(new String[]{"Administrador", "Usuário"});

        add(new JLabel("Caminho do Certificado Digital:")); add(campoCert);
        add(new JLabel("Caminho da Chave Privada:")); add(campoChave);
        add(new JLabel("Frase Secreta:")); add(campoFrase);
        add(new JLabel("Grupo:")); add(comboGrupo);
        add(new JLabel("Senha Pessoal:")); add(campoSenha);
        add(new JLabel("Confirmar Senha:")); add(campoConfirmacao);

        JButton botaoCadastrar = new JButton("Cadastrar");
        JButton botaoVoltar = new JButton("Voltar");

        botaoCadastrar.addActionListener(e -> {
            String caminhoCert = campoCert.getText().trim();
            String caminhoChave = campoChave.getText().trim();
            String frase = campoFrase.getText().trim();
            String grupo = String.valueOf(comboGrupo.getSelectedItem());
            String senha = new String(campoSenha.getPassword());
            String confSenha = new String(campoConfirmacao.getPassword());

            if (!senha.equals(confSenha)) {
                JOptionPane.showMessageDialog(this, "As senhas não coincidem.");
                return;
            }
            if (senha.length() < 8 || senha.length() > 10 || temDigitosRepetidos(senha)) {
                JOptionPane.showMessageDialog(this, "Senha inválida: deve ter 8 a 10 dígitos sem repetições consecutivas.");
                return;
            }

            try {
                // 1. Carrega certificado
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                FileInputStream fis = new FileInputStream(caminhoCert);
                X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
                fis.close();

                String[] dados = Certificado.extrairNomeEEmail(cert);
                String nome = dados[0];
                String email = dados[1];

                // 2. Exibe dados para confirmação
                String msg = String.format("""
                        Versão: %d
                        Série: %s
                        Validade: %s até %s
                        Assinatura: %s
                        Emissor: %s
                        Sujeito: %s
                        E-mail: %s

                        Deseja confirmar o cadastro?
                        """,
                        cert.getVersion(),
                        cert.getSerialNumber(),
                        cert.getNotBefore(), cert.getNotAfter(),
                        cert.getSigAlgName(),
                        cert.getIssuerX500Principal().getName(),
                        cert.getSubjectX500Principal().getName(),
                        email
                );

                int opcao = JOptionPane.showConfirmDialog(this, msg, "Confirmação", JOptionPane.YES_NO_OPTION);
                if (opcao != JOptionPane.YES_OPTION) return;

                // 3. Verifica duplicação
                Integer uidExistente = DB.buscarUid(email);
                if (uidExistente != null) {
                    JOptionPane.showMessageDialog(this, "E-mail já cadastrado.");
                    return;
                }

                // 4. Validação de chave privada
                /*
                PrivateKey chavePrivada = ChaveDigitalAux.carregarChavePrivada(caminhoChave, frase);
                byte[] dadosTeste = ChaveDigitalAux.gerarBytesAleatorios(8192);
                byte[] assinatura = ChaveDigitalAux.assinarComChavePrivada(dadosTeste, chavePrivada);
                boolean verificado = ChaveDigitalAux.verificarAssinatura(dadosTeste, assinatura, cert.getPublicKey());
                if (!verificado) {
                    JOptionPane.showMessageDialog(this, "Assinatura inválida.");
                    return;
                }*/
                PrivateKey chavePrivada = ChaveDigitalAux.carregarChavePrivada(caminhoChave, frase);
                boolean verificado = ChaveDigitalAux.validarChaveComCertificado(chavePrivada, cert);
                if (!verificado) {
                    JOptionPane.showMessageDialog(this, "Assinatura inválida.");
                    return;
                }

                    // 5. Processa TOTP e hash
                String hashSenha = TecladoVirtualSeguro.criarSenha(senha);
                String segredoTOTP = TOTPAux.gerarTOTPBase32();
                SecretKeySpec chaveAES = TOTPAux.gerarChaveAES(frase);
                byte[] secretTotp = TOTPAux.criptografarAES(segredoTOTP, chaveAES);

                int grupoId = grupo.equals("Administrador") ? 1 : 2;
                if (!DB.inserirUsuario(email, nome, hashSenha, secretTotp, grupoId, null)) {
                    JOptionPane.showMessageDialog(this, "Erro ao inserir usuário.");
                    return;
                }

                Integer uid = DB.buscarUid(email);
                if (uid == null || uid == -1) {
                    JOptionPane.showMessageDialog(this, "Erro ao buscar UID após inserção.");
                    return;
                }

                String pemCert = ChaveDigitalAux.converterCertificadoParaPEM(cert);
                byte[] chaveBinaria = chavePrivada.getEncoded();
                int kid = DB.inserirChaveiro(uid, pemCert, chaveBinaria);
                if (kid == -1) {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar chave.");
                    return;
                }

                DB.atualizarKidDoUsuario(uid, kid);

                // 6. QRCode
                String uri = "otpauth://totp/Cofre%20Digital:" + email + "?secret=" + segredoTOTP + "&issuer=Cofre%20Digital";
                JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!\n\nCódigo TOTP: " + segredoTOTP);
                QRCode.mostrarQRCode(uri);

                // 7. Redirecionamento
                if (fecharAposCadastro) {
                    System.exit(0);
                } else {
                    mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame, uid));
                    mainFrame.revalidate();
                    mainFrame.repaint();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        botaoVoltar.addActionListener(e -> {
            if (!fecharAposCadastro) {
                mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame, uidLog));
                mainFrame.revalidate();
            } else {
                System.exit(0);
            }
        });

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
