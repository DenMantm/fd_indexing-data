import java.sql.*;
import java.util.ArrayList;


import oracle.jdbc.OracleTypes;

public class Connection {
	private final String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private final String DB_CONNECTION = "jdbc:oracle:thin:@172.21.64.72:1521:SYSIMP";
	public final String DB_USER;// = "simp_dstrods";
	private final String DB_PASSWORD;// = "diamond1987";
	User u = null;
	
		
		//getting user details here
	    public Connection(User u){
	    	
	    	//decripting stored encripted password
	    	 StrongAES enc = new StrongAES();
		     u.username = enc.decript(u.username);
		     u.password = enc.decript(u.password);
	    	
	    	DB_USER = u.username;
	    	DB_PASSWORD = u.password;
	    	
	    }
	    
	    public void testConnection(){
	    	
	    	//just a test connection
	    	java.sql.Connection dbConnection = null;


			
			String sql = "Select table_name from all_tables"+
			" where table_name like 'CHT_%' and table_name"+
			" not in('CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN') order by 1";

			try {
				dbConnection = getDBConnection();
				
				  Statement stmt = dbConnection.createStatement();
				  ResultSet rows = stmt.executeQuery(sql);
				  
//				while (rows.next()) {
//
//				}
			
			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (dbConnection != null) {
					try {
						dbConnection.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
			}
	    	
	    	
	    	
	    	
	    	
	    	
	    }
		
		//CHT_INDEXING_ENVIRONMENTS is for drop down list::
		
		
		public void getAllEnvoirments(ArrayList<String> list) throws SQLException{
			// except 'CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN'
			
			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;
			
			
			String getDBUSERCursorSql = "{call sysimp_util.SIMP_INDEXING_DATA.GET_INDEX_ENVIRONMENTS(?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);

				
				//defining return type of the out
				callableStatement.registerOutParameter(1, OracleTypes.CURSOR);

				// execute getDBUSERCursor store procedure
				callableStatement.executeUpdate();

				// get cursor and cast it to ResultSet
				rs = (ResultSet) callableStatement.getObject(1);
				
				
				while (rs.next()) {

					list.add(rs.getString("name"));
				}
				
			

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}
			
			
			
			

//			String sql = "select * from SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS order by 2";
//
//			try {
//				dbConnection = getDBConnection();
//				
//				  Statement stmt = dbConnection.createStatement();
//				  ResultSet rows = stmt.executeQuery(sql);
//				  
//				  
//				while (rows.next()) {
//					
//					list.add(rows.getString("name"));
//					System.out.println(rows.getString("name"));
//
//				}
//			
//			} catch (SQLException e) {
//
//				System.out.println(e.getMessage());
//
//			} finally {
//
//				if (dbConnection != null) {
//					try {
//						dbConnection.close();
//					} catch (SQLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			
//			}
//			
//			
		}
		
		
		public void getAllChtTables(ArrayList<String> list){
			// except 'CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN'
			
			java.sql.Connection dbConnection = null;
			//CallableStatement callableStatement = null;
			//ResultSet rs = null;

			
			String sql = "Select table_name from all_tables"+
			" where table_name like 'CHT_%' and table_name"+
			" not in('CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN') order by 1";

			try {
				dbConnection = getDBConnection();
				
				  Statement stmt = dbConnection.createStatement();
				  ResultSet rows = stmt.executeQuery(sql);
				while (rows.next()) {
					list.add(rows.getString("table_name"));
					System.out.println(rows.getString("table_name"));

				}
			
			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (dbConnection != null) {
					try {
						dbConnection.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
			}
			
			
		}
		
		
		public void callOracleStoredProcCURSORParameter(ArrayList<ChtIndexTableRow> list,String table_name)
				throws SQLException {

			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;

			String getDBUSERCursorSql = "{call sysimp_util.SIMP_INDEXING_DATA.GET_INDEX_BY_TABLE(?,?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
				
				
				//setting up parametres to the procedure
				
				System.out.println(table_name);
				callableStatement.setString(1, table_name);
				
				//defining return type of the out
				callableStatement.registerOutParameter(2, OracleTypes.CURSOR);

				// execute getDBUSERCursor store procedure
				callableStatement.executeUpdate();

				// get cursor and cast it to ResultSet
				rs = (ResultSet) callableStatement.getObject(2);
				
				
				while (rs.next()) {
					System.out.println("Printing: " + rs.getString("ENV"));
					
					list.add(new ChtIndexTableRow(rs.getString("ENV"),rs.getString("INSTITUTION_NUMBER"),rs.getString("TABLE_NAME"),
							rs.getString("INDEX_VALUE"),rs.getString("PROJECT"),rs.getString("COMMENTS"),rs.getString("RECORD_DATE"),
							rs.getString("STATUS")));
					
				}
				
			

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}

		}
		
		
		public void getNewIndexes(NewIndexEntity ent)
				throws SQLException {

			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;

			String getDBUSERCursorSql = "{call sysimp_util.SIMP_INDEXING_DATA.GET_NEW_INDEX_FOR_TABLE(?,?,?,?,?,?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
				
				
				//setting up parametres to the procedure
				callableStatement.setString(1, ent.table_name);
				callableStatement.setString(2, ent.occurances);
				
				//defining return type of the out
				callableStatement.registerOutParameter(3, OracleTypes.VARCHAR);
				
				callableStatement.setString(4, ent.institution);
				
				callableStatement.setString(5, ent.range_min);
				
				callableStatement.setString(6, ent.range_max);

				// execute getDBUSERCursor store procedure
				callableStatement.executeUpdate();
				
				ent.new_index =  callableStatement.getString(3);
				
				System.out.println(ent.new_index);


			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}

		}
		
		
		//generating insrts here
		
		public String generateInsert(ChtIndexTableRow row,String inserts) throws SQLException{
			
			
			
			
			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;

			String getDBUSERCursorSql = "{? = call sysimp_util.SIMP_INDEXING_DATA.GENERATE_INSERT_SCRIPT(?,?,?,?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
				
				callableStatement.registerOutParameter (1, Types.VARCHAR);
				callableStatement.setString(2, row.INSTITUTION_NUMBER);
				callableStatement.setString(3, row.TABLE_NAME);
				callableStatement.setString(4, row.INDEX_VALUE);
				callableStatement.setString(5, row.COMMENTS);

				System.out.println("Start");
				System.out.println("INSTITUTION_NUMBER "+row.INSTITUTION_NUMBER);
				System.out.println("TABLE_NAME "+row.TABLE_NAME);
				System.out.println("INDEX_VALUE "+row.INDEX_VALUE);
				System.out.println("COMMENTS "+row.COMMENTS);

				
				// execute getDBUSERCursor store procedure
				
				callableStatement.executeQuery();

				String b = callableStatement.getString(1);
				
				
				inserts += b+"\n"; 
				
				//System.out.println(inserts);
				
				

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}
			return inserts;

		}
		public void refreshDatabaseSet()
				throws SQLException {

			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;

			String getDBUSERCursorSql = "{? = call sysimp_util.SIMP_INDEXING_DATA.SYNCH_ENVIRONMENT_INDEXES()}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
				
				callableStatement.registerOutParameter (1, Types.INTEGER);

				// execute getDBUSERCursor store procedure
				
				callableStatement.executeQuery();

				int b = callableStatement.getInt(1);
				System.out.println("Refresh: "+b);
				

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}

		}
		
		
		
		//inserting new cht_index_value here
		
		public void addEntrieToDatabase(ChtIndexTableRow row)
				throws SQLException {

			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;

			String getDBUSERCursorSql = "{? = call sysimp_util.SIMP_INDEXING_DATA.INSERT_NEW_INDEX(?,?,?,?,?,?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
				
				callableStatement.registerOutParameter (1, Types.INTEGER);
				callableStatement.setString(2, row.env);
				callableStatement.setString(3, row.INSTITUTION_NUMBER);
				callableStatement.setString(4, row.TABLE_NAME);
				callableStatement.setString(5, row.INDEX_VALUE);
				callableStatement.setString(6, row.PROJECT);
				callableStatement.setString(7, row.COMMENTS);
				
				
				System.out.println("env "+row.env);
				System.out.println("INSTITUTION_NUMBER "+row.INSTITUTION_NUMBER);
				System.out.println("TABLE_NAME "+row.TABLE_NAME);
				System.out.println("INDEX_VALUE "+row.INDEX_VALUE);
				System.out.println("PROJECT "+row.PROJECT);
				System.out.println("COMMENTS "+row.COMMENTS);

				
				
				
				
				// execute getDBUSERCursor store procedure
				
				callableStatement.executeQuery();

				int b = callableStatement.getInt(1);
				System.out.println(b);
				
				// get cursor and cast it to ResultSet
//				rs = (ResultSet) callableStatement.getObject(2);
//
//				while (rs.next()) {
//								
//
//				}

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}

		}
		
		
		
		
		
		public void deleteEntrieFromDatabase(ChtIndexTableRow row)
				throws SQLException {

			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;

			String getDBUSERCursorSql = "{? = call sysimp_util.SIMP_INDEXING_DATA.DELETE_INDEX(?,?,?,?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
				
				callableStatement.registerOutParameter (1, Types.INTEGER);
				callableStatement.setString(2, row.env);
				callableStatement.setString(3, row.INSTITUTION_NUMBER);
				callableStatement.setString(4, row.TABLE_NAME);
				callableStatement.setString(5, row.INDEX_VALUE);

				System.out.println("Start");
				System.out.println("env "+row.env);
				System.out.println("INSTITUTION_NUMBER "+row.INSTITUTION_NUMBER);
				System.out.println("TABLE_NAME "+row.TABLE_NAME);
				System.out.println("INDEX_VALUE "+row.INDEX_VALUE);
				System.out.println("PROJECT "+row.PROJECT);
				System.out.println("COMMENTS "+row.COMMENTS);

				
				// execute getDBUSERCursor store procedure
				
				callableStatement.executeQuery();

				int b = callableStatement.getInt(1);
				System.out.println("Result: "+b);
				

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}

		}
		

		public java.sql.Connection getDBConnection() {

			java.sql.Connection dbConnection = null;

			try {

				Class.forName(DB_DRIVER);

			} catch (ClassNotFoundException e) {

				System.out.println(e.getMessage());

			}

			try {

				dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,DB_PASSWORD);
				
				return dbConnection;

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			}

			return dbConnection;

}
		
		
}
