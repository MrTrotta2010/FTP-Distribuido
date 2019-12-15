package connectionserver;

import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface UserController extends Remote {
    
    public String login(String username, String password) throws RemoteException; 
    public String useradd(String username, String password, String currentUser) throws RemoteException; 
    public String userdel(String username, String password, String currentUser) throws RemoteException; 
    public String changepwd(String username, String password) throws RemoteException; 
    public String listusers(String username) throws RemoteException; 

}