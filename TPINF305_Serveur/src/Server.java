import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.net.InetAddress; 
import java.net.URL; 
import java.io.BufferedReader;
import java.io.InputStreamReader;

 

public class Server{

	private static ServerSocket listener;       

	/*
	 * Application serveur
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		/*declaration des variables*/
		Scanner sc = new Scanner(System.in);   //la variable sc sauvegarde les saisies claviers
		int clientNumber = 0;                  //la variable clientNumber sauvegarde le nombre de client connecte au serveur
		int serverPort;                        //la variable serverPort sauvegarde le numero de port
		String serverAddress;                 //la variable serverAddress sauvegarde le adresse ip du serveur
		
        try
        { 
            URL url_name = new URL("http://bot.whatismyipaddress.com"); 
  
            BufferedReader br = new BufferedReader(new InputStreamReader(url_name.openStream())); 
  
            // lecture de l adresse IP locale
            serverAddress = br.readLine().trim(); 
        } 
        catch (Exception e) 
        { 
        	serverAddress = InetAddress.getLocalHost().getHostAddress().trim();
        } 
		
        /* adresse et port du serveur*/  
		
		System.out.println("Veuillez entrer un numéro de port compris entre 5000 et 5050 :");
		try {
			serverPort = sc.nextInt();
		}catch(Exception e){
			serverPort=0; //vous avez saisi un numéro de port invalide
		}// Au cas ou le numéro de port saisi n'est pas un int
		
		sc.nextLine();
		
		
		/* Boucle permettant de verifier le numero de port rentre par le client*/
		while(serverPort<= 5000 || serverPort>= 5050) {
			System.out.println("Port invalide!\nVeuillez entrer un numéro de port compris entre 5000 et 5050 :");
			try {
				serverPort = sc.nextInt();
			}catch(Exception e){
				serverPort=0; //vous avez saisi un numéro de port invalide
			}//Au cas ou le numéro de port saisi n'est pas un int
			sc.nextLine();
			
		}

