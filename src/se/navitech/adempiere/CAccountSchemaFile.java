/*
 * CAccountSchemaFile.java
 *
 * Created on den 31 december 2005, 16:23
 *
 */

package se.navitech.adempiere;

import java.util.*;
import java.util.regex.*;
import java.io.*;

/**
 * Represents an account schema file as accepted by Compiere.
 *
 * The file format should be comma separated with or without quotation marks " "
 * to enclose the values.
 *
 * The first row of the file is always fixed. The file has 19 columns (values per row).
 *
 * @author Daniel Norin
 */
public class CAccountSchemaFile {

    /** Column names */
    private final static String[] m_cols = {
             "Account_Value",
             "Account_Name",
             "Account_Description",
             "Account_Type",
             "Account_Sign",
             "Account_Document",
             "Account_Summary",
             "Default_Account",
             "Account_Parent",
             "Balance Sheet",
             "Balance Sheet_Name",
             "US 1120 Balance Sheet",
             "US 1120 Balance Sheet_Name",
             "Profit & Loss",
             "Profit & Loss_Name",
             "US 1120 Income Statement",
             "US 1120 Income Statement_Name",
             "Cash Flow",
             "Cash Flow_Name"
    };
    
    private final static int MIN_NCOLS = 9;  // just first 9 columns are needed
    private final static int NCOLS = m_cols.length;
    
    private File    m_file;
    private char    m_separator;
    private Vector<StringBuffer[]>  m_lines;
    private CAccountSchema      m_schema;
    private SortedMap<String, CDefaultAccount>  m_defaultAccounts;
    
    /** Creates a new instance of CAccountSchemaFile */
    public CAccountSchemaFile(SortedMap<String,CDefaultAccount> defaultAccounts) {
        m_defaultAccounts = defaultAccounts;
    }
    
    /**
     * Opens given file
     */
    public void readFile(File file) throws Exception {
        m_file = file;
        m_lines = new Vector();
        // Read the file into lines
        readFile();
        // Convert the lines into account elements
        m_schema = new CAccountSchema(m_defaultAccounts);
        convertToElements();
    }

    /**
     * Creates a new file
     */
    public void newFile() {
        m_file = null;
        m_schema = new CAccountSchema(m_defaultAccounts);
    }

