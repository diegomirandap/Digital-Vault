package Main;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.SecureRandom;
import java.util.*;

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
        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[16];
        random.nextBytes(salt);
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
    /*
    public static void main(String[] args) { // args[0] == login (ainda descobrir se vai ser apenas o email ou uid
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

        //String hashBanco = DB.buscarSenhaHash(args[0]);
        String hashBanco = "$2y$08$zLfjWZprtU2xoTJJshoNdutUl1Orw.6um8Mrrwo0uLni6GBS7sP4O";

        for (String senha : possiveisSenhas) {
            /
            if (senha.equals("13572468")) {
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                String hash = OpenBSDBCrypt.generate(senha.toCharArray(), salt, 8);
            *
            //vai acessar o banco e pegar o salt atraves da senha armazenada em texto plano
            //calcula o hash para a senha atual e compara com a optida dentro do banco
            //utiliza uma função do proprio openbsbcrypt para realizar todo esse processo

            boolean resultado = OpenBSDBCrypt.checkPassword(hashBanco, senha.toCharArray());
            if (resultado){
                System.out.println("Senha validada");
            }
            else{
                // Retry
                System.out.println("Senha não validada");
            }
        }
    }

     */
}






/*
primeiro login: acesso direto ao cadastramento e depois fecha a execução
a partir do segundo: tela de login e resto do programa

senha pessoal 8 a 10 digitos
pressiona o botão ate 10 vezes, após a 8tava pode habilitar o botão de inserção
usa essa senha como semente do prng para o hash da cripto do token

adm ve indice e seus qrquivos e pode cadastrar o usuário

para o sistema funcionar, deve fornecer a chave privada do primeiro adm (interrogaçao)


apos o cadastro
verifica dados do cd
gera o segredo na tela para cadastro - utiliza o secure random pra gerar aleatoriamente (array de bites) e passa para base32
adiciona no authenticator (qrcode ou digita)
adiciona no banco o segredo criptografado com a senha do usuário
 */
