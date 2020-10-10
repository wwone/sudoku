import java.io.File;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import java.util.Properties;
import java.util.Date;

import java.text.DateFormat;



// for JSON processing
// 

import com.bfo.json.Json;
import com.bfo.json.JsonReadOptions;

/*
 * Updated:
 *
 * Sun 09 Aug 2020 10:37:07 AM CDT
 *
 * provide some helpful static methods to make
 * work with Json objects easier.
 *
 */
public class JsonUtils
{
	/*
	* pass a Json object that can enclose the following:
	*
	* 1) a List, which will contain Strings. Anything else
	*    will be treated as String.
	*
	*    we return an array of String[] with the String contents
	*/
	public static String[] getStrings(Json morestuff) throws IllegalArgumentException
	{
		Object someobject = null;
		if (!morestuff.isList())
		{
			throw new IllegalArgumentException("Passed a Json that is not a List, : " + 
					morestuff.type());
		}
		List alist = morestuff.listValue();
		String[] res = new String[alist.size()]; // get ready
		for (int ii = 0 ; ii < alist.size() ; ii ++)
		{
		    someobject = alist.get(ii);
		    Json jj = (Json)someobject; // cast MUST work
		    /* interesting possibility, but let the user deal with any strange Strings they receive
		    if (!(jj.isString()))
		    {
			throw new IllegalArgumentException("Json List key item not String, object: " + 
					jj + ", position: " + ii);
		    }
		    */
		    String jval = jj.stringValue();  // content of List is Strings!
			res[ii] = jval;
		} // end loop through all strings (or something like them)
		return res;
	} // end getStrings(json)


} // end JsonUtils
