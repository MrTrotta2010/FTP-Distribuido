package connectionserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FTC {

    private static final int sizeBuffer = 8192;
    private String dir;
    private long size;
    
    public FTC (String dir, long size){
        this.dir = dir;
        this.size = size;
    }

    public String getDir() {
        return dir;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }
    
    public boolean sendRemote(Socket connectionSocket, FileController fileC){
        
        try {
            char p = '%';
            int currentBuffer, read;
            long currentSize = size;
            long sendBytes = 0, bytesRead = 0;
            byte [] vetorBytes  = new byte [sizeBuffer];
            InputStream in = connectionSocket.getInputStream();
            
            System.out.println("CurrentSize: "+currentSize);
            while (currentSize > 0 && (read = in.read(vetorBytes, 0, (int)Math.min(vetorBytes.length, currentSize))) != -1){   
                String resp = "";
                if(sendBytes == 0) resp = fileC.put(dir, vetorBytes, read, false);
                else resp = fileC.put(dir, vetorBytes, read, true);
                
                System.out.println("Resp: "+resp);
                currentSize -= read;
                System.out.println("currentsize: "+currentSize);
                bytesRead += read;
                sendBytes = bytesRead;
                double percent = ((double)bytesRead/(double)size)*100;
                System.out.printf("Uploading: %.2f%c\r", percent, p);
            }

        } catch (IOException e) {
            System.err.println("Falha ao enviar o arquivo"+e.getMessage());
            return false;
        }

        System.out.println("Arquivo enviado com sucesso!");
        return true;
    }
    
    public boolean getRemote(Socket connectionSocket, FileController fileC, DataOutputStream writeCl){
        
        long offset = 0;
        long currentBuffer = sizeBuffer;
        byte [] vetorBytes;
        while(size > 0){
            
            if(size < sizeBuffer) currentBuffer = size;
            try {
               vetorBytes = fileC.get(dir, offset, (int)currentBuffer);
               if(vetorBytes == null) {
                   System.out.println("Vetor de bytes nulo");
                   return false;
               }
               offset += currentBuffer;
               size -= currentBuffer;
                try {
                    writeCl.write(vetorBytes);
                } catch (IOException ex) {
                    System.out.println("Falha ao enviar o arquivo para o cliente!");
                    return false;
                }
            } catch (RemoteException ex) {
                System.out.println("Falha: "+ex.getMessage());
                return false;
            }
        }
        return true;
    }
}
