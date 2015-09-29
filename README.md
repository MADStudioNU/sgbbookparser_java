# sgbbookparser_java
Parse Stanford Graph Base book data files into edge and node CSV files suitable for Gephi.

## usage: parse complete data file
java -jar CompiledJARFile.jar PATH_TO_SGB_FILE.dat PATH_TO_NODE_CSV_OUTPUT.csv PATH_TO_EDGE_CSV_OUTPUT.csv PATH_TO_HTML_CHARACTER_TABLE_OUTPUT.html PATH_TO_HTML_CHAPTER_TABLE_OUTPUT.html

## special usage: create partial node and edge files using chapter ID filter
java -jar CompiledJARFile.jar PATH_TO_SGB_FILE.dat PATH_TO_NODE_CSV_OUTPUT.csv PATH_TO_EDGE_CSV_OUTPUT.csv PATH_TO_HTML_CHARACTER_TABLE_OUTPUT.html PATH_TO_HTML_CHAPTER_TABLE_OUTPUT.html CHAPTER_IDS_MATCHING_THIS_REGULAR_EXPRESSION

## example usage
