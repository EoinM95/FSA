package fsa;
import java.util.ArrayList;
import java.util.Scanner;
public final class FSAMain {
	
	public static final String TEST_FILE="test.txt";
	public enum Transformation{
		MINIMISE,COMPLETE,EPSILON_FREE,DETERMINISE;
	}
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
	
	public static void constructAutomaton(){
		
	}
	
	public static void runTests(ArrayList<FSA> automata){
		if(automata.isEmpty()){
			System.out.println("Aucun FSA trouvé dans ce fichier");
			return;
		}
		System.out.println("On a trouvé "+automata.size()+" FSA(s) dans le fichier");
		int index=1;
		for(FSA fsa:automata){
			System.out.println("Faisant des testes sur fsa:"+index);
			System.out.println(" Est-il détérministe?: "+fsa.isDeterministic());
			System.out.println(" Est-il complet?: "+fsa.isComplete());
			System.out.println(" Est-il minimaliste?: "+fsa.isMinimal());
			System.out.println(" Est-il epsilonLibre?: "+fsa.isEpsilonFree());
		}
		if(automata.size()==1){
			transformAndAccept(automata.get(0));
		}
		else{
			boolean finished=false;
			Scanner sc = null;
			while(!finished){
				System.out.println("Choisissez un automate par son indice (0-"+(automata.size()-1)
						+"), ou tapez fin pour retourner au menu");
				sc=new Scanner(System.in);
				if(sc.hasNext("fin")){
					finished=true;
				}
				else{
					if(sc.hasNextInt()){
						int choix=sc.nextInt();
						if(choix>=0&&choix<automata.size()){
							transformAndAccept(automata.get(choix));
						}
						else{
							System.out.println("Indice hors de bourne, essayez encore");
						}
					}
					else{
						System.out.println("Il faut entrer un entier, essayez encore");
					}
				}
			}
			if(sc!=null)
				sc.close();
		}
	}
	
	public static void transformAndAccept(FSA fsa){
		Scanner sc= null;
		boolean finished=false;
		while(!finished){
			System.out.println("Pour tester si votre automate accepte un mot taper:0");
			System.out.println("Pour faire des transformations sur votre automate taper:1");
			System.out.println("Pour retourner au menu taper:2");
			sc=new Scanner(System.in);
			if(sc.hasNextInt()){
				int choix=sc.nextInt();
				switch(choix){
				case 0:
					testAccept(fsa);
					break ;
				case 1:
					transform(fsa);
				case 2:
					finished=true;
					break;
				default:
					System.out.println("Votre commande n'était pas reconnu");
				}
			}
			else{
				System.out.println("Votre commande n'était pas reconnu");
			}
		}
		if(sc!=null)
			sc.close();
	}
	
	public static void testAccept(FSA fsa){
		System.out.println("Entrez le mot que vous voulez tester");
		Scanner sc=new Scanner(System.in);
		if(sc.hasNext()){
			System.out.println("Mot accepté?: "+fsa.accepts(sc.next()));
		}
		sc.close();
	}
	
	public static void transform(FSA fsa){
		Scanner sc= null;
		System.out.println("Pour tester si votre automate accepte un mot taper:0");
		System.out.println("Pour faire des transformations sur votre automate taper:1");
		System.out.println("Pour retourner au menu taper:2");
		sc=new Scanner(System.in);
		if(sc.hasNextInt()){
			int choix=sc.nextInt();
			switch(choix){
			case 0:
				testAccept(fsa);
				break ;
			case 1:
				transform(fsa);
			case 2:
				break;
			default:
				System.out.println("Votre commande n'était pas reconnu");
			}
		}
		else{
			System.out.println("Votre commande n'était pas reconnu");
		}

		if(sc!=null)
			sc.close();
	}
	
	public static void transform(FSA fsa, Transformation t){
		switch(t){
		case COMPLETE:
			fsa=fsa.complete();
			break;
		case DETERMINISE:
			fsa=fsa.determinise();
			break;
		case EPSILON_FREE:
			fsa.epsilonFree();
			break;
		case MINIMISE:
			fsa=fsa.minimise();
			break;
		default:
			return;
		}
	}
	
}
