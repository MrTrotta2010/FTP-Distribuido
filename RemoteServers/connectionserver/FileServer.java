package connectionserver;

import java.rmi.Naming;

public class FileServer{
	
	FileServer(){
		
		try{
			FileController c = new FileControllerImpl();
			Naming.rebind("rmi://172.18.0.206:1099/FileService", c);

		}catch(Exception e){
			System.err.println(e.getMessage());
			System.exit(1);
		}

		System.out.println("Allright, fuck you baby!");
	}
	public static void main(String args[]){
		new FileServer();
	}
}