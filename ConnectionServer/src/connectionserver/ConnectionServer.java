package connectionserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionServer{
    
    private final ServerSocket socket;
    private static ArrayList<Users> connectedUsers;
    private UserController userC;
    private FileController fileC;
    private final String remoteServerIP;
    private final int remoteServerPort;
    private int port;
    private int numberConnections;

    public ConnectionServer(int port, String remoteServerIP, int remoteServerPort) throws IOException{
        this.port = port;
        this.remoteServerIP = remoteServerIP;
        this.remoteServerPort = remoteServerPort;
        this.socket = new ServerSocket(port);
        this.numberConnections = 0;
        ConnectionServer.connectedUsers = new ArrayList<>();
        try {
            this.userC =  (UserController) Naming.lookup("//"+remoteServerIP+":"+remoteServerPort+"/UserService");
            this.fileC =  (FileController) Naming.lookup("//"+remoteServerIP+":"+remoteServerPort+"/FileService");
        } catch (MalformedURLException | NotBoundException | RemoteException ex) {
            System.err.println("Falha ao tentar conectar com o servidor UserController: "+ex.getMessage());
            System.exit(0);
        }
    }
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public void init() throws IOException{

        while(true){
            try {
                System.out.println("\nNumero de usuários conectados: "+numberConnections);
                Socket connectionSocket = socket.accept();
                System.out.println("\nAbrindo nova conexão");
                openConnection(connectionSocket);
                numberConnections += 1;
            } catch (IOException ex) {
                System.out.println("Falha ao aceitar conexão: \n"+ex.getMessage());
            }
        }
    }
    
    public String cmdServer(String cmd, Socket connectionSocket, BufferedReader readCl, DataOutputStream writeCl, AES aes) throws IOException, Exception{
        
        String argumentos[] = cmd.split("::");
        cmd = argumentos[0].toLowerCase();
        
        System.out.println("\nComando: "+cmd);
        String saida;
        
        switch(cmd){
            case "login": saida = login(argumentos, connectionSocket); break;
            case "useradd": saida = useradd(argumentos, connectionSocket); break;
            case "userdel": saida = userdel(argumentos, connectionSocket); break;
            case "listonline": saida = usersConnecteds(connectionSocket); break;
            case "changepwd": saida = changepwd(argumentos, connectionSocket); break;
            case "listusers": saida = listusers(argumentos, connectionSocket); break;
            case "put": saida = put(argumentos, connectionSocket, readCl, writeCl, aes); break;
            case "get": saida = get(argumentos, connectionSocket, readCl, writeCl, aes); break;
            case "cd": saida = cd(argumentos, connectionSocket); break;
            case "ls": saida = ls(argumentos, connectionSocket); break;
            case "mkdir": saida = mkdir(argumentos, connectionSocket); break;
            case "help": saida = help(); break;
            default: saida = "Comando Invalido!";
        }
        
        return saida + "\n";
    }
    
    public void openConnection(Socket connectionSocket) throws IOException{
        
        new Thread(){@Override public void run(){
            
            String msgCl, msgSv;
            BufferedReader readCl = null;
            DataOutputStream writeCl = null;
            AES aes = new AES();
            while(true){
                try{
                    readCl = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    writeCl = new DataOutputStream(connectionSocket.getOutputStream());

                    msgCl = readCl.readLine();
                    System.out.println("Mensagem recebida: " + msgCl);
                    try {
                        msgCl = aes.Decrypt(msgCl);
                        System.out.println("Mensagem recebida descriptografada: " + msgCl);
                    } catch (Exception ex) {
//                        ex.printStackTrace();
                    }
                    if(msgCl == null) break;

                    try {
                        msgSv = cmdServer(msgCl, connectionSocket, readCl, writeCl, aes);
                    } catch (Exception ex) {
                        System.out.println("Falha ao descriptografar mensagem!!"+ex.getMessage());
                        continue;
                    }
                    if(msgSv.contains("NONE")) continue;
                    
                    try {
                        writeCl.write((aes.Encrypt(msgSv)+"\n").getBytes());
                    } catch (Exception ex) {
                        System.out.println("Falha ao encriptografar mensagem!!"+ex.getMessage());
                    }
                
                }catch(IOException io){
                    System.out.println("Falha na conexao!");
                    break;
                }
           }
            try {
                System.out.println("Cliente desconectado: "+getCurrentUser(connectionSocket).getName());
                desconnectUser(connectionSocket);
                connectionSocket.close();
                readCl.close(); writeCl.close();
                numberConnections -= 1;
            } catch (IOException io) {
                desconnectUser(connectionSocket);
                System.err.println("Falha ao fechar o socket: \n"+io.getMessage());
                numberConnections -= 1;
            }}
        }.start();
    }
    
    public Users getCurrentUser(Socket connectionSocket){
        
        for(Users user: connectedUsers){
            if(user.getPort() == connectionSocket.getPort()) return user;
        }
        return null;
    }
    
    public String login(String[] args, Socket connectionSocket){
        
        if(args.length != 3) return Message.INVALID_ARGS;
        String login = args[1];
        String password = args[2];
//        return getMD5(password);
        System.out.println("Login: "+login+" - Senha: "+password);
        try {
            String resp = userC.login(login, getMD5(password));
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.INVALID_LOGIN)) return Message.INVALID_LOGIN;
            else if(resp.equals(Message.INVALID_PSWD)) return Message.INVALID_PSWD;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.LOGIN_FAIL;
        }

        connectedUsers.add(new Users(login, connectionSocket.getPort()));
        return Message.LOGIN_SUCCESS;
    }
    
    public String useradd(String[] args, Socket connectionSocket){
        
        if(args.length != 3) return Message.INVALID_ARGS;
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        String login = args[1];
        String password = args[2];
        System.out.println("useradd: "+login+" - Senha: "+password);
        try {
            String currentUser = getCurrentUser(connectionSocket).getName();
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser);
            String resp = userC.useradd(login, getMD5(password), currentUser);
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.USER_EXISTS)) return Message.USER_EXISTS;
            else if(resp.equals(Message.PERM_DENIED)) return Message.PERM_DENIED;
            
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.ADDUSER_FAIL;
        }
        return Message.ADDUSER_SUCCESS;
    }

    public String userdel(String[] args, Socket connectionSocket){
        
        if(args.length != 3) return Message.INVALID_ARGS;
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        String login = args[1];
        String password = args[2];
        System.out.println("userdel: "+login+" - Senha: "+password);
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            String resp = userC.userdel(login, currentUser.getName(), getMD5(password));
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.USER_NOTEXISTS)) return Message.USER_NOTEXISTS;
            else if(resp.equals(Message.PERM_DENIED)) return Message.PERM_DENIED;
            else if(resp.equals(Message.INVALID_PSWD)) return Message.INVALID_PSWD;
            
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.RMUSER_FAIL;
        }
        return Message.RMUSER_SUCCESS;
    }
    
    public boolean isLogged(Socket connectionSocket){
        for(Users user: connectedUsers){
            if(user.getPort() == connectionSocket.getPort()) return true;
        }
        return false;
    }
    
    public String changepwd(String[] args, Socket connectionSocket){
        
        if(args.length != 2) return Message.INVALID_ARGS;
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        String newpassword = args[1];
        System.out.println("changepwd: new password: "+newpassword);
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            String resp = userC.changepwd(currentUser.getName(), getMD5(newpassword));
            System.out.println("Resposta UserController: "+resp);
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.RMUSER_FAIL;
        }
        return Message.CHANGE_SUCCESS;
    }
    
    public String listusers(String[] args, Socket connectionSocket){
        
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        if(args.length != 1) return Message.INVALID_ARGS;
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            String resp = userC.listusers(currentUser.getName());
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.PERM_DENIED)) return Message.PERM_DENIED;
            return resp;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.NONE;
        }
    }
    
    public static String help(){
        
        String comandos = "useradd nomeUser senhaUser"+"::"+
                          "userdel nomeUser senhaUser"+"::"+
                          "login nomeUser senhaUser"+"::"+
                          "mkdir nomeArquivo"+"::"+
                          "rmdir nomeArquivo"+"::"+
                          "cd nomeArquivo"+"::"+
                          "put nomeArquivo"+"::"+
                          "put nomeArquivo"+"::"+
                          "get nomeArquivo"+"::"+
                          "ls diretorio";
        return comandos;
    }
    
    public String mkdir(String[] args, Socket connectionSocket){
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        if(args.length == 1) return Message.INVALID_ARGS;
        String dir = args[1];
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            String resp = fileC.mkdir(currentUser.getWorkDir()+dir);
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.DIR_EXISTS)) return Message.DIR_EXISTS;
            else if(resp.equals(Message.CREATE_FAIL)) return Message.CREATE_FAIL;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.NONE;
        }
        return Message.MKDIR_SUCCESS;
    }
    
    public String ls(String[] args, Socket connectionSocket){
        
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        String dir = "";
        if(args.length != 1) dir = args[1];
        
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            String resp = fileC.ls(currentUser.getWorkDir()+dir);
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.DIR_NOTEXISTS)) return Message.DIR_NOTEXISTS;
            if(resp.contains(Message.NOT_DIR)) return resp;
            return resp;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.NONE;
        }
    }

    public void updateDir(String workDir, Socket connectionSocket){
        
        for(int i = 0; i < connectedUsers.size(); i++){
            if(connectedUsers.get(i).getPort() == connectionSocket.getPort()){
                connectedUsers.get(i).setWorkDir(workDir);
                break;
            }
        }
    }
    
    public String cd(String[] args, Socket connectionSocket){
        
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        if(args.length != 2) return Message.INVALID_ARGS;
        String dir = args[1];
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            String resp = fileC.cd(currentUser.getWorkDir()+dir);
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.DIR_NOTEXISTS)) return Message.DIR_NOTEXISTS;
            else if(resp.contains(Message.NOT_DIR)) return resp;
            updateDir(resp+"/", connectionSocket);
            return resp;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.NONE;
        }
    }
    
    public String rmdir(String[] args, Socket connectionSocket){
        
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        if(args.length != 2) return Message.INVALID_ARGS;
        String dir = args[1];
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            String resp = fileC.rmdir(currentUser.getWorkDir()+dir);
            System.out.println("Resposta UserController: "+resp);
            if(resp.equals(Message.DIR_NOTEXISTS)) return Message.DIR_NOTEXISTS;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.NONE;
        }
        return Message.RMDIR_SUCCESS;
    }
    
    public String put(String[] args, Socket connectionSocket, BufferedReader readCl, DataOutputStream writeCl, AES aes) throws Exception{
        
        if(args.length != 4) return Message.INVALID_ARGS;
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        String archive = args[1].split("/")[args[1].split("/").length-1];
        String dir = args[2];
        long size = Long.parseLong(args[3]);
        
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            if (!fileC.direxists(currentUser.getWorkDir()+dir)) return Message.DIR_NOTEXISTS;
            try {
                writeCl.write((aes.Encrypt(Message.ALLRIGTH)+"\n").getBytes());
            } catch (IOException ex) {
                System.out.println("Falha ao enviar mensagem ao cliente!");
                return "";
            }
            if(!new FTC("Homes/"+currentUser.getName()+"/"+dir+"/"+archive, size).sendRemote(connectionSocket, fileC)) return Message.SEND_FAIL;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.NONE;
        }
        return Message.SEND_SUCCESS;
    }
    
    public String get(String[] args, Socket connectionSocket, BufferedReader readCl, DataOutputStream writeCl, AES aes) throws Exception{
        
        if(args.length != 2) return Message.INVALID_ARGS;
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        String archive = args[1];
        
        try {
            Users currentUser = getCurrentUser(connectionSocket);
            if(currentUser == null) return Message.NOT_LOGGED;
            System.out.println("Current user: "+currentUser.getName());
            long size = fileC.getfsize(currentUser.getWorkDir()+archive);
            System.out.println("Size: "+size);
            try {
                String resp2 = size+"";
                writeCl.write((aes.Encrypt(resp2)+"\n").getBytes());
                resp2  = readCl.readLine();
                resp2 = aes.Decrypt(resp2);
                System.out.println("Iniciando download..."+resp2);
                
                if(!resp2.contains("Fuck")) return Message.NONE;
                
            } catch (IOException ex) {
                System.out.println("Falha ao enviar mensagem ao cliente!");
                return "";
            }
            if(!new FTC(currentUser.getWorkDir()+archive, size).getRemote(connectionSocket, fileC, writeCl)) return Message.GET_FAIL;
        } catch (RemoteException ex) {
            System.err.println("Falha conexão remota: \n"+ex.getMessage());
            return Message.NONE;
        }
        System.out.println("Arquivo enviado com sucesso!");
        return Message.NONE;
    
    }
    
    public void closeConnection(){}
    
    public String usersConnecteds(Socket connectionSocket){
        
        if(!isLogged(connectionSocket)) return Message.NOT_LOGGED;
        String users = "";
        for(Users user: connectedUsers) users += user.getName()+"::"+user.getPort()+"::#";
        System.out.println("Usuários conectados: "+users);
        return users;
    }
    
    public boolean desconnectUser(Socket connectionSocket){
        
        for(Users user: connectedUsers){
            if(user.getPort() == connectionSocket.getPort()){
                connectedUsers.remove(user);
                return true;
            }
        }
        return false;
    }
    
    public static String getMD5(String senha){
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(senha);
    }
}
