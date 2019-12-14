package client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Archive {

    private File file;
    private static final int sizeBuffer = 8192;
    private String dir;
    
    public Archive (String dir){
        this.dir = dir;
        file = new File(this.dir);
    }
    
    public boolean exists() {
        return file.exists();
    }

    public long getSize() {
        if(file.exists()) return file.length();
        return -1;
    }
    public String getDir() {
        return dir;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }
    
    public void send(Socket connectionSocket){
        
        try {
            char p = '%';
            int currentBuffer;
            long currentSize = file.length();
            long sendBytes = 0;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataOutputStream send = new DataOutputStream(connectionSocket.getOutputStream());

            while(currentSize > 0){
                if (currentSize < sizeBuffer) currentBuffer = (int) currentSize;
                else currentBuffer = sizeBuffer;
                
                byte [] buffer  = new byte [currentBuffer];
                bis.read(buffer, 0, buffer.length);
                sendBytes += currentBuffer;
                double percent = ((double)(sendBytes)/(double)file.length())*100;
                System.out.printf("Uploading: %.2f%c\r",percent, p);
                send.write(buffer);
                currentSize -= buffer.length;
            }
            System.out.println("");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void receive(Socket connectionSocket, String nome, long size){

        int read = 0;
        char p = '%';
        byte [] vetorBytes  = new byte [sizeBuffer];
        long totalSize = size;
        long bytesRead = 0, sendBytes = 0;
        try {
            System.out.println("Salvando arquivo: "+nome);
            FileOutputStream fos = new FileOutputStream(nome);
            InputStream in = connectionSocket.getInputStream();
            while (size > 0 && (read = in.read(vetorBytes, 0, (int)Math.min(vetorBytes.length, size))) != -1){   
                fos.write(vetorBytes, 0, read);   
                size -= read;
                bytesRead += read;
                sendBytes = bytesRead;
                double percent = ((double)bytesRead/(double)totalSize)*100;
                System.out.printf("Downloading: %.2f%c\r", percent, p);
            }
            fos.close();
        } catch (IOException ex) {
            System.err.println("\n>> Falha ao enviar arquivo!");
        }
        System.out.println("\n>> Arquivo baixado com sucesso!");
    }
}
