package fsa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class FSABuilder {
	public static final String LS=System.getProperty("line.separator");
	private FSABuilder(){}
	
	public static ArrayList<FSA> buildFromFile(String filename){
		ArrayList<FSA> list= new ArrayList<FSA>();
		try {
			String fileContents=readFile(filename);
			String[] automata=fileContents.split("init 0");
			for(String a:automata){
				if(!a.equals("")){
					FSA f=new DFA();
					String[] instructions=a.split(LS);
					String transitionFormat="(?<stateA>[0-9]+) (?<label>[a-zA-Z]) (?<stateB>[0-9]+)";
					String finalFormat="final (?<state>[0-9]+)";
					Pattern transitionPattern=Pattern.compile(transitionFormat);
					Pattern finalPattern=Pattern.compile(finalFormat);
					for(String instruction:instructions){
						if(!instruction.equals("")){
							Matcher tm=transitionPattern.matcher(instruction);
							Matcher fm=finalPattern.matcher(instruction);
							if(tm.matches()){
								int stateA=Integer.parseInt(tm.group("stateA"));
								int stateB=Integer.parseInt(tm.group("stateB"));
								String label=tm.group("label");
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
		} catch (IOException e) {
			System.out.println("Erreur d'entrée/sortie");
			return null;
		}
		return list;
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
	
	public static void main(String[] args){
		FSA f=buildFromFile("test.txt").get(0);
		System.out.println(f.accepts("aa"));
		System.out.println(f.accepts("aaaa"));
		System.out.println(f.accepts("aaaab"));
		f.save("");
		((DFA)f).minimise();
		System.out.println(f.accepts("aa"));
		System.out.println(f.accepts("aaaa"));
		System.out.println(f.accepts("aaaab"));
		f.save("");
	}
	
	
}
