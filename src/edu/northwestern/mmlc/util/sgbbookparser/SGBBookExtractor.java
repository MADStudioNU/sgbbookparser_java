package edu.northwestern.mmlc.util.sgbbookparser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.print.attribute.HashDocAttributeSet;

/* imports */

public class SGBBookExtractor {
	
	private Hashtable<String, Chapter> chapterTable;
	private Hashtable<String, Character> characterTable;
	private Vector<Meetup> allMeetups;
	private Hashtable<String, Vector<Meetup>> characterPairMeetups;
	private Hashtable<String, Vector<Meetup>> chapterMeetups;
	private Hashtable<String, Vector<Meetup>> characterMeetups;
	
	public static void main(String[] args) {
		SGBBookExtractor bookProcessor = null;
		String PATH_TO_SGB_FILE=null,PATH_TO_NODE_CSV=null,PATH_TO_EDGE_CSV=null,PATH_TO_SORTABLE_TABLE=null;
		try {
			PATH_TO_SGB_FILE 		= args[0];
			PATH_TO_NODE_CSV 		= args[1];
			PATH_TO_EDGE_CSV 		= args[2];
			PATH_TO_SORTABLE_TABLE 	= args[3];
		} catch(Exception e) {
			System.err.println("java -jar BookParser.jar PATH_TO_SGB_FILE.dat PATH_TO_NODE_CSV_OUTPUT.csv PATH_TO_EDGE_CSV_OUTPUT.csv PATH_TO_HTML_TABLE_OUTPUT.html");
			System.exit(1);
		}
		try {
			bookProcessor = new SGBBookExtractor(readFile(PATH_TO_SGB_FILE));	
		} catch(Exception e) {
			System.err.println("Can't read book file");
			System.exit(1);
		}	
		try {
			writeFile(PATH_TO_NODE_CSV, bookProcessor.generateNodeCSV());
			writeFile(PATH_TO_EDGE_CSV, bookProcessor.generateEdgeCSV());
			writeFile(PATH_TO_SORTABLE_TABLE, bookProcessor.generateHTMLTable());
		} catch(Exception e) {
			System.err.println("Can't write one or more output files");
			System.exit(1);
		}
	}

