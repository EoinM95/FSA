package fsa;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class FSABuilder {
	public static final String LS=System.getProperty("line.separator");
	public static final String transitionFormat="(?<stateA>[0-9]+) (?<label>([a-zA-Z]|eps)) (?<stateB>[0-9]+)";
	public static final String finalFormat="final (?<state>[0-9]+)";
	public static final Pattern transitionPattern=Pattern.compile(transitionFormat);
	public static final Pattern finalPattern=Pattern.compile(finalFormat);
	public static ArrayList<FSA> buildFromFile(String filename) throws IOException{
		ArrayList<FSA> list= new ArrayList<FSA>();
		String fileContents=readFile(filename);
		String[] automata=fileContents.split("init 0");
		for(String a:automata){
			a.replaceAll("#","##");
			if(!a.equals("")){
				FSA f=new NFA();
				String[] instructions=a.split(LS);
				for(String instruction:instructions){
					if(!instruction.equals("")){
						Matcher tm=transitionPattern.matcher(instruction);
						Matcher fm=finalPattern.matcher(instruction);
						if(tm.matches()){
							int stateA=Integer.parseInt(tm.group("stateA"));
							int stateB=Integer.parseInt(tm.group("stateB"));
							String label=tm.group("label");
							if(label.equals("eps"))
								f.addEpsilonTransition(stateA,stateB);
							else
								f.addTransition(stateA,label,stateB);
						}
						else if(fm.matches()){
							int state=Integer.parseInt(fm.group("state"));
							f.setFinal(state);
						}
						else{
							System.out.println("Instruction pas reconnu: "+instruction+"...");
						}
					}
				}
				list.add(f);
			}
		}		

		return list;
	}
	
	public static FSA buildFromStringList(ArrayList<String> transitions){
		if(transitions==null||transitions.isEmpty())
			return null;
		FSA f = new NFA();
		for(String transition:transitions){
			Matcher tm=transitionPattern.matcher(transition);
			Matcher fm=finalPattern.matcher(transition);
			if(tm.matches()){
				int stateA=Integer.parseInt(tm.group("stateA"));
				int stateB=Integer.parseInt(tm.group("stateB"));
				String label=tm.group("label");
				if(label.equals("eps"))
					f.addEpsilonTransition(stateA,stateB);
				else
					f.addTransition(stateA,label,stateB);
			}
			else if(fm.matches()){
				int state=Integer.parseInt(fm.group("state"));
				f.setFinal(state);
			}

		}
		return f;
	}
	
	public static String readFile(String path) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (path));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			while((line = reader.readLine())!= null){
				stringBuilder.append(line);
				stringBuilder.append(LS);
			}
			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}
	
	public static boolean write(String output, String path, boolean canOverwrite){
		File outputFile=new File(path);
		PrintWriter outputStream=null;
		try{
			if(outputFile.exists()){
				if(canOverwrite){
					outputStream = new PrintWriter(new FileWriter(outputFile)) ;
					outputStream.print(output);
				}
				else{
					outputStream = new PrintWriter(new FileWriter(outputFile,true)) ;
					outputStream.print(LS+output);
				}
			}
			else if(outputFile.createNewFile()){
				outputStream = new PrintWriter(new FileWriter(outputFile)) ;
				outputStream.print(output);
			}
			else
			{
				System.out.println("Nom du fichier pas valide");
				return false;
			}
		}
		catch (IOException e) {
			System.out.println("Erreur IO");
			return false;
		}
		if(outputStream!=null)
			outputStream.close();
		return true;
	}
	
	
	public static void main(String[] args){
		try{
			FSA f=buildFromFile("test.txt").get(0);
			//System.out.println(f.accepts("aa"));
			//System.out.println(f.accepts("aaaa"));
			//System.out.println(f.accepts("aaaab"));
			//System.out.println(f.transitionList());
			f.epsilonFree();
			System.out.println(f.transitionList());
			//System.out.println(f.size());
			FSA dfa=f.determinise();
			dfa.minimise();
			System.out.println(dfa.size());
			System.out.println(dfa.transitionList());
			System.out.println(dfa.accepts("aa"));
			System.out.println(dfa.accepts("aaaa"));
			System.out.println(dfa.accepts("aaaab"));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
}
