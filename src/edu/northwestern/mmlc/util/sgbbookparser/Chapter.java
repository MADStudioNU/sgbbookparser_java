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
	private ArrayList<Meetup> meetupList;
	private ArrayList<Character> solitaryMentions;
	
	public Chapter(String newIdentifier) {
		identifier = newIdentifier;
		meetupList = new ArrayList<Meetup>();
		solitaryMentions = new ArrayList<Character>();
	}
	
	public String identifier() {
		return identifier;
	}

	public void addToMeetupList(Meetup meetup) {
		// TODO Auto-generated method stub
		meetupList.add(meetup);
	}
	
	public void addToSolitaryMentions(Character newCharacter) {
	        solitaryMentions.add(newCharacter);
	}


	public ArrayList<Meetup> allMeetups()
	{
		ArrayList<Meetup> combinedVector = new ArrayList<Meetup>();
		combinedVector.addAll(meetupList);
		return combinedVector;
	    }

	public ArrayList<Character> listOfCharacters() {
		Set<Character> characterSet = new HashSet<Character>();
        Meetup meetupTemp;
        for(Iterator<Meetup> meetupIterator = meetupList.iterator(); meetupIterator.hasNext(); characterSet.add(meetupTemp.secondCharacter()))
        {
            meetupTemp = (Meetup)meetupIterator.next();
            characterSet.add(meetupTemp.firstCharacter());
        }

        for(Iterator<Character> solitaryMentionsIterator = solitaryMentions.iterator(); solitaryMentionsIterator.hasNext(); characterSet.add((Character)solitaryMentionsIterator.next()));
        ArrayList<Character> allCharacters = new ArrayList<Character>();
        allCharacters.addAll(characterSet);
        return allCharacters;

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
