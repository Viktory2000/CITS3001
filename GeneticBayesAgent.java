import java.util.*;


public class GeneticBayesAgent implements Agent{

	private String players = ""; //String representing the set of players
	private String spies = ""; //String representing the set of spies. Player names are '?' if I am resistance
	private String name; //String storing my name
	private String leader; //Player that nominated the current mission
	private boolean spy = false; //Whether or not I am a spy
	private int missionNumber = 0; //Number of the current mission
	private String myNom = ""; //Mission I would nominate if I were leader
	private int failures = 0; //Number of missions that have failed so far
	
	private Random rand;
	private String[] worldSpies;
	
	private double[] worldProb;
	private Set<String> taboos; //Set of teams that definitely contain a spy
	
	private String proposed="";
	private String mission="";
	private int traitors=0;
	private boolean solved = false; //True if 100% sure who the spies are
	
	private boolean init = true; //used for the initialization of the games
	
	private double maxWorldProb;
	
	private Genome dna;
	private double noSpyVoteProb = 0.1;
	private double multSpyVoteProb = 0.3;
	private double onlySpyBetrayProb = 0.9;
	private double multSpyBetrayProb = 0.9;
	private double allSpyBetrayProb = 0;
	private double teamThreshold = 0.5;
	
	private double dumbProb = 0.3; //Value used to balance effects of malice v incompetence
	private ArrayList<String> worldSpiesList = new ArrayList<String>();
	
	public GeneticBayesAgent(Genome dna){
		this.dna = dna;
		noSpyVoteProb = dna.noSpyVoteProb;
		multSpyVoteProb = dna.multSpyVoteProb;
		onlySpyBetrayProb = dna.onlySpyBetrayProb;
		multSpyBetrayProb = dna.multSpyBetrayProb;
		allSpyBetrayProb = dna.allSpyBetrayProb;
		teamThreshold = dna.teamThreshold;
		taboos = new HashSet<String>();
	    rand = new Random();
	}
	
	public GeneticBayesAgent(){
		dna = new Genome(0);
		dna.noSpyVoteProb = 0.05;
		dna.multSpyVoteProb = 0.5;
		dna.onlySpyBetrayProb = 0.9;
		dna.multSpyBetrayProb = 0.8;
		dna.allSpyBetrayProb = 0.1;
		dna.teamThreshold = 0.8;
		noSpyVoteProb = dna.noSpyVoteProb;
		multSpyVoteProb = dna.multSpyVoteProb;
		onlySpyBetrayProb = dna.onlySpyBetrayProb;
		multSpyBetrayProb = dna.multSpyBetrayProb;
		allSpyBetrayProb = dna.allSpyBetrayProb;
		teamThreshold = dna.teamThreshold;
		taboos = new HashSet<String>();
	    rand = new Random();
	}
	
    // The method that prints all possible strings of length k.  It is
    //  mainly a wrapper over recursive function printAllKLengthRec()
    public void printAllKLength(String set, int k) {
        int n = set.length();        
        int max = 0;
        printAllKLengthRec(set, "", n, k, k, max);
    }
 
    // The main recursive method to print all possible strings of length k
    public void printAllKLengthRec(String set, String prefix, int n, int k, int length, int max) {
    	
        // Base case: k is 0, print prefix
        if (k == 0 && prefix.length() == length ) {
            //System.out.println(prefix);
            worldSpiesList.add(prefix);
            return;
        }else if(k ==0)
        	return;
 
        // One by one add all characters from set and recursively 
        // call for k equals to k-1
        for (int i = 0; i < n; ++i) {
        	
        	for(int j = 0; j<prefix.length(); j++){
        		if((int)prefix.charAt(j)>max)
        	    max = (int)prefix.charAt(j);
        	}
        		
        	String newPrefix;
            // Next character of input added
        	if(prefix.indexOf(set.charAt(i)) == -1 && (int)set.charAt(i)>max){
        		newPrefix = prefix + set.charAt(i); 
        	}else{
        		newPrefix = prefix;
        	}
             //System.out.println(prefix);
            // k is decreased, because we have added a new character
            printAllKLengthRec(set, newPrefix, n, k - 1, length, max);
        }
    }
	

	 
	
