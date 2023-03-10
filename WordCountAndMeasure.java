
import components.map.Map;
import components.map.Map.Pair;
import components.map.Map1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;
import java.util.Comparator;
/**
 * Program for counting and ordering words in a text file. Will use
 * OSU components to output a html file with two tables: one of words ordered
 * alphabetically, the other ordered by occurrence.
 *
 * @author Piotr Ignatik
 */

public final class WordCountAndMeasure {

	/**
     * Default constructor--private to prevent instantiation.
     */
	private WordCountAndMeasure() {
        // no code needed here
    }
    
    /**
     * Class to override the compare method so the program can create the proper
     * comparator object comparing the String keys in two map pairs.
     *
     */
	private static class StringLT implements Comparator<Pair<String, Integer>> {
		@Override
		public int compare(Pair<String, Integer> c1, Pair<String, Integer> c2) {
			int test = c1.key().compareTo(c2.key());
			if (test == 0) {
				test = c2.value().compareTo(c1.value());
			}
			return test;
		}
	}

	/**
	 * Class to override the compare method so the program can create the proper
	 * comparator object comparing the integer values in two map pairs.
	 *
	 */
	private static class IntegerGT implements Comparator<Pair<String, Integer>> {

		@Override
		public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
			int test = o2.value().compareTo(o1.value());
			if (test == 0) {
				test = (o1.key().compareTo(o2.key()));
			}
			return test;

		}

	}

	/**
	 * Returns the first "word" (maximal length string of characters not in
	 * {@code separators}) or "separator string" (maximal length string of
	 * characters in {@code separators}) in the given {@code text} starting at the
	 * given {@code position}.
	 * 
	 * 
	 * @param text       the {@code String} from which to get the word or separator
	 *                   string
	 * @param pos        the starting index
	 * @param separators the {@code Set} of separator characters
	 * @return the first word or separator string found in {@code text} starting at
	 *         index {@code position}
	 * @requires 0 <= position < |text|
	 * @ensures
	 * 
	 *          <pre>
	 * nextWordOrSeparator =
	 *   text[position, position + |nextWordOrSeparator|)  and
	 * if entries(text[position, position + 1)) intersection separators = {}
	 * then
	 *   entries(nextWordOrSeparator) intersection separators = {}  and
	 *   (position + |nextWordOrSeparator| = |text|  or
	 *    entries(text[position, position + |nextWordOrSeparator| + 1))
	 *      intersection separators /= {})
	 * else
	 *   entries(nextWordOrSeparator) is subset of separators  and
	 *   (position + |nextWordOrSeparator| = |text|  or
	 *    entries(text[position, position + |nextWordOrSeparator| + 1))
	 *      is not subset of separators)
	 *          </pre>
	 */
	private static String nextWordOrSeparator(String text, int pos, String separators) {

		char first = text.charAt(pos);
		int length = text.length();

		int finInd = pos + 1;
		boolean sep = separators.indexOf(first) < 0;

		while (finInd < length && (separators.indexOf(text.charAt(finInd)) < 0) == sep) {

			finInd++;
		}

		return text.substring(pos, finInd);
	}

	/**
	 * Creates the map that stores each word to be printed in the output along with
	 * each words respective frequencies. Words are distinguished by separators. If
	 * a word is not already in the map, it is added and assigned a count of one. If
	 * the word is already in the map, the existing total count is increased by one.
	 *
	 *
	 * @param in the SimpleReader to receive input
	 * @requires [in.is open]
	 * @return a Map<String, Integer> containing the collection of words and respective
	 *         frequencies.
	 */
	private static Map<String, Integer> createMap(SimpleReader in) {
		Map<String, Integer> test = new Map1L<String, Integer>();

		String separators = " \t\n\r,-.![];:/()\"”“`'*1234567890‘–&—…";
		while (!in.atEOS()) {
			String line = in.nextLine();
			line = line.toLowerCase();
			int pos = 0;

			while (pos < line.length()) {
				// Take individual word from line
				String word = nextWordOrSeparator(line, pos, separators);
				// Only use the words, ignore separators
				if (separators.indexOf(word.charAt(0)) < 0) {

					// Add strings into map keeping track of numbers
					if (!test.hasKey(word)) {
						test.add(word, 1);
					} else {
						int count = test.value(word) + 1;
						test.replaceValue(word, count);
					}
				}

				// Position must be increased by length of word
				pos += word.length();
			}
		}

		return test;
	}

	/**
	 * Method for ordering specified number of Map.Pairs alphabetically, 
	 * and storing the ordered result in a Sorting Machine.
	 *
	 * @param test		input of map containing collection of words and respective
	 *              	frequencies
	 * @param num		the number of map entries wanted
	 * @return Sorting Machine with specified number of alphabetically ordered words
	 * 
	 */
	private static SortingMachine<Pair<String, Integer>> orderMapAlph(
			Map<String, Integer> test, int num) {
		// Comparator to order alphabetically
		Comparator<Pair<String, Integer>> ordAlph = new StringLT();
		SortingMachine<Pair<String, Integer>> toSortAlph = new SortingMachine1L<>(ordAlph);

		Map<String, Integer> temp = test.newInstance();
		// Place pairs from map into sorting machine to sort pairs numerically
		while (test.size() > 0) {
			Pair<String, Integer> rem = test.removeAny();
			toSortAlph.add(rem);
			temp.add(rem.key(), rem.value());
		}
		test.transferFrom(temp);

		toSortAlph.changeToExtractionMode();
		return toSortAlph;
	}

	/**
	 * Method for ordering specified number of Map.Pairs numerically, 
	 * and storing the ordered result in a Sorting Machine.
	 *
	 * @param test		input of map containing collection of words and respective
	 *              	frequencies
	 * @param num		the number of map entries wanted
	 * @return Sorting Machine with specified number of numerically ordered words
	 * 
	 */
	private static SortingMachine<Pair<String, Integer>> orderMapInt(Map<String, Integer> test, int num) {
		// Comparator to order by occurrence
		Comparator<Pair<String, Integer>> ordInt = new IntegerGT();
		SortingMachine<Pair<String, Integer>> toSortInt = new SortingMachine1L<>(ordInt);

		Map<String, Integer> temp = test.newInstance();
		// Place pairs from map into sorting machine to sort pairs numerically
		while (test.size() > 0) {
			Pair<String, Integer> rem = test.removeAny();
			toSortInt.add(rem);
			temp.add(rem.key(), rem.value());
		}
		test.transferFrom(temp);
		toSortInt.changeToExtractionMode();
		return toSortInt;
	}

	/**
	 * Outputs the opening tags in the generated HTML index file. 
	 * 
	 * 
	 * @param out		the output stream to HTML
	 * @param fileName 	the name of the file the words have been taken from
	 * @updates out.content
	 * @requires [out.is_open]
	 * @ensures out.content = #out.content * [the HTML tags]
	 */
	private static void outputHeader(SimpleWriter out, String titleName) {
		assert out != null : "Violation of: out is not null";
		assert out.isOpen() : "Violation of: out.is_open";

		out.print("<html> <head><title>Words Counted in " + titleName + "</title> </head>");
		out.println("<body style = \"background-color: #FFFF99\" ><h2>Words Counted in " 
				+ titleName + "</h2><hr />");
	}

	/**
	 * Method outputs the body of the html file.
	 * 
	 * @param out		the output stream to HTML
	 * @param last		the SortingMachine containing collection of words and
	 *                  respective frequencies
	 *               
	 * @param purpose   label that will be put above table
	 * @param side		string telling where the table will be placed
	 * @updates out.content
	 * @requires [out.is_open]
	 */
	private static void outputBody(SimpleWriter out, SortingMachine<Map.Pair<String, Integer>> last,
			String purpose, String side) {

		out.println("<div><table style = \"float: " + side + " \" border=\"4\"><caption><b>" + purpose
				+ "</b></caption><tr><th>Words</th><th>Counts</th></tr>");
		int length = last.size();
		for (int i = 0; i < length; i++) {

			Pair<String, Integer> vocabWord = last.removeFirst();
			out.println("<tr><td>" + vocabWord.key() + "</td><td>" + vocabWord.value() + "</td></tr>");

			//last.add(vocabWord); Line in Original!!
		}
		out.println("</div>");

	}

	/**
	 * Method outputs footer of the HTML index file.
	 *
	 * @param out the output stream to HTML
	 * @updates out.content
	 * @requires [out.is_open]
	 * @ensures out.content = #out.content * [the HTML tags]
	 */
	private static void outputFooter(SimpleWriter out) {
		assert out.isOpen() : "Violation of: input.is_open";

		out.println("</table></body></html>");
	}

	/**
	 * Main method.
	 *
	 * @param args the command line arguments; unused here
	 */
	public static void main(String[] args) {
		SimpleReader in = new SimpleReader1L();
		SimpleWriter out = new SimpleWriter1L();
		/**
		 * Get input values needed and order input and output strings into their
		 * respective files.
		 */
		out.print("Enter name of input file here (full extension): ");
		String inputName = in.nextLine();
		SimpleReader inputFile = new SimpleReader1L(inputName);

		out.print("Enter name of output file here (full extension): ");
		String outputName = in.nextLine();
		SimpleWriter fileHTML = new SimpleWriter1L(outputName);

		/**
		 * Contents of input file (each word) placed into a map alongside counts of how
		 * many times that word occurred.
		 */
		Map<String, Integer> filledMap = createMap(inputFile);
		int sNum = filledMap.size();

		/**
		 * Method for sorting the specified number of map entries into a sortingMachine,
		 * ordered by occurrence, and alphabetically.
		 */
		SortingMachine<Map.Pair<String, Integer>> IntMap = orderMapInt(filledMap, sNum);
		SortingMachine<Map.Pair<String, Integer>> AlphMap = orderMapAlph(filledMap, sNum);

		/**
		 * Write HTML tags to complete output file and verify that program finished
		 * correctly.
		 */
		String t1 = "Ordered Alphabetically";
		String t2 = "Ordered by Occurrence";

		outputHeader(fileHTML, inputName);
		outputBody(fileHTML, AlphMap, t1, "left");
		outputBody(fileHTML, IntMap, t2, "right");
		outputFooter(fileHTML);
		System.out.println("Confirm: Output file finished");

		/**
		 * Close all file streams since they are finished
		 */
		inputFile.close();
		fileHTML.close();
		in.close();
		out.close();
	}

}
