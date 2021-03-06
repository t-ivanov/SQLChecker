package sqlchecker.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import dbfit.MySqlTest;
import fit.Parse;
import sqlchecker.config.Config;
import sqlchecker.io.impl.ScriptReader;
import sqlchecker.io.impl.SolutionReader;


/**
 * This is a facade class which provides easy to use functions 
 * for checking a students submission via DBFit
 * @author Max Hofmann
 *
 */
public class DBFitFacade {

	/**
	 * File name/path of the file which is being checked
	 */
	private String filePath = "";
	
	/**
	 * Connection properties in the following order:
	 *  (host, user, pw, dbname)
	 */
	private Config conf;
	
	/**
	 * Temporary storage for the results of a submission
	 */
	private String storage = "";


	/**
	 * Initialize a DBFit facade object
	 * @param fPath Path of the submission that should be checked
	 * @param dbConf Connection properties in the following order:
	 *  (host, user, pw, dbname)
	 */
	public DBFitFacade(String fPath, Config dbConf) {
		this.filePath = fPath;
		this.conf = dbConf;
	}
	
	
	
	
	
	
	/**
	 * Runs a submission
	 * @param sqlhtml HTML containing the submitted SQL statements, name and student id(s) of the submitter. 
	 * @throws SQLException If the function was unable to close the sql connection
	 */
	public ResultStorage runSubmission(String sqlhtml, ArrayList<String> name, ArrayList<String> matrikelnummer) throws SQLException {
		
		//System.out.println("* IN_START* \n\n" + sqlhtml + "\n \n* IN_END * \n\n");
		MySqlTest tester = null;

		ResultStorage rs = null;
		
		String errStr = "";
		
		try {
			// init connection
			tester = init();
			
			// parse & execute the submission 
			Parse target = new Parse(sqlhtml);
			tester.doTables(target);
			
			System.out.println("\n* * * RESULTS * * *");
			
			String result = getParseResult(target);
			
			// System.out.println("* OUT_START* \n\n" + result + "\n \n* OUT_END * \n\n");
			
			rs = new ResultStorage(filePath, name, matrikelnummer, result
					, tester.counts.right, tester.counts.wrong
					, tester.counts.ignores, tester.counts.exceptions);

			// right, wrong, ignored, exception
			System.out.println("Counts(1):\n\t" + tester.counts);
			System.out.println("Counts(2):\n\t" + Arrays.toString(rs.getCounts()));
			
			
			// this one is not a SQL exception!
		} catch (Exception e) {
			// store stack trace
			errStr += "\n[DBFitFacade] Exception \n" + e.getMessage() + "\n";
			for (StackTraceElement ste : e.getStackTrace()) {
				errStr += ste + "\n";
			}
			errStr += "\n\n";
			
			//print stack trace
			e.printStackTrace(System.out);
			
		} finally {
			// close connection
			if (tester != null) tester.close();
		}
		
		// a stacktrace was thrown
		if (rs == null) {
			rs = new ResultStorage(filePath, name, matrikelnummer, errStr
					, 0, 0
					, 0, sqlhtml.split("<table>").length);
		}
		
		return rs;
	}
	
	
	
	/**
	 * Creates a database connection for a MySQL endpoint
	 * @return MySqlTest instance
	 * @throws SQLException If no database connection could be created
	 * (This usually happens when the mysql service is not running)
	 */
	private MySqlTest init() throws SQLException {
		// Initialize test

		String host = conf.getHost();
		String db = conf.getDbName();
		String dbuser = conf.getUser();
		String dbpw = conf.getPw();
		
		System.out.println("Connection with values \n\thost=" + host + "\n\tdb=" + db + "\n\tuser=" + dbuser + "\n\tpw=" + dbpw);
		
		MySqlTest tester = new MySqlTest();
		tester.connect(host, dbuser, dbpw, db);
		
		return tester;
	}
	
	
	
	
	

	private String getParseResult(Parse p) {

		// get result as string
		storage = "";
		printParseStr(p, 0);
		
		return storage;
		
	}
	

	/**
	 * Stores the annotated parse String in a class
	 * variable. This makes sure that the output is stored
	 * in the correct order. <br>
	 * This function was adapted from the Parse.print() function
	 * in the fitnesse github repository
	 * @param p Parse object which should be stored
	 * @param iter Iteration counter, start at 0
	 * @see https://github.com/unclebob/fitnesse/blob/master/src/fit/Parse.java
	 */
	private void printParseStr(Parse p, int iter) {
		if(iter > SubmissionExecuter.resultLimit) {
			storage+= "\n reached threshhold";
			return;
		}
		storage += p.leader; 
		storage += p.tag;

		if (p.parts != null) {
			printParseStr(p.parts, iter++);
		} else {
			storage += p.body; 
		}
		
		storage += p.end; 
		
		if (p.more != null) {
			printParseStr(p.more, iter++);
		} else {
			storage += p.trailer;
		}

	}
	
	public static void main(String[] args){
		String html = "private/kh_b4/abgabe.txt";
		SolutionReader abgabe = new SolutionReader(html);
		abgabe.loadFile();
		DBFitFacade tester = new DBFitFacade(html, abgabe.getConnectionProperties());
		
		ScriptReader resetter = new ScriptReader("private/kh_b4/b4_reset.sql", ScriptReader.DEFAULT_DELIM, abgabe.getConnectionProperties());
		resetter.loadFile();
		
		try {
			ResultStorage rs = tester.runSubmission(abgabe.getHTML().toString(), new ArrayList<String>(), new ArrayList<String>());
			System.out.println(rs.getLogEntry());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
}
