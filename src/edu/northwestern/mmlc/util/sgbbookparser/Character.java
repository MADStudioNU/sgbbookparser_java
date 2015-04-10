package edu.northwestern.mmlc.util.sgbbookparser;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import edu.northwestern.mmlc.util.sgbbookparser.NaturalOrderComparator;

public class Character {

	private String identifier;
	private String shortName;
	private String longName;
	private ArrayList<Meetup> meetupsAsFirstCharacter;
	private ArrayList<Meetup> meetupsAsSecondCharacter;
		
	public Character(String newIdentifier, String newShortName, String newLongName) {
		identifier = newIdentifier;
		shortName = newShortName;
		longName = newLongName;
		meetupsAsFirstCharacter = new ArrayList<Meetup>();
		meetupsAsSecondCharacter = new ArrayList<Meetup>();
	}
	
	public String identifier() {
		return identifier;
	}
	public String shortName() {
		return shortName;
	}
	public String longName() {
		return longName;
	}

	public void addToMeetupsAsFirstCharacter(Meetup meetup) {
		meetupsAsFirstCharacter.add(meetup);
	}

	public void addToMeetupsAsSecondCharacter(Meetup meetup) {
		meetupsAsSecondCharacter.add(meetup);
	}
	
	public ArrayList<Meetup> allMeetups() {
		ArrayList<Meetup> combinedVector = new ArrayList<Meetup>();
		combinedVector.addAll(meetupsAsFirstCharacter);
		combinedVector.addAll(meetupsAsSecondCharacter);
		return combinedVector;
	}
	
	public ArrayList<Character> allCharactersEncountered() {
		Set<Character> characterSet = new HashSet<Character>();
		// for all first character meetups, add the second characters
		Iterator<Meetup> firstCharsMeetupIter = meetupsAsFirstCharacter.iterator();
		while (firstCharsMeetupIter.hasNext()) {
			characterSet.add(((Meetup)firstCharsMeetupIter.next()).secondCharacter()); 
		}
		// for all second character meetups, add the first character
		Iterator<Meetup> secondCharsMeetupIter = meetupsAsSecondCharacter.iterator();
		while (secondCharsMeetupIter.hasNext()) {
			characterSet.add(((Meetup)secondCharsMeetupIter.next()).firstCharacter()); 
		}
		ArrayList<Character> allChars = new ArrayList<Character>();
		allChars.addAll(characterSet);
		return allChars;
	}
	
	public ArrayList<Chapter> allChapters() {
		Set<Chapter> chapterSet = new HashSet<Chapter>();
		Iterator<Meetup> chapterMeetupIter = allMeetups().iterator();
		while((chapterMeetupIter).hasNext()) {
			chapterSet.add(((Meetup)chapterMeetupIter.next()).chapter());
		}
		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
		allChapters.addAll(chapterSet);
		return allChapters;
	}
	
	public ArrayList<String> sortedListOfCharactersEncountered() {
		ArrayList<String> characterNames = new ArrayList<String>();
		Iterator<Character> allChars = allCharactersEncountered().iterator();
		while(allChars.hasNext()) {
			characterNames.add(allChars.next().identifier());
		}
		Collections.sort(characterNames, new NaturalOrderComparator());
		return characterNames;
	}
	
	public ArrayList<String> sortedListOfChaptersEncountered() {
		ArrayList<String> chapterNames = new ArrayList<String>();
		Iterator<Chapter> allChaps = allChapters().iterator();
		while(allChaps.hasNext()) {
			chapterNames.add(allChaps.next().identifier());
		}
		Collections.sort(chapterNames, new NaturalOrderComparator());
		return chapterNames;
	}
	
	public int compareTo(Character comparisonCharacter) {		 
		// basic sorting (not natural sort)
		String comparisonCharacterIdentifier = ((Character) comparisonCharacter).identifier(); 
 		return this.identifier().compareTo(comparisonCharacterIdentifier);
	}
 
	public static Comparator<Character> CharacterLongNameNaturalSort = new Comparator<Character>() {
		NaturalOrderComparator comparator = new NaturalOrderComparator();
 
	    public int compare(Character character1, Character character2) {
 
	      String characterName1 = character1.identifier().toUpperCase();
	      String characterName2 = character2.identifier().toUpperCase();
 
	      return comparator.compare(characterName1, characterName2);
	    }
 
	};
	
}