	/**
	* Reports the current status, inlcuding players name, the name of all players, the names of the spies (if known), the mission number and the number of failed missions
	* @param name a string consisting of a single letter, the agent's names.
	* @param players a string consisting of one letter for everyone in the game.
	* @param spies a String consisting of the latter name of each spy, if the agent is a spy, or n questions marks where n is the number of spies allocated; this should be sufficient for the agent to determine if they are a spy or not. 
	* @param mission the next mission to be launched
	* @param failures the number of failed missions
	* */
	public void get_status(String name, String players, String spies, int mission, int failures){
		this.name = name;
		this.players = players;
		this.spies = spies;
		if(spies.indexOf(name)!=-1) spy = true;
		
		if(init){
			printAllKLength(players,spies.length());
			
			worldSpies = worldSpiesList.toArray( new String[worldSpiesList.size()]);
			worldProb = new double[worldSpies.length];
			boolean isWorld;
			
			if(spy) //If a spy, mark which other players are spies
			{
				//System.out.println(name+" IS A SPY **********");
			}
			else
			{
				//System.out.println(name+" IS RESISTANCE **********");
			}
			
			int worlds_without_me = 0;
			for(int i=0; i<worldSpies.length; i++)
			{
				if(worldSpies[i].indexOf(name) != -1) //If I am a spy in this world
					worldProb[i] = 0; //This world is not possible
				else
					worlds_without_me++; //Increase count of worlds in which I am not a spy
			}
			
			//Initialise uniform probability of worlds in which I am not a spy
			for(int i=0; i<worldSpies.length; i++)
			{
				if(worldSpies[i].indexOf(name) == -1) //If I am not a spy in this world
					worldProb[i] = 1/((double)worlds_without_me); //THis world has probability 1/(worlds in which I am not a spy)
			}
			
			for(int i=0; i<worldSpies.length; i++)
			{
				//System.out.println(name+": "+worldSpies[i]+": "+worldProb[i]);
			}
			init = false;
		}

	}
  
	/**
	* Nominates a group of agents to go on a mission.
	* If the String does not correspond to a legitimate mission (<i>number</i> of distinct agents, in a String), 
	* a default nomination of the first <i>number</i> agents (in alphabetical order) will be reported, as if this was what the agent nominated.
	* @param number the number of agents to be sent on the mission
	* @return a String containing the names of all the agents in a mission
	* */
	public String do_Nominate(int number){
		return getMyNom(number);
	}
	
	//Get mission I would nominate if I were leader
	public String getMyNom(int number){
		//Nominates self as well as people least likely to be spies
		String nom = name; //Always nominates self
		int n= 1;
		
		double[] spyProb = new double[players.length()]; //Probability that player at position i in the player list is a spy
		for(char c: players.toCharArray()){
			spyProb[players.indexOf(c)] = 0;
			for (int i = 0; i < worldProb.length; i++) {//Sum probability of someone being a spy over all worlds
				if(worldSpies[i].indexOf(c) != -1) {
					spyProb[players.indexOf(c)] += worldProb[i];
				}
			}
		}
		
		while (n < number) {
			double best = 1;
			int bestID = 0;
			for (int i = 0; i < players.length(); i++) { //Find minimum probability of not being a spy i.e
				if((spyProb[i]) < best && nom.indexOf(players.toCharArray()[i])==-1) {//Find probability each is resistance ensuring no duplicates
					best = spyProb[i];
					bestID = i;
				}
			}
			nom += players.toCharArray()[bestID];

			n++;
		}
		myNom = nom;
		return nom;
	}

	/**
	* Provides information of a given mission.
	* @param leader the leader who proposed the mission
	* @param mission a String containing the names of all the agents in the mission 
	**/
	public void get_ProposedMission(String leader, String mission){
		this.leader = leader;
		this.proposed = mission;
	}

