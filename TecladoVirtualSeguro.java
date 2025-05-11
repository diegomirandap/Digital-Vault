import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.SecureRandom;
import java.util.*;

public class TecladoVirtualSeguro {
    static final int NUM_DIGITOS = 10;
    static final int NUM_PARES = 5;
    static final int COMPRIMENTO_SENHA = 8;

    static class Par {
        int a, b;
        Par(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SecureRandom random = new SecureRandom();
        List<Par> sequenciaEscolhida = new ArrayList<>();

        System.out.println("=== Teclado Virtual Seguro com Bcrypt ===");

        // Etapas de entrada
        for (int etapa = 1; etapa <= COMPRIMENTO_SENHA; etapa++) {
            System.out.println("\nEtapa " + etapa + ":");

            List<Integer> digitos = new ArrayList<>();
            for (int i = 0; i < NUM_DIGITOS; i++) digitos.add(i);
            Collections.shuffle(digitos);

            List<Par> pares = new ArrayList<>();
            for (int i = 0; i < NUM_PARES; i++) {
                pares.add(new Par(digitos.get(i * 2), digitos.get(i * 2 + 1)));
            }

            for (int i = 0; i < NUM_PARES; i++) {
                System.out.println("Campo " + (i + 1) + ": [" + pares.get(i).a + " " + pares.get(i).b + "]");
            }

            int escolha;
            while (true) {
                System.out.print("Escolha o campo (1 a 5): ");
                if (scanner.hasNextInt()) {
                    escolha = scanner.nextInt();
                    if (escolha >= 1 && escolha <= NUM_PARES) break;
                }
                System.out.println("Entrada inválida.");
                scanner.nextLine();
            }

            sequenciaEscolhida.add(pares.get(escolha - 1));
        }

        scanner.close();

        // Gerar combinações possíveis
        Set<String> possiveisSenhas = new HashSet<>();
        for (int i = 0; i < (1 << COMPRIMENTO_SENHA); i++) {
            StringBuilder senha = new StringBuilder();
            for (int j = 0; j < COMPRIMENTO_SENHA; j++) {
                Par p = sequenciaEscolhida.get(j);
                int digito = ((i >> j) & 1) == 0 ? p.a : p.b;
                senha.append(digito);
            }
            possiveisSenhas.add(senha.toString());
        }

        System.out.println("\nGerando bcrypt hashes para " + possiveisSenhas.size() + " senhas possíveis:");
        for (String senha : possiveisSenhas) {
            if (senha.equals("13572468")) {
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                String hash = OpenBSDBCrypt.generate(senha.toCharArray(), salt, 12);
                System.out.println("Senha encontrada: " + senha);
                System.out.println("BCrypt hash: " + hash);
            }
        }
    }
}
