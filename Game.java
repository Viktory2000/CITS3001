import java.util.*;
import java.io.*;
/**
 * A Class to represent a single game of resistance
 * @author Tim French
 * */

public class Game{

  private Map<Character,Agent> players;
  private Set<Character> spies;
  private String playerString = "";
  private String spyString = "";
  private String resString = "";
  private static int numPlayers = 0;
  private static final int[] spyNum = {2,2,3,3,3,4}; //spyNum[n-5] is the number of spies in an n player game
  private static final int[][] missionNum = {{2,3,2,3,3},{2,3,4,3,4},{2,3,3,4,4},{3,4,4,5,5},{3,4,4,5,5},{3,4,4,5,5}};
                                    //missionNum[n-5][i] is the number to send on mission i in a  in an n player game
  private Random rand;
  private File logFile;
  private boolean logging = false;
  private boolean started = false;
  private long stopwatch = 0;
  
  private static ArrayList<String> botNames = new ArrayList<>();
  private Character leader = (char)65;

  /**
   * Creates an empty game.
   * Game log printed to stdout
   * */
  public Game(){
    init();
  }

  /**
   * Creates an empty game
   * @param logFile path to the log file
   * */
  public Game(String fName){
    logFile = new File(fName);
    logging = true;
    init();
  }

  /**
   * Initializes the data structures for the game
   * */
  private void init(){
    players = new HashMap<Character,Agent>();
    spies = new HashSet<Character>();
    rand = new Random();
    long seed = rand.nextLong();
    rand.setSeed(seed);
    log("Seed: "+seed);
  }

  /**
   * Writes the String to the log file
   * @param msg the String to log
   * */
  private void log(String msg){
    if(logging){
      try{
        FileWriter log = new FileWriter(logFile);
        log.write(msg);
        log.close();
      }catch(IOException e){e.printStackTrace();}
    }
    else{
      System.out.println(msg);
    }
  }  


  /**
   * Adds a player to a game. Once a player is added they cannot be removed
   * @param a the agent to be added
   * */
  public void addPlayer(Agent a){
    if(numPlayers > 9) throw new RuntimeException("Too many players");
    else if(started) throw new RuntimeException("Game already underway");
    else{
      Character name = (char)(65+numPlayers++);
      players.put(name, a);
      log("Player "+name+" added.");
      botNames.add(a.name());
    }
  }

  /**
   * Sets up the game and informs all players of their status.
   * This involves assigning players as spies according to the rules.
   */
  public void setup(){
    if(numPlayers < 5) throw new RuntimeException("Too few players");
    else if(started) throw new RuntimeException("Game already underway");
    else{
      for(int i = 0; i<spyNum[numPlayers-5]; i++){
        char spy = ' ';
        while(spy==' ' || spies.contains(spy)){
          spy = (char)(65+rand.nextInt(numPlayers));
        }
        spies.add(spy);
      }
      for(Character c: players.keySet())playerString+=c;
      for(Character c: spies){spyString+=c; resString+='?';}
      statusUpdate(1,0);
      started= true;
      log("Game set up. Spys allocated");
    }
    
  }

  /** 
   * Starts a timer for Agent method calls
   * */
  private void stopwatchOn(){
    stopwatch = System.currentTimeMillis();
  }

  /**
   * Checks how if timelimit exceed and if so, logs a violation against a player.
   * @param limit the limit since stopwatch start, in milliseconds
   * @param player the player who the violation will be recorded against.
   * */
  private void stopwatchOff(long limit, Character player){
    long delay = System.currentTimeMillis()-stopwatch;
    if(delay>limit)
      log("Player: "+player+". Time exceeded by "+delay);
  }

  /**
   * Sends a status update to all players.
   * The status includes the players name, the player string, the spys (or a string of ? if the player is not a spy, the number of rounds played and the number of rounds failed)
   * @param round the current round
   * @param fails the number of rounds failed
   **/
  private void statusUpdate(int round, int fails){
    for(Character c: players.keySet()){
      if(spies.contains(c)){
        stopwatchOn(); players.get(c).get_status(""+c,playerString,spyString,round,fails); stopwatchOff(100,c);
      }
      else{ 
        stopwatchOn(); players.get(c).get_status(""+c,playerString,resString,round,fails); stopwatchOff(100,c);
      }
    }
  }

