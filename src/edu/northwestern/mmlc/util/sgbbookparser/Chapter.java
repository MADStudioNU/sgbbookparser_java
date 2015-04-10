package edu.northwestern.mmlc.util.sgbbookparser;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;


public class Chapter implements Comparable<Chapter> {
	private String identifier;
	private Vector<Meetup> meetupList;
	
	public Chapter(String newIdentifier) {
		identifier = newIdentifier;
		meetupList = new Vector<Meetup>();
	}
	
	public String identifier() {
		return identifier;
	}

	public void addToMeetupList(Meetup meetup) {
		// TODO Auto-generated method stub
		meetupList.add(meetup);
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
