import java.util.*;


public class TrustyAgent implements Agent{

	private Random rand;
	private Scanner scanner;
	private String players = "";
	private String spies = "";
	private String name;
	private String leader;
	private String yays;
	private int failures;
	private boolean spy = false;
	private Map<Character,Double> spyState;
	
	private String proposed="";
	private String mission="";
	private int traitors=0;
	private int missionNum = 0;
	
	private double onlySpyBetrayProb = 1.0;
	private double multSpyBetrayProb = 0.8;
	private double allSpyBetrayProb = 0;
	
	private double goodNomValue = 0.2;
	private double goodVoteValue = 0.2;
	private double goodMissionValue = 0.3;
	private double badNomValue = 0.1;
	private double badVoteValue = 0.4;
	private double badMissionValue = 0.5;
	
	public TrustyAgent(){
		rand = new Random();
		spyState = new HashMap<Character, Double>();
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
		this.failures = failures;
		if(spies.indexOf(name)!=-1) spy = true;
		for(char c : players.toCharArray())
		{
			if(!spyState.containsKey(c))
				spyState.put(c,0.0);
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
		String nom = name; //Always nominates self
		int n = 1;
		Set<Character> playerset= spyState.keySet();
		playerset.remove(name.charAt(0));
		while(n < number) //Repeatedly add most trust worthy remaining player
		{
			double maxtrust = Double.NEGATIVE_INFINITY;
			char maxplayer = '-';
			for(char c : playerset)
			{
				if(spyState.get(c) > maxtrust)
				{
					maxtrust = spyState.get(c);
					maxplayer = c;
				}
			}
			playerset.remove(maxplayer);
			nom += maxplayer;
			n++;	
		}
		
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
	public boolean do_Vote(){
		if(spy)
		{
			int spynum = 0;
			for(char c : proposed.toCharArray()) //Count known spies on proposed mission
			{
				if(spies.indexOf(c) != -1)
					spynum++;
			}
			return (spynum == 1); //Vote yes if there is exactly one spy
		}
		else
		{
			if(proposed.indexOf(name) == -1 && ((players.length()-spies.length()-1) < proposed.length()) )
				return false; //Do not approve mission not containing self if that means the mission will contain a spy
			double trust = 1.0; //Default of 1 for including self
			int n = proposed.length();
			for(char c : proposed.toCharArray())
			{
				if(c != name.charAt(0) && spyState.containsKey(c))
					trust += spyState.get(c);
			}
			trust /= n;
			return (trust >= 0);
		}
	}  

  /**
   * Reports the votes for the previous mission
   * @param yays the names of the agents who voted for the mission
   **/
  public void get_Votes(String yays){
    this.yays = yays;
  }
  /**
   * Reports the agents being sent on a mission.
   * Should be able to be infered from tell_ProposedMission and tell_Votes, but incldued for completeness.
   * @param mission the Agents being sent on a mission
   **/
  public void get_Mission(String mission){
		missionNum++;
		this.mission = mission;
  }

  /**
   * Agent chooses to betray or not.
   * @return true if agent betrays, false otherwise
   **/
  public boolean do_Betray(){
		if(!spy)
			return false;
		if(failures > 2)
			return true; //Betray if one more failure is needed for victory
		int spynum = 0;
		for(char c : proposed.toCharArray()) //Count spies on mission
		{
			if(spies.indexOf(c) != -1)
				spynum++;
		}
		if(spynum == 1) //Self is only spy
			return doProb(onlySpyBetrayProb);
		else if(spynum == mission.length()) //Mission is all spies
			return doProb(allSpyBetrayProb);
		else //Mission is multiple spies
			return doProb(multSpyBetrayProb);
   }  

  /**
   * Reports the number of people who betrayed the mission
   * @param traitors the number of people on the mission who chose to betray (0 for success, greater than 0 for failure)
   **/
  public void get_Traitors(int traitors){
		this.traitors = traitors;
		if(traitors == mission.length()) //If everyone betrayed, everyone must be a spy
		{
			for(char c : mission.toCharArray())
			{
				spyState.put(c,Double.NEGATIVE_INFINITY);
			}
		}
		if(traitors == 0)
		{
			add(leader.charAt(0),goodNomValue);
			for(char c : yays.toCharArray())
				add(c,goodVoteValue);
			for(char c : mission.toCharArray())
				add(c,goodMissionValue);
		}
		else
		{
			add(leader.charAt(0),-badNomValue);
			for(char c : yays.toCharArray())
				add(c,-badVoteValue);
			for(char c : mission.toCharArray())
				add(c,-badMissionValue);
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
		return spyState.toString();
  }

  /**
   * Optional method to process an accusation.
   * @param accuser the name of the agent making the accusation.
   * @param accused the names of the Agents being Accused, concatenated in a String.
   * */
  public void get_Accusation(String accuser, String accused){

  }
  
  //Returns true with probability p
  public boolean doProb(double p)
  {
	  return (rand.nextDouble() < p);
  }
  
  //Add amount to entry of spyState map
  public void add(char c, double d)
  {
	  if(spyState.containsKey(c))
		spyState.put(c,spyState.get(c)+d);
  }
  
}
