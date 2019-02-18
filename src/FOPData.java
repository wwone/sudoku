import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class FOPData 
{
//    private static final String CELL_CONTENT_BLOCK =    "<fo:block text-align=\"center\">";
    private static final String CELL_CONTENT_BLOCK =    "<fo:block text-align=\"center\" font-size=\"30pt\" >";
    private static final String BLOCK_END = "</fo:block>";

    private static final String HEAD_START = "<fo:block space-before=\"36pt\">Puzzle Difficulty: ";
    private static final String COUNT_START = "<fo:block>";
    private static final String TAIL_START = "<fo:block space-before=\"18pt\" >Cell Counts: </fo:block>";
    private static final String TAIL_START_TITLE = "<fo:block space-before=\"18pt\" >";
    private static final String TAIL_START_AFTER = " </fo:block>";
//    private static final String TAIL_START = "<fo:block space-before=\"18pt\" page-break-after=\"always\">Cell Counts: ";
       private static final String MAKE_BREAK = "<fo:block page-break-after=\"always\"></fo:block>";
    private static final String CELL_START2 = ">\n"; // put in all attributes then terminate tag
    private static final String CELL_START1  = "<fo:table-cell display-align=\"center\" ";
//    private static final String CELL_START1  = "<fo:table-cell ";
    private static final String CELL_END  = "</fo:table-cell> ";
    
    private static final String TABLE_START = "<fo:table border-collapse=\"separate\" table-layout=\"fixed\" width=\"100%\">";

    private static final String COLUMN_DESCRIPTORS =
        "<fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/><fo:table-column column-width=\"3cm\"/>";
    
    private static final String START_TABLE_BODY = "<fo:table-body>";
    
    private static final String TABLE_BODY_END = "</fo:table-body></fo:table>";
//    private static final String TABLE_BODY_END = "</fo:table-body></fo:table><fo:block page-break-after=\"always\"/>";
//    private static final String TABLE_BODY_END = "</fo:table-body></fo:table>";
    
    
    private static final String ROW_START = "<fo:table-row height=\"36pt\">";
    //    private static final String ROW_START = "<fo:table-row>";
    private static final String ROW_END = "</fo:table-row>";

    private static final String CELL_BORDERS[][] = {
        {    "border-top-color=\"black\" border-top-width=\".5pt\" border-top-style=\"solid\"",
                "border-top-color=\"black\" border-top-width=\"2pt\" border-top-style=\"solid\""},
    {  "border-right-color=\"black\" border-right-width=\".5pt\" border-right-style=\"solid\"",
            "border-right-color=\"black\" border-right-width=\"2pt\" border-right-style=\"solid\""},
                {    "border-bottom-color=\"black\" border-bottom-width=\".5pt\" border-bottom-style=\"solid\"",
                        "border-bottom-color=\"black\" border-bottom-width=\"2pt\" border-bottom-style=\"solid\""},
            {        "border-left-color=\"black\" border-left-width=\".5pt\" border-left-style=\"solid\"",
                    "border-left-color=\"black\" border-left-width=\"2pt\" border-left-style=\"solid\""}
    };
/*    private static final String CELL_BORDERS[][] = {
        {    "border-top-color=\"black\" border-top-width=\"1pt\" border-top-style=\"solid\"",
                "border-top-color=\"black\" border-top-width=\"2pt\" border-top-style=\"solid\""},
    {  "border-right-color=\"black\" border-right-width=\"1pt\" border-right-style=\"solid\"",
            "border-right-color=\"black\" border-right-width=\"2pt\" border-right-style=\"solid\""},
                {    "border-bottom-color=\"black\" border-bottom-width=\"1pt\" border-bottom-style=\"solid\"",
                        "border-bottom-color=\"black\" border-bottom-width=\"2pt\" border-bottom-style=\"solid\""},
            {        "border-left-color=\"black\" border-left-width=\"1pt\" border-left-style=\"solid\"",
                    "border-left-color=\"black\" border-left-width=\"2pt\" border-left-style=\"solid\""}
    };
    */
    
    private static final int CELL_POSITIONS[][][] =
    {
        {            {2,1,1,2}, // row 1,4,7, col 1,4,7
        {2,1,1,1}, // subscripts to border setups above (must subtract 1)
        {2,2,1,1}}, // col 3,6,9
    {    {1,1,1,2}, // row 2,5,8, col 1,4,7
    {1,1,1,1},  // col 2,5,8
    {1,2,1,1}},     // col 3,6,9
        {         {1,1,2,2}, // row 3,6,9, col 1,4,7
        {1,1,2,1},   // col 2,5,8
        {1,2,2,1}  // col 3,6,9
        } 
    };
    
    public static String showNumbers(String info)
    {
        StringBuffer result = new StringBuffer(1000);
        result.append(TAIL_START);
        
        result.append(info);        
        result.append(BLOCK_END); // will space after HERE
        return result.toString();
    }
    
    public static String showNumbersprev(Map themap)
    {
        StringBuffer result = new StringBuffer(1000);
       result.append(TAIL_START);
        /*
         * show the counts
         */
        String position = null;
        Iterator ii = themap.keySet().iterator();
        while (ii.hasNext())
        {
            position = (String)ii.next(); // key
            // one line each
            /*
             * next idea is to make a histogram
             */
            result.append(COUNT_START);
            result.append(position + ": ");
            Integer count = (Integer)themap.get(position); // get the count
            result.append(count.toString());
            result.append(BLOCK_END); 
        }
        result.append(MAKE_BREAK); 
        return result.toString();
    }
    
    public static String showComment(String contents)
    {
        //StringBuffer result = new StringBuffer(1000);
        //result.append(TAIL_START_TITLE + contents  + 
	//	TAIL_START_AFTER);  // in its own line
        return(TAIL_START_TITLE + contents  + 
		TAIL_START_AFTER);  // in its own line
	}
    
    public static String showNumbers(Map themap)
    {
        StringBuffer result = new StringBuffer(1000);
        result.append(TAIL_START_TITLE + "Cell Population Counts:" + TAIL_START_AFTER); // title in its own line
        /*
         * show the counts
         */
        String position = null;
        Iterator ii = themap.keySet().iterator();
        result.append(COUNT_START); // counts in their own line
        while (ii.hasNext())
        {
            position = (String)ii.next(); // key
            // one line each
            /*
             * next idea is to make a histogram
             */
            result.append(position + ": ");
            Integer count = (Integer)themap.get(position); // get the count
            result.append(count.toString() + ",  ");
        }
        result.append(BLOCK_END); // end count line
        /*
         * create longs that will contain all populations. if 3 1's, then we create 3 separate 1's
         */
        Kurtosis k = new Kurtosis();
        ArrayList valuesx = new ArrayList(50); // sorta
        ii = themap.keySet().iterator();
        while (ii.hasNext())
        {
            position = (String)ii.next(); // key
            Integer count = (Integer)themap.get(position); // get the count
            for (int inner=0 ; inner < count.intValue() ; inner++)
            {
                // make one each
                valuesx.add(new Double(position));
            }
        }
        // convert Doubles to double array
        double valuesy[] = new double[valuesx.size()];
        ii = valuesx.iterator();
        int inx = 0;
        double values_total = 0.0;
        while (ii.hasNext())
        {
            valuesy[inx] = ((Double)ii.next()).doubleValue();
            values_total += valuesy[inx];
            inx++;
        }
        double result_kurtosis = k.evaluate(valuesy,0,valuesx.size());
        Mean mm = new Mean();
        double result_mean = mm.evaluate(valuesy,0,valuesx.size());
        StandardDeviation stdev = new StandardDeviation();
        double result_stdev = stdev.evaluate(valuesy,result_mean,0,valuesx.size());
        Skewness skew = new Skewness();
        double result_skew = skew.evaluate(valuesy,0,valuesx.size());
        Variance var = new Variance();
        double result_var = var.evaluate(valuesy);
        String formatter = "%4.2f"; // should work for most
        result.append(COUNT_START); // stats in their own line
        result.append("Total Items Filled In: " + valuesx.size());
        result.append(BLOCK_END); // end totals line
        result.append(COUNT_START); // stats in their own line
        result.append("Mean: " + String.format(formatter,result_mean) + ", StDev: " + String.format(formatter,result_stdev) + ", Variance: " + String.format(formatter,result_var));
        //        result.append("Mean: " + result_mean + ", StDev: " + result_stdev + ", Variance: " + result_var);
        result.append(BLOCK_END); // end mean/stdevs line
        result.append(COUNT_START); // stats in their own line
        result.append("Kurtotis: " + String.format(formatter,result_kurtosis) + ", Skewness: " + String.format(formatter,result_skew));
//        result.append("Kurtotis: " + result_kurtosis + ", Skewness: " + result_skew);
        result.append(BLOCK_END); // end kurtosis line
        result.append(MAKE_BREAK); // PAGE BREAK HERE!
        return result.toString();
    }
    
    public static String getFOP(int row, int col, String contents, int difficulty)
    {
        StringBuffer result = new StringBuffer(1000);
        if ( (row == 0) && (col == 0) ) 
        {
            // no difficulty right now result.append(HEAD_START + String.valueOf(difficulty) + BLOCK_END);
            // START OF TABLE
            result.append(TABLE_START);
// no force column width            result.append(COLUMN_DESCRIPTORS);
            result.append(START_TABLE_BODY);
        }
        if (col == 0)
        {
            // START ROW
            result.append(ROW_START);
            
        }
        /*
         * make a cell
         */
        result.append(CELL_START1); // start of cell tag
        result.append(getFOPCellBorders(row,col)); // border settings
        result.append(CELL_START2);
        result.append(CELL_CONTENT_BLOCK);
        result.append(contents);
        result.append(BLOCK_END);
        result.append(CELL_END);
        
        if (col == 8)
        {
            // END ROW after cell
            result.append(ROW_END);
            if (row == 8)
            { // end of whole thing
                result.append(TABLE_BODY_END);
            }
        }
        return result.toString();
    } // end getFOP
    
    
    public static String getFOPCellBorders(int row, int col)
    {
        int row_position = (row % 3); // repeats in triads
        int col_position = (col % 3); // repeats in triads
    //    System.out.println("Getting data for row,col: " + row + ", " + col); 
        int[] subs = CELL_POSITIONS[row_position][col_position];
        StringBuffer result = new StringBuffer();
        for (int i = 0 ; i < subs.length ; i++)
        {
            // subs item will be value 1 or 2, make subscript by subtracting 1
            //            CELL_BORDERS[i][subs[i] - 1];
//            result.append(subs[i] + " ");
            result.append(CELL_BORDERS[i][subs[i] - 1] + " ");
        }
        return result.toString();
    } // end getFOPCellBorders
    
    public static String showCountsprev(String title,
                                    int[] counts)
    {
//        StringBuffer result = new StringBuffer("<!-- " + title + " ");
        StringBuffer result = new StringBuffer();
        result.append(TAIL_START_TITLE + title + TAIL_START_AFTER);
        
        for (int i = 0 ; i < 10; i++)
        {
            result.append(COUNT_START);
            result.append(String.valueOf(i) + ": ");
            result.append(String.valueOf(counts[i]));
            result.append(BLOCK_END); 
            
//            result.append(String.valueOf(i) + ": " + String.valueOf(counts[i]) + " ");
        }
        // no page break
        return result.toString();
    } // end showcounts
    
    public static String showCounts(String title,
                                    int[] counts)
    {
        //        StringBuffer result = new StringBuffer("<!-- " + title + " ");
        StringBuffer result = new StringBuffer();
        result.append(TAIL_START_TITLE + title + TAIL_START_AFTER); // title by itself
        result.append(COUNT_START); // counts on their own line
        for (int i = 0 ; i < 10; i++)
        {
            result.append(String.valueOf(i) + ": ");
            result.append(String.valueOf(counts[i]) + ",  ");
            
            //            result.append(String.valueOf(i) + ": " + String.valueOf(counts[i]) + " ");
        }
        result.append(BLOCK_END); // end of line with the counts
        // no page break
        return result.toString();
    } // end showcounts
        
    
} 
