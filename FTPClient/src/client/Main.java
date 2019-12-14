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
   
                    if (arguments[0].equalsIgnoreCase("put") || arguments[0].equalsIgnoreCase("mkdir") || arguments[0].equalsIgnoreCase("ls") || arguments[0].equalsIgnoreCase("rmdir")) {
                        input = arguments[0]+"::"+String.join(" ", Arrays.copyOfRange(arguments, 1, arguments.length))+"::";
                    } else {
                        input = String.join("::", arguments) + "::";
                    }
                    
                    if (arguments[0].equalsIgnoreCase("put")) {
                        System.out.println("-->"+input.split("::")[1]);
                        file = new Archive(input.split("::")[1]);
                        long size = file.getSize();
                        if (size < 0) {
                            System.out.println(">> O arquivo não existe!");
                            continue;
                        }
                        System.out.print("Digite o diretório de destino: ");
                        String dir = scan.nextLine();
                        input += dir+"::"+size+"::";
                    } 

                    //Enviando msg para o servidor
                    writeBuffer.write((input+"\n").getBytes());
                    
                    if (arguments[0].equalsIgnoreCase("put")) file.send(clientSocket);

                    //Resposta do Servidor
                    String serverMessage = readBuffer.readLine();
                    if (serverMessage == null) throw new IOException();
                    
                    // Formata a resposta do servidor
                    printServerMessage(serverMessage, input);
                }
            }
            clientSocket.close();
         
        } catch(IOException io){
            System.err.println("Falha ao se conectar ao servidor!");
            System.exit(0);
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
                    System.out.print("\n\tUsuário: "+pair[0]+" - porta: "+pair[1]);
                }
                System.out.print("\n");
            }
        } else {
            System.out.println(">> "+serverMessage);
        }
    }
}
