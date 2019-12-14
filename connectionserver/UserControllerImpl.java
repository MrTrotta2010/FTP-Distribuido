package connectionserver;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.sql.*;

public class UserControllerImpl extends UnicastRemoteObject implements UserController{
	
	private static final long serialVersionUID = 1L;

	private Connection database;

	protected UserControllerImpl(Connection database) throws RemoteException {
		super();
		this.database = database;
	}

	private boolean isAdmin(String username) {
		
		String query = "SELECT * FROM ADMINS WHERE userName='"+username+"'";
		Statement statement;
		ResultSet results = null;

		try{
			statement = database.createStatement();
			results = statement.executeQuery(query);

			if (results.next() == false) {
				// O usuário não existe
				return false;
			}
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}

	private boolean checkAdminPswd(String username, String password) {
		
		String query = "SELECT * FROM ADMINS WHERE userName='"+username+"' AND passwd='"+password+"'";
		Statement statement;
		ResultSet results = null;

		try{
			statement = database.createStatement();
			results = statement.executeQuery(query);

			if (results.next() == false) {
				// O usuário não existe
				return false;
			}
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}

	@Override
	public String login(String username, String password) throws RemoteException{

		String query = "SELECT * FROM USERS WHERE userName='"+username+"'"+
						" UNION SELECT * FROM ADMINS WHERE userName='"+username+"'";
		Statement statement;
		ResultSet results = null;

		try{
			statement = database.createStatement();
			results = statement.executeQuery(query);

			if (results.next() == false) {
				// O usuário não existe
				return LoginMessage.INVALID_USER;
			}

			String pwd = results.getString("passwd");
			
			if (password.equals(pwd))
				return LoginMessage.LOGIN_SUCCESS;

			return LoginMessage.INVALID_PSWD;
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return LoginMessage.REMOTE_ERR;
	}

	@Override
	public String useradd(String username, String password, String currentUser) throws RemoteException{

		if (!isAdmin(currentUser))
			return LoginMessage.PERM_DENIED;
			
		try {
			database.setAutoCommit(false);
			String query = "INSERT INTO USERS VALUES ('"+username+"', '"+password+"')";
			PreparedStatement statement;
			
			try{
				statement = database.prepareStatement(query);
				statement.executeUpdate();
			}
			catch (SQLException e) {
				System.err.println(e.getMessage());
				database.setAutoCommit(true);
				return LoginMessage.USER_EXISTS;
			}
			
			//Cria o diretório
			try {
				FileController fileC = (FileController) Naming.lookup("//172.18.0.206:1099/FileService");
				if (fileC.initUserHome(username) != 0) {
					this.database.rollback();
					database.setAutoCommit(true);
					return LoginMessage.HOME_FAIL;
				}
			}
			catch (Exception e) {
				System.out.println("Falha ao tentar conectar-se ao servidor de arquivos!");
				this.database.rollback();
				database.setAutoCommit(true);
				return LoginMessage.REMOTE_ERR;
			}
			
			database.commit();
			database.setAutoCommit(true);
			return LoginMessage.ADD_SUCCESS;
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
			return LoginMessage.DBCONN_FAIL;
		}
	}
	
	@Override
	public String userdel(String username, String currentUser, String currentUserpswd) throws RemoteException{
		
		if (!isAdmin(currentUser))
			return LoginMessage.PERM_DENIED;
			
		if (!checkAdminPswd(currentUser, currentUserpswd))
			return LoginMessage.INVALID_PSWD;
		
			
		try {
			database.setAutoCommit(false);
			String query = "DELETE FROM Users WHERE userName='"+username+"'";
			PreparedStatement statement;
			int result;
			
			try{
				statement = database.prepareStatement(query);
				result = statement.executeUpdate();
			}
			catch (SQLException e) {
				database.rollback();
				database.setAutoCommit(true);
				System.err.println(e.getMessage());
				return LoginMessage.USER_NEXISTS;
			}
			if (result == 0) {
				database.rollback();
				database.setAutoCommit(true);
				return LoginMessage.USER_NEXISTS;
			}
			
			// Remove o diretório
			try {
				FileController fileC = (FileController) Naming.lookup("//172.18.0.206:1099/FileService");
				if (fileC.delUserHome(username) != 0) {
					this.database.rollback();
					database.setAutoCommit(true);
					return LoginMessage.DEL_FAIL;
				}
			} catch (Exception e) {
				System.out.println("Falha ao tentar conectar-se ao servidor de arquivos!");
				this.database.rollback();
				database.setAutoCommit(true);
				return LoginMessage.REMOTE_ERR;
			}
			
			database.commit();
			database.setAutoCommit(true);
			return LoginMessage.DEL_SUCCESS;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return LoginMessage.DBCONN_FAIL;
		}
	}

	@Override
	public String changepwd(String currentUser, String newPassword) throws RemoteException {
		
		final String query = "UPDATE Users SET passwd='"+newPassword+"' WHERE userName='"+currentUser+"'";
		PreparedStatement statement;

		try {
			statement = database.prepareStatement(query);
			statement.executeUpdate();
		} catch (final SQLException e) {
			System.err.println(e.getMessage());
			return LoginMessage.REMOTE_ERR;
		}
		
		return LoginMessage.PSWD_SUCCESS;
	}
	
	@Override
	public String listusers(String currentUser) throws RemoteException {
		
		if (!isAdmin(currentUser))
		return LoginMessage.PERM_DENIED;
		
		String query = "SELECT userName FROM USERS";
		Statement statement;
		ResultSet results = null;
		String returnMsg = "Usuários comuns#";
		
		try {
			statement = database.createStatement();
			results = statement.executeQuery(query);
			
			if (results.next())
				returnMsg += results.getString("userName");
			
			while (results.next())
				returnMsg += "::"+results.getString("userName");
				
			query = "SELECT userName FROM ADMINS";
			returnMsg += "#Administradores#";

			statement = database.createStatement();
			results = statement.executeQuery(query);
				
			if (results.next())
				returnMsg += results.getString("userName");
				
			while (results.next())
				returnMsg += "::"+results.getString("userName");
				
			return returnMsg;
			
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return LoginMessage.REMOTE_ERR;
		}
	}
}