package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBStart {
    private static final String URL = "jdbc:sqlite:cofre_digital.db";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        String[] comandos = {
                // Tabela Grupos
                "CREATE TABLE IF NOT EXISTS Grupos (" +
                        " gid INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " nome_grupo TEXT NOT NULL UNIQUE);",

                // Tabela Usuarios
                "CREATE TABLE IF NOT EXISTS Usuarios (" +
                        " uid INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " login_name TEXT NOT NULL UNIQUE," +
                        " nome TEXT NOT NULL," +
                        " senha_hash TEXT NOT NULL," +
                        " secret_totp BLOB NOT NULL," +
                        " grupo_id INTEGER NOT NULL," +
                        " tot_acessos INTEGER DEFAULT 0," +
                        " tot_consultas INTEGER DEFAULT 0," +
                        " kid INTEGER," +
                        " FOREIGN KEY (grupo_id) REFERENCES Grupos(gid)," +
                        " FOREIGN KEY (kid) REFERENCES Chaveiro(kid));",

                // Tabela Chaveiro
                "CREATE TABLE IF NOT EXISTS Chaveiro (" +
                        " kid INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " uid INTEGER NOT NULL," +
                        " cert_pem TEXT NOT NULL," +
                        " chave_privada BLOB NOT NULL," +
                        " FOREIGN KEY (uid) REFERENCES Usuarios(uid));",

                // Tabela Mensagens
                "CREATE TABLE IF NOT EXISTS Mensagens (" +
                        " mid INTEGER PRIMARY KEY," +
                        " descricao TEXT NOT NULL);",

                // Tabela Registros
                "CREATE TABLE IF NOT EXISTS Registros (" +
                        " rid INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        " mid INTEGER NOT NULL," +
                        " uid INTEGER," +
                        " arquivo TEXT," +
                        " FOREIGN KEY (mid) REFERENCES Mensagens(mid)," +
                        " FOREIGN KEY (uid) REFERENCES Usuarios(uid));"
        };
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            for (String comando : comandos) {
                stmt.execute(comando);
            }
            System.out.println("Banco de dados inicializado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        inicializarBanco();
    }
}
