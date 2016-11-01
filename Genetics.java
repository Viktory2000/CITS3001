package s21494053_and_21577338;

import cits3001_2016s2.*;
import java.util.*;
import java.io.*;

/**
 * A class used to simulate the genetics of a population of genomes.
 * @author Lewis Tolonen / 21577338 Viktor Fidanovsi 21494052
 **/

class Genetics
{
	public static final int GENS = 200; //Number of generations
	public static final int POPULATION = 1000; //Population size
	public static final int GAMES_PER_GEN = 1000; //Game played in each generation
	
	/**
	 *Performs the simulation
	 **/s
	public static void main(String[] args)
	{
		Random rand = new Random();
		ArrayList<Genome> pop = new ArrayList<Genome>();
		
		for(int i=0; i<POPULATION; i++) //Initialise population with random genomes
		{
			pop.add(new Genome(i)); //Genome id corresponds to position in list
		}
		
		int gen = 0;
		while(gen < GENS)
		{
			for(int game = 0; game < GAMES_PER_GEN; game++)
			{
				System.out.println("Gen "+gen+" Game "+(game+1));
				//Assign genomes to games randomly
				ArrayList<ArrayList<Genome>> games = new ArrayList<ArrayList<Genome>>();
				ArrayList<Genome> remaining = new ArrayList<Genome>();
				//Copy pop into remaining
				for(int i=0; i<pop.size(); i++)
					remaining.add(pop.get(i));
				int cur = 0;
				games.add(new ArrayList<Genome>());
				while(!remaining.isEmpty())
				{
					int i = rand.nextInt(remaining.size());
					(games.get(cur)).add(remaining.get(i));
					remaining.remove(i);
					if (games.get(cur).size() > 5 && remaining.size() > 5)
					{
						cur++;
						games.add(new ArrayList<Genome>());
					}
				}
				
				//Simulate games to get fitness of each genome
				for(int i=0; i<games.size(); i++)
				{
					Map<Integer,Integer> result = FitGame.playFitGame(games.get(i));
					//System.out.println(result.toString());
					for(Integer n : result.keySet())
					{
						pop.get(n).fitness += result.get(n);
					}
				}
			}
			
			//Eliminate least fit half
			System.out.println("Eliminating");
			Collections.sort(pop); //Sort genomes by fitness
			ArrayList<Genome> newpop = new ArrayList<Genome>();
			for(int i=0; i<pop.size()/2; i++) //Add the best half to the new population
			{
				Genome g = pop.get(i);
				g.id = i; //Assign correct id
				newpop.add(g);
			}
			
			//Display best performing genomes at the end of the generation
			System.out.println(pop.get(0).toString()); //Display best genome
			System.out.println(pop.get(1).toString()); //Display 2nd best genome
			System.out.println(pop.get(2).toString()); //Display 3rd best genome
			
			//Do not alter population after final generation
			if(gen == GENS-1)
				break;
			
			//Refill population via cross over between random pairs of parents
			System.out.println("Reproducing");
			while(newpop.size() < pop.size()) 
			{
				int p1 = rand.nextInt(pop.size()/2);
				int p2 = rand.nextInt(pop.size()/2);
				int index = newpop.size();
				newpop.add(new Genome(index,pop.get(p1),pop.get(p2)));
			}
			
			//Mutate population  and reset fitness
			System.out.println("Mutating");
			for(int i=0; i<newpop.size(); i++)
			{
				newpop.get(i).fitness = 0;
				newpop.get(i).mutate(0.2);
			}
			
			//Replace population
			pop = newpop;
			gen++;
		}
		//Make CSV for scatter plots
		try
		{
			PrintWriter writer = new PrintWriter("Result.csv", "UTF-8");
			writer.println("Fitness,multSpyVote,noSpyVote,onlySpyBetray,multSpyBetray,allSpyBetray,teamThresh");
			for(Genome g : pop)
			{
				writer.println(g.fitness+","+g.multSpyVoteProb+","+g.noSpyVoteProb+","+g.onlySpyBetrayProb+","+g.multSpyBetrayProb+","+g.allSpyBetrayProb+","+g.teamThreshold);
			}
			writer.close();
		}
		catch(Exception e)
		{
			System.out.println("Error creating CSV file");
		}
	}
	
}