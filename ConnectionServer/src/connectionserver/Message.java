package connectionserver;

public class Message{
    
    // Erros
    public static final String INVALID_CMD = "Comando inválido!\n";
    public static final String INVALID_ARGS = "Argumentos inválidos!\n";
    public static final String INVALID_LOGIN = "Nome de usuário inválido!\n";
    public static final String INVALID_PSWD = "Senha inválida!\n";
    public static final String LOGIN_FAIL = "Falha ao acessar o servidor de login!\n";
    public static final String ADDUSER_FAIL = "Falha ao adicionar usuário!\n";
    public static final String RMUSER_FAIL = "Falha ao remover usuário!\n";
    public static final String NOT_LOGGED = "É necessário estar logado!\n";
    public static final String USER_EXISTS = "O usuário já está cadastrado!\n";
    public static final String USER_NOTEXISTS = "O usuário não existe!\n";
    public static final String PERM_DENIED = "O usuário não tem permissão para executar esse comando!\n";
    public static final String DIR_EXISTS = "O diretório já existe!\n";
    public static final String DIR_NOTEXISTS = "O diretório não existe!\n";
    public static final String FILE_NOTEXISTS = "O arquivo não existe!\n";
    public static final String CREATE_FAIL = "Falha ao criar o diretório!\n";
    public static final String RMDIR_FAIL = "Falha ao remover o diretório!\n";
    public static final String NOT_DIR = " não é um diretório!\n";
    public static final String SEND_FAIL = "Falha ao enviar o arquivo!\n";
    public static final String GET_FAIL = "Falha ao enviar o baixar o arquivo!\n";
    public static final String NONE = "NONE!\n";
    
    // Sucesso
    public static final String LOGIN_SUCCESS = "Login efetuado com sucesso!\n";
    public static final String ADDUSER_SUCCESS = "Usuário adicionado com sucesso!\n";
    public static final String RMUSER_SUCCESS = "Usuário removido com sucesso!\n";
    public static final String CHANGE_SUCCESS = "Senha alterada com sucesso!\n";
    public static final String MKDIR_SUCCESS = "Diretório criado com sucesso!\n";
    public static final String RMDIR_SUCCESS = "Diretório removido com sucesso!\n";
    public static final String SEND_SUCCESS = "Arquivo enviado com sucesso!\n";
    public static final String ALLRIGTH = "Fuck you baby, let's go!\n";
}
