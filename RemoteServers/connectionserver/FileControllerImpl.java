package connectionserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Comparator;

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

	private void deleteDirectoryStream(Path path) throws IOException {
		Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}
	
	public int delUserHome(String user) throws RemoteException {
		try {
//			File dir = new File("Homes/"+user);
			deleteDirectoryStream(Paths.get("Homes/"+user));
			// File[] allContents = dir.listFiles();
			// if (allContents != null) {
			// 	for (File file : allContents) {
			// 		deleteDirectory(file);
			// 	}
			// }
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
			fileOut.write(buffer, 0, bufferLength);
			fileOut.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return FileMessage.WRITE_FAIL;
		}
		return FileMessage.ALLRIGHT;
	}

	@Override
	public byte[] get(String filePath, long offset, int bufferLength) throws RemoteException {
		byte[] buffer = new byte[bufferLength];
		try {
			RandomAccessFile file = new RandomAccessFile(filePath, "r");
	
			file.seek(offset);
			file.readFully(buffer);
			file.close();
	
		} catch (Exception e) {
			e.printStackTrace();
			buffer = null;
		}
		return buffer;
	}

	@Override
	public boolean direxists(String path) throws RemoteException {
		if ((new File(path)).exists())
			return true;
		return false;
	}

	@Override
	public long getfsize(String path) throws RemoteException {
		System.out.println(path);
		File file = new File(path);
		if (!file.exists())
			return -1;
		return file.length();
	}
}