package connectionserver;

import java.rmi.Naming;
import java.sql.*;

public class UserServer{
	
	UserServer(){
		
		Connection database = null; 

		try {
			Class.forName("org.sqlite.JDBC");
			database = DriverManager.getConnection("jdbc:sqlite:UserServer.db");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Deu erro");
			e.printStackTrace();
			System.exit(1);
        }

		try{
			UserController c = new UserControllerImpl(database);
			Naming.rebind("rmi://172.18.0.206:1099/UserService", c);

		}catch(Exception e){
			System.err.println(e.getMessage());
			System.exit(1);
		}

		System.out.println("Servidor online, baby!");
	}
	public static void main(String args[]){
		new UserServer();
	}
}