	/**
	* Gets an agents vote on the last reported mission
	* @return true, if the agent votes for the mission, false, if they vote against it.
	* */
	public boolean do_Vote() {
		
		if(missionNumber == 0)//Always vote yes for first mission
				return true;

		
		if(leader.equals(name)) //Always vote for missions that I proposed
			return true;
			
		if(setEquals(proposed,getMyNom(proposed.length()))) //Vote yes if I would have nominated the same mission
			return true;
		
		if(spy)
		{
			int spynum = 0;
			for(char c : proposed.toCharArray()) //Count known spies on proposed mission
			{
				if(spies.indexOf(c) != -1)
					spynum++;
			}
			if(spynum >1) //Check for multiple spies
				return doProb(multSpyVoteProb);
			else if (spynum == 0)
				return doProb(noSpyVoteProb);
			else
				return true; //Vote yes if there is exactly one spy
		}
		else
		{
	
			//Check taboo set
			for(String s : taboos)
			{
				if(subset(s,proposed)){ //If the proposed mission is a superset of a set known to contain a spy, then the proposed mission contains a spy
					//System.out.println(name+": "+"Team is known to contain a spy");
					return false;
				}
			}
			int count;
			double[] spyProb = new double[players.length()];
			for (char c : players.toCharArray()) {
				spyProb[players.indexOf(c)] = 0;
				for (int i = 0; i < worldProb.length; i++) {// Sum probability of someone being a spy over all worlds
					if (worldSpies[i].indexOf(c) != -1) {
						spyProb[players.indexOf(c)] += worldProb[i];
					}
				}
			}

			double noSpyProb =1; //Probability the nominated mission contains 0 spies
			for(char c : proposed.toCharArray())
				noSpyProb *= (1-spyProb[players.indexOf(c)]);

			double myNomNoSpyProb =1; //Probability the mission I would nominate contains 0 spies
			for(char c : myNom.toCharArray())
				myNomNoSpyProb *= (1-spyProb[players.indexOf(c)]);
			
			double eps = 1e-4;
			if(noSpyProb >= myNomNoSpyProb-eps) //Vote yes if the mission is just as good as what I would nominate
				return true;
			
			return (noSpyProb > teamThreshold); //Vote yes if it is sufficiently likely that the proposed mission contains no spies
		}
	}


	/**
	 * Reports the votes for the previous mission
	 * 
	 * @param yays
	 *            the names of the agents who voted for the mission
	 **/
  public void get_Votes(String yays){
		//Check taboo set
		boolean bad = false;
		for(String s : taboos)
		{
			if(subset(s,proposed)){ //If the proposed mission is a superset of a set known to contain a spy, then the proposed mission contains a spy
				bad = true;
				break;
			}
		}
		if(bad && !solved)
		{
			double[] ProbAGivW = new double[worldProb.length];
			double ProbA = 0;
			for(int i = 0; i<worldProb.length ; i++){
				int spies_voted= 0; //Number of spies that voted yes if this is the correct world
				for(int j=0; j<worldSpies[i].length(); j++)
				{
					if(yays.indexOf(worldSpies[i].charAt(j)) != -1)
					spies_voted++;
				}
				//given this is the world what is the probability that these people would have voted yes
				ProbAGivW[i] = 1.0;
				for(char c : yays.toCharArray())
				{
					if(worldSpies[i].indexOf(c) == -1) //Not a spy in this world
						ProbAGivW[i] *= dumbProb;
					else //Is a spy in this world
						ProbAGivW[i] *= (1-dumbProb);
				}
				ProbA += ProbAGivW[i]*worldProb[i];
				
			}
			for(int i = 0; i<worldProb.length; i++){
				if(ProbA > 0)
				{
					worldProb[i] = ProbAGivW[i]*worldProb[i]/(ProbA);
					//System.out.println(name+": "+worldSpies[i]+ " HAS PROBABILITY = " + worldProb[i]);
				}
				if(worldProb[i]>=1){
					//System.out.println(name+": "+"SOLVED! "+worldSpies[i]+" are spies");
					solved = true;
				}
			}	
		}
  }
  /**
   * Reports the agents being sent on a mission.
   * Should be able to be infered from tell_ProposedMission and tell_Votes, but incldued for completeness.
   * @param mission the Agents being sent on a mission
   **/
  public void get_Mission(String mission){
		this.mission = mission;
		missionNumber++;
  }

  /**
   * Agent chooses to betray or not.
   * @return true if agent betrays, false otherwise
   **/
  public boolean do_Betray(){
		if(!spy)
			return false;
		int spynum = 0;
		if(failures >= 2) //Betray if this will win the game
			return true;
		for(char c : proposed.toCharArray()) //Count spies on mission
		{
			if(spies.indexOf(c) != -1)
				spynum++;
		}
		if(spynum == 1)
			return doProb(onlySpyBetrayProb);
		else if(spynum == mission.length())
			return doProb(allSpyBetrayProb);
		else
			return doProb(multSpyBetrayProb);
   }  