  /**
   * This method picks a random leader for the next round and has them nominate a mission team.
   * If the leader does not pick a legitimate mission team (wrong number of agents, or agents that are not in the game) a default selection is given instead.
   * @param round the round in the game the mission is for.
   * @return a String containing the names of the agents being sent on the mission
   * */
  private String nominate(int round){
    leader = (char)(((leader-64)%numPlayers)+65);
    System.out.println("LEADER IS " + leader);
    int mNum = missionNum[numPlayers-5][round-1];
    stopwatchOn(); String team = players.get(leader).do_Nominate(mNum); stopwatchOff(1000,leader);
    char[] tA = team.toCharArray();
    Arrays.sort(tA);
    boolean legit = tA.length==mNum;
    for(int i = 0; i<mNum && legit; i++){
      if(!players.keySet().contains(tA[i])) legit = false;
      if(i>0 && tA[i]==tA[i-1]) legit=false;
    }
    if(!legit){
      team = "";
      for(int i = 0; i< mNum; i++) team+=(char)(65+i);
    }
    for(Character c: players.keySet()){
      stopwatchOn(); players.get(c).get_ProposedMission(leader+"", team); stopwatchOff(100, c);
    }
    log(leader+" nominated "+team);
    return team;
  }

  /**
   * This method requests votes from all players on the most recently proposed mission teams, and reports whether a majority voted yes.
   * It counts the votes and reports a String of all agents who voted in favour to the each agent.
   * @return true if a strict majority supported the mission.
   * */
  private boolean vote(){
   int votes = 0;
   String yays = "";
   for(Character c: players.keySet()){
      stopwatchOn(); 
      if(players.get(c).do_Vote()){
        votes++;
        yays+=c;
       }
      stopwatchOff(1000,c);
    }
    for(Character c: players.keySet()){
      stopwatchOn();
      players.get(c).get_Votes(yays);
      stopwatchOff(100,c);
    }
    log(votes+" votes for: "+yays);
    return (votes>numPlayers/2);  
  }

  /**
   * Polls the mission team on whether they betray or not, and reports the result.
   * First it informs all players of the team being sent on the mission. 
   * Then polls each agent who goes on the mission on whether or not they betray the mission.
   * It reports to each agent the number of betrayals.
   * @param team A string with one character for each member of the team.
   * @return the number of agents who betray the mission.
   * */
  public int mission(String team){
    for(Character c: players.keySet()){
      stopwatchOn();
      players.get(c).get_Mission(team);
      stopwatchOff(100,c);
    }
    int traitors = 0;
    for(Character c: team.toCharArray()){
      stopwatchOn();
      if(spies.contains(c) && players.get(c).do_Betray()) traitors++;
      stopwatchOff(1000,c);
    }
    for(Character c: players.keySet()){
      stopwatchOn();
      players.get(c).get_Traitors(traitors);
      stopwatchOff(100,c);
    }
    log(traitors +" betrayed the mission");
    return traitors;  
  }

  public static int[][] matrixAdd(int[][] A, int[][] B)
  {
      // Check if matrices have contents
      //if ((A.length < 0) || (A[0].length < 0)) return B;
      //if ((B.length < 0) || (B[0].length < 0)) return A;

      // create new matrix to store added values in

      int[][] C = new int[A.length][A[0].length];
      for (int i = 0; i < A.length; i++) {
          for (int j = 0; j < A[0].length; j++) {
              C[i][j] = A[i][j] + B[i][j];
          }
      }
      return C;
  }
  
  /**
   * Conducts the game play, consisting of 5 rounds, each with a series of nominations and votes, and the eventual mission.
   * It logs the result of the game at the end.
   * */
  public int[][] play(){
    int fails = 0;
    int[][] playStat = new int[4][numPlayers];
    for(int round = 1; round<=5; round++){
      String team = nominate(round);
      int voteRnd = 0;
      while(voteRnd++<5 && !vote())
        team = nominate(round);
      log(team+" elected");
      int traitors = mission(team);
      if(traitors !=0 && (traitors !=1 || round !=4 || numPlayers<7)){
        fails++;
        log("Mission failed");
      }
      else log("Mission succeeded");
      statusUpdate(round+1, fails);
      HashMap<Character,String> accusations = new HashMap<Character, String>();
      for(Character c: players.keySet()){
        stopwatchOn();
        accusations.put(c,players.get(c).do_Accuse());
        stopwatchOff(1000,c);
      }
      for(Character c: players.keySet()){
        log(c+" accuses "+accusations.get(c));
        for(Character a: players.keySet()){
          stopwatchOn();
          players.get(a).get_Accusation(c+"", accusations.get(c));
          stopwatchOff(100,c);
        }
      }  

    log("The Government Spies were "+spyString+".");
    
    }

    for(int i = 0;i < numPlayers ; i++){
    	System.out.println(playStat[0][i]);
    	System.out.println("CHARACTER FOR TESTIN " + (char)(65+i));
		if (spyString.indexOf((char)(65+i)) >= 0) {// ie E is a spy
			//log("E is a spy");
			if (fails > 2) {
				log("Government Wins! " + fails + " missions failed.");
				playStat[0][i]++;
				playStat[2][i]++;
			} else {
				log("Resistance Wins! " + fails + " missions failed.");
				//Bot Failed
				playStat[2][i]++;
			}
		} else {
			//log("E is a Resistance");
			if (fails > 2) {
				log("Government Wins! " + fails + " missions failed.");
				//Bot Failed
				playStat[3][i]++;
			} else {
				log("Resistance Wins! " + fails + " missions failed.");
				playStat[1][i]++;
				playStat[3][i]++;
			}
		}
    }
    

    return playStat;
	}
  

