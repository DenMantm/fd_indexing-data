import java.util.Date;


public class ChtIndexTableRow {

	String env;
	String INSTITUTION_NUMBER;
	String TABLE_NAME;
	String INDEX_VALUE;
	String PROJECT;
	String COMMENTS;
	String RECORD_DATE;
	String Status;
	
 public ChtIndexTableRow(String env, String INSTITUTION_NUMBER, String TABLE_NAME, 
		 				String INDEX_VALUE, String PROJECT, String COMMENTS, String RECORD_DATE,String Status){
	 this.env = env;
	 this.INSTITUTION_NUMBER = INSTITUTION_NUMBER;
	 this.TABLE_NAME = TABLE_NAME;
	 this.INDEX_VALUE = INDEX_VALUE;
	 this.PROJECT = PROJECT;
	 this.COMMENTS = COMMENTS;
	 this.RECORD_DATE = RECORD_DATE;
	 this.Status = Status;
	 
 }

}
