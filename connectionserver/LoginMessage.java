package connectionserver;

public class LoginMessage{
	
	// Erros
	public static final String INVALID_USER = "Nome de usuário inválido!\n";
	public static final String INVALID_PSWD = "Senha inválida!\n";
	public static final String USER_EXISTS = "O usuário já está cadastrado!\n";
	public static final String USER_NEXISTS = "O usuário não existe!\n";
	public static final String PERM_DENIED = "O usuário não tem permissão para executar esse comando!\n";
	public static final String REMOTE_ERR = "Erro no servidor remoto!\n";
	public static final String HOME_FAIL = "Ocorreu um erro ao criar a pasta 'home' do usuário!\n";
	public static final String DEL_FAIL = "Ocorreu um erro ao remover a pasta 'home' do usuário!\n";
	public static final String DBCONN_FAIL = "Ocorreu um erro ao conectar-se ao banco de dados!\n";
	
    // Sucesso
	public static final String LOGIN_SUCCESS = "Login efetuado com sucesso!\n";
	public static final String PSWD_SUCCESS = "Senha alterada com sucesso!\n";
	public static final String ADD_SUCCESS = "Usuário cadastrado com sucesso!\n";
	public static final String DEL_SUCCESS = "Usuário removido com sucesso!\n";
	public static final String ALLRIGHT = "Fuck you baby, let's go!\n";
}
