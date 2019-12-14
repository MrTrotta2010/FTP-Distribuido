package connectionserver;

public class FileMessage{
	
	// Erros
	public static final String PERM_DENIED = "O usuário não tem permissão para executar esse comando!\n";
	public static final String REMOTE_ERR = "Erro no servidor remoto!\n";
	public static final String DIR_EXISTS = "O diretório já existe!\n";
	public static final String DIR_NEXISTS = "O diretório não existe!\n";
	public static final String MKDIR_FAIL = "Falha ao criar o diretório!\n";
	public static final String RMDIR_FAIL = "Falha ao remover o diretório!\n";
	public static final String NOT_DIR = " não é um diretório!\n";
	public static final String WRITE_FAIL = "Falha na escrita do arquivo!\n";
	
    // Sucesso
	public static final String MKDIR_SUCCESS = "Diretório criado com sucesso!\n";
	public static final String RMDIR_SUCCESS = "Diretório removido com sucesso!\n";
	public static final String ALLRIGHT = "Fuck you baby, let's go!\n";
}