	public SGBBookExtractor(String sourceFileData) {

		chapterTable = new Hashtable<String, Chapter>();
		characterTable = new Hashtable<String, Character>();
		allMeetups = new Vector<Meetup>();
		characterPairMeetups = new Hashtable<String, Vector<Meetup>>();
		chapterMeetups = new Hashtable<String, Vector<Meetup>>();
		characterMeetups = new Hashtable<String, Vector<Meetup>>();
		
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
							if (meetupParticipants.length < 2 ) {
								// not interested in meet-up sizes less than two
							} else {
								// we want to generate a list of meet-ups: combination of things 2 at a time
								Chapter chapter = existingOrNewChapterWithIdentifier(chapterIdentifier.trim());
								for (int outerMeetupCounter = 0; outerMeetupCounter < meetupParticipants.length - 1; outerMeetupCounter++) {
									for (int innerMeetupCounter = outerMeetupCounter + 1; innerMeetupCounter < meetupParticipants.length; innerMeetupCounter++) {
										Character char1 = characterWithIdentifier(meetupParticipants[outerMeetupCounter].trim());
										Character char2 = characterWithIdentifier(meetupParticipants[innerMeetupCounter].trim());
										if (char1 == null || char2 == null) {
											throw new Exception("One or more characters not found in character table: "
													+ meetupParticipants[outerMeetupCounter].trim() + ","
													+ meetupParticipants[innerMeetupCounter].trim());
										}
										if (char1.compareTo(char2) > 0) {
											// swap the order by alphabet
											Character tempFutureChar1 = char2;
											char2 = char1;
											char1 = tempFutureChar1;
											tempFutureChar1 = null;
										}
										//System.out.println("Meetup: " + chapter.identifier() + ": " + char1.identifier() + " " + char2.identifier());
										Meetup newMeetup = new Meetup(chapter, char1, char2);
										addToAllMeetups(newMeetup);
										addToCharacterPairMeetups(char1, char2, newMeetup);
										
										// I don't know why I am doing this
										addToChapterMeetups(chapter, newMeetup);
										addToCharacterMeetups(char1, newMeetup);
										addToCharacterMeetups(char2, newMeetup);
									}
								}							
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
		Iterator<Meetup> meetupIterator = allMeetups.iterator();
		
		String csv = "Source,Target,Type,Id,Label" + "\n";
		int lineIDCounter = 0;
		while(meetupIterator.hasNext()) {
			lineIDCounter++;
			Meetup meetup = meetupIterator.next();
			csv += ( "\"" + meetup.firstCharacter().identifier().trim() + "\",\"" 
					+ meetup.secondCharacter().identifier().trim() + "\",\""
					+ "Undirected" + "\",\""
					+ lineIDCounter + "\",\""
					+ meetup.chapter().identifier() + "\""
					+ "\n");
		}
		return csv;	
	}
	
	public String generateHTMLTable() {
		String htmlPreamble = "<html>" + "\n"
				   + "<head>" + "\n"
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
				   + "<table id=\"myTable\" class=\"tablesorter\" style=\"tr.valign='top';\">"+ "\n"
				   + "<thead>"+ "\n"
				   + "<tr>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Abbr.</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Short Name</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Long Name</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Chapter Count</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Number of Meetups</th>"+ "\n"
	               + "<th align=\"left\" width=\"1%\">Chapters Encountered</th>"+ "\n"
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
				String csv = "Id,Label,Description" + "\n";
		while (characterIterator.hasNext()) {
			Character character = characterIterator.next();
			// what chapters did this character meet peoeple in
			StringBuilder chapterListSB = new StringBuilder();
			Iterator<String> chapterListIterator = character.sortedListOfChaptersEncountered().iterator();
			while (chapterListIterator.hasNext()) {
				if (chapterListSB.length() > 0) chapterListSB.append(", ");
				chapterListSB.append(chapterListIterator.next());
			}
			// what people did this character meet?
			StringBuilder characterListSB = new StringBuilder();
			Iterator<String> characterListIterator = character.sortedListOfCharactersEncountered().iterator();
			while (characterListIterator.hasNext()) {
				if (characterListSB.length() > 0) characterListSB.append(", ");
				characterListSB.append(characterListIterator.next());
			}
			
			String nextTableRow = "<tr>"
					+ "<td>" + character.identifier() + "</td>" + "\n"
					+ "<td>" + character.shortName() + "</td>" + "\n"
					+ "<td>" + character.longName() + "</td>" + "\n"
					+ "<td>" + character.allChapters().size() + "</td>" + "\n"
					+ "<td>" + character.allMeetups().size() + "</td>" + "\n"
					+ "<td>" + chapterListSB.toString() + "</td>" + "\n"
					+ "<td>" + characterListSB.toString() + "</td>" + "\n";
			
			innerTableBody += nextTableRow;
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
	
	private void addToAllMeetups(Meetup meetup) {
		allMeetups.add(meetup);
	}
	
	private void addToCharacterPairMeetups(Character char1, Character char2, Meetup meetup) {
		// get existing meetup list
		String key = char1.identifier().trim() + "|" + char2.identifier().trim();
		Vector<Meetup> meetupList = characterPairMeetups.get(key);
		if (meetupList == null) {
			meetupList = new Vector<Meetup>();
			meetupList.add(meetup);
			characterPairMeetups.put(key, meetupList);
		} else {
			meetupList.add(meetup);
		}
	}
	
	private void addToChapterMeetups(Chapter chapter, Meetup meetup) {
		String key = chapter.identifier().trim();
		Vector<Meetup> meetupList = chapterMeetups.get(key);
		if (meetupList == null) {
			meetupList = new Vector<Meetup>();
			meetupList.add(meetup);
			chapterMeetups.put(key, meetupList);
		} else {
			meetupList.add(meetup);
		}
	}
	
	private void addToCharacterMeetups(Character character, Meetup meetup) {
		String key = character.identifier().trim();
		Vector<Meetup> meetupList = characterMeetups.get(key);
		if(meetupList == null) {
			meetupList = new Vector<Meetup>();
			meetupList.add(meetup);
			characterMeetups.put(key, meetupList);
		} else {
			meetupList.add(meetup);
		}
	}

}
