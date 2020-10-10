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
 * extend Properties to add methods of handling
 * Json input, using the BFO Json objects.
 *
 * Updated:
 *
 * Sun 09 Aug 2020 10:37:07 AM CDT
 *
 *
 */
public class JsonProperties extends Properties
{
	/*
	 * no-arg constructor
	public JsonProperties()
	{
		this(); // initialize empty resource
	}
	maybe not...
	 */

	/*
	 * constructor where we give it brand-new, proper Json object (see replace())
	 */
	public JsonProperties(Json input) throws Exception
	{
		// maybe not...this(); // initialize empty resource
		replace(input);
	}



	public void replace(Json newstuff) throws Exception
	{
		clear(); // remove any contents
		add(newstuff);
	}

	/*
	* pass a Json object that can enclose the following:
	*
	* 1) a Map, then we just copy everything over, key=strint
	*    content=String (we hope, but actually Json could have
	*    other stuff) that is populated into this Properties
	*    instance
	*
	* 2) a List, which must have an even number of entries
	*    contents of each Json in the List will be a String
	*    (we hope, but actually Json could have
	*    other stuff). the pairs are populated into this Properties
	*    instance
	*/
	public void add(Json morestuff) throws IllegalArgumentException
	{
		Object someobject = null;
		/*
		if (!morestuff.isMap())
		{
		throw new Exception ("JSON is not Map: " + morestuff);
		}
		*/
		if (morestuff.isMap())
		{
			Map stuff = morestuff.mapValue(); // remember Json Maps are String keys!
			Iterator ii = stuff.keySet().iterator();
			int position = 0; // for debugging
			while (ii.hasNext())
			{
				someobject = ii.next(); // better be a Json with string
				String jkey = (String)someobject;  // cast had better work
				someobject = stuff.get(jkey); // key is String, payload is Json with string (probably)
				Json jvalue = (Json)someobject; // cast must work
//					System.out.println("Have value: " + jvalue.stringValue());
				setProperty(jkey,jvalue.stringValue()); // populate the key value pairs (value could be something else, user must deal with it)
				position++; // for debugging
			} // end loop on all keys in Map
			return; // good
		} // end if is a Map
		else
		{
			/*
			if (!desired.isList())
			{
			throw new Exception ("JSON project is not List: " + desired);
			}
			*/
			if (morestuff.isList())
			{
				List alist = morestuff.listValue();
				if (alist.size() %2 != 0)
				{
					throw new IllegalArgumentException("Passed a Json List, but length is not even, object: " + 
							morestuff + ", size: " + alist.size());
				}
				for (int ii = 0 ; ii < alist.size() ; ii += 2)
				{
				    someobject = alist.get(ii);
				    Json jj = (Json)someobject; // cast MUST work
				    if (!(jj.isString()))
				    {
					throw new IllegalArgumentException("Json List key item not String, object: " + 
							jj + ", position: " + ii);
				    }
				    String key = jj.stringValue();
				    someobject = alist.get(ii+1); // value can be anything if user can stand it
				    jj = (Json)someobject; // cast MUST work
				    setProperty(key,jj.stringValue()); // value might not be String, user is responsible
				} // end loop through all key/value pairs
				return;
			}  // end if List with correct formatting
			else
			{
				// something ELSE, don't know what
				// morestuff is data we cannot work with (for now)
				throw new IllegalArgumentException("Json Properties input is unknown, object: " + 
							morestuff);
			}
		} // end else if not Map
		//System.out.println("Properties: " + all_props);
	} // end add(json)

} // end Json Properties
