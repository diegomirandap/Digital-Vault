/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package views;

import DB.DB;
import Main.ChaveDigitalAux;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

public class TelaConsulta extends JPanel {
    private JTextField campoPasta, campoFrase;
    private JTable tabelaArquivos;
    private DefaultTableModel modelo;

    public TelaConsulta(JFrame mainFrame, int uid) {
        DB.inserirLog(7001,uid,null);
        setLayout(new BorderLayout(10, 10));

        JPanel topo = new JPanel(new GridLayout(6, 2, 5, 5));
        topo.add(new JLabel("Login: " + DB.buscarEmail(uid)));
        topo.add(new JLabel("Grupo: " + DB.buscarGrupo(uid)));
        topo.add(new JLabel("Nome: " + DB.buscarNome(uid)));
        topo.add(new JLabel("Total de consultas do usuário: " + DB.contarConsultas(uid)));

        campoPasta = new JTextField(255);
        campoFrase = new JTextField(255);

        topo.add(new JLabel("Caminho da pasta:"));
        topo.add(campoPasta);
        topo.add(new JLabel("Frase secreta:"));
        topo.add(campoFrase);

        JButton botaoListar = new JButton("Listar");
        JButton botaoVoltar = new JButton("Voltar");
        topo.add(botaoListar); topo.add(botaoVoltar);
        add(topo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new Object[]{"Nome Código", "Nome Secreto", "Dono", "Grupo"}, 0);
        tabelaArquivos = new JTable(modelo);
        add(new JScrollPane(tabelaArquivos), BorderLayout.CENTER);

        // Ação do botão Listar
        botaoListar.addActionListener(e -> {
            DB.inserirLog(7003,uid,null);
            String pasta = campoPasta.getText().trim();
            String frase = campoFrase.getText().trim();

            DB.incrementarConsultas(uid);

            if (pasta.isEmpty() || frase.isEmpty()) {
                DB.inserirLog(7004,uid,null);
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
                return;
            }

            try {
                // Valida frase e carrega chave/certificado
                PrivateKey chavePrivada = ChaveDigitalAux.carregarChavePrivadaDoBanco(uid, frase);
                X509Certificate cert = ChaveDigitalAux.carregarCertificadoDoBanco(uid);
                boolean valido = ChaveDigitalAux.validarChaveComCertificado(chavePrivada, cert);

                if (!valido) {
                    JOptionPane.showMessageDialog(this, "Frase inválida ou chave incompatível.");
                    return;
                }

                // Carrega índice
                File dir = new File(pasta);
                File fEnc = new File(dir, "index.enc");
                File fEnv = new File(dir, "index.env");
                File fAsd = new File(dir, "index.asd");

                if (!fEnc.exists() || !fEnv.exists() || !fAsd.exists()) {
                    JOptionPane.showMessageDialog(this, "Arquivos do índice não encontrados.");
                    return;
                }
                ///  /////////////////////////////////
                List<String[]> linhas = ChaveDigitalAux.processarIndice(fEnc, fEnv, fAsd, uid);
                modelo.setRowCount(0);
                String grupoUser = DB.buscarGrupo(uid).toString();
                String donoUser = DB.buscarEmail(uid);

                for (String[] dados : linhas) {
                    if (dados[2].equalsIgnoreCase(donoUser) || dados[3].equalsIgnoreCase(grupoUser)) {
                        modelo.addRow(dados);
                    }
                }
                DB.inserirLog(7009,uid,null);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao processar: " + ex.getMessage());
            }
        });

        // Ação do botão Voltar
        botaoVoltar.addActionListener(e -> {
            DB.inserirLog(7002,uid,null);
            mainFrame.setContentPane(new TelaMenuPrincipal(mainFrame, uid));
            mainFrame.revalidate();
        });

        // Ao clicar em linha
        tabelaArquivos.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            int row = tabelaArquivos.getSelectedRow();
            if (row == -1) return;

            String nomeCod = (String) modelo.getValueAt(row, 0);
            String nomeReal = (String) modelo.getValueAt(row, 1);
            String dono = (String) modelo.getValueAt(row, 2);
            String userEmail = DB.buscarEmail(uid);

            if (!userEmail.equalsIgnoreCase(dono)) {
                JOptionPane.showMessageDialog(this, "Acesso negado: você não é o dono do arquivo.");
                return;
            }

            try {
                String pasta = campoPasta.getText().trim();
                String frase = campoFrase.getText().trim();

                PrivateKey chavePrivada = ChaveDigitalAux.carregarChavePrivadaDoBanco(uid, frase);
                X509Certificate cert = ChaveDigitalAux.carregarCertificadoDoBanco(uid);

                File arqEnc = new File(pasta, nomeCod + ".enc");
                File arqEnv = new File(pasta, nomeCod + ".env");
                File arqAsd = new File(pasta, nomeCod + ".asd");

                if (!arqEnc.exists() || !arqEnv.exists() || !arqAsd.exists()) {
                    JOptionPane.showMessageDialog(this, "Arquivos do segredo não encontrados.");
                    return;
                }

                ChaveDigitalAux.decriptarArquivoSecreto(arqEnc, arqEnv, arqAsd, nomeReal, chavePrivada, cert.getPublicKey());
                JOptionPane.showMessageDialog(this, "Arquivo decriptado como: " + nomeReal);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao acessar o arquivo: " + ex.getMessage());
            }
        });
    }
}
