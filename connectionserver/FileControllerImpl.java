package connectionserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Arrays;

public class FileControllerImpl extends UnicastRemoteObject implements FileController{
	
	private static final long serialVersionUID = 1L;

	protected FileControllerImpl() throws RemoteException {
		super();
	}

	public int initUserHome(String user) throws RemoteException {
		try {
			File dir = new File("Homes/"+user);
			Files.createDirectory(dir.toPath());
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	@Override
	public String mkdir(String directory) throws RemoteException {
		try {
			File dir = new File(directory);
			
			if (dir.exists())
				return FileMessage.DIR_EXISTS;

			Files.createDirectories(dir.toPath());
		}
		catch (Exception e) {
			e.printStackTrace();
			return FileMessage.MKDIR_FAIL;
		}
		return FileMessage.MKDIR_SUCCESS;
	}
	
	@Override
	public String rmdir(String directory) throws RemoteException {
		try {
			File dir = new File(directory);
			
			if (!dir.exists())
				return FileMessage.DIR_NEXISTS;

			if (!dir.delete())
				return FileMessage.RMDIR_FAIL;
		}
		catch (Exception e) {
			e.printStackTrace();
			return FileMessage.RMDIR_FAIL;
		}
		return FileMessage.RMDIR_SUCCESS;
	}

	@Override
	public String ls(String directory) throws RemoteException {
		
		File file = new File(directory);
		
		if (!file.exists())
			return FileMessage.DIR_NEXISTS;

		if (file.isFile())
			return (directory + FileMessage.NOT_DIR);

		File listFile[] = file.listFiles();
		String directories = "";

		for (File fl : listFile) {
			directories += fl.getName()+"::";
		}

		return directories;
	}

	@Override
	public String cd(String directory) throws RemoteException {
		
		String[] dirs = directory.split("/");

		if (dirs[dirs.length-1].equals("..")) {
			directory = String.join("/", Arrays.copyOfRange(dirs, 0, dirs.length-2));
			System.out.println(directory);
		}

		File file = new File(directory);
		
		if (!file.exists())
			return FileMessage.DIR_NEXISTS;
			
		if (file.isFile())
			return (directory+FileMessage.NOT_DIR);
			
		return directory;
	}

	@Override
	public String put(String filePath, byte[] buffer, int bufferLength, boolean append) throws RemoteException {
		try {
			File f = new File(filePath);
			if (!append) f.createNewFile();

			FileOutputStream fileOut = new FileOutputStream(filePath, append);
			fileOut.write(buffer, 0, bufferLength);;
			fileOut.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return FileMessage.WRITE_FAIL;
		}
		return FileMessage.ALLRIGHT;
	}

	@Override
	public boolean direxists(String path) throws RemoteException {
		if ((new File(path)).exists())
			return true;
		return false;
	}
}