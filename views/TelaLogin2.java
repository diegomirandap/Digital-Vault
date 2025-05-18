/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package views;

import Main.TecladoVirtualSeguro;
import Main.TecladoVirtualSeguro.Par;
import Main.Main;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import DB.*;

public class TelaLogin2 extends JPanel {
    private final JFrame mainFrame;
    private final String hashDoBanco;
    private final int uid;
    private final List<Par> sequenciaEscolhida = new ArrayList<>();
    private final StringBuilder senhaDigitada = new StringBuilder();
    private final JLabel lblSenha = new JLabel("Insira sua senha");
    private final JButton btnConfirmar = new JButton("Confirmar");
    private final JPanel botoesPanel = new JPanel(new GridLayout(1, 5, 10, 10));
    private final Random random = new Random();

    public static final int COMPRIMENTO_MIN = 8;
    public static final int COMPRIMENTO_MAX = 10;

    public TelaLogin2(JFrame frame, String hashDoBanco, int uid) {
        DB.inserirLog(3001,uid,null);
        this.mainFrame = frame;
        this.hashDoBanco = hashDoBanco;
        this.uid = uid;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblSenha.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSenha.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblSenha);
        add(Box.createVerticalStrut(200));

        botoesPanel.setMaximumSize(new Dimension(400, 50));
        botoesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(botoesPanel);
        add(Box.createVerticalStrut(200));

        btnConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirmar.setEnabled(false);
        btnConfirmar.addActionListener(e -> confirmarSenha());
        add(btnConfirmar);

        gerarTeclado();
    }

    private void gerarTeclado() {
        botoesPanel.removeAll();
        List<Par> pares = TecladoVirtualSeguro.gerarParesAleatorios();

        for (Par par : pares) {
            JButton btn = new JButton("[" + par.a + " " + par.b + "]");
            btn.setFont(new Font("Arial", Font.BOLD, 14));

            btn.addActionListener(e -> {
                int escolhido = random.nextBoolean() ? par.a : par.b;
                senhaDigitada.append(escolhido);
                sequenciaEscolhida.add(par);
                lblSenha.setText("Senha: " + "*".repeat(senhaDigitada.length()));

                if (senhaDigitada.length() >= COMPRIMENTO_MIN) {
                    btnConfirmar.setEnabled(true);
                }
                if (senhaDigitada.length() < COMPRIMENTO_MAX) {
                    gerarTeclado(); // embaralha novamente
                } else {
                    for (Component c : botoesPanel.getComponents()) c.setEnabled(false);
                }
            });

            botoesPanel.add(btn);
        }

        revalidate();
        repaint();
    }

    private void confirmarSenha() {
        Set<String> combinacoes = TecladoVirtualSeguro.gerarCombinacoesSenha(sequenciaEscolhida);
        boolean validado = false;
        String senhaFinal = "";
        for (String senha : combinacoes) {
            if (OpenBSDBCrypt.checkPassword(hashDoBanco, senha.toCharArray())) {
                validado = true;
                senhaFinal = senha;
            }
        }

        if (validado) {
            Main.tentativasFalhas.put(uid, 0);
            DB.inserirLog(3003,uid,null);
            JOptionPane.showMessageDialog(this, "Senha validada com sucesso!");
            DB.inserirLog(3002,uid,null);
            mainFrame.setContentPane(new TelaLogin3(mainFrame, uid, senhaFinal));
            mainFrame.revalidate();
        } else {
            int tentativas = Main.tentativasFalhas.getOrDefault(uid, 0) + 1;
            Main.tentativasFalhas.put(uid, tentativas);

            if (tentativas >= 3) {
                long tempoBloqueio = System.currentTimeMillis() + (2 * 60 * 1000); // 2 minutos
                Main.bloqueios.put(uid, tempoBloqueio);
                JOptionPane.showMessageDialog(this, "Usuário bloqueado por 2 minutos após 3 tentativas inválidas.");
                DB.inserirLog(3006,uid,null);
                DB.inserirLog(3007,uid,null);
                DB.inserirLog(3002,uid,null);
                mainFrame.setContentPane(new TelaLogin1(mainFrame));
            } else {
                JOptionPane.showMessageDialog(this, "Senha incorreta. Tentativa " + tentativas + "/3.");
                int mid = 3003 + tentativas;
                DB.inserirLog(mid,uid,null);
            }
        }

        // Reinicia
        senhaDigitada.setLength(0);
        sequenciaEscolhida.clear();
        lblSenha.setText("Insira sua senha");
        btnConfirmar.setEnabled(false);
        gerarTeclado();
    }
}
