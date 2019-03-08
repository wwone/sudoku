import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.List;
import java.util.ArrayDeque;

// for JSON processing

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

// for FOP
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;

/*
 * Sudoku processor
 * 3/13/2018
 *
 * This program was created by Robert Swanson
 * bobswansonp@protonmail.com
 * It is being made freely available on GitHub under the MIT license, 2019
 *
 * CHANGED to use Jackson as the JSON reader
 * 
 * ADDED FOP processor invocation, so that the final 
 * output of this program is a PDF file
 *
 * NOW changing to use an "options.json" for OPTIONS ONLY
 * sud.json will be driving data
 *
 * TODO:
 *
 * Use PDFBOX to make the PDF, rather than FOP? It would
 * be more direct. Otherwise, can we just embed PDF creation
 * using FOP? (so far this has not worked....)
 *
 * Allow for top row (or any?) to be in ascending order,
 * just for fun. This is done by number shuffling
 * as the LAST operation before creating empty spots
 *
 * Allow for X format across puzzle,
 * just for fun. This is done by reserving these
 * cells as the LAST operation before creating empty spots
 *
 * Note the puzzle number used, using a random
 * number to pick it ALLOW USER to pick one
 *
 * swap quadrants (tridents?) in addition to swapping columns/rows
 *
 * would it make sense to use gaussian distributions,
 * instead of pure random, when picking things, such
 * as cells to remove?
 
 * allow remove all in a row/col EXCEPT for 1 or 2 remaining
 * (currently can remove all contents of row/col)
 *
 * allow remove all instances of a single number EXCEPT
 * allowing 1 or more to remain. This is TRICKY, because
 * we are using Genghis Khan techniques to remove items.
 * If we remove all except 1 or 2 instances of a digit,
 * we have to GUARANTEE that the following removals
 * don't touch it.
 *
 * test all puzzles after reading the JSON, make sure
 * they are valid
 * 
 * We have read in a complete solution. We
 * want to make a believable puzzle from it.
 *
 * We:
 *
 * 1) switch columns WITHIN the groups of 3
 *    pick which group(s) to process:
 *    1, 2, 3, 1+2, 1+3, 2+3, 1+2+3 (7 choices)
 *    within group, switch: 1 for 2,
 *    1 for 3, 2 for 3 (could switch more,
 *    but why...) DONE, using random selection of switchables
 *
 * 2) switch rows within the groups of 3
 *    same as above, but rows DONE see above
 *
 * 3) rotate: 90, 180, 270 (only need 90, just
 *     repeat as needed
 *
 * 4) substitute digits, so 1 becomes 8, 8
 *    becomes 3, etc, etc. We could even
 *    use letters of the alphabet and have the
 *    same puzzle DONE on digit switch
 *
 * finally) remove some cells, so the user has
 *    the fun of filling them in. Why? who knows?
 *    Then, print it out in FOP, so I can make
 *    a PDF for printing  DONE
 *
 *    various algorithms:
 *    a) pure random, must repeat to get the desired
 *       number of removals DONE
 *    b) remove entire rows and/or columns, finish
 *       with (a) random to get the desired count DONE
 *    c) remove entire 3x3 subsquares, finish with
 *       (a) random to get the desired count DONE
 *    d) SEE ABOVE for another way to do it
 *
 *    leave 20 -- evil puzzle, 30 -- hardish
 *    leave 30 with removed column or row -- evil!
 *    leave 35, remove box, acceptable
 *    leave 40, easy peasy
 */
public  class Sud
{
    List sudoku_array = null;
    List fop_front = null;
    List fop_back = null;
    List options_object = null;
    List frequency_object = null;
    
    Random g_ran = new Random(); // use global random generator (good?)
    
    // configure fopFactory as desired
    private final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());

	public Option g_rotate = new Option(); //  false;  default
	public Option g_remove_row = new Option(); // false;  default
	public Option g_remove_box = new Option(); // false;  default
	public Option g_remove_column = new Option(); // false by  default
	public Option g_frequency = new Option(); // false;  default
    public Option g_remove_single_value = new Option(); // false;  default
	public short g_organize_numbers = ORG_RANDOMIZE;  // random;  default
	public short g_switch_columns = 0; // default none
	public short g_pick_puzzle = -1; // default any
	public short g_switch_rows = 0; // default none
	public short g_output = 0; // default numbers, 1 = letters
	public short g_given_count = 35; // default 

	public static final short ORG_RANDOMIZE = 0;
	public static final short ORG_ORDER = 1;

	public StringBuffer g_puzzle_number = new StringBuffer();
	public StringBuffer g_row_switch_history = new StringBuffer();
//	public StringBuffer g_frequency_history = new StringBuffer();
	public StringBuffer g_col_switch_history = new StringBuffer();
