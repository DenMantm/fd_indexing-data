import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;




//everything goes trough this class, al lfunctionality is then after called in the configuration
public class Controller {
	
	
	ArrayList<ChtIndexTableRow> listTable = new ArrayList();
	ArrayList<String> listChtValues = new ArrayList();
	ArrayList<String> EnvoirmentsList = new ArrayList();
	Connection c = null;
	Object[] ModelObject;
	
	public Controller(Connection c){
		this.c = c;
	}
	public void refreshDataset() throws SQLException{
		String tmp = c.DB_USER.toUpperCase();
		
		JOptionPane.showMessageDialog(null, "This Could take a while, be patient");
			c.refreshDatabaseSet();


	}
	
	public Object[][] getTableData (JTable table) {
	    DefaultTableModel dtm = (DefaultTableModel) table.getModel();
	    int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
	    Object[][] tableData = new Object[nRow][nCol];
	    for (int i = 0 ; i < nRow ; i++)
	        for (int j = 0 ; j < nCol ; j++)
	        	if(dtm.getValueAt(i,j)==null||dtm.getValueAt(i,j)==""){
	        		tableData[i][j] = "-";
	        	}
	        	else
	            tableData[i][j] = dtm.getValueAt(i,j);
	    return tableData;
	}
	
	
	
	
	public ArrayList<ChtIndexTableRow> RetrieveData(String table_name){

			try {

				c.callOracleStoredProcCURSORParameter(listTable,table_name);
				

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
			
		return listTable;
		
	}
	
	public ArrayList<String> fillChtValues(){
		
		c.getAllChtTables(listChtValues);
		
		return listChtValues;
		
	}
	
	
	//returning envoirment list
	public ArrayList<String> getEnvoirmentsList() throws SQLException{
		
		c.getAllEnvoirments(EnvoirmentsList);
		
		return EnvoirmentsList;
		
	}
	
	
	
	//when pressed 
	public void addToChtTable(ArrayList<ChtIndexTableRow> rowList) throws SQLException{
		
		for (int i = 0; i<rowList.size();i++){
			
			c.addEntrieToDatabase(rowList.get(i));
		}
		//c.addEntrieToDatabase(row);
		
	}
	
	//deleteEntrieFromDatabase
	
	
	public void deleteFromChtTable(ArrayList<ChtIndexTableRow> rowList) throws SQLException{
		

		for (int i = 0; i<rowList.size();i++){

			c.deleteEntrieFromDatabase(rowList.get(i));

		}
		//c.deleteEntrieFromDatabase(row);
		
	}
	
	
	public String generateInserts(ArrayList <ChtIndexTableRow> rowList) throws SQLException{
		
		//ChtIndexTableRow row = null;
		String inserts = "";
		for (int i = 0; i< rowList.size();i++){
			
			
			inserts = c.generateInsert(rowList.get(i),inserts);
			
			//row = rowList.get(i);
			
		}
		
		return inserts;
		
	}

	
	
	public DefaultTableModel getNewChtValues(NewIndexEntity ent) throws SQLException{
		
		//setting table name here
		ent.table_name = ModelObject[2].toString();
		
		c.getNewIndexes(ent);

		String col[] = {"Envoirment","INSTITUTION_NUMBER","TABLE_NAME", "INDEX_VALUE", "PROJECT", "COMMENTS"};
		
		DefaultTableModel tableModel = new DefaultTableModel(col, 0);
		
		try{
		String [] index_fields = ent.new_index.split(",");
		

		
		
		

		
		for (int i=0;i<index_fields.length;i++){
			
			Object[] o = ModelObject.clone();
			
			System.out.println(index_fields[i]);
			System.out.println(ent.institution);
			
			o[0] = "SYSIMP";
			o[1] = ent.institution;
			o[3] = index_fields[i];
			o[4] = "";
			o[5] = "";
			
			tableModel.addRow(o);
			
		}
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null, "There are no indexes avalable in the range!");
		}
		
		//ModelObject[3] = ent.new_index;
		

		
		
		
		
//		String table_name;
//		String occurances;
//		String range_min;
//		String range_max;
//		String new_index;
		
//		Object[] data = {env, INSTITUTION_NUMBER, TABLE_NAME, INDEX_VALUE, PROJECT, COMMENTS, 
//				   RECORD_DATE.toString()};
		
		
		
		
		//c.getNewIndexes(ent);
		
		return tableModel;
	}
	
//	private int env;
//	private String INSTITUTION_NUMBER;
//	private String TABLE_NAME;
//	private String INDEX_VALUE;
//	private String PROJECT;
//	private String COMMENTS;
//	private Date RECORD_DATE;
	
	
	public DefaultTableModel GenerateTable(ArrayList<ChtIndexTableRow> list){
		
		String col[] = {"Envoirment","INSTITUTION","TABLE_NAME", "INDEX", "PROJECT", "COMMENTS", "DATE","Status","Select"};

		DefaultTableModel tableModel = new DefaultTableModel(col, 0);
		                                            // The 0 argument is number rows.
		//JTable table = new JTable(tableModel);

		String env;
		String INSTITUTION_NUMBER;
		String TABLE_NAME; 
		String INDEX_VALUE;
		String PROJECT;
		String COMMENTS;
		String RECORD_DATE;
		String Status;
		
		for (int i = 0; i < list.size(); i++){
			
			
			env = list.get(i).env;
			INSTITUTION_NUMBER = list.get(i).INSTITUTION_NUMBER;
			TABLE_NAME = list.get(i).TABLE_NAME;
			INDEX_VALUE = list.get(i).INDEX_VALUE;
			PROJECT = list.get(i).PROJECT;
			COMMENTS = list.get(i).COMMENTS;
			RECORD_DATE = list.get(i).RECORD_DATE;
			Status = list.get(i).Status;
			   Object[] data = {env, INSTITUTION_NUMBER, TABLE_NAME, INDEX_VALUE, PROJECT, COMMENTS, 
					   RECORD_DATE.toString(),Status,false};
			   
			   tableModel.addRow(data);
			   
			   
			   
			   //dISPLAYING TO THE CONSOLE
			   
				System.out.println("Env is : " + env);
				System.out.println("INSTITUTION_NUMBER : " + INSTITUTION_NUMBER);
				System.out.println("TABLE_NAME : " + TABLE_NAME);
				System.out.println("INDEX_VALUE: " + INDEX_VALUE);
				System.out.println("PROJECT:  " + PROJECT);
			   
				
				//This peace provides information on currently selected cht.,.
				if (i == 0){
					ModelObject = data;
				}
			   
			   
			}
			
			
			
		return tableModel;
		
	}
	

}
