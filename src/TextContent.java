import java.io.PrintWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayDeque;

// for JSON processing

import com.bfo.json.Json;
import com.bfo.json.JsonReadOptions;


/*
 * modified 
 * Mon 10 Aug 2020 05:53:27 AM CDT
 *
 * encapsulates a "text" array containing
 * important content that will be written out
 * by AUTHOR Sinks
 *
 * The "text" in this case, comes from Json files
 */

public class TextContent
{
	public String[] text;  // cheating, this is public for easy access

	public String tag; // tag used to find the text in the JSON source

	public File output = null; // OPTIONAL, may contain File object for the output file to be created from this text

	/*
	 * no param constructor
	 */
	public TextContent()
	{
		tag = "";
		output = null;
		text = null;
	}

	public TextContent(String t)
	{
		tag = t;
		output = null;
	}

	public TextContent(String t, String fi)
	{
		tag = t;
		output = new File(fi); // throws excpetion?
	}

	/*
	 * the Map passed contains Json objects
	 */
	public void create(Map source) throws Exception
	{
		Json jj = (Json)source.get(tag); // top-level structure within the map for the type of string grouping we want
		if (jj == null)
		{
			throw new IllegalArgumentException("Didnt find: " + tag + ", inside JSON: " + source);
		}
		text = JsonUtils.getStrings(jj); // use helper to populate our array of Strings
	}

	public String[] get()
	{
		return text; // correct getter
	}

	public File getFile()
	{
		return output; // correct getter
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("TextContent with name: " + tag + ", contains " + text.length + 
			" strings.");
		if (output != null)
		{
			sb.append("  Will be written to file: " + output);
		}
		return sb.toString();
	}
} // end textcontent encapsulation