  /**
   * Reports the number of people who betrayed the mission
   * @param traitors the number of people on the mission who chose to betray (0 for success, greater than 0 for failure)
   **/
  public void get_Traitors(int traitors){
	double[] ProbAGivW = new double[worldProb.length];
	double ProbA = 0;
	this.traitors = traitors;
	//Update taboo list
	if(traitors > 0)
	{
		failures++;
		taboos.add(mission); //Someone betrayed so this set contains at least one spy
	}
	
	if(traitors == mission.length()) //If everyone betrayed, everyone must be a spy
	{
		//System.out.println(name+": "+"SOLVED! "+mission+" are spies");
		for(char c : mission.toCharArray() )
		{
			taboos.add(Character.toString(c));
		}
		solved = true; //World is now known
		for(int i = 0; i<worldProb.length ; i++){
			if(setEquals(worldSpies[i],mission))
				worldProb[i] = 1;
			else
				worldProb[i] = 0;
		}
		
	}
	else if(!solved)//Not everyone on the mission betrayed
	{
		for(int i = 0; i<worldProb.length ; i++){
			if(worldProb[i]>=1){
				solved = true;
				break;
			}
				
			int spies_on_mission= 0; //Number of spies that are on the mission if this is the correct world
			for(int j=0; j<worldSpies[i].length(); j++)
			{
				if(mission.indexOf(worldSpies[i].charAt(j)) != -1)
				spies_on_mission++;
			}
			double betrayProb; //Set appropriate betray probability
			if(spies_on_mission <= 1)
				betrayProb = onlySpyBetrayProb;
			else if(spies_on_mission == mission.length())
				betrayProb = allSpyBetrayProb;
			else
				betrayProb = multSpyBetrayProb;
			
			ProbAGivW[i] = NChooseR(spies_on_mission,traitors)*Math.pow(betrayProb,traitors)*Math.pow(1-betrayProb,spies_on_mission-traitors); //Calculate Bernouli probability
			
			ProbA += ProbAGivW[i]*worldProb[i];
		}
		
		//System.out.println(name+": "+"Solved: "+solved);
		for(int i = 0; i<worldProb.length; i++){
			if(ProbA > 0)
			{
				worldProb[i] = ProbAGivW[i]*worldProb[i]/(ProbA);
				//System.out.println(name+": "+worldSpies[i]+ " HAS PROBABILITY = " + worldProb[i]);
			}
			if(worldProb[i]>=1){
				//System.out.println(name+": "+"SOLVED! "+worldSpies[i]+" are spies");
				solved = true;
			}
		}		
	}	
  }


  /**
   * Optional method to accuse other Agents of being spies. 
   * Default action should return the empty String. 
   * Convention suggests that this method only return a non-empty string when the accuser is sure that the accused is a spy.
   * Of course convention can be ignored.
   * @return a string containing the name of each accused agent. 
   * */
  public String do_Accuse(){
	  String World = "";
	  double temp = 0.0;
	  
		for (int i = 0; i < worldProb.length; i++) { // Find lowest probability world
			temp = 0;
			if (worldProb[i] > temp) {
				temp = worldProb[i];
				World = worldSpies[i]; // Lowest Probability world ie lowest probability spies
				//System.out.println("Highest Probability World found = " + World);
			}
		}
		maxWorldProb = temp;
		return World;
  }

  /**
   * Optional method to process an accusation.
   * @param accuser the name of the agent making the accusation.
   * @param accused the names of the Agents being Accused, concatenated in a String.
   * */
  public void get_Accusation(String accuser, String accused){

  }
  
  /**
   * Use a recursive representation of Pascal's Triangle to
   * calcukate N choose R
   * @param N number of trials
   * @param R the number of items to choose
   * */
  public int NChooseR(int N, int R){
    if(N < R)
        return 0;
    if(R == 0 || R == N)
        return 1;
    return NChooseR(N-1,R-1)+NChooseR(N-1,R);
  }
  
  //Returns true if the set represented by A is equal to the set represented by B
  public boolean setEquals(String A, String B)
  {
	  char[] Achar = A.toCharArray();
	  char[] Bchar = B.toCharArray();
	  Arrays.sort(Achar);
	  Arrays.sort(Bchar);
	  return Arrays.equals(Achar,Bchar);
  }
  
  //Returns true if the set represented by A is a subset of the string represented by B
  public boolean subset(String A, String B)
  {
		for(char c : A.toCharArray())
		{
			if(B.indexOf(c) == -1)
				return false;
		}
		return true;
  }
  
  //Returns true with probability p
  public boolean doProb(double p)
  {
	  return (rand.nextDouble() < p);
  }
  
  //Get the id of the genome
  public int getID()
  {
	return dna.id;
  }

}