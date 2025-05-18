/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package Main;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.util.*;
import static Main.ChaveDigitalAux.gerarBytesAleatorios;

public class TecladoVirtualSeguro {
    public static final int NUM_DIGITOS = 10;
    public static final int NUM_PARES = 5;

    public static class Par {
        public int a, b;
        Par(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    public static String criarSenha(String senha){
        byte[] salt = gerarBytesAleatorios(16);
        String hash = OpenBSDBCrypt.generate(senha.toCharArray(), salt, 8);

        return hash;
    }

    public static List<Par> gerarParesAleatorios() {
        List<Integer> digitos = new ArrayList<>();
        for (int i = 0; i < NUM_DIGITOS; i++) digitos.add(i);
        Collections.shuffle(digitos);

        List<Par> pares = new ArrayList<>();
        for (int i = 0; i < NUM_PARES; i++) {
            pares.add(new Par(digitos.get(i * 2), digitos.get(i * 2 + 1)));
        }
        return pares;
    }

    public static Set<String> gerarCombinacoesSenha(List<Par> sequenciaEscolhida) {
        Set<String> possiveisSenhas = new HashSet<>();
        int total = sequenciaEscolhida.size();
        for (int i = 0; i < (1 << total); i++) {
            StringBuilder senha = new StringBuilder();
            for (int j = 0; j < total; j++) {
                Par p = sequenciaEscolhida.get(j);
                senha.append(((i >> j) & 1) == 0 ? p.a : p.b);
            }
            possiveisSenhas.add(senha.toString());
        }
        return possiveisSenhas;
    }

    public static boolean validarSenha(Set<String> tentativas, String hashDoBanco) {
        for (String senha : tentativas) {
            if (OpenBSDBCrypt.checkPassword(hashDoBanco, senha.toCharArray())) {
                return true;
            }
        }
        return false;
    }
}
