import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

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
        criarBanco();
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

                // Tabela Usuarios
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

    public boolean inserirUsuario(String login, String nome, String senhaHash, byte[] secretTotp, int grupoId, int kid) {
        String sql = "INSERT INTO Usuarios (login_name, nome, senha_hash, secret_totp, grupo_id, kid) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, nome);
            stmt.setString(3, senhaHash);
            stmt.setBytes(4, secretTotp);
            stmt.setInt(5, grupoId);
            stmt.setInt(6, kid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuário: " + e.getMessage());
            return false;
        }
    }

    public int inserirChaveiro(int uid, String certPem, byte[] chavePrivada) {
        String sql = "INSERT INTO Chaveiro (uid, cert_pem, chave_privada) VALUES (?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, uid);
            stmt.setString(2, certPem);
            stmt.setBytes(3, chavePrivada);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir chaveiro: " + e.getMessage());
            return -1;
        }
    }

    public static String buscarSenhaHash(int id) {
        String sql = "SELECT senha_hash FROM Usuarios WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("senha_hash") : null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar senha: " + e.getMessage());
            return null;
        }
    }

    public byte[] buscarSecretTotp(String login) {
        String sql = "SELECT secret_totp FROM Usuarios WHERE login_name = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBytes("secret_totp") : null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar TOTP: " + e.getMessage());
            return null;
        }
    }
/*
    public void registrarLog(int mid, Integer uid, String arquivo) {
        String sql = "INSERT INTO Registros (mid, uid, arquivo) VALUES (?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mid);
            if (uid != null) stmt.setInt(2, uid); else stmt.setNull(2, Types.INTEGER);
            if (arquivo != null) stmt.setString(3, arquivo); else stmt.setNull(3, Types.VARCHAR);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao registrar log: " + e.getMessage());
        }
    }
*/
    public void incrementarAcessos(int uid) {
        String sql = "UPDATE Usuarios SET tot_acessos = tot_acessos + 1 WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao incrementar acessos: " + e.getMessage());
        }
    }

    public boolean inserirGrupo(String nomeGrupo) {
        String sql = "INSERT INTO Grupos (nome_grupo) VALUES (?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeGrupo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir grupo: " + e.getMessage());
            return false;
        }
    }

    public int buscarGid(String nomeGrupo) {
        String sql = "SELECT gid FROM Grupos WHERE nome_grupo = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeGrupo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("gid") : -1;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar grupo: " + e.getMessage());
            return -1;
        }
    }

    public static void addLog(int mid, String email, String filename){

        return;
    }

    public static int contarUsuarios() {
        String sql = "SELECT COUNT(*) AS total FROM Usuarios";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            System.err.println("Erro ao contar usuários: " + e.getMessage());
            return -1;
        }
    }
}
