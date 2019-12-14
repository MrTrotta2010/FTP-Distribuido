package connectionserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileController extends Remote{

	public int initUserHome(String user) throws RemoteException;
	public String mkdir(String directory) throws RemoteException;
	public String rmdir(String directory) throws RemoteException;
	public String ls(String directory) throws RemoteException;
	public String cd(String directory) throws RemoteException;
	public String put(String filePath, byte[] buffer, int bufferLength, boolean append) throws RemoteException;
	public boolean direxists(String path) throws RemoteException;
}