//	public StringBuffer g_rotate_history = new StringBuffer();
//    public StringBuffer g_remove_single_history = new StringBuffer();

	public static final String OUTPUT = "sudoku.fo"; // scratch file
	public String g_output_file; // where PDF is written

	public PrintWriter g_pr = null;
	
	public Puz the_puzzle = null; // current active puzzle

	/*
	 * when make a letter puzzle, 1=A, 2=B, etc
	 */
	public static final String[] LETTERS = {"X","A","B","C","D","E","F","G","H","I"};

    public void init(String [] outfile) throws Exception
    {
	if (outfile.length < 1)
	{
		throw new Exception("Must specify output PDF file!");
	}
	g_output_file = outfile[0];
        /*
         * read in the data that is used by this object
         */
        String filename = "sud.json"; // filename to read
        File input = new File(filename);
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        Map<String,Object> userData = mapper.readValue(input,Map.class);
        
        // userData is a Map containing the named arrays
        
        sudoku_array = (List)userData.get("sudoku");

        fop_front = (List)userData.get("FOPFront");

        fop_back = (List)userData.get("FOPBack");

	g_pr = new PrintWriter(new File(OUTPUT)); // output here
	copyContent(fop_front,g_pr);

	/*
	 * process options
	 */

        filename = "options.json"; // filename to read
        input = new File(filename);
        mapper = new ObjectMapper(); // can reuse, share globally
        Map<String,Object> userData2 = mapper.readValue(input,Map.class);
        
        options_object = (List)userData2.get("options");

	boolean found_key = false;
	// MUST be in pairs
	for (int inner = 0 ; inner < options_object.size() ; inner += 2)
	{
		String key = (String)options_object.get(inner);
		String val = (String)options_object.get(inner + 1);
		// test keys
		found_key = false;
		if(key.equalsIgnoreCase("removerow"))
		{
			g_remove_row.value = getBoolean(val);
			found_key = true;
		}
		if(key.equalsIgnoreCase("removebox"))
		{
			g_remove_box.value = getBoolean(val);
			found_key = true;
		}
		if(key.equalsIgnoreCase("removesinglevalue"))
		{
                    g_remove_single_value.value = getBoolean(val);
                    found_key = true;
		// debug System.out.println("single value remove: " + g_remove_single_value);
		}
		if(key.equalsIgnoreCase("removecolumn"))
		{
			g_remove_column.value = getBoolean(val);
			found_key = true;
		}
		/*
		 * frequency is a keyword EASY, HARD, MEDIUM
		 * 
		 * NOTE that we fetch the frequency array from 
		 * the MAIN SUD.JSON file (userData object still active)!
		 */
		if(key.equalsIgnoreCase("frequency"))
		{
			if (val.equals("HARD"))
			{
				frequency_object = (List)userData.get("frequencyHARD");
			}
			else
			{
				if (val.equals("EASY"))
				{
					frequency_object = (List)userData.get("frequencyEASY");
				}
				else
				{
					if (val.equals("MEDIUM"))
					{
						frequency_object = (List)userData.get("frequencyMEDIUM");
					}
					else
					{
						throw new Exception("'frequency' option must be 'HARD', 'EASY', or 'MEDIUM'");
					}
				}
			}
			g_frequency.value = true; // now will use it

			// frequency is array of arrays, each 9 digits
			System.out.println("Frequency items available: " +
				frequency_object.size());
			found_key = true;
		} // end frequency key
		if(key.equalsIgnoreCase("organizenumbers"))
		{
			if(val.equalsIgnoreCase("random"))
			{
				g_organize_numbers = ORG_RANDOMIZE;
				found_key = true;
			}
			if(val.equalsIgnoreCase("order"))
			{
				g_organize_numbers = ORG_ORDER;
				found_key = true;
			}
			if (!found_key)
			{
				throw new Exception("'organizenumbers' option must be 'random' or 'order'");
			}
		} // end if organize numbers
		if(key.equalsIgnoreCase("rotate"))
		{
			g_rotate.value = getBoolean(val);
			found_key = true;
		}
		if(key.equalsIgnoreCase("switchcols"))
		{
			if(val.equalsIgnoreCase("no"))
			{
				g_switch_columns = 0;
				found_key = true;
			}
			if(val.equalsIgnoreCase("false"))
			{
				g_switch_columns = 0;
				found_key = true;
			}
			if (!found_key)
			{
				// must be number at this point
				g_switch_columns = Short.parseShort(val); // might fail
				found_key = true;
			}
		} // end switchcols
		if(key.equalsIgnoreCase("puzzle"))
		{
			if(val.equalsIgnoreCase("any"))
			{
				g_pick_puzzle = -1;
				found_key = true;
			}
			if (!found_key)
			{
				// must be number at this point
				g_pick_puzzle = Short.parseShort(val); // might fail
				found_key = true;
			}
		} // end switchrows
		if(key.equalsIgnoreCase("switchrows"))
		{
			if(val.equalsIgnoreCase("no"))
			{
				g_switch_rows = 0;
				found_key = true;
			}
			if(val.equalsIgnoreCase("false"))
			{
				g_switch_rows = 0;
				found_key = true;
			}
			if (!found_key)
			{
				// must be number at this point
				g_switch_rows = Short.parseShort(val); // might fail
				found_key = true;
			}
		} // end switchrows
		if(key.equalsIgnoreCase("givens"))
		{
			// must be number at this point
			g_given_count = Short.parseShort(val); // might fail
			found_key = true;
		} // end givens
		if(key.equalsIgnoreCase("output"))
		{
			if(val.equalsIgnoreCase("numbers"))
			{
				g_output = 0;
				found_key = true;
			}
			if(val.equalsIgnoreCase("letters"))
			{
				g_output = 1;
				found_key = true;
			}
			if (!found_key)
			{
				throw new Exception("'output' option must be numbers or letters");
			}
		} // end output
		if (!found_key)
		{
			throw new Exception("option key not found: " + key);
		}
	} // loop for all option values	
        
    } // end init

	public boolean getBoolean(String val)
	{
		if (val.equalsIgnoreCase("yes"))
		{
			return true;
		}
		if (val.equalsIgnoreCase("true"))
		{
			return true;
		}
		return false;
	} // end getboolean

 public static void main(String args[]) 
{
	Sud ss = new Sud();
	try
	{
		ss.init(args);
		ss.execute();
		// debug ss.showPuzzle(System.out,"Puzzle after execute: ");
		ss.finish();
		// debug ss.showPuzzle(System.out,"Puzzle after finish: ");
	}
	catch (Exception e)
	{
		System.err.println("Exception: " + e);
		e.printStackTrace();
	}

} // and main 

	// debug
	public void showPuzzle(PrintStream pr,String comment)
	{
		pr.println(comment + the_puzzle.toString());
	}

public void execute() throws Exception
{
	// sudoku_array is array of globs, each glob 9 rows of 9 numbers

	System.out.println("number of puzzles: " + sudoku_array.size());
	the_puzzle = new Puz(); // initialize, will fill in loop below
	/*
	 * did someone ask for a specific puzzle?
	 */
    int apuzzle = 0;
	if (g_pick_puzzle < 0)
	{
		// any allowed
	    apuzzle = g_ran.nextInt(sudoku_array.size()); // 0 to size
	}
	else
	{
		apuzzle = g_pick_puzzle;
	}
	// FOR NOW ONE ONLY
    System.out.println("Using puzzle: " + apuzzle);
    g_puzzle_number.append(", Puz:" + apuzzle);
	for (int inner = apuzzle ; inner < apuzzle + 1 ; inner++)
	{
		System.out.println("Processing glob number: " + inner);
		Object oo = sudoku_array.get(inner);
		if (oo instanceof List)
		{
			List arr = (List)oo;
			System.out.println("inner leng: " + arr.size());
			if (arr.size() != 9)
			{
				throw new Exception("Badly formed array of arrays");
			}
			for (short row = 0  ; row < 9 ; row++)
			{
				System.out.println("row: " + row);
				Object arow = arr.get(row);
			    // debug System.out.println("innermost: " + arow.getClass().getName());
				List acol = (List)arow;
				if (acol.size() != 9)
				{
					throw new Exception("Badly formed inner column list");
				}
				for (short col = 0  ; col < 9 ; col++)
				{
					Object number = acol.get(col);
					Integer lnumber = (Integer)number;
// debug				    System.out.println("number: " + number.getClass().getName());
					// number is a Long
	// debug				System.out.print(number + ",");
					// following ONLY USED to creat the puzzle for printing
				/*	    g_pr.print(FOPData.getFOP(row, 
						col,
						number.toString(),
						0) + "\n\n"); // difficulty is defaulted to zero for now
				 */
					// fill the_puzzle
					the_puzzle.populate(
					row,col,lnumber.shortValue());
				} // end loop on column
				g_pr.println(); // finish line
			} // end loop each row
		} // end correct outer glob
		else
		{
		    throw new Exception("Problems with glob: " + oo.getClass().getName());
		}
	} // end for each glob
	/*
	 * now TRANSFORM!
	 */
/*
	showPuzzle(System.out,"Puzzle before shuffle: ");
*/
	if (g_organize_numbers == ORG_RANDOMIZE)
	{
		the_puzzle.shuffleNumbers(); // before anything else, shuffle numbers
	}
	if (g_rotate.value)
	{
		// random from 1 to 3 rotations (90, 180, 270)
		int times = g_ran.nextInt(3); // 0 to 2
		g_rotate.add(String.valueOf((times + 1)*90));
		while (times >= 0)
		{
			the_puzzle.rotate90(); // test rotate
			times--;
		}
	}
	/*
	 * switch columns if requested
	 */
	if (g_switch_columns > 0)
	{
		// set up switchable list

		List switch_list = create_switchable_list();

		// use the first "n" items after shuffle

		for (int inner = 0 ; inner < g_switch_columns; inner++)
		{
			Pair use = (Pair)switch_list.get(inner);
			the_puzzle.switchCol(use.row,use.col);
			g_col_switch_history.append(use.row +
				"," + use.col + "::");
		}
	} // end if switching columns
	/*
	 * switch rows if requested
	 */
	
	if (g_switch_rows > 0)
	{
		// set up switchable list

		List switch_list = create_switchable_list();

		// use the first "n" items after shuffle

		for (int inner = 0 ; inner < g_switch_columns; inner++)
		{
			Pair use = (Pair)switch_list.get(inner);
			the_puzzle.switchRow(use.row,use.col);
			g_row_switch_history.append(use.row +
				"," + use.col + "::");
		}
	} // end if switching rows
	/*
	 * after much mixing up, before we remove items,
	 * order one row or column. This "mixes" the
	 * digits. Alternatively "randomize" is done BEFORE this. 
	 */
	if (g_organize_numbers == ORG_ORDER)
	{
		the_puzzle.orderNumbers(); // before anything else, order numbers
		/*
		 * NOW, do not shuffle any more
		 */
	}
	if (g_remove_row.value)
	{
		the_puzzle.removeRow(); // row removal
	}
	if (g_remove_box.value)
	{
		the_puzzle.removeBox(); // box removal
	}
	if (g_remove_column.value)
	{
		the_puzzle.removeColumn(); // col removal
	}
	if (g_remove_single_value.value)
	{
            the_puzzle.removeSingleValue(); // single value removal
	}
	//showPuzzle(System.out,"Puzzle after rotate: ");
 // NEEDS MORE WORK    the_puzzle.removeSingleValueLeaving(1); // TESTING


//	short rem = 30; // 40 was easy, 35 is harder NOT USED
	if (g_frequency.value)
	{
		// REMOVE individual numbers, using the frequency chart
		the_puzzle.removeByFrequency();
	}
	else
	{
		the_puzzle.removeCells(g_given_count); // remove some stuff
	}
} // end execute

