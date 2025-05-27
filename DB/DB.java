/*
Diego Miranda - 2210996
Felipe Cancella 2210487
 */

package DB;

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

            // Inicializar tabelas
            inserirGrupo("Adiministrador");
            inserirGrupo("Usuario");
            System.out.println("Tabelas inicializadas com sucesso!");

            inserirMensagem(1001, "Sistema iniciado.");
            inserirMensagem(1002, "Sistema encerrado.");
            inserirMensagem(1003, "Sessão iniciada para <login_name>.");
            inserirMensagem(1004, "Sessão encerrada para <login_name>.");
            inserirMensagem(1005, "Partida do sistema iniciada para cadastro do administrador.");
            inserirMensagem(1006, "Partida do sistema iniciada para operação normal pelos usuários.");
            inserirMensagem(2001, "Autenticação etapa 1 iniciada.");
            inserirMensagem(2002, "Autenticação etapa 1 encerrada.");
            inserirMensagem(2003, "Login name <login_name> identificado com acesso liberado.");
            inserirMensagem(2004, "Login name <login_name> identificado com acesso bloqueado.");
            inserirMensagem(2005, "Login name <login_name> não identificado.");
            inserirMensagem(3001, "Autenticação etapa 2 iniciada para <login_name>.");
            inserirMensagem(3002, "Autenticação etapa 2 encerrada para <login_name>.");
            inserirMensagem(3003, "Senha pessoal verificada positivamente para <login_name>.");
            inserirMensagem(3004, "Primeiro erro da senha pessoal contabilizado para <login_name>.");
            inserirMensagem(3005, "Segundo erro da senha pessoal contabilizado para <login_name>.");
            inserirMensagem(3006, "Terceiro erro da senha pessoal contabilizado para <login_name>.");
            inserirMensagem(3007, "Acesso do usuário <login_name> bloqueado pela autenticação etapa 2.");
            inserirMensagem(4001, "Autenticação etapa 3 iniciada para <login_name>.");
            inserirMensagem(4002, "Autenticação etapa 3 encerrada para <login_name>.");
            inserirMensagem(4003, "Token verificado positivamente para <login_name>.");
            inserirMensagem(4004, "Primeiro erro de token contabilizado para <login_name>.");
            inserirMensagem(4005, "Segundo erro de token contabilizado para <login_name>.");
            inserirMensagem(4006, "Terceiro erro de token contabilizado para <login_name>.");
            inserirMensagem(4007, "Acesso do usuário <login_name> bloqueado pela autenticação etapa 3.");
            inserirMensagem(5001, "Tela principal apresentada para <login_name>.");
            inserirMensagem(5002, "Opção 1 do menu principal selecionada por <login_name>.");
            inserirMensagem(5003, "Opção 2 do menu principal selecionada por <login_name>.");
            inserirMensagem(5004, "Opção 3 do menu principal selecionada por <login_name>.");
            inserirMensagem(6001, "Tela de cadastro apresentada para <login_name>.");
            inserirMensagem(6002, "Botão cadastrar pressionado por <login_name>.");
            inserirMensagem(6003, "Senha pessoal inválida fornecida por <login_name>.");
            inserirMensagem(6004, "Caminho do certificado digital inválido fornecido por <login_name>.");
            inserirMensagem(6005, "Chave privada verificada negativamente para <login_name> (caminho inválido).");
            inserirMensagem(6006, "Chave privada verificada negativamente para <login_name> (frase secreta inválida).");
            inserirMensagem(6007, "Chave privada verificada negativamente para <login_name> (assinatura digital inválida).");
            inserirMensagem(6008, "Confirmação de dados aceita por <login_name>.");
            inserirMensagem(6009, "Confirmação de dados rejeitada por <login_name>.");
            inserirMensagem(6010, "Botão voltar de cadastro para o menu principal pressionado por <login_name>.");
            inserirMensagem(7001, "Tela de consulta de arquivos secretos apresentada para <login_name>.");
            inserirMensagem(7002, "Botão voltar de consulta para o menu principal pressionado por <login_name>.");
            inserirMensagem(7003, "Botão Listar de consulta pressionado por <login_name>.");
            inserirMensagem(7004, "Caminho de pasta inválido fornecido por <login_name>.");
            inserirMensagem(7005, "Arquivo de índice decriptado com sucesso para <login_name>.");
            inserirMensagem(7006, "Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.");
            inserirMensagem(7007, "Falha na decriptação do arquivo de índice para <login_name>.");
            inserirMensagem(7008, "Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.");
            inserirMensagem(7009, "Lista de arquivos presentes no índice apresentada para <login_name>.");
            inserirMensagem(7010, "Arquivo <arq_name> selecionado por <login_name> para decriptação.");
            inserirMensagem(7011, "Acesso permitido ao arquivo <arq_name> para <login_name>.");
            inserirMensagem(7012, "Acesso negado ao arquivo <arq_name> para <login_name>.");
            inserirMensagem(7013, "Arquivo <arq_name> decriptado com sucesso para <login_name>.");
            inserirMensagem(7014, "Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.");
            inserirMensagem(7015, "Falha na decriptação do arquivo <arq_name> para <login_name>.");
            inserirMensagem(7016, "Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.");
            inserirMensagem(8001, "Tela de saída apresentada para <login_name>.");
            inserirMensagem(8002, "Botão encerrar sessão pressionado por <login_name>.");
            inserirMensagem(8003, "Botão encerrar sistema pressionado por <login_name>.");
            inserirMensagem(8004, "Botão voltar de sair para o menu principal pressionado por <login_name>.");



            // Adiciona constraints apenas se ainda não existem
            adicionarConstraintSeNaoExiste(conn, "Usuarios", "fk_usuarios_grupo_id",
                    "ALTER TABLE Usuarios ADD CONSTRAINT fk_usuarios_grupo_id FOREIGN KEY (grupo_id) REFERENCES Grupos(gid)");
            adicionarConstraintSeNaoExiste(conn, "Usuarios", "fk_usuarios_kid",
                    "ALTER TABLE Usuarios ADD CONSTRAINT fk_usuarios_kid FOREIGN KEY (kid) REFERENCES Chaveiro(kid)");
            adicionarConstraintSeNaoExiste(conn, "Chaveiro", "fk_chaveiro_uid",
                    "ALTER TABLE Chaveiro ADD CONSTRAINT fk_chaveiro_uid FOREIGN KEY (uid) REFERENCES Usuarios(uid)");
            adicionarConstraintSeNaoExiste(conn, "Registros", "fk_registros_mid",
                    "ALTER TABLE Registros ADD CONSTRAINT fk_registros_mid FOREIGN KEY (mid) REFERENCES Mensagens(mid)");
            adicionarConstraintSeNaoExiste(conn, "Registros", "fk_registros_uid",
                    "ALTER TABLE Registros ADD CONSTRAINT fk_registros_uid FOREIGN KEY (uid) REFERENCES Usuarios(uid)");

            System.out.println("Constraints adicionadas com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }

    public static boolean inserirUsuario(String login, String nome, String senhaHash, byte[] secretTotp, int grupoId, Integer kid) {
        String sql = "INSERT INTO Usuarios (login_name, nome, senha_hash, secret_totp, grupo_id, kid) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, nome);
            stmt.setString(3, senhaHash);
            stmt.setBytes(4, secretTotp);
            stmt.setInt(5, grupoId);
            if (kid == null || kid == -1) stmt.setNull(6, java.sql.Types.INTEGER);
            else stmt.setInt(6, kid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuário: " + e.getMessage());
            return false;
        }
    }

    public static int inserirChaveiro(int uid, String certPem, byte[] chavePrivada) {
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

    public static void inserirMensagem(int mid, String descricao) {
        String sql = "INSERT INTO mensagens(mid, descricao) VALUES(?,?)";
        String query = "SELECT COUNT(*) FROM mensagens WHERE MID = ?";
        try (Connection conn = conectar();PreparedStatement check = conn.prepareStatement(query)) {
            check.setInt(1, mid);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, mid);
                    stmt.setString(2, descricao);
                    stmt.executeUpdate();
                    ResultSet rs2 = stmt.getGeneratedKeys();
                    return;
                } catch (SQLException e) {
                    System.err.println("Erro ao inserir Mensagens: " + e.getMessage());
                    return;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean atualizarKidDoUsuario(int uid, int kid) {
        String sql = "UPDATE Usuarios SET kid = ? WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kid);
            stmt.setInt(2, uid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar KID do usuário: " + e.getMessage());
            return false;
        }
    }

    public static Integer buscarUid(String email) {
        String sql = "SELECT uid FROM Usuarios WHERE login_name = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("uid");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar UID: " + e.getMessage());
        }
        return null;
    }

    public static Integer buscarUidAdm() {
        String sql = "SELECT uid FROM Usuarios WHERE grupo_id = 1";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("uid");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar UID: " + e.getMessage());
        }
        return null;
    }

    public static String buscarEmail(Integer uid) {
        String sql = "SELECT login_name FROM Usuarios WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("login_name");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar email: " + e.getMessage());
        }
        return null;
    }

    public static String buscarNome(Integer uid) {
        String sql = "SELECT nome FROM Usuarios WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nome");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar nome: " + e.getMessage());
        }
        return null;
    }

    public static Integer buscarGrupo(Integer uid) {
        String sql = "SELECT grupo_id FROM Usuarios WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("grupo_id");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar nome: " + e.getMessage());
        }
        return null;
    }

    public static Integer contarAcessos(Integer uid) {
        String sql = "SELECT tot_acessos FROM Usuarios WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("tot_acessos");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar nome: " + e.getMessage());
        }
        return null;
    }

    public static Integer contarConsultas(Integer uid) {
        String sql = "SELECT tot_consultas FROM Usuarios WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("tot_consultas");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar nome: " + e.getMessage());
        }
        return null;
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

    public static byte[] buscarSecretTotp(int id) {
        String sql = "SELECT secret_totp FROM Usuarios WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBytes("secret_totp") : null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar TOTP: " + e.getMessage());
            return null;
        }
    }

    public static void inserirLog(int mid, Integer uid, String arquivo) {
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

    public static void incrementarAcessos(int uid) {
        String sql = "UPDATE Usuarios SET tot_acessos = tot_acessos + 1 WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao incrementar acessos: " + e.getMessage());
        }
    }

    public static void incrementarConsultas(int uid) {
        String sql = "UPDATE Usuarios SET tot_consultas = tot_consultas + 1 WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao incrementar consultas: " + e.getMessage());
        }
    }

    public static String buscarCertificadoPEM(int uid) {
        String sql = "SELECT cert_pem FROM Chaveiro WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("cert_pem") : null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar certificado PEM: " + e.getMessage());
            return null;
        }
    }

    public static byte[] buscarChavePrivada(int uid) {
        String sql = "SELECT chave_privada FROM Chaveiro WHERE uid = ?";
        try (Connection conn = conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBytes("chave_privada") : null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar chave privada: " + e.getMessage());
            return null;
        }
    }

    public static boolean inserirGrupo(String nomeGrupo) {
        String sqlVerifica = "SELECT COUNT(*) FROM Grupos WHERE nome_grupo = ?";
        String sqlInsere = "INSERT INTO Grupos (nome_grupo) VALUES (?)";

        try (Connection conn = conectar()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(sqlVerifica)) {
                checkStmt.setString(1, nomeGrupo);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsere)) {
                insertStmt.setString(1, nomeGrupo);
                int linhasAfetadas = insertStmt.executeUpdate();
                if (linhasAfetadas > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir grupo: " + e.getMessage());
        }

        return false;
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

    public static void adicionarConstraintSeNaoExiste(Connection conn, String tableName, String constraintName, String alterSql) throws SQLException {
        String query = "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_name = ? AND constraint_name = ?";
        try (PreparedStatement check = conn.prepareStatement(query)) {
            check.setString(1, tableName);
            check.setString(2, constraintName);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterSql);
                }
            }
        }
    }
    public static void mostraLog() {
        String sql = "SELECT " +
                "r.mid, " +
                "r.rid, " +
                "m.descricao, " +
                "u.login_name, " +
                "r.timestamp " +
                "FROM Registros r " +
                "JOIN Mensagens m ON r.mid = m.mid " +
                "LEFT JOIN Usuarios u ON r.uid = u.uid " +
                "ORDER BY r.rid DESC";

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.printf("%-20s | %-5s | %-90s | %-20s%n", "Timestamp", "MID", "Descrição", "Login");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                int mid = rs.getInt("mid");
                String login = rs.getString("login_name");
                String timestamp = rs.getString("timestamp");
                String descricao = rs.getString("descricao");

                // Substituir <login_name> pelo login (ou por "(anônimo)" se null)
                String loginDisplay = (login != null) ? login : "(anônimo)";
                if (descricao != null && descricao.contains("<login_name>")) {
                    descricao = descricao.replace("<login_name>", loginDisplay);
                }

                System.out.printf("%-20s | %-5d | %-90s | %-20s%n",
                        timestamp,
                        mid,
                        descricao,
                        loginDisplay);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao consultar registros: " + e.getMessage());
        }
    }

}

