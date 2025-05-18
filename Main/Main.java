/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package Main;
import DB.DB;
import views.*;
import javax.swing.*;
import java.util.HashMap;

public class Main {
    public static HashMap<Integer, Integer> tentativasFalhas = new HashMap<>();

    public static HashMap<Integer, Long> bloqueios = new HashMap<>();

    public static String fraseAdm = "";

    public static void main(String[] args) {
        DB.inserirLog(1001,null, null);
        DB.inicializarBanco();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Cofre Digital");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            int totalUsuarios = DB.contarUsuarios();

            if (totalUsuarios == 0) {
                DB.inserirLog(1005,null,null);
                frame.setContentPane(new TelaCadastro(frame, true, null)); // encerra app após cadastro
            } else {
                if (totalUsuarios != -1){
                    DB.inserirLog(1006,null,null);
                    frame.setContentPane(new TelaFraseAdm(frame));
                }
                else{
                    System.out.println("Não foi possível iniciar o sistema");
                    System.exit(-1);
                }

            }

            frame.setVisible(true);
        });
    }
}
