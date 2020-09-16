import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;

class ServerPath{
	ServerPath previous= null;
	ServerPath next=null;
	ServerPath first;
	String name;
	
	public ServerPath(String name) {
		this.name = name;
		first = this;
	}
	public ServerPath(String name,ServerPath previous) {
		this.name = name;
		previous.next = this;
		this.previous=previous;
		this.first = previous.first;
	}
	
	private String path() {
		if(this.next==null) {
			return this.name+">";
		}
		return this.name+"\\"+this.next.path();
	}
	
	public String getPath() {
		return this.first.path();
	}
	
	public ServerPath del() {
		this.previous.next = null;
		return this.previous;
	}
	
	public ServerPath add(String name) {
		return new ServerPath(name,this);
	}
	
	
}

//la classe Client represente l application client
public class Client {
	private static Socket socket;
	/*Application Client
	 * */

	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		
		// declaration des variables Adresses et port du serveur
		String serverAdress;
		int port;
		
		System.out.println("Veuillez entrer l'adresse IP du serveur :");
		serverAdress = sc.nextLine();
		//serverAdress = "127.0.0.1";

		while(!IPvalid(serverAdress)) {
			System.out.println("IP invalide! \nVeuillez entrer une adresse IP valide :");
			serverAdress = sc.nextLine();
		}
		
		
		System.out.println("Veuillez entrer un numéro de port compris entre 5000 et 5050 :");
		try {
			port = sc.nextInt();
		}catch(Exception e){
			port=0; //vous avez saisi un numéro de port invalide
		}// Au cas ou le numéro de port saisi n'est pas un int
		
		sc.nextLine();
		
		
		/*Boucle permettant de verifier le numero de port rentre par le client */
		
		while(port<= 5000 || port>= 5050) {
			System.out.println("Port invalide! \nVeuillez entrer un numéro de port compris entre 5000 et 5050 :");
			try {
				port = sc.nextInt();
			}catch(Exception e){
				port=0;
			}
			sc.nextLine();	
		}
		
		
		/*Creation d une nouvelle connexion avec le serveur*/
		socket=new Socket(serverAdress, port);
		System.out.format("Le Serveur est connecté avec les informations suivantes:  %s:%d%n", serverAdress, port);
		
		/*creation d un canal entrant pour recevoir les messages envoyes par le serveur sur le canal*/	
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		
		
		/*Attente de la reception d'un message envoye par le serveur sur le canal*/

		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		String commande ="";    //la variable commande recupere la commande saisi par le client
		String nomRep;         //la variable nomRep recupere le nom du repertoire saisi par le client
		String response;      //la variable response recupere la reponse envoye par le serveur au client
		String fileName;     //la variable fileName recupere le nom du fichier a televerser ou a telecharger
		byte[] uploadFile;  // la variable uploadFile est un tableau de bytes qui sauvegarde les bytes des fichiers a televerser  
		int n;
		int i;
		byte[] fileBytes;   //la variable fileBytes est un tableau de bytes qui sauvegarde les bytes des fichiers a telecharger
		String error;      //la variable error recupere la reponse envoye par le serveur au client en cas d erreur 
		
		ServerPath currentPath = new ServerPath("serveur:");
		
		/*La boucle ci dessous permet d executer au niveau du client les differentes commandes cd,ls,mkdir,upload, download et exit, en general il verifie si les commandes rentres sont correctes et il envoit les commandes rentres au serveur pour traitement*/
		
		while(!commande.equals("exit")) {
			System.out.print(currentPath.getPath());
			commande = sc.next();
			switch(commande) {
			
			case "cd":
				nomRep=sc.next();
				sc.nextLine();	
				out.writeUTF("cd");
				out.writeUTF(nomRep);
				
				response =in.readUTF();
				if(nomRep.equals("..")){
					if(response.equals("|")) {
						System.out.println("Vous êtes à la racine");
					}
					else {
						currentPath = currentPath.del();
					}
				}
				else {
					if(response.equals("|")) {
						System.out.println("Pas de dossier correspondant");
					}
					else{
						currentPath = currentPath.add(nomRep);
						System.out.println(response);
					}
				}
				break;
				
			case "cd..":
				out.writeUTF("cd");
				out.writeUTF("..");
				
				response =in.readUTF();
				if(response.equals("|")) {
					System.out.println("Vous êtes à la racine");
				}
				else {
					currentPath = currentPath.del();
				}
				break;
				
			case "ls":
				sc.nextLine();
				out.writeUTF("ls");

				
				response=in.readUTF();
				while(!response.equals("|")) {
					System.out.println(response);
					response=in.readUTF();
				}
				
				break;
			
			case "mkdir":
				out.writeUTF("mkdir");
				String nouveauRep=sc.next();
				out.writeUTF(nouveauRep);
				
				response =in.readUTF();
				System.out.println(response);//utilisé pour l'affichage
				break;
				
			case "upload":
				fileName = sc.next();
				sc.nextLine();
				
				
				try {
					uploadFile = Files.readAllBytes(Paths.get(fileName));
					out.writeUTF("upload"); //exceptionnellement je mets ça à ce niveau là comme ça si le fichier n'existe pas il n'y pas de commandes envoyés
					out.writeUTF(fileName);
					n= uploadFile.length;
					out.writeInt(n);
					out.write(uploadFile, 0,n);
					response=in.readUTF();
					System.out.println(response);//utilisé pour l'affichage
				}catch (Exception e){
					System.out.println("Désolé, fichier non trouvé");
				}
				error = in.readUTF(); 
				if(error.equals("|")) {
					commande = "exit";
				}
				uploadFile = null;
				break;
			case "download":
				out.writeUTF("download");
				fileName = sc.next();
				sc.nextLine();
				out.writeUTF(fileName);
				
				response = in.readUTF();
				if(response.equals("|")) {
					System.out.println("Désolé, fichier non trouvé");
					break;
				}
				
				n = in.readInt();
			    fileBytes = new byte [n];
			    i =0;
			    while (i < n) {
			    	i += in.read(fileBytes, i, n-i);
			    }
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(fileName);
					fos.write(fileBytes);
					fos.close();
					System.out.println(response);//utilisé pour l'affichage
					out.writeUTF("||");
				} catch (Exception e) {
					out.writeUTF("|");
					e.printStackTrace();
				}
				fileBytes = null;
				break;
				
			case "exit":
				out.writeUTF("exit");
				response = in.readUTF();
				System.out.println(response);
				break;
			default:
				System.out.println("Commande inconnue");
				sc.nextLine();
				break;
			}
		}
		
		
		// Fermeture de la connexion avec le serveur
		socket.close();
		sc.close();
	}

	/*La methode IPvalid permet de verifier la validite d une adresse IP saisi par le client, il verifie que l adrsse contient des chiffres et qu il a une valeur de  4 octets*/
	
	static boolean IPvalid(String IP) {
		String[] splittedIP;
		splittedIP = IP.split("\\.");
		if(splittedIP.length!=4) {
			return false;
		}
		int[] intIP = new int[4]; 
		for(int i=0; i<4; i++) {
			try {
		        intIP[i] = Integer.parseInt(splittedIP[i]);
		    } catch (NumberFormatException | NullPointerException nfe) {
		        return false;
		    }
			if(intIP[i]<0 || intIP[i]>255) {
				return false;
			}
		}
		return true;
	}

}