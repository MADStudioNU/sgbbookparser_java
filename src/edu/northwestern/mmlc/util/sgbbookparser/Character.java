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
	// where the character is the first character of a pair of characters meeting
	private ArrayList<MeetupPair> meetupsAsFirstCharacter;
	// where the character is the second character of a pair of characters meeting
	private ArrayList<MeetupPair> meetupsAsSecondCharacter;
	// a list of single occurrences (solitary mentions) for the character
	private ArrayList<Chapter> solitaryMentions;
	private ArrayList<CharacterGroupEvent> characterGroupEventList;
		
	public Character(String newIdentifier, String newShortName, String newLongName) {
		identifier = newIdentifier;
		shortName = newShortName;
		longName = newLongName;
		meetupsAsFirstCharacter = new ArrayList<MeetupPair>();
		meetupsAsSecondCharacter = new ArrayList<MeetupPair>();
		solitaryMentions = new ArrayList<Chapter>();
		characterGroupEventList = new ArrayList<CharacterGroupEvent>();
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

	public void addToMeetupsAsFirstCharacter(MeetupPair meetupPair) {
		meetupsAsFirstCharacter.add(meetupPair);
	}

	public void addToMeetupsAsSecondCharacter(MeetupPair meetupPair) {
		meetupsAsSecondCharacter.add(meetupPair);
	}
	
	public void addToListOfSolitaryMentions(Chapter chapter) {
		solitaryMentions.add(chapter);
	}
	
	public void addToCharacterGroupEventList(CharacterGroupEvent groupEvent) {
		characterGroupEventList.add(groupEvent);
	}
	
	public ArrayList<CharacterGroupEvent> listOfCharacterGroupEvents() {
		ArrayList<CharacterGroupEvent> combinedVector = new ArrayList<CharacterGroupEvent>();
		combinedVector.addAll(characterGroupEventList);
		return combinedVector;
	}
	
	public ArrayList<MeetupPair> allMeetups() {
		ArrayList<MeetupPair> combinedVector = new ArrayList<MeetupPair>();
		combinedVector.addAll(meetupsAsFirstCharacter);
		combinedVector.addAll(meetupsAsSecondCharacter);
		return combinedVector;
	}
	
	public ArrayList<Chapter> allSolitaryMentions() {
		return solitaryMentions;
	}
	
	public ArrayList<Character> allCharactersEncountered() {
		Set<Character> characterSet = new HashSet<Character>();
		// for all first character meetups, add the second characters
		Iterator<MeetupPair> firstCharsMeetupIter = meetupsAsFirstCharacter.iterator();
		while (firstCharsMeetupIter.hasNext()) {
			characterSet.add(firstCharsMeetupIter.next().secondCharacter()); 
		}
		// for all second character meetups, add the first character
		Iterator<MeetupPair> secondCharsMeetupIter = meetupsAsSecondCharacter.iterator();
		while (secondCharsMeetupIter.hasNext()) {
			characterSet.add(secondCharsMeetupIter.next().firstCharacter()); 
		}
		ArrayList<Character> allChars = new ArrayList<Character>();
		allChars.addAll(characterSet);
		return allChars;
	}
	
	public ArrayList<Chapter> allChaptersWithMeetups() {
		Set<Chapter> chapterSet = new HashSet<Chapter>();
		Iterator<MeetupPair> chapterMeetupIter = allMeetups().iterator();
		while((chapterMeetupIter).hasNext()) {
			chapterSet.add(chapterMeetupIter.next().chapter());
		}
		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
		allChapters.addAll(chapterSet);
		return allChapters;
	}
	
	public ArrayList<Chapter> allChaptersWithSolitaryMentions() {
		Set<Chapter> chapterSet = new HashSet<Chapter>();
		Iterator<Chapter> chapterIter = solitaryMentions.iterator();
		while(chapterIter.hasNext()) {
			chapterSet.add(chapterIter.next());
		}
		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
		allChapters.addAll(chapterSet);
		return allChapters;
	}
	
	public ArrayList<Chapter> allChaptersAppearingIn() {
		Set<Chapter> chapterSet = new HashSet<Chapter>();
		Iterator<Chapter> meetupChaptersIter = allChaptersWithMeetups().iterator(); 
		while(meetupChaptersIter.hasNext()) {
			chapterSet.add(meetupChaptersIter.next());
		}
		Iterator<Chapter> singleAppearanceChaptersIter = allChaptersWithSolitaryMentions().iterator();
		while(singleAppearanceChaptersIter.hasNext()) {
			chapterSet.add(singleAppearanceChaptersIter.next());
		}
		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
		allChapters.addAll(chapterSet);
		return allChapters;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> sortedListOfChaptersWithSolitaryMentions() {
		ArrayList<String> chapterNames = new ArrayList<String>();
		Iterator<Chapter> allChaps = allChaptersWithSolitaryMentions().iterator();
		while(allChaps.hasNext()) {
			chapterNames.add(allChaps.next().identifier());
		}
		Collections.sort(chapterNames, new NaturalOrderComparator());
		return chapterNames;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> sortedListOfCharacterIdentifiersEncountered() {
		ArrayList<String> characterNames = new ArrayList<String>();
		Iterator<Character> allChars = allCharactersEncountered().iterator();
		while(allChars.hasNext()) {
			characterNames.add(allChars.next().identifier());
		}
		Collections.sort(characterNames, new NaturalOrderComparator());
		return characterNames;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> sortedListOfChapterIdentifiersWithEncounters() {
		ArrayList<String> chapterNames = new ArrayList<String>();
		Iterator<Chapter> allChaps = allChaptersWithMeetups().iterator();
		while(allChaps.hasNext()) {
			chapterNames.add(allChaps.next().identifier());
		}
		Collections.sort(chapterNames, new NaturalOrderComparator());
		return chapterNames;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> sortedListOfChapterAppearances() {
		ArrayList<String> chapterNames = new ArrayList<String>();
		Iterator<Chapter> allChaps = allChaptersAppearingIn().iterator();
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
