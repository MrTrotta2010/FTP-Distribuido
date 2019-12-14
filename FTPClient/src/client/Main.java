package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main (String[] args) {
        
        boolean login = false;
        String serverIP = "172.18.5.62";
        int connectionPort = 6790;
        
        Scanner scan = new Scanner(System.in);
        Socket clientSocket;
        
        DataOutputStream writeBuffer;
        BufferedReader readBuffer;
        
        AES aes = new AES();

        try {
            //Faz a conexão com o servidor
            clientSocket = new Socket(serverIP, connectionPort);
            
            System.out.println("Bem vindo ao fucking FTP!");
            System.out.println("Conectado ao servidor de IP "+serverIP+" na porta "+connectionPort);
            
            while(true != false) {

                writeBuffer = new DataOutputStream(clientSocket.getOutputStream());
                readBuffer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                System.out.print(serverIP+" $ ");
                String input = scan.nextLine();

                String[] arguments = input.split(" ");
                Archive file = null;
                
                if (arguments[0].equalsIgnoreCase("exit")) break;
                else {
   
                    if (arguments[0].equalsIgnoreCase("get") || arguments[0].equalsIgnoreCase("put") || arguments[0].equalsIgnoreCase("mkdir") || arguments[0].equalsIgnoreCase("ls") || arguments[0].equalsIgnoreCase("rmdir")) {
                        input = arguments[0]+"::"+String.join(" ", Arrays.copyOfRange(arguments, 1, arguments.length))+"::";
                    } else {
                        input = String.join("::", arguments) + "::";
                    }
                    
                    if (arguments[0].equalsIgnoreCase("put")) {
                        file = new Archive(input.split("::")[1]);
                        long size = file.getSize();
                        if (size < 0) {
                            System.out.println(">> O arquivo não existe!");
                            continue;
                        }
                        System.out.print("<< Digite o diretório de destino: ");
                        String dir = scan.nextLine();
                        input += dir+"::"+size+"::";
                    } 

                    //Enviando msg para o servidor
                    sendMessage(input, writeBuffer, aes);

                    //Resposta do Servidor
                    String serverMessage = receiveMessage(readBuffer, aes);
                    if (serverMessage == null) throw new IOException();
                    
                    if (arguments[0].equalsIgnoreCase("put") && !serverMessage.contains("não existe")) {
                        file.send(clientSocket);
                        serverMessage = receiveMessage(readBuffer, aes);
                        if (serverMessage == null) throw new IOException();
                    }
                    else if (arguments[0].equalsIgnoreCase("get")) {
                        long size = Long.parseLong(serverMessage);
                        if (size < 0) {
                            System.out.println(">> O arquivo não existe!");
                            sendMessage("Not allright\n", writeBuffer, aes);
                            continue;
                        }
                        System.out.print(">> Digite o diretório de destino: ");
                        String dir = scan.nextLine();
                        file = new Archive(dir);
                        if (!file.exists()) {
                            System.out.println("<< O diretório não existe!");
                            sendMessage("Not allright\n", writeBuffer, aes);
                            continue;
                        }
                        sendMessage("Fuck you baby, let's go!\n", writeBuffer, aes);
                        dir += input.split("/")[input.split("/").length-1].split("::")[0];
                        file.receive(clientSocket, dir, size);
                        continue;
                    } 
                    
                    // Formata a resposta do servidor
                    printServerMessage(serverMessage, input);
                }
            }
            clientSocket.close();
         
        } catch(IOException io){
            System.err.println("Falha ao se conectar ao servidor!");
        }
        catch (Exception e) {
            System.err.println("Falha de criptografia!");
            e.printStackTrace();
        }
    }

    private static void printServerMessage(String serverMessage, String input) {
        if (serverMessage.contains("#")) {
            String[] messageArray = serverMessage.split("#");

            if (input.equals("listusers::")) { //listusers
                System.out.print(">> "+messageArray[0]+":\n\t");
                System.out.print(String.join("\n\t", messageArray[1].split("::")));
                System.out.print("\n>> "+messageArray[2]+":\n\t");
                System.out.print(String.join("\n\t", messageArray[3].split("::")));
                System.out.print("\n");
            } else {
                System.out.print(">> Usuários online:");
                for (String message : messageArray) {
                    String[] pair = message.split("::");
                    if (pair.length == 2)
                        System.out.print("\n\tUsuário: "+pair[0]+" - porta: "+pair[1]);
                }
                System.out.print("\n");
            }
        }
        else if (input.charAt(0) == 'l' && input.charAt(1) == 's'){
            String[] messageArray = serverMessage.split("::");
            for (String message : messageArray) {
                System.out.print("\t"+message+"\n");
            }
        }else {
            System.out.println(">> "+serverMessage);
        }
    }
    
    private static void sendMessage(String message, DataOutputStream writeBuffer, AES aes) throws Exception {
        String encrypted = aes.Encrypt(message)+"\n";
        writeBuffer.write(encrypted.getBytes());
    }
    
    private static String receiveMessage(BufferedReader readBuffer, AES aes) throws Exception {
        String message = readBuffer.readLine();
        message = aes.Decrypt(message);
        return message;
    }
}