		/* création de la connexion pour communiquer avec les clients*/
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAddress);

		/* Association de l'adresse et du port à la connexion*/
		listener.bind(new InetSocketAddress(serverIp, serverPort));

		System.out.format("Le serveur est connecté avec les informations suivantes:  %s:%d%n", serverAddress, serverPort);

		try {
			/*
			 * A chaque fois qu'un nouveau client se connecte, on execute la fonction run()
			 * de l'objet ClientHandler
			 * 
			 */
			while (true) {
				// important: la fonction accept() est bloquante: attend qu'un prochain client
				// se connecte
				// une nouvelle connexion: on incrémente le compteur clientNumber

				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		} finally

		{
			// fermeture de la connexion
			listener.close();
			sc.close();
		}
	}

	/*
	 * un thread qui se charge de traiter la demande chaque client sur un socket
	 * particulier
	 */

	private static class ClientHandler extends Thread

	{
		private Socket socket;
		private int clientNumber;

		public ClientHandler(Socket socket, int clientNumber)

		{
			this.socket = socket;
			this.clientNumber = clientNumber;

			System.out.println("Nouvelle connexion avec le client N° " + clientNumber + " avec la  " + socket);
		}

		/*
		 * un thread se charge d'envoyer au client un message de bienvenue
		 */
		public void run()
		

		{
			

			try {
				/* création d'un canal sortant pour envoyer un message au client*/

				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream());
				
				out.writeUTF("Salut je suis le serveur - Vous êtes le client N° " + clientNumber);
				
				File currentFolder = new File(System.getProperty("user.dir")+"\\serverFolder");
				if(!currentFolder.exists())
				{
					currentFolder.mkdir();

				}
				File root = new File(System.getProperty("user.dir")+"\\serverFolder");
				String commande = "";                     // la variable commande recupere la commande envoye par le serveur
				String fileName;                         // la variable fileName recupere le nom du fichier saisi pour le televersement
				byte[] uploadFile;                      //la variable uploadFile est un tableau de bytes qui sauvegarde les bytes des fichiers a telecharger
				int n;
				byte fileBytes [];                    //la variable fileBytes est un tableau de bytes qui sauvegarde les bytes des fichiers a televerser
				Date aujourdhui = new Date();        //la variable aujourdhui recupere la date du jour
				
				DateFormat datereq = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);          // la variable date requête renvoit la date du jour et l heure sur un format bien defini
				String IP=socket.getRemoteSocketAddress().toString();                                             //la variable IP recupere l adresse IP et le numero du port du client connecte
				String error;  
				while(!commande.equals("exit")) {
					commande = in.readUTF();
					switch(commande) {
					
					case "cd":
						
						String nomRep=in.readUTF();
						System.out.println("Connexion du client N° "+ clientNumber +" [ "+IP+" - "+ datereq.format(aujourdhui)+" ] : "+ commande+" "+ nomRep);
						
						if(nomRep.equals("..")) {
							if(currentFolder.equals(root)) {
								out.writeUTF("|");
							}
							else {
								currentFolder = currentFolder.getParentFile();
								out.writeUTF("||");
							}
						}
						else {
							File newCurrentFolder = new File(currentFolder.getPath()+"\\"+nomRep);
							if(!newCurrentFolder.exists())
							{
								out.writeUTF("|");
							}
							else
							{
								currentFolder = newCurrentFolder;
								out.writeUTF("Vous êtes dans le dossier "+nomRep);
							}
						}
						
						break;
					
					
					case "ls":
						System.out.println("Connexion du client N° "+ clientNumber +" [ "+IP+" - "+ datereq.format(aujourdhui)+" ] : "+ commande);
						String[] subfolder = currentFolder.list();
						for(String f:subfolder){
							if(f.indexOf('.') == -1) {
								out.writeUTF("[Folder] "+f);
							}
							else {
								out.writeUTF("[File] "+f);
							}
							
						}
						out.writeUTF("|");
						break;
						
					case "mkdir":
						String nouveauRep=in.readUTF();
						System.out.println("Connexion du client N° "+ clientNumber +" [ "+IP+" - "+ datereq.format(aujourdhui)+" ] : "+ commande+" "+ nouveauRep);
						File newFolder = new File(currentFolder.getPath()+"\\"+nouveauRep);
						if(newFolder.exists())
						{
							out.writeUTF("Ce dossier a déjà été créé");
						}
						else 
						{
							newFolder.mkdir();
							//out.writeUTF("||");
							out.writeUTF("Le dossier "+nouveauRep + " a été créé");
						}
						break;
						
						
					case "upload":
						fileName = in.readUTF();
						System.out.println("Connexion du client N° "+ clientNumber +" [ "+IP+" - "+ datereq.format(aujourdhui)+" ] : "+ commande+" "+ fileName);
						n = in.readInt();
					    fileBytes = new byte [n];
					    int i =0;
					    while (i < n) {
					    	i += in.read(fileBytes, i, n-i);
					    }
						FileOutputStream fos;
						try {
							fos = new FileOutputStream(currentFolder.getPath()+"\\"+fileName);
							fos.write(fileBytes);
							fos.close();
							out.writeUTF("Le fichier "+fileName+" à bien été téléversé");//ligne d'affichage
							out.writeUTF("||");
						} catch (Exception e) {
							out.writeUTF("Le serveur ne répond plus");
							out.writeUTF("|");
							e.printStackTrace();
						}
						fileBytes = null; 
						break;
					case "download":
						fileName = in.readUTF();
						System.out.println("Connexion du client N° "+ clientNumber +" [ "+IP+" - "+ datereq.format(aujourdhui)+" ] : "+ commande+" "+ fileName);
						try {
							uploadFile = Files.readAllBytes(Paths.get(currentFolder.getPath()+"\\"+fileName));
							n= uploadFile.length;
							out.writeUTF("Le fichier "+fileName+" à bien été télechargé");//ligne d'affichage
							out.writeInt(n);
							out.write(uploadFile, 0,n);
							
						}catch (Exception e){
							//System.out.println("File not found"); -> il faudrait afficher que le fichier n'est pas sur le servueur
							// au niveau du serveur, mais il y a tout un standard a respecter pour ça je te laisse la tache (voir consignes)
							
							out.writeUTF("|");
							break;
						}
						
						error = in.readUTF();
						if(error.equals("|")) {
							throw new IOException();
						}
						uploadFile = null;
						break;
					default:
					/*exit*/	System.out.println("Connexion du client N° "+ clientNumber +" [ "+IP+" - "+ datereq.format(aujourdhui)+" ] : "+ commande);
					}
				}
				// envoi d'un message d'un client
				out.writeUTF("Vous avez été déconnecté avec succès");//pour affichage
			} catch (IOException e)

			{
				System.out.println("Erreur de traitement du client N° #" + clientNumber + ":" + e);
			} finally {
				try {
					// fermeture de la connexion avec le client
					socket.close();

				} catch (IOException e)

				{
					System.out.println("Impossible de fermer la socket, que se passe t'il?");

				}

				System.out.println("La connexion avec le client N° " + clientNumber + " est terminée");
			}

		}
	}
}