public void finish() throws Exception
{
	// for now, just display puzzle as stored 
	the_puzzle.makeFOP(g_pr);
	the_puzzle.showOptions(g_pr);
	copyContent(fop_back,g_pr);
	g_pr.close();
	/*
	 * NOW we have created a .fo file, we want
	 * to convert to PDF for printing purposes.
	 * fortunately, we have some whiz-bang code from
	 * Apache FOP to do that. 
	 * 
	 * Out output becomes input to the FOP processor 
	 */
	convertFO2PDF(new File(OUTPUT), new File(g_output_file));
} // end finish



    private void copyContent(List content_object,
	PrintWriter pr) throws Exception
    {
        Object someobject = null;
        Iterator ii = content_object.iterator();
        while (ii.hasNext())
        {	
            someobject =  ii.next();
            if (someobject instanceof String)
            {
                pr.println(someobject);
                continue; // done for now
            }
            //  now if there is a fall-through, the objects are somebody we don't know about
            throw new Exception("Problems with JSON inside: " + 
		content_object + ", object: " + someobject.getClass().getName());
        } // end write the content to the stream
    } // end copy content

	public List create_switchable_list()
	{
		// set up switchable list

		ArrayList switch_list = new ArrayList(9);
		switch_list.add(new Pair(0,1));
		switch_list.add(new Pair(0,2));
		switch_list.add(new Pair(1,2));
		switch_list.add(new Pair(3,4));
		switch_list.add(new Pair(3,5));
		switch_list.add(new Pair(4,5));
		switch_list.add(new Pair(6,7));
		switch_list.add(new Pair(6,8));
		switch_list.add(new Pair(7,8));
		Collections.shuffle(switch_list); // randomize
		return switch_list; // ready to use
	} // end create switchable list

	/*
	 * a true or false option, as well as the
	 * history associated with its use
	 */
	public class Option
	{
		public boolean value;
		public StringBuffer history;

		public Option(boolean val)
		{	
			value = val;
			history = new StringBuffer();
		}

		// default object is set false
		public Option()
		{	
			this(false);
		}
		public void add(String x)
		{
			history.append(x);
		}
		public String showHistory(String front)
		{
			if (value)
			{
				// true, show something
				return front + history.toString();
			}
			else
			{
				return ""; // false, return empty string
			}
		} // show history
	} // end option object

	public class Pair
	{
		public short col;
		public short row;

		public Pair(short ro, short co)
		{
			col = co;
			row = ro;
		}

		public Pair(int ro, int co)
		{
			col = (short)co;
			row = (short)ro;
		}
	} // end Pair 

	public class Cell
	{
		public short sval;
		public String cval;
		public boolean removed; // true if removed for final puzzle


		public Cell(short val)
		{
			sval = val;
			String allofit = String.valueOf(sval);
			// want only the last char
		     char data[] = {
			allofit.charAt(allofit.length() - 1)
			};
			     cval  = new String(data);
			removed = false;
 
		}
		
		public String toString()
		{
			if (removed)
			{
				return " "; // removed is blank
			}
			else
			{
				return cval;
			}
		}
	} // end Cell 

	/*
	 * contains complete puzzle with methods
	 * to manipulate it
	 */
	public class Puz
	{
		public Cell[][] contents; // will be 9x9
		public Pair[] removals; // will be 81 long

		public Puz()
		{
			// initialize the puzzle content
			contents = new Cell[9][9]; // allocate

			// for use later, set up removal pairs
			removals = new Pair[81]; // allocate
			short the_count = 0;
			for (short row = 0  ; row < 9 ; row++)
			{
				for (short col = 0  ; col < 9 ; col++)
				{
					// populate
					removals[the_count] = new Pair(row,col);
					the_count++;
				} // end col loop
			} // end row loop
		} // end constructor

		public void populate(short row, short col, short sval)
		{
			contents[row][col] = new Cell(sval);
		} // end populate
		
		public String toString()
		{
			StringBuffer result = new StringBuffer();
			result.append("Puzzle Contents:\n");
			for (short row = 0  ; row < 9 ; row++)
			{
				for (short col = 0  ; col < 9 ; col++)
				{
					result.append("[" + row +
					"][" + col + "]: " +
					contents[row][col] + "\n"); // uses toString()
				}
			}
			return result.toString();
		} // end simple to string

		public void makeFOP(PrintWriter pr)
		{
			// for now, just display puzzle as stored 
			for (short row = 0 ; row < 9 ; row++)
			{
				for (short col = 0 ; col < 9 ; col++)
				{
					if (g_output == 0)
					{
						pr.print(FOPData.getFOP(row, 
						col,
						contents[row][col].toString(),
						0) + "\n\n"); // difficulty is defaulted to zero for now
					}
					if (g_output == 1)
					{
						if (contents[row][col].removed)
						{
							pr.print(FOPData.getFOP(row, 
							col,
							" ", // empty cell
							0) + "\n\n"); // difficulty is defaulted to zero for now
						}
						else
						{
							pr.print(FOPData.getFOP(row, 
							col,
							LETTERS[contents[row][col].sval],
							0) + "\n\n"); // difficulty is defaulted to zero for now
						}
					}
				} // end columns
			} // end rows
		} // end makeFOP

		/*
		 * show a string and flag value ONLY if true
		 */
		public void showBoolean(StringBuffer sb,
			String front, boolean flag)
		{
			if (flag)
			{
				sb.append(front + flag);
			}
		}
		
		public void showOptions(PrintWriter pr)
		{
			StringBuffer result = new StringBuffer(1000);
			/*
				public boolean g_remove_row = false; // default
				public boolean g_remove_column = false; // default
				public boolean g_randomize_numbers = false; // default
				public short g_switch_columns = 0; // default none
				public short g_switch_rows = 0; // default none
				public short g_output = 0; // default numbers, 1 = letters
				public short g_given_count = 35; // default 
			showBoolean(result,"Shuff: ",g_randomize_numbers.value);
			*/
			if (g_organize_numbers == ORG_RANDOMIZE)
			{
				result.append("Shuff: true");
			}
			if (g_organize_numbers == ORG_ORDER)
			{
				result.append("Order: true");
			}
			showBoolean(result,", RRow: ",g_remove_row.value);
			showBoolean(result,", RCol: ",g_remove_column.value);
			showBoolean(result,", RBox: ",g_remove_box.value);
                    result.append(g_remove_single_value.showHistory(", RSing: "));
			result.append(", SRow: " + g_row_switch_history);
			result.append(g_rotate.showHistory(", Rote: "));
			//result.append(", Rote: " + g_rotate_history);
			//result.append(", SRow: " + g_switch_rows); // FILL WITH ACTUAL DONE
			//result.append(", SCol: " + g_switch_columns); // FILL WITH ACTUAL DONE
			result.append(", SCol: " + g_col_switch_history);
			if (g_frequency.value)
			{
				result.append(g_frequency.showHistory(", Freq: "));
			}
			else
			{
				result.append(", Givens: " + g_given_count);
			}
			result.append(g_puzzle_number);
			pr.print(FOPData.showComment(result.toString()));
                    // following uses non-breaking spaces
                    pr.print(FOPData.showComment("1&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;2&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;      3&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;      4&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;      5&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;      6&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;      7&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;      8&#xa0;&#xa0;&#xa0;&#xa0;&#xa0;      9"));
		} // end show options on puzzle

		public void switchRow(int from_row, int to_row)
		{
			Cell[] hold = new Cell[9];
			for (short col = 0 ; col < 9 ; col++)
			{
				// when overwriting DO NOT copy the reference, make new
				hold[col] = new Cell(contents[from_row][col].sval); // all columns
				// ok to move referenced
				contents[from_row][col] = contents[to_row][col]; // overwrite
				contents[to_row][col] = hold[col];
			}
		} // end switch row

		public void switchCol(int from_col, int to_col)
		{
			// work with global items
			Cell[] hold = new Cell[9];
			for (short row = 0 ; row < 9 ; row++)
			{
				// when overwriting DO NOT copy the reference, make new
				hold[row] = new Cell(contents[row][from_col].sval); // all rows
				// ok to move referenced
				contents[row][from_col] = contents[row][to_col]; // overwrite
				contents[row][to_col] = hold[row];
			}
		} // end switch column

		public short getOccupiedCellCount()
		{
			short tot = 0;
			for (short row = 0 ; row < 9 ; row++)
			{
				for (short col = 0 ; col < 9 ; col++)
				{
					if (!contents[row][col].removed)
					{
						tot++; // not removed
					}
				}
			}
			return tot;
		} // get occupied cell count

		// remove single row
		public void removeRow()
		{
			// first, for testing, erase a single
			// row
			int remove_this = g_ran.nextInt(9); // 0 to 8
			for (short col = 0 ; col < 9 ; col++)
			{
				contents[remove_this][col].removed = true;
			}
		}

		// remove single box
		public void removeBox()
		{
			// first, for testing, erase a single
			// box
			int remove_this = g_ran.nextInt(9); // 0 to 8
			int start_col = 3 * (remove_this % 3); // modulo, gives 0, 3, 6
			int start_row = 3 * (remove_this / 3); // divide no remainder
			for (int row = start_row ; row < (start_row + 3) ; row++)
			{
				contents[row][start_col].removed = true;
				contents[row][start_col + 1].removed = true;
				contents[row][start_col + 2].removed = true;
			}
		} // remove box of item
		
		public void removeColumn()
		{
			// first, for testing, erase a single
			// column
			int remove_this = g_ran.nextInt(9); // 0 to 8
			for (short row = 0 ; row < 9 ; row++)
			{
				contents[row][remove_this].removed = true;
			}
		}
		
		public void removeSingleValue()
		{
                    // first, for testing, erase a single
                    // value
                    int remove_this = g_ran.nextInt(9); // 0 to 8
                    remove_this++; // now 1 to 9
                    g_remove_single_value.add(String.valueOf(remove_this)); // value removed
                    for (short row = 0 ; row < 9 ; row++)
                    {
                        for (short col = 0 ; col < 9 ; col++)
                        {
                            if(contents[row][col].sval == remove_this)
                            {
                                contents[row][col].removed = true;
                            }
                        } // loop all columns
                    } // loop all rows
		} // end remove single value


		public void removeSingleValueLeaving(int remain)
		{
                    //  erase a single value EXCEPT leave 1 or
                    // more instances
                    int remove_this = g_ran.nextInt(9); // 0 to 8
                    remove_this++; // now 1 to 9
			// REWRITE history entry!
                    g_remove_single_value.history = new StringBuffer("leave " + remain + " for: " + remove_this); // clean and add our narrative
                    // first make list of where items are
                    ArrayList locations = new ArrayList(9);
                    for (short row = 0 ; row < 9 ; row++)
                    {
                        for (short col = 0 ; col < 9 ; col++)
                        {
                            if(contents[row][col].sval == remove_this)
                            {
                                locations.add(new Pair(row,col));
                                System.out.println("candidate for removal: [" +
                                                   row + "," + col + "] = " +
                                                   remove_this);
                            }
                        } // loop all columns
                    } // end all rows
                    // SHUFFLE locationshere!!
                    // locations now has all instances of an entry
                    int to_remove = 9 - remain; // 1 says remove 8, 2 says remove 7, etc
                    while (locations.size() > to_remove)
                    {
                        System.out.println("check size: " +
                                           locations.size() +
                                           ", need: " +
                                           to_remove);
                        // too many items
                        locations.remove(0); // everyone shift, now size is one less                        
                    }
                    Iterator ii = locations.iterator();
                    Pair remove_me = null;
                    while (ii.hasNext())
                    {
                        remove_me = (Pair)ii.next();
                        System.out.println("Removing: [" +
                                           remove_me.row +
                                            "," + remove_me.col +
                                            "], val = " + contents[remove_me.row][remove_me.col].sval);
                        System.out.println("  Previous State: " + contents[remove_me.row][remove_me.col].removed);
                        contents[remove_me.row][remove_me.col].removed = true;
                    }
		} // end remove all of a single value with some remaining 

		/*
		 * remove cells by using a frequency set
		 */
		public void removeByFrequency()
		{
		    int frequency_set = g_ran.nextInt(frequency_object.size()); // 0 to size
			List somefreqs = (List)frequency_object.get(frequency_set);
			List the_numbers = new ArrayList();
			for (short inner = 1 ; inner < 10; inner++)
			{
				the_numbers.add(new Short(inner)); // 1 to 9
			}
                            Collections.shuffle(the_numbers); // randomize
			/*
			 * these two arrays line up, numbers being
			 * the digit to remove, somefreqs being the
			 * amount to leave
			 */
			int total_givens = 0;
			for (short inner2 = 0 ; inner2 < 9 ; inner2++)
			{
				Short the_digit = (Short)the_numbers.get(inner2);
				Integer the_remains = (Integer)somefreqs.get(inner2);
				g_frequency.add(String.valueOf(the_remains));
				/*
				 * we:
				 * 1) gather up all locations that match
				 *    "the digit"
				 * 2) randomize that list
				 * 3) remove items until "the remains"
				 *    remain...
				 */
				short dig = the_digit;
				ArrayList work = new ArrayList();
				for (short row = 0 ; row < 9 ; row++)
				{
					for (short col = 0 ; col < 9 ; col++)
					{
						if (dig == contents[row][col].sval)
						{
							// it is one we want
							work.add(new Pair(row,col));
						}
					} // end cols
				} // end rows
				// work should contain 9 sets of locations
				    Collections.shuffle(work); // randomize
				int rem = the_remains;  // how many to remain
				total_givens += rem; // run total

				rem = 9 - rem; // how many to remove
				for (int inner3 = 0 ; inner3 < rem ; inner3++)
				{
					Pair remove_me = (Pair)work.get(inner3);
					// remove this location
					contents[remove_me.row][remove_me.col].removed = true;
				}
			} // end process each set of items to remove
			// total_givens has the given amount
			g_frequency.add(":" + String.valueOf(total_givens));


			/* Iterator ii = somefreqs.iterator();
			while (ii.hasNext())
			{
				System.out.println("Freq: " + ii.next());
			}
			debugging */
		} // end remove by frequency

		/*
		 * remove cells by various methods, leaving
		 * a set number of givens
		 */
		public void removeCells(short final_count)
		{
				//final_count -= 9; // account for these????
			// now remove random items, which could be
			// one of those in the above row
			//
			// create random selection to remove

			// removals is an array, we'll make it into
			// a List and shuffle it
			//
			ArrayList the_list = new ArrayList(81);
			for (short inner2 = 0 ; inner2 < 81 ; inner2++)
			{
				the_list.add(removals[inner2]);
			}
			// we have to repeat the erasure, because we
			// may have removed some items in the random
			// removal list (yes this can loop forever)
			//while (getOccupiedCellCount() > final_count)
			boolean keep_trying = true;
			while (keep_trying)
			{
                            //	short current_count = final_count;
                            // keep trying re-shuffle the list of possible removals
                            Collections.shuffle(the_list); // randomize
                            Iterator ii = the_list.iterator(); // loop through
                            Pair xx = null;
                            while (ii.hasNext())
                            {
				xx = (Pair)ii.next();
				contents[xx.row]
				[xx.col].removed = true;
				/*
				 * have removed, check occupied count
				 */
				if (getOccupiedCellCount() <= final_count)
				{
                                    keep_trying = false; // will cause end of outer loop
                                    break; // enuf
				}
                                //	current_count--; // do no more than count
                                //	if (current_count < 0)
                                //	{
                                //		break; // enough
                                //	}
                            } // loop through all 81 values
                            // fall through if (1) we used up all 81 erase values (trouble)
                            // or (2) occupied count met the criteria (keep trying is set to false)
                            
                            // will be 81 values.
                            //for (short inner = 0 ; inner < (81 - final_count) ; inner++)
                            //short inner = 80;
                            //while (final_count > 0)
                            //for (short inner = 0 ; inner < (81 - final_count) ; inner++)
                            //{
                            //	contents[removals[inner].row]
                            //		[removals[inner].col].removed = true;
                            //	inner--;
                            //	final_count--;
                            //}
                            //removals[80];
                            //
                            // remove the 81 items, until the final_count
                            // remains
                            //
                            // if we fail after 81, pick another 81 and
                            // try to remove until final_count
                            // remains
                            //
                            // allow for 3 failures...
			} // keep trying to remove
		} // end remove cells

		/*
		 * make list of numbers and shuffle them, so
		 * that most are changed to new numbers
		 *
		 * this REPLACES the contents of the puzzle after shuffle
		 */
		public void shuffleNumbers()
		{
			ArrayList the_list = new ArrayList(9);
			for (short inner2 = 1 ; inner2 < 10 ; inner2++)
			{
				the_list.add(new Short(inner2)); // the new number
			}
			Collections.shuffle(the_list); // randomize
			// item in list position(x) becomes new value
			Cell[][] xcontents = new Cell[9][9]; // allocate for new
			for (short row = 0 ; row < 9 ; row++)
			{
				for (short col = 0 ; col < 9 ; col++)
				{
					// lookup
					Short newvalue = (Short)the_list.
					  get(contents[row][col].sval - 1);
					// put in short value from list
					xcontents[row][col] =  new Cell(newvalue);
				} // end cols
			} // end rows
			// at this point xcontents is a newly populated puzzle
			contents = xcontents; // replace full reference
		} // end shuffle numbers

		/*
		 * take one row or column from the existing solution
		 * 
		 * remap all digits, so that this row or col is 
		 * in exact numeric order
		 * 
		 * use that "map" to shuffle all numbers in the puzzle 
		 *
		 * this REPLACES the contents of the puzzle after reorder
		 * 
		 * as an enhancement, randomly select: row vs col and, 
		 * exact row/col number to be used as a template
		 */
		public void orderNumbers()
		{
			    boolean rowcol = g_ran.nextBoolean(); // odd/even gives row or col
			    int position = g_ran.nextInt(9); // row or col 0 to 8
			short [] replacements = new short[9];
//			short rrow = 0; // testing use first row
			if (rowcol)
			{
				// use a row
				for (short col = 0 ; col < 9 ; col++)
				{
					  replacements[contents[position][col].sval - 1] = col; // map each number
					  replacements[contents[position][col].sval - 1]++; // must be one more
				}
			} // end using a row as template
			else
			{
				// use a col
				for (short row = 0 ; row < 9 ; row++)
				{
					  replacements[contents[row][position].sval - 1] = row; // map each number
					  replacements[contents[row][position].sval - 1]++; // must be one more
				}
			} // end using a column as template
			// item in replacement position(x) becomes new value
			Cell[][] xcontents = new Cell[9][9]; // allocate for new
			for (short row = 0 ; row < 9 ; row++)
			{
				for (short col = 0 ; col < 9 ; col++)
				{
					// lookup
					// put in short value from list
					xcontents[row][col] =  new Cell(replacements[contents[row][col].sval - 1]);
				} // end cols
			} // end rows
			// at this point xcontents is a newly populated puzzle
			contents = xcontents; // replace full reference
		} // end order numbers


		/*
		 * simple rotation 90 degrees clockwise
		 * repeat as necessary
		 */
		public void rotate90()
		{
			Cell[][] xcontents = new Cell[9][9]; // allocate for new
			// row 1 becomes column 9
			// row 2 becomes column 8
			// . . .
			// row 9 becomes column 1
			//
			short destcol = 8; // destination column
			for (short row = 0 ; row < 9 ; row++)
			{
				for (short col = 0 ; col < 9 ; col++)
				{
					// steal "col" value to use as row
					xcontents[col][destcol] =  new 
						Cell(contents[row][col].sval);
				} // end cols
				destcol--; // next column working leftwards
			} // end rows
			// at this point xcontents is a newly populated puzzle
			contents = xcontents; // replace full reference
		} // end rotate by 90
	} // end Puz 

