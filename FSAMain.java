package fsa;
import java.util.ArrayList;
import java.util.Scanner;
public final class FSAMain {
	
	public static final String TEST_FILE="test.txt";
	
	public static void main(String[] args){
		boolean terminated=false;
		Scanner sc=new Scanner(System.in);
		while(!terminated){
			System.out.println("Pour lancer le jeu des testes tapez: 0");
			System.out.println("Pour construire un ou plusiers automates d'un autre fichier tapez: 1");
			System.out.println("Pour construire un automate de la ligne de commande tapez: 2");
			System.out.println("Pour terminer le programme tapez: fin");
			if(sc.hasNext("fin")){
				terminated=true;
			}
			else{
				if(sc.hasNextInt()){
					int choix=sc.nextInt();
					switch(choix){
					case 0:
						runTestSuite();
						break;
					case 1:
						System.out.println("Entrez le nom du fichier de charger");
						sc=new Scanner(System.in);
						if(sc.hasNext()){
							String filename=sc.next();
							try{
								ArrayList<FSA> automata=FSABuilder.buildFromFile(filename);
								runTests(automata);
							}
							catch(java.io.IOException e){
								System.out.println("Fichier pas trouvé; essayez encore");
							}
						}
						break;
					case 2:
						constructAutomaton();
						break;
					default:
						System.out.println("Command non reconnu, essayez encore");
					}
				}
			}
		}
		sc.close();
	}

	public static void runTestSuite(){
		try{
			ArrayList<FSA> automata=FSABuilder.buildFromFile("test.txt");
		}
		catch(java.io.IOException e){
			System.out.println("Fichier des automates pas trouvé");
		}
	}
	
	public static void constructAutomaton(){}
	
	public static void runTests(ArrayList<FSA> automata){}
	
}
