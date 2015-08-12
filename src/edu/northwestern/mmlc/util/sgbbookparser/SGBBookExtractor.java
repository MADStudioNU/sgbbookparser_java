package edu.northwestern.mmlc.util.sgbbookparser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.print.attribute.HashDocAttributeSet;

/* imports */

public class SGBBookExtractor {
	
	private Hashtable<String, Chapter> chapterTable;
	private Hashtable<String, Character> characterTable;
	private Vector<MeetupPair> masterListOfMeetupPairs;
	private Vector<CharacterGroupEvent> masterListOfCharacterGroupEvents;

	
    public static void main(String args[])
    {
        SGBBookExtractor bookProcessor = null;
        String PATH_TO_SGB_FILE = null;
        String PATH_TO_NODE_CSV = null;
        String PATH_TO_EDGE_CSV = null;
        String PATH_TO_CHARACTER_TABLE_OUTPUT = null;
        String PATH_TO_CHAPTER_TABLE_OUTPUT = null;
        try
        {
            PATH_TO_SGB_FILE = args[0];
            PATH_TO_NODE_CSV = args[1];
            PATH_TO_EDGE_CSV = args[2];
            PATH_TO_CHARACTER_TABLE_OUTPUT = args[3];
            PATH_TO_CHAPTER_TABLE_OUTPUT = args[4];
        }
        catch(Exception e)
        {
            System.err.println("java -jar sgbbookparser.jar PATH_TO_SGB_FILE.dat PATH_TO_NODE_CSV_OUTPUT.csv PATH_TO_EDGE_CSV_OUTPUT.csv PATH_TO_CHARACTER_TABLE_OUTPUT.html PATH_TO_CHAPTER_TABLE_OUTPUT.html");
            System.exit(1);
        }
        try
        {
            bookProcessor = new SGBBookExtractor(readFile(PATH_TO_SGB_FILE));
        }
        catch(Exception e)
        {
            System.err.println("Can't read book file");
            System.exit(1);
        }
        try
        {
            writeFile(PATH_TO_NODE_CSV, bookProcessor.generateNodeCSV());
            writeFile(PATH_TO_EDGE_CSV, bookProcessor.generateEdgeCSV());
            writeFile(PATH_TO_CHARACTER_TABLE_OUTPUT, bookProcessor.generateHTMLCharacterTable(PATH_TO_CHARACTER_TABLE_OUTPUT, PATH_TO_CHAPTER_TABLE_OUTPUT));
            writeFile(PATH_TO_CHAPTER_TABLE_OUTPUT, bookProcessor.generateHTMLChapterTable(PATH_TO_CHAPTER_TABLE_OUTPUT, PATH_TO_CHARACTER_TABLE_OUTPUT));
        }
        catch(Exception e)
        {
            System.err.println("Can't write one or more output files");
            e.printStackTrace();
            System.exit(1);
        }
    }
	public SGBBookExtractor(String sourceFileData) {

		chapterTable = new Hashtable<String, Chapter>();
		characterTable = new Hashtable<String, Character>();
		masterListOfMeetupPairs = new Vector<MeetupPair>();
		masterListOfCharacterGroupEvents = new Vector<CharacterGroupEvent>();

		
		parseSourceFile(sourceFileData);
		
	}
	