// following from Apache FOP example

    /**
     * Converts an FO file to a PDF file using FOP
     * @param fo the FO file
     * @param pdf the target PDF file
     * @throws IOException In case of an I/O problem
     * @throws FOPException In case of a FOP problem
     */
    public void convertFO2PDF(File fo, File pdf) throws IOException, FOPException {

        OutputStream out = null;

        try {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            // configure foUserAgent as desired

            // Setup output stream.  Note: Using BufferedOutputStream
            // for performance reasons (helpful with FileOutputStreams).
            out = new FileOutputStream(pdf);
            out = new BufferedOutputStream(out);

            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Setup input stream
            Source src = new StreamSource(fo);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

            // Result processing
            FormattingResults foResults = fop.getResults();
            java.util.List pageSequences = foResults.getPageSequences();
            for (java.util.Iterator it = pageSequences.iterator(); it.hasNext();) {
                PageSequenceResults pageSequenceResults = (PageSequenceResults)it.next();
                System.out.println("PageSequence "
                        + (String.valueOf(pageSequenceResults.getID()).length() > 0
                                ? pageSequenceResults.getID() : "<no id>")
                        + " generated " + pageSequenceResults.getPageCount() + " pages.");
            }
            System.out.println("Generated " + foResults.getPageCount() + " pages in total.");

        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        } finally {
            out.close();
        }
    } // end FOP converter
} // end sudoku processor
