import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
    private static String SUA_SENHA = "@@1";
    private static final String URL_SERVIDOR = "jdbc:mysql://localhost/?user=root&password=" + SUA_SENHA;
    private static final String URL_BANCO = "jdbc:mysql://localhost/cofredigital?user=root&password=" + SUA_SENHA;

    public static void criarBanco() {
        String comandoCriarBanco = "CREATE DATABASE IF NOT EXISTS cofredigital;";
        try (Connection conn = DriverManager.getConnection(URL_SERVIDOR);
             Statement stmt = conn.createStatement()) {
            stmt.execute(comandoCriarBanco);
            System.out.println("Banco de dados 'cofredigital' criado (ou já existia).");
        } catch (SQLException e) {
            System.err.println("Erro ao criar o banco: " + e.getMessage());
        }
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL_BANCO);
    }

    public static void inicializarBanco() {
        String[] comandosCriacao = {
                // Tabela Grupos
                "CREATE TABLE IF NOT EXISTS Grupos (" +
                        " gid INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        " nome_grupo VARCHAR(255) NOT NULL UNIQUE);",

                // Tabela Chaveiro
                "CREATE TABLE IF NOT EXISTS Chaveiro (" +
                        " kid INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        " uid INTEGER NOT NULL," +
                        " cert_pem TEXT NOT NULL," +
                        " chave_privada BLOB NOT NULL);",

                // Tabela Usuarios (sem as foreign keys por enquanto)
                "CREATE TABLE IF NOT EXISTS Usuarios (" +
                        " uid INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        " login_name VARCHAR(255) NOT NULL UNIQUE," +
                        " nome VARCHAR(255) NOT NULL," +
                        " senha_hash VARCHAR(255) NOT NULL," +
                        " secret_totp BLOB NOT NULL," +
                        " grupo_id INTEGER NOT NULL," +
                        " tot_acessos INTEGER DEFAULT 0," +
                        " tot_consultas INTEGER DEFAULT 0," +
                        " kid INTEGER);",

                // Tabela Mensagens
                "CREATE TABLE IF NOT EXISTS Mensagens (" +
                        " mid INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        " descricao TEXT NOT NULL);",

                // Tabela Registros
                "CREATE TABLE IF NOT EXISTS Registros (" +
                        " rid INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        " timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        " mid INTEGER NOT NULL," +
                        " uid INTEGER," +
                        " arquivo TEXT);"
        };

        String[] comandosConstraints = {
                // Foreign key Usuarios.grupo_id -> Grupos.gid
                "ALTER TABLE Usuarios ADD CONSTRAINT fk_usuarios_grupo_id FOREIGN KEY (grupo_id) REFERENCES Grupos(gid);",

                // Foreign key Usuarios.kid -> Chaveiro.kid
                "ALTER TABLE Usuarios ADD CONSTRAINT fk_usuarios_kid FOREIGN KEY (kid) REFERENCES Chaveiro(kid);",

                // Foreign key Chaveiro.uid -> Usuarios.uid
                "ALTER TABLE Chaveiro ADD CONSTRAINT fk_chaveiro_uid FOREIGN KEY (uid) REFERENCES Usuarios(uid);",

                // Foreign key Registros.mid -> Mensagens.mid
                "ALTER TABLE Registros ADD CONSTRAINT fk_registros_mid FOREIGN KEY (mid) REFERENCES Mensagens(mid);",

                // Foreign key Registros.uid -> Usuarios.uid
                "ALTER TABLE Registros ADD CONSTRAINT fk_registros_uid FOREIGN KEY (uid) REFERENCES Usuarios(uid);"
        };

        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            // Criar tabelas
            for (String comando : comandosCriacao) {
                stmt.execute(comando);
            }
            System.out.println("Tabelas criadas com sucesso!");

            // Adicionar constraints
            for (String comando : comandosConstraints) {
                stmt.execute(comando);
            }
            System.out.println("Constraints adicionadas com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        criarBanco();
        inicializarBanco();
    }
}