  /**
   * Sets up game with random agents and plays
   **/
  public static void main(String[] args){
	  int BotWins[] = new int[3];

	  //int sets = 10000;

	  //for(int b = 0; b < sets; b++){
		  int size = 10000;
		  int gameSize = 5;
		  boolean firstTime = true;
		  int[][] result = new int[4][numPlayers];

		  Genome dna = new Genome(0);
		  dna.noSpyVoteProb = 0.1;
		  dna.multSpyVoteProb = 0.3;
		  dna.onlySpyBetrayProb = 0.9;
		  dna.multSpyBetrayProb = 0.9;
		  dna.allSpyBetrayProb = 0;
		  dna.teamThreshold = 0.5;

		  for (int p = 0; p < size; p++){
			  Game g = new Game();
			  numPlayers = 0;
			  int l = 0;
			  Random rand = new Random();
//			  while( l<gameSize){
//				  int pick = rand.nextInt(3);
//
//				  if(pick == 0){
//					  g.addPlayer(new GeneticBayesAgent(dna));
//				  }else if(pick == 1){
//					  g.addPlayer(new TrustyAgent());
//				  }else{
//					  g.addPlayer(new NaiveAgent());
//				  }
//				  l++;
				  		  g.addPlayer(new GeneticBayesAgent(dna));
				  		  g.addPlayer(new TrustyAgent());
				  		  g.addPlayer(new NaiveAgent());
				  		  g.addPlayer(new TrustyAgent());
				  		  g.addPlayer(new GeneticBayesAgent(dna));
				  		  g.addPlayer(new GeneticBayesAgent(dna));
			  //}
			  g.setup();
			  System.out.println("NUMPLAYERS" + numPlayers);

			  //result = matrixAdd(result , g.play());
			  if(firstTime){
				  result = new int[4][numPlayers];
				  firstTime = false;
				  System.out.println("FirstTime");
			  }
			  int[][] temp = g.play();
			  for (int k = 0; k < result.length; k++) {
				  for (int j = 0; j < result[0].length; j++) {
					  result[k][j] = result[k][j] + temp[k][j];
					  //System.out.println("RESULT UPDATE" + result[k][j]);
				  }
			  }  
			  //result = g.play();
			  //System.out.println(result[0][0]);

		  }

		  System.out.println("Bot Name		Spy Win Rate		Resistance Win Rate			OVERALL");
		  double max = 0;
		  String BestBot = "FAILED";
		  int SpyWins = 0;
		  int SpyGames = 0;
		  int ResWins = 0;
		  int ResGames = 0;
		  
		  for(int i = 0;i < numPlayers ; i++){
			  System.out.println(botNames.get(i) + "			" + (double)result[0][i]/result[2][i]*100 + "%"+ "			" + (double)result[1][i]/result[3][i]*100 + "%" + "			" + (double)(result[1][i]+result[0][i])/(result[2][i]+result[3][i])*100 + "%"  );
			  SpyWins = result[0][i];
			  SpyGames = result[2][i];
			  ResWins = result[1][i];
			  ResGames = result[3][i];
			  if((double)(result[1][i]+result[0][i])/(result[2][i]+result[3][i])*100>=max){
				  max = (double)(result[1][i]+result[0][i])/(result[2][i]+result[3][i])*100;
				  BestBot = botNames.get(i);
			  }
		  }
		  
		  System.out.println("Spy win Rate = " + (double)SpyWins/SpyGames*100);
		  System.out.println("Resistance win Rate = " + (double)ResWins/ResGames*100);
		  
		  if(BestBot.equals("Naive")){
			  BotWins[0]++;
			  System.out.println("Naive Wins " + BotWins[0]);
		  }else if(BestBot.equals("Trusty")){
			  BotWins[1]++;
			  System.out.println("Trusty Wins " + BotWins[1]);
		  }else if(BestBot.equals("Genetic Bayes")){
			  BotWins[2]++;
			  System.out.println("Bayes Wins " + BotWins[2]);
		  }
	  //}
//	  System.out.println("SUCCESS RATE OF NAIVE = " + (double)BotWins[0]/sets*100 + "%");
//	  System.out.println("SUCCESS RATE OF Trusty = " + (double)BotWins[1]/sets*100 + "%");
//	  System.out.println("SUCCESS RATE OF Bayes = " + (double)BotWins[2]/sets*100 + "%");
  }
}  
        
        
        









