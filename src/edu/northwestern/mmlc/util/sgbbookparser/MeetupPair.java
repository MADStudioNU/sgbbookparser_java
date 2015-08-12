package edu.northwestern.mmlc.util.sgbbookparser;


// meetup pairs represent the deconstruction of a meetup into combination tuples of two nodes
// in general, a meetup should always be stored with the lowest order character first

public class MeetupPair {

	private Chapter chapter;
	private Character firstCharacter;
	private Character secondCharacter;

	public MeetupPair(Chapter newChapter, Character newFirstCharacter, Character newSecondCharacter) {
		chapter = newChapter;
		firstCharacter = newFirstCharacter;
		secondCharacter = newSecondCharacter;
		
		// tell the character about this meetup
		firstCharacter.addToMeetupsAsFirstCharacter(this);
		secondCharacter.addToMeetupsAsSecondCharacter(this);
		chapter.addToMeetupList(this);
	}
		
	public Chapter chapter() {
		return chapter;
	}

	public Character firstCharacter() {
		return firstCharacter;
	}

	public Character secondCharacter() {
		return secondCharacter;
	}
}
