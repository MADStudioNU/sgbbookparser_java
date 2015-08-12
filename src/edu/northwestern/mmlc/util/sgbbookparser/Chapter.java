package edu.northwestern.mmlc.util.sgbbookparser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class Chapter implements Comparable<Chapter> {
	private String identifier;
	private ArrayList<CharacterGroupEvent> characterGroupEventList;
	private ArrayList<MeetupPair> meetupPairList;
	private ArrayList<Character> solitaryMentions;
	
	public Chapter(String newIdentifier) {
		identifier = newIdentifier;
		meetupPairList = new ArrayList<MeetupPair>();
		solitaryMentions = new ArrayList<Character>();
		characterGroupEventList = new ArrayList<CharacterGroupEvent>();
	}
	
	public String identifier() {
		return identifier;
	}

	public void addToMeetupList(MeetupPair meetupPair) {
		// TODO Auto-generated method stub
		meetupPairList.add(meetupPair);
	}
	
	public void addToSolitaryMentions(Character newCharacter) {
	        solitaryMentions.add(newCharacter);
	}
	
	public void addToCharacterGroupEventList(CharacterGroupEvent groupEvent) {
		characterGroupEventList.add(groupEvent);
	}


	public ArrayList<MeetupPair> allMeetups()
	{
		ArrayList<MeetupPair> combinedVector = new ArrayList<MeetupPair>();
		combinedVector.addAll(meetupPairList);
		return combinedVector;
	    }

	public ArrayList<Character> listOfCharacters() {
		Set<Character> characterSet = new HashSet<Character>();
        MeetupPair meetupTemp;
        for(Iterator<MeetupPair> meetupIterator = meetupPairList.iterator(); meetupIterator.hasNext(); characterSet.add(meetupTemp.secondCharacter()))
        {
            meetupTemp = (MeetupPair)meetupIterator.next();
            characterSet.add(meetupTemp.firstCharacter());
        }

        for(Iterator<Character> solitaryMentionsIterator = solitaryMentions.iterator(); solitaryMentionsIterator.hasNext(); characterSet.add((Character)solitaryMentionsIterator.next()));
        ArrayList<Character> allCharacters = new ArrayList<Character>();
        allCharacters.addAll(characterSet);
        return allCharacters;

	}
	
	public ArrayList<CharacterGroupEvent> listOfCharacterGroupEvents() {
		ArrayList<CharacterGroupEvent> combinedVector = new ArrayList<CharacterGroupEvent>();
		combinedVector.addAll(characterGroupEventList);
		return combinedVector;
	}
	
	
	public int compareTo(Chapter compcomparisonChapter) {		 
		// basic sorting (not natural sort)
		String comparisonIdentifier = ((Chapter) compcomparisonChapter).identifier(); 
 		return this.identifier().compareTo(comparisonIdentifier);
	}
 
	public static Comparator<Chapter> ChapterNaturalSort = new Comparator<Chapter>() {
		NaturalOrderComparator comparator = new NaturalOrderComparator();
 
	    public int compare(Chapter chapter1, Chapter chapter2) {
 
	      String chapterName1 = chapter1.identifier().toUpperCase();
	      String chapterName2 = chapter2.identifier().toUpperCase();
 
	      return comparator.compare(chapterName1, chapterName2);
	    }
 
	};
	
	
	
	
}
