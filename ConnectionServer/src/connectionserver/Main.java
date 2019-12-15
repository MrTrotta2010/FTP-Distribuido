package connectionserver;

import java.io.IOException;
import java.rmi.NotBoundException;

public class Main {
    
    public static void main(String []args) throws NotBoundException{
        
        int port = 6790;
        String remoteServerIP = "172.18.0.206";
        int remoteServerPort = 1099;
        
        try {
            System.out.println("Iniciando servidor na porta: "+port);
            ConnectionServer connectionServer = new ConnectionServer(port, remoteServerIP, remoteServerPort);
            System.out.println("Servidor de conexão online!");
            connectionServer.init();
        } catch (IOException ex) {
            System.out.println("Erro ao iniciar ConnectionServer: "+ex.getMessage());
        }
        
    }
}
