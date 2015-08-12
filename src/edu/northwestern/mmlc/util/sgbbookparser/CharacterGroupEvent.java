package edu.northwestern.mmlc.util.sgbbookparser;

import java.util.ArrayList;
import java.util.Iterator;

public class CharacterGroupEvent {

	private Chapter chapter;
	private ArrayList<Character> characterList;

	public CharacterGroupEvent(Chapter newChapter) {
		chapter = newChapter;
		characterList = new ArrayList<Character>();
		chapter.addToCharacterGroupEventList(this);
	}
	
	public CharacterGroupEvent(Chapter newChapter, ArrayList<Character> seedList) {
		chapter = newChapter;
		characterList = new ArrayList<Character>();
		chapter.addToCharacterGroupEventList(this);
		Iterator<Character> seedListItemIter = seedList.iterator();
		while (seedListItemIter.hasNext()) {
			addCharacter(seedListItemIter.next());
		}
	}
		
	public void addCharacter(Character newCharacter) {
		characterList.add(newCharacter);
		newCharacter.addToCharacterGroupEventList(this);
	}
	
	public Chapter chapter() {
		return chapter;
	}

	public ArrayList<Character> allCharacters() {
		return characterList;
	}
	
	// is somehow a character group event of only 1 person, perhaps a mention?
	public boolean isSingleton() {
		return (characterList.size() == 1);
	}
	
	public String manfiest() {
		StringBuffer sb = new StringBuffer();
		Iterator<Character> characterIterator = allCharacters().iterator();
		while (characterIterator.hasNext()) {
			sb.append(characterIterator.next().identifier() + " ");
		}
		return sb.toString();
	}

}