	/* Should deliver a hashtable of "characterdata" and "meetupdata" */
	private void parseSourceFile(String fileContent) {
		Vector<String> characterSection = new Vector<String>(),
				        meetupSection = new Vector<String>();
		int section = 0;	// section 0 is character section; 1 is meetup data
		StringReader sr = new StringReader(fileContent);
		BufferedReader bsr = new BufferedReader(sr);
		String fileLine = null;
		try {
			fileLine = bsr.readLine();
		} catch (Exception e) {
			fileLine = null;
		}
		while (fileLine != null) {
			
			
			if (fileLine.matches("\\*.*")) {
				// if begins with a comment, ignore
			}
			else if (fileLine.trim().equals("")) {
				// empty lines between sections
				section++;
			}
			else if (section == 0) {
				characterSection.add(fileLine);
			}
			else if (fileLine.matches("&:.*")){
				// includes &:, so post pend to previousline
				String meetupSectionLastLine = meetupSection.lastElement();
				String newLastLine = new String(meetupSectionLastLine.concat("," + fileLine.replaceFirst("&:", "")));
				meetupSection.remove(meetupSectionLastLine);
				meetupSection.add(newLastLine);
			}
			else {
				meetupSection.add(fileLine);
			}
			try {
				fileLine = bsr.readLine();
			} catch (Exception e) {
				fileLine = null;
			}
		}
	
		// let the parsing begin!
		populateCharacterTables(characterSection);
		try {
		populateMeetupAndChapterTables(meetupSection);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return;
		
	}
	
	private void populateCharacterTables(Vector<String> characterData) {
		Iterator<String> characterDataIterator = characterData.iterator();
		while (characterDataIterator.hasNext()) {
			String characterString = characterDataIterator.next();
			if (characterString != null) {
				// split on first space in the character line
				String[] idAndDescription = characterString.trim().split("\\s", 2);
				// if there is at least one part assume first part is identifier, second part description, otherwise just id
				String identifier = (idAndDescription.length > 1 ) ? idAndDescription[0].trim() : characterString.trim(); 
				// if there are two parts the second part is long and short name descriptions
				String longAndShortName = (idAndDescription.length > 1) ? idAndDescription[1] : idAndDescription[0];				

				// a series of tabs might separate a long description from a short label 
				String[] longAndShortNameParts = longAndShortName.trim().split("\\t", 2);
				String longName = (longAndShortNameParts.length > 1) ? longAndShortNameParts[0].trim() : longAndShortName.trim();
				String shortName = (longAndShortNameParts.length > 1) ? longAndShortNameParts[1].trim() : longAndShortName.trim();
				
				// ready to create character
				//System.out.println("CHAR: " + identifier + " SN: " + shortName + " LN: " + longName);
				Character newCharacter = new Character(identifier, shortName, longName);
				characterTable.put(identifier, newCharacter);
			}
		}	
	}
	
	private void populateMeetupAndChapterTables(Vector<String> meetupData) throws Exception {
		Iterator<String> meetupDataIterator = meetupData.iterator();
		while (meetupDataIterator.hasNext()) {
			String chapterAndMeetupString = meetupDataIterator.next();
			if (chapterAndMeetupString != null) {
				if(chapterAndMeetupString.contains(":")) {
					// split chapter identifier and meet-up list on first colon
					String[] chapterAndMeetupParts = chapterAndMeetupString.split(":", 2);
					String chapterIdentifier = (chapterAndMeetupParts.length > 1 ) ? chapterAndMeetupParts[0].trim() : chapterAndMeetupString.trim();
					if (chapterAndMeetupParts.length > 1 && chapterAndMeetupParts[1] != null && !(chapterAndMeetupParts[1].trim().equals(""))) {						
						// if there is meet-up data let's split on semicolons for meetup groups
						String[] meetupGroups = chapterAndMeetupParts[1].split(";");
						// go through each meet-up group
						for (int meetupGroupCounter = 0; meetupGroupCounter < meetupGroups.length; meetupGroupCounter++) {
							// split the meet-up-group into individual participant IDs
							String[] meetupParticipants = meetupGroups[meetupGroupCounter].split(",");
							Chapter chapter = existingOrNewChapterWithIdentifier(chapterIdentifier.trim());
							
							// create a meetupGroup
							CharacterGroupEvent newCharacterGroupEvent = new CharacterGroupEvent(chapter);
							for (int groupMeetupParticipantIndex = 0; groupMeetupParticipantIndex < meetupParticipants.length; groupMeetupParticipantIndex++) {
								Character singleCharacter = characterWithIdentifier(meetupParticipants[groupMeetupParticipantIndex].trim());
								if(singleCharacter == null) {
									throw new Exception("No character found with identifier: " + meetupParticipants[0].trim());
								}
								newCharacterGroupEvent.addCharacter(singleCharacter);
							}
							
							if (newCharacterGroupEvent.allCharacters().size() < 2 ) {												
								// then process the solitary mentions
								//if (meetupParticipants[0] != null && meetupParticipants[0].trim().length() > 0) { }
								Character singleCharacter = newCharacterGroupEvent.allCharacters().get(0);
								if(singleCharacter == null) {
										throw new Exception("No character found with identifier: " + meetupParticipants[0].trim());
								}
								singleCharacter.addToListOfSolitaryMentions(chapter);
								chapter.addToSolitaryMentions(singleCharacter);
								addToCharacterGroupEventMasterList(newCharacterGroupEvent);
								
								
							} else {
								// we want to generate a list of meet-ups: combination of things 2 at a time
								//System.out.println("Adding charactergroup: " + newCharacterGroupEvent.manfiest());
								for (int outerMeetupCounter = 0; outerMeetupCounter < newCharacterGroupEvent.allCharacters().size() - 1; outerMeetupCounter++) {
									for (int innerMeetupCounter = outerMeetupCounter + 1; innerMeetupCounter < newCharacterGroupEvent.allCharacters().size(); innerMeetupCounter++) {
										Character char1 = newCharacterGroupEvent.allCharacters().get(outerMeetupCounter);
										Character char2 = newCharacterGroupEvent.allCharacters().get(innerMeetupCounter);
										if (char1 == null || char2 == null) {
											throw new Exception("One or more characters not found in character table.");
										}
										if (char1.compareTo(char2) > 0) {
											// swap the order by alphabet
											Character tempFutureChar1 = char2;
											char2 = char1;
											char1 = tempFutureChar1;
											tempFutureChar1 = null;
										}
										//System.out.println("Meetup: " + chapter.identifier() + ": " + char1.identifier() + " " + char2.identifier());
										MeetupPair newMeetup = new MeetupPair(chapter, char1, char2);
										addToMeetupPairMasterList(newMeetup);
									}
								}
								addToCharacterGroupEventMasterList(newCharacterGroupEvent);
							}							
						}
					}
					
				} else {
					// ignore this line, if it doesn't have colon, it doesn't have meet-ups
				}
			}
			
		}
		
	}
	
	
	/* 
	 * generates node CSV suitable for Gephi
	 */
	public String generateNodeCSV() {
		Collection<Character> characters = characterTable.values();
		Iterator<Character> characterIterator = characters.iterator();
		
		String csv = "Id,Label,Description" + "\n";
		while (characterIterator.hasNext()) {
			Character character = characterIterator.next();
			csv += ( "\"" + character.identifier() + "\",\"" + character.shortName().trim() + "\",\"" + character.longName().trim() + "\"" + "\n");
		}
		return csv;
	}
	
	/* 
	 * generates edge CSV suitable for Gephi
	 */
	public String generateEdgeCSV() {
		Iterator<MeetupPair> meetupIterator = masterListOfMeetupPairs.iterator();
		
		String csv = "Source,Target,Type,Id,Label" + "\n";
		int lineIDCounter = 0;
		while(meetupIterator.hasNext()) {
			lineIDCounter++;
			MeetupPair meetupPair = meetupIterator.next();
			csv += ( "\"" + meetupPair.firstCharacter().identifier().trim() + "\",\"" 
					+ meetupPair.secondCharacter().identifier().trim() + "\",\""
					+ "Undirected" + "\",\""
					+ lineIDCounter + "\",\""
					+ meetupPair.chapter().identifier() + "\""
					+ "\n");
		}
		return csv;	
	}
	
	public String generateHTMLCharacterTable(String myFilePath, String filePathToChapterTable) {
		String CHAPTERTABLEURI = new File(myFilePath).getParentFile().toURI().relativize(new File(filePathToChapterTable).toURI()).getPath();

		String htmlPreamble = "<html>" + "\n"
				   + "<head>" + "\n"
				   + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" + "\n"
				   + "<meta charset=\"utf-8\" />" + "\n"
				   + "<title>Character Data</title>"+ "\n"
				   + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js\"></script>" + "\n"
				   + "<script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.21.5/js/jquery.tablesorter.js\"></script>" + "\n"
				   + "</head>"+ "\n"
				   + "<body>"+ "\n"
				   + "<script type=\"text/javascript\">"+ "\n"
				   + "$(document).ready(function()"+ "\n"
				   + "{" + "\n"
	               + "$(\"#myTable\").tablesorter();" + "\n"
				   + "}"+ "\n"
				   + ");" + "\n"
				   + "</script>"+ "\n"
				   + "<table id=\"myTable\" class=\"tablesorter\" style=\"\">"+ "\n"
				   + "<thead>"+ "\n"
				   + "<tr valign=bottom>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Abbr.</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Long Name</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Short Name</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Number of Chapters in which Character Appears</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Number of Ecounters</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Chapters Where Encounters Occur</th>"+ "\n"	               
	               + "<th align=\"left\" width=\"1%\">Characters Encountered</th>"+ "\n"
	               + "</tr>" + "\n"
	               + "</thead>"+ "\n"
	               + "<tbody>"+ "\n";
		String htmlPostamble = "</tbody>" + "\n"
	               + "</table>" + "\n"
				   + "</body>" + "\n"
	               + "</html>" + "\n";
		
		String innerTableBody = "";
		
		// build the table by the list of all characters
		Collection<Character> characters = characterTable.values();
		Iterator<Character> characterIterator = characters.iterator();
        int count = 1;
		while (characterIterator.hasNext()) {
			Character character = characterIterator.next();
			Set<Chapter> chaptersWhereCharacterAppears = new HashSet<Chapter>();
			// in which chapters was this character simply mentioned
			StringBuilder chapterListOfSolitaryMentionsSB = new StringBuilder();
			Iterator<String> solitaryMentionchapterListIterator = character.sortedListOfChaptersWithSolitaryMentions().iterator();
			while (solitaryMentionchapterListIterator.hasNext()) {
				if (chapterListOfSolitaryMentionsSB.length() > 0) chapterListOfSolitaryMentionsSB.append(", ");
				String chapterIdentifierIter = solitaryMentionchapterListIterator.next();
				chapterListOfSolitaryMentionsSB.append("<a href=\"" + CHAPTERTABLEURI + "#" + idifyString("chapter", chapterIdentifierIter) + "\"" + ">" + chapterIdentifierIter + "</a>");
				chaptersWhereCharacterAppears.add(chapterTable.get(chapterIdentifierIter));
			}
			// what chapters did this character meet/encounter other people in
			StringBuilder chapterListWithEncountersSB = new StringBuilder();
			Iterator<String> chapterListIterator = character.sortedListOfChapterIdentifiersWithEncounters().iterator();
			while (chapterListIterator.hasNext()) {
				if (chapterListWithEncountersSB.length() > 0) chapterListWithEncountersSB.append(", ");
				String chapterIdentifierIterator = chapterListIterator.next();
				chapterListWithEncountersSB.append("<a href=\"" + CHAPTERTABLEURI + "#" + idifyString("chapter", chapterIdentifierIterator) + "\"" + ">" + chapterIdentifierIterator + "</a>");
				chaptersWhereCharacterAppears.add(chapterTable.get(chapterIdentifierIterator));
			}
			// what people did this character meet?
			StringBuilder characterListSB = new StringBuilder();
			Iterator<String> characterListIterator = character.sortedListOfCharacterIdentifiersEncountered().iterator();
			while (characterListIterator.hasNext()) {
				if (characterListSB.length() > 0) characterListSB.append(", ");
				String characterIdentifier = characterListIterator.next();			
				characterListSB.append("<span title=\"" + characterTable.get(characterIdentifier).longName() + "\">" + characterIdentifier + "</span>"); 
			}
			
			String nextTableRow = "<tr valign=top id=\"" + idifyString("character", character.identifier()) + "\" data-character=\"" + character.identifier() + "\"" + ">" + "\n"
					+ "<td>" + character.identifier() + "</td>" + "\n"
					+ "<td>" + character.longName() + "</td>" + "\n"
					+ "<td>" + character.shortName() + "</td>" + "\n"
					+ "<td>" + (chaptersWhereCharacterAppears.size()) + "</td>" + "\n"
					+ "<td>" + character.allMeetups().size() + "</td>" + "\n"
					+ "<td>" + chapterListWithEncountersSB.toString() + "</td>" + "\n"
					+ "<td>" + characterListSB.toString() + "</td>" + "\n";
			
			innerTableBody += nextTableRow;
			count++;
		}
		
		return htmlPreamble + innerTableBody + htmlPostamble;
		
	}
	
	@SuppressWarnings("unchecked")
	public String generateHTMLChapterTable(String myFilePath, String filePathToCharacterTable) {
		String CHARACTERTABLEURI = new File(myFilePath).getParentFile().toURI().relativize(new File(filePathToCharacterTable).toURI()).getPath();
		
		String htmlPreamble = "<html>" + "\n"
				   + "<head>" + "\n"
				   + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" + "\n"
				   + "<meta charset=\"utf-8\" />" + "\n"
				   + "<title>Chapter Data</title>"+ "\n"
				   + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js\"></script>" + "\n"
				   + "<script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.21.5/js/jquery.tablesorter.js\"></script>" + "\n"
				   + "</head>"+ "\n"
				   + "<body>"+ "\n"
				   + "<script type=\"text/javascript\">"+ "\n"
				   + "$(document).ready(function()"+ "\n"
				   + "{" + "\n"
	               + "$(\"#myTable\").tablesorter();" + "\n"
				   + "}"+ "\n"
				   + ");" + "\n"
				   + "</script>"+ "\n"
				   + "<table id=\"myTable\" class=\"tablesorter\" style=\"\">"+ "\n"
				   + "<thead>"+ "\n"
				   + "<tr valign=bottom>"+ "\n"
	               // + "<th align=\"left\" width=\"1%\">Sequence</th>"+ "\n" 
	               + "<th align=\"left\" width=\"1%\">Chapter ID</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Number of Characters Appearing in Chapter</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Number of Person-to-Person Encounters</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Meetups and Mentions occuring In Chapter</th>"+ "\n"	               
	               + "</tr>" + "\n"
	               + "</thead>"+ "\n"
	               + "<tbody>"+ "\n";
		String htmlPostamble = "</tbody>" + "\n"
	               + "</table>" + "\n"
				   + "</body>" + "\n"
	               + "</html>" + "\n";
		
		String innerTableBody = "";
		
		// build the table by the list of all chapters, but sort it first
		Collection<Chapter> chapters = chapterTable.values();
        Iterator<Chapter> chapterIterator = chapters.iterator();
        ArrayList<String> sortedChapterIdentifiers = new ArrayList<String>();
        while (chapterIterator.hasNext()) {
        	sortedChapterIdentifiers.add(chapterIterator.next().identifier());        	
        }
        Collections.sort(sortedChapterIdentifiers, new NaturalOrderComparator());
        int count = 1;
        for(Iterator<String> sortedChapterIdentifierIter = sortedChapterIdentifiers.iterator(); sortedChapterIdentifierIter.hasNext();)
        {
        	// build, ultimately, a StringBuffer of encountered characters sorted by natural order comparison
            Chapter chapter = (Chapter)chapterTable.get(sortedChapterIdentifierIter.next());
            ArrayList<String> sortedCharacterIdentifiers = new ArrayList<String>();
            for(Iterator<Character> characterObjectsIter = chapter.listOfCharacters().iterator();
            		characterObjectsIter.hasNext();
            		sortedCharacterIdentifiers.add(((Character)characterObjectsIter.next()).identifier()));
            Collections.sort(sortedCharacterIdentifiers, new NaturalOrderComparator());
            ArrayList<Character> sortedCharacters = new ArrayList<Character>();
            for(Iterator<String> sortedCharacterIdentifierIter = sortedCharacterIdentifiers.iterator();
            		sortedCharacterIdentifierIter.hasNext();
            		sortedCharacters.add((Character)characterTable.get(sortedCharacterIdentifierIter.next())));
            StringBuilder characterListSB = new StringBuilder();
            Iterator<Character> sortedListIterator = sortedCharacters.iterator();
			while (sortedListIterator.hasNext()) {
				if (characterListSB.length() > 0) characterListSB.append(", ");
				Character characterIter = sortedListIterator.next();
				characterListSB.append("<a href=\"" + CHARACTERTABLEURI + "#" + idifyString("character", characterIter.identifier()) + "\"" + "><span title=\"" + characterIter.longName() + "\">" + characterIter.identifier() + "</span></a>");
			}
            
			// build a fancy list of meetup data
			StringBuffer characterMeetupListDisplayStringBuffer = new StringBuffer();
 			ArrayList<MeetupPair> meetupList = chapter.allMeetups();
			Iterator<MeetupPair> meetupListIter = meetupList.iterator();
			while (meetupListIter.hasNext()) {
				if (characterMeetupListDisplayStringBuffer.length() > 0) characterMeetupListDisplayStringBuffer.append("; ");
				MeetupPair aMeetup = meetupListIter.next();
				characterMeetupListDisplayStringBuffer.append("(<a href=\"" + CHARACTERTABLEURI + "#" + idifyString("character", aMeetup.firstCharacter().identifier()) + "\"" + "><span title=\"" + aMeetup.firstCharacter().longName() + "\">" + aMeetup.firstCharacter().identifier() + "</span></a>, "
						+ "<a href=\"" + CHARACTERTABLEURI + "#" + idifyString("character", aMeetup.secondCharacter().identifier()) + "\"" + "><span title=\"" + aMeetup.secondCharacter().longName() + "\">" + aMeetup.secondCharacter().identifier() + "</span></a>)"); 
			}
			
			// build a fancy list of meetupgroups
			StringBuffer characterGroupsDisplayStringBuffer = new StringBuffer();
			ArrayList<CharacterGroupEvent> groupEvents = chapter.listOfCharacterGroupEvents();
			Iterator<CharacterGroupEvent> groupEventsIter = groupEvents.iterator();
			while (groupEventsIter.hasNext()) {
				CharacterGroupEvent nextGroupEvent = groupEventsIter.next();
				// set condition to !nextGroupEvent.isSingleton() to only include meetup groups
				if ( true ) {
					if (characterGroupsDisplayStringBuffer.length() > 0) characterGroupsDisplayStringBuffer.append("; ");
					ArrayList<Character> groupEventParticipants = nextGroupEvent.allCharacters();
					characterGroupsDisplayStringBuffer.append("(");
					StringBuffer rosterDisplay = new StringBuffer();
					Iterator<Character> groupParticipantsIterator = groupEventParticipants.iterator();
					while (groupParticipantsIterator.hasNext()) {
						Character nextCharacter = groupParticipantsIterator.next();
						if (rosterDisplay.length() > 0) {
							rosterDisplay.append(", ");
						}
						rosterDisplay.append("<a href=\"" + CHARACTERTABLEURI + "#" + idifyString("character", nextCharacter.identifier()) + "\"" + "><span title=\"" + nextCharacter.longName() + "\">" + nextCharacter.identifier() + "</span></a>");
					}
					characterGroupsDisplayStringBuffer.append(rosterDisplay);
					characterGroupsDisplayStringBuffer.append(")");

				}
			}
			
			String nextTableRow = "<tr valign=top id=\"" + idifyString("chapter", chapter.identifier()) + "\" data-chapterid=\"" + chapter.identifier() + "\">" + "\n"
					// + "<td>" + count + "</td>" + "\n" 
					+ "<td>" + chapter.identifier() + "</td>" + "\n"
					+ "<td>" + sortedCharacters.size() + "</td>" + "\n"
					+ "<td>" + chapter.allMeetups().size() + "</td>" + "\n"
					+ "<td>" + characterGroupsDisplayStringBuffer.toString() + "</td>" + "\n";
			
			innerTableBody += nextTableRow;
			count++;
		}
		
		return htmlPreamble + innerTableBody + htmlPostamble;
		
		
	}
	
	public static String readFile(String filePath) {
	      BufferedReader bfreader = null;
	      StringBuffer inStringBuffer = new StringBuffer();
	      try {
	    	  bfreader = new BufferedReader(new FileReader(filePath));
	      } catch (Exception e) {
	    	  System.err.println("Can't open: " + filePath + " for reading.");
	    	  System.exit(1);
	      }
	      try {
			   String fileLine = bfreader.readLine();
			   while (fileLine != null) {
			      inStringBuffer.append(fileLine + "\n");
			      fileLine = bfreader.readLine();
			   }
			   bfreader.close();
	      } catch (Exception e) {
	    	  System.err.println("Can't read data from: " + filePath + " .");
	    	  System.exit(1);	    	  
	      }

	      if (inStringBuffer != null) {
	    	  return inStringBuffer.toString();
	      }
	      else return null;
	}
	
	public static void writeFile(String filePath, String content) {		 
		File outFile = new File(filePath);

		try {
			if (!outFile.exists()) {
				outFile.createNewFile();
			}
		} catch (Exception e) {
			System.err.println("Could not create output file at path: " + filePath);
		}
		try {
			FileWriter fw = new FileWriter(filePath);
			fw.write(content);
			fw.close();
		} catch (Exception e) {
			System.err.println("Could not write data to file at path: " + filePath);
			e.printStackTrace();
		}
	}
	
	private Chapter existingOrNewChapterWithIdentifier(String identifier) {
		Chapter possibleResult = chapterTable.get(identifier);
		if (possibleResult == null) {
			possibleResult = new Chapter(identifier);
			chapterTable.put(identifier, possibleResult);
		}
		return possibleResult;
	}
	
	private Character characterWithIdentifier(String identifier) {
		Character possibleResult = characterTable.get(identifier);
		return possibleResult;
	}
	
	private void addToMeetupPairMasterList(MeetupPair meetupPair) {
		masterListOfMeetupPairs.add(meetupPair);
	}
	
	private void addToCharacterGroupEventMasterList(CharacterGroupEvent newCharacterGroupEvent) {
		masterListOfCharacterGroupEvents.add(newCharacterGroupEvent);
	}
	
	private String idifyString(String type, String inString) {
		inString.replaceAll(".", "_");
		return type.trim() + "_" + inString.trim();
	}


}