    /**
     * Saves file
     */
    public void saveFile(File file) throws Exception {
        // Open the file for writing
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        // Make sure we have comma as a separator char
        m_separator = ',';
        // Create the first line
        StringBuffer lineBuf = new StringBuffer();
        for(int i=0; i<m_cols.length; i++) {
            if (lineBuf.length()>0) {
                // Add separator
                lineBuf.append(m_separator);
            }
            lineBuf.append("[" + m_cols[i] + "]");
        }
        lineBuf.append("\n");
        out.write(lineBuf.toString());
        
        // Write the remaining lines
        SortedMap<String,CAccountElement> elements = m_schema.getElements();
        CAccountElement elem;
        for (Iterator<String> it = elements.keySet().iterator(); it.hasNext(); ) {
            elem = elements.get(it.next());
            lineBuf = new StringBuffer();
            lineBuf.append(elem.getKey());
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getName()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getDescription()));
            lineBuf.append(m_separator);
            lineBuf.append(elem.getType());
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getSign()));
            lineBuf.append(m_separator);
            lineBuf.append(elem.isDocument() ? "Yes" : "");
            lineBuf.append(m_separator);
            lineBuf.append(elem.isSummary() ? "Yes" : "");
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getDefaultAccount()));
            lineBuf.append(m_separator);
            lineBuf.append(elem.getParentKey());
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getBalanceSheet()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getBalanceSheetName()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getUs1120sheet()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getUs1120sheetName()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getProfitLoss()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getProfitLossName()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getUs1120incomeStmt()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getUs1120incomeStmtName()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getCashFlow()));
            lineBuf.append(m_separator);
            lineBuf.append(formatColumn(elem.getCashFlowName()));
            lineBuf.append("\n");
            out.write(lineBuf.toString());
        }
        out.close();
        
    }

    /**
     * Format the column. If it contains separator characters it will be enclosed
     * by quotation marks.
     */
    private String formatColumn(String col) {
        if (col==null) return("");
        if (col.indexOf(m_separator)>=0) {
            // Check if we have quote characters
            if (col.indexOf("\"")>=0) {
                col = replaceQuotes(col);
            }
            // Enclose the column with quotes
            col = "\"" + col + "\"";
        }
        return(col);
    }
    
    private String replaceQuotes(String str) {
        Pattern pattern = Pattern.compile("\"");
        Matcher match = pattern.matcher(str);
        return(match.replaceAll("'"));
    }
    
    public CAccountSchema getSchema() {
        return(m_schema);
    }
    
    /**
     * Convert the lines into account elements
     */
    private void convertToElements() {
        CAccountElement elem;
        String defAcctStr;
        for (int i=0; i<m_lines.size(); i++) {
            try {
                elem = new CAccountElement(m_lines.get(i));
                // Check the default accounts
                defAcctStr = elem.getDefaultAccount();
                if (defAcctStr!=null && defAcctStr.trim().length()>0) {
                    // Make sure the given default account is listed in the list
                    CDefaultAccount defAcct = m_defaultAccounts.get(elem.getDefaultAccount());
                    if (defAcct==null) {
                        // Issue a warning and set default account to null.
                        System.err.println("Invalid default account '" + elem.getDefaultAccount() + "' on line " + (i+2) + ".");
                        elem.setDefaultAccount(null);
                    }
                }
                m_schema.addAccountElement(elem);
            } catch (Exception e) {
                System.err.println("Error on line " + (i+2) + ". " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    } 
    
    /**
     * Validate the format of the file
     */
    private boolean readFile() throws Exception {
        // Open the file for reading
        BufferedReader in = new BufferedReader(new FileReader(m_file));
        String line;
        String firstLine = null;
        boolean result = false;
        Pattern pattern = null;
        while((line = in.readLine())!=null) {
            if (firstLine == null) {
                // We are processing the first header line.
                firstLine = line;
                result = checkFirstLine(line);
            } else {
                // We're processing a normal line.
                processLine(line);
            }
            
        }
        in.close();
        return(result);
    }

    /** 
     * Processes a normal line.
     */
    public boolean processLine(String line) throws Exception {
        StringBuffer[] cols = new StringBuffer[NCOLS];
        // Walk through the line
        int n = 0;
        int pos = 0;
        boolean end = false;
        boolean begin = true;
        char lookFor = 0;
        cols[0] = new StringBuffer();
        while(!end && pos<line.length()) {
            if (begin) {
                lookFor = 0;
                if (line.charAt(pos)=='"') {
                    lookFor = '"';
                }
                if (line.charAt(pos)=='\'') {
                    lookFor = '\'';
                }
                begin = false;
                if (lookFor!=0) pos++;
                continue;
            }
            if (line.charAt(pos)==lookFor) {
                pos++;  // Go to next character
                lookFor=0;
                continue;
            }
            if (line.charAt(pos)==m_separator && lookFor==0) {
                pos++;
                n++;    // Go to next field
                if (n>=NCOLS) {
                    end = true;
                    continue;
                }
                cols[n] = new StringBuffer();
                begin = true;
                continue;
            }
            cols[n].append(line.charAt(pos));
            pos++;
        }
        // Add the line to the structure
        m_lines.add(cols);
        return(true);
    }
    
    /**
     * Checks if the first line is a valid header. Sets separator character. (comma or semicolon)
     *
     * @param   line    The line to be checked.
     */
    private boolean checkFirstLine(String line) throws Exception {
        // Split the line
        Pattern pattern = Pattern.compile("\\]{0,1},\\[{0,1}", Pattern.CASE_INSENSITIVE);
        String[] colNames = pattern.split(line);
        if (colNames.length>=MIN_NCOLS) {
            m_separator = ',';
        } else {
            // Check semi colon
            pattern = Pattern.compile("\\]{0,1};\\[{0,1}", Pattern.CASE_INSENSITIVE);
            colNames = pattern.split(line);
            if (colNames.length>=MIN_NCOLS) {
                m_separator = ';';
            }
        }
        int numLoadedCols = colNames.length;
        // Remove quotes and square brackets (if any) in the beginning and end
        if (colNames.length>=MIN_NCOLS) {
            for (int i=0; i<numLoadedCols; i++) {
                if (colNames[i].startsWith("\"")) {
                    colNames[i] = colNames[i].substring(1);
                }
                if (colNames[i].endsWith("\"")) {
                    colNames[i] = colNames[i].substring(0,  colNames[i].length()-1);
                }
                if (colNames[i].startsWith("[")) {
                    colNames[i] = colNames[i].substring(1);
                }
                if (colNames[i].endsWith("]")) {
                    colNames[i] = colNames[i].substring(0,  colNames[i].length()-1);
                }
            }
        }
        if (colNames.length<=1) {
            throw new Exception("There's no header in this file.");
        }
        
        // Go through each column to make sure they are in order
        for (int i=0; i<numLoadedCols; i++) {
            if (!m_cols[i].equals(colNames[i])) {
                throw new Exception("Expected column [" + m_cols[i] + "]. Found [" + colNames[i] + "].");
            }
        }
        
        return(colNames.length>=MIN_NCOLS);
    }
    
}
