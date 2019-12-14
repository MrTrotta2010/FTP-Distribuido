package connectionserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserController extends Remote{

    //======= ANY USER =======//
	public String login(String username, String password) throws RemoteException;
	public String changepwd(String currentUser, String newPassword) throws RemoteException;
    
    //======= ROOT ONLY =======//
    public String useradd(String username, String password, String currentUser) throws RemoteException;
	public String userdel(String username, String currentUser, String currentUserpswd) throws RemoteException;
	public String listusers(String currentUser) throws RemoteException;
}