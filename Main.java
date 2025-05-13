import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        // abrir o banco de dados
        DB.inicializarBanco();

        DB.addLog(1001, null, null);
        /*
        if semUser{
            sequenciaSemUser()
        }
        else{
            sequenciaComUser()
        */


    }
}

/*
sequenciaSemUser(){
    sequencia de cadastro
        Formulário de Cadastro:
        – Caminho do arquivo do certificado digital: <campo com 255 caracteres>
        – Caminho do arquivo da chave privada: <campo com 255 caracteres>
        – Frase secreta: <campo de 255 caracteres>
        – Grupo: <lista de opções: Administrador e Usuário>
        – Senha pessoal: <campo com 10 caracteres> (8 a 10 digitos sem dois consecutivos iguais)
        – Confirmação senha pessoal: <campo com 10 caracteres> (8 a 10 digitos sem dois consecutivos iguais)
        <Botão Cadastar> <Botão Voltar de Cadastrar para o Menu Principal>

        Ao confirmar:
            gerar o codigo para itoken com secure random em array de bites e converte para base32
            passar para o user como qrcode e escrito
                otpauth://TYPE/LABEL?PARAMETERS
                    totp é o tipo do One-Time Password, ou seja, Time-based OTP (TOTP).
                    Cofre%20Digital é o identificador do programa no qual a TOTP deve ser usada.
                    admin@inf1416.puc-rio.br é a identificação da conta do usuário.
                    JXXMGK33L7S3H3JOY5GMUXC7G7ASJHTD é o segredo do código TOTP em BASE32
            exibir certificado digital com  Versão, Série, Validade, Tipo de Assinatura, Emissor, Sujeito (Friendly Name) e E-mail. S
    encerrar sistema
}
sequenciaComUser{
    login etapa 1
        inserir email
    login etapa 2
        senha via teclado digital (a partir do 8 digito libera o botão de prosseguir)
    login etapa 3
        itoken
    switch case
        case 1 menu
            Login: login_name_do_usuário
            Grupo: grupo_do_usuário
            Nome: nome_do_usuário
            Total de acessos do usuário: total_de _acessos_do_usuario
            Menu Principal:
            1 – Cadastrar um novo usuário
            2 – Consultar pasta de arquivos secretos do usuário
            3 – Sair do Sistema

        case 2 cadastro
            Login: login_name_do_usuário
            Grupo: grupo_do_usuário
            Nome: nome_do_usuário
            Total de usuários do sistema: total_de_usuários
            Formulário de Cadastro:
            – Caminho do arquivo do certificado digital: <campo com 255 caracteres>
            – Caminho do arquivo da chave privada: <campo com 255 caracteres>
            – Frase secreta: <campo de 255 caracteres>
            – Grupo: <lista de opções: Administrador e Usuário>
            – Senha pessoal: <campo com 10 caracteres> (8 a 10 digitos sem dois consecutivos iguais)
            – Confirmação senha pessoal: <campo com 10 caracteres> (8 a 10 digitos sem dois consecutivos iguais)
            <Botão Cadastar> <Botão Voltar de Cadastrar para o Menu Principal>

            Ao confirmar:
                gerar o codigo para itoken com secure random em array de bites e converte para base32
                passar para o user como qrcode e escrito
                    otpauth://TYPE/LABEL?PARAMETERS
                        totp é o tipo do One-Time Password, ou seja, Time-based OTP (TOTP).
                        Cofre%20Digital é o identificador do programa no qual a TOTP deve ser usada.
                        admin@inf1416.puc-rio.br é a identificação da conta do usuário.
                        JXXMGK33L7S3H3JOY5GMUXC7G7ASJHTD é o segredo do código TOTP em BASE32
                exibir certificado digital com  Versão, Série, Validade, Tipo de Assinatura, Emissor, Sujeito (Friendly Name) e E-mail. S

        case 3 certificado
            Login: login_name_do_usuário
            Grupo: grupo_do_usuário
            Nome: nome_do_usuário
            Total de consultas do usuário: total_de_consultas_do_usuário
            Caminho da pasta: <campo com 255 caracteres>
            Frase secreta: <campo com 255 caracteres>
            <Botão Listar>
            Tabela com nomes dos arquivos secretos do usuário e
            respectivos atributos (dono e grupo do arquivo).
            <Botão Voltar de Consultar para o Menu Principal>

        case 4 sair
            Login: login_name_do_usuário
            Grupo: grupo_do_usuário
            Nome: nome_do_usuário
            Total de acessos do usuário: total_de _acessos_do_usuario
            Saída do sistema:
            Mensagem de saída.
            <Botão Encerrar Sessão> <Botão Encerrar Sistema>
            <Botão Voltar de Sair para o Menu Principal>


}
 */