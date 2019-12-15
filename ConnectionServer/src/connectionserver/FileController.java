package connectionserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileController extends Remote{
 
    public String mkdir(String dir) throws RemoteException; 
    public String ls(String dir) throws RemoteException; 
    public String cd(String dir) throws RemoteException; 
    public String rmdir(String dir) throws RemoteException; 
    public boolean direxists(String dir) throws RemoteException; 
    public String put(String dir, byte[] buffer, int bytesRead, boolean append) throws RemoteException; 
    public byte[] get(String dir, long offset, int bufferLength) throws RemoteException; 
    public long getfsize(String dir) throws RemoteException; 
}
