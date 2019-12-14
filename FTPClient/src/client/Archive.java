package client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class Archive {

    private File file;
    private static final int sizeBuffer = 8192;
    private String dir;
    
    public Archive (String dir){
        this.dir = dir;
        file = new File(this.dir);
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
}
