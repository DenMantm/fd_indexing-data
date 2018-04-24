import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import java.awt.Choice;
import javax.swing.JTextField;


public class Gui extends JFrame {

	private JPanel contentPane;
	DefaultTableModel tableModel;
	ArrayList<ChtIndexTableRow> list;
	
	private JTable table;
	Object item = "CHT_ACCOUNT_CATEGORY";
	Gui gui;
	
	
	
	Connection c = null;
	Controller ctr;
	/**
	 * Create the frame.
	 */
	public Gui(Connection c) {
		this.c = c;
		
		//settings here
		this.setResizable(false);
		gui = this;

		
		
		setTitle("CHT Indexes Generator V1.1");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1114, 566);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		final JPanel panel = new JPanel();
		panel.setBounds(10, 73, 1096, 366);
		contentPane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);
		
		
		ctr  = new Controller(c);
		list = ctr.RetrieveData("cht_curency");
		
		tableModel = ctr.GenerateTable(list);


		table = new JTable();
		scrollPane.setViewportView(table);
		

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(10, 11, 885, 51);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Table:");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblNewLabel.setBounds(272, 11, 58, 29);
		panel_1.add(lblNewLabel);
		
		
		final Choice choice = new Choice();
		choice.setBounds(324, 20, 177, 20);
		choice.addItemListener(new ItemChangeListener(){
		    public void itemStateChanged(ItemEvent event) {
		        if (event.getStateChange() == ItemEvent.SELECTED) {
		        	
		           item = event.getItem();


		           refreshTable();
		           
		        }
		     }    
			
			
			
			
		});
		
		list = ctr.RetrieveData(item.toString());

        //	DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        	//tableModel.setRowCount(0);
        	tableModel = ctr.GenerateTable(list);
        		
        	//clearing array list of the entries
        	list.clear();

	          //Tell the JTable to update
        	
       
        	table.setAutoCreateRowSorter(true);
        	table.setModel(tableModel);
        	
        	//table.setAutoResizeMode(JTable.AUTO_RESIZE_ON);
        	
        	//table.getColumnModel().getColumn(0).setPreferredWidth(150);
        	//table.getColumnModel().getColumn(1).setPreferredWidth(100);
        	//table.getColumnModel().getColumn(2).setPreferredWidth(165);
        	//table.getColumnModel().getColumn(3).setPreferredWidth(50);
        	//table.getColumnModel().getColumn(4).setPreferredWidth(100);
        	//table.getColumnModel().getColumn(5).setPreferredWidth(300);
        	//table.getColumnModel().getColumn(6).setPreferredWidth(70);
        	//table.getColumnModel().getColumn(7).setPreferredWidth(70);
        	//table.getColumnModel().getColumn(7).setPreferredWidth(35);
        	
        	table.setRowHeight(20);
        	
        	
        	TableColumn col4 = table.getColumnModel().getColumn(8);
        	col4.setCellEditor(table.getDefaultEditor(Boolean.class));
        	col4.setCellRenderer(table.getDefaultRenderer(Boolean.class));
        	//col4.setPreferredWidth(50);
		
		
		
		ArrayList<String> list = ctr.fillChtValues();
		
		for(int i =0; i<list.size();i++){
			
			choice.add(list.get(i));
		}
		
		//choice.add("cht_currency");
		//choice.add("cht_country");
		
		
		
		panel_1.add(choice);
		
		//add button here
		
		
		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	           
				
				
				String tableName = choice.getSelectedItem();
				GuiInserIndexes g = null;
				try {
					g = new GuiInserIndexes(tableName,ctr,gui);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				g.setVisible(true);
				
				gui.setEnabled(false);
				
			}
		});
		btnNew.setBounds(784, 17, 91, 23);
		panel_1.add(btnNew);
		

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(0, 450, 1096, 78);
		contentPane.add(panel_2);
		
		JButton btnNewButton = new JButton("Delete Selected");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				
				boolean ask = true;
				
				//converting to an object
				Object[][] obj = ctr.getTableData(table);
				
				//putting information into object of the row--
				ArrayList <ChtIndexTableRow> rowList = new ArrayList();
				try{
				for (int i = 0; i < obj.length;i++){
					
					if( Boolean.parseBoolean(obj[i][8].toString())){
						
						if ( ask ){
					int val	= JOptionPane.showConfirmDialog(contentPane, "Are you sure you want to delete these items?");
						System.out.println(val);
						if(val==1||val==2){return;}
						ask = false;
						}
						
					ChtIndexTableRow row = new ChtIndexTableRow(obj[i][0].toString(),obj[i][1].toString(),
															obj[i][2].toString(),obj[i][3].toString(),
															obj[i][4].toString(),obj[i][5].toString(),null,"Booked");
					
					rowList.add(row);
					}
				}
				}
				catch(java.lang.NullPointerException ex){
					
					JOptionPane.showMessageDialog(contentPane, ex.getMessage());
				}
				
				try {
					
					ctr.deleteFromChtTable(rowList);
					
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				//here we would have to refresh the table -->
				refreshTable();
				
				
				
			}
		});
		
		JButton btnNewButton_1 = new JButton("Refresh Dataset");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					ctr.refreshDataset();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		//btnNewButton_1.setEnabled(false);
		panel_2.add(btnNewButton_1);
		panel_2.add(btnNewButton);
		
		refreshTable();
	}
	
	
	
	public void refreshTable(){
		
    	
        //Object item = event.getItem();


        	list = ctr.RetrieveData(item.toString());

        //	DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        	//tableModel.setRowCount(0);
        	tableModel = ctr.GenerateTable(list);
        		
        	//clearing array list of the entries
        	list.clear();

	          //Tell the JTable to update
        	
       
        	
        	table.setModel(tableModel);
        	
        	//table.setAutoResizeMode(JTable.AUTO_RESIZE_ON);
        	
        	//table.getColumnModel().getColumn(0).setPreferredWidth(150);
        	//table.getColumnModel().getColumn(1).setPreferredWidth(100);
        	//table.getColumnModel().getColumn(2).setPreferredWidth(165);
        	//table.getColumnModel().getColumn(3).setPreferredWidth(50);
        	//table.getColumnModel().getColumn(4).setPreferredWidth(100);
        	//table.getColumnModel().getColumn(5).setPreferredWidth(300);
        	//table.getColumnModel().getColumn(6).setPreferredWidth(70);
        	//table.getColumnModel().getColumn(7).setPreferredWidth(70);
        	//table.getColumnModel().getColumn(7).setPreferredWidth(35);
        	
        	table.setRowHeight(20);
        	
        	
        	TableColumn col4 = table.getColumnModel().getColumn(8);
        	col4.setCellEditor(table.getDefaultEditor(Boolean.class));
        	col4.setCellRenderer(table.getDefaultRenderer(Boolean.class));
        	//col4.setPreferredWidth(50);
        	
        	
        	table.updateUI();

        
        System.out.println(item.toString());
		
		
		
		
	}
}

class ItemChangeListener implements ItemListener{

	
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

}




//class ItemChangeListener implements ItemListener{
//   
//    JTable table;
//    public ItemChangeListener(JTable table){
//    	this.table = table;
//    	
//    }
//    public void itemStateChanged(ItemEvent event) {
//       if (event.getStateChange() == ItemEvent.SELECTED) {
//          Object item = event.getItem();
//         // item.toString();
//          
//          
//      	DefaultTableModel tableModel;
//    	ArrayList<ChtIndexTableRow> list;
//    	Controller ctr;
//          
//  		ctr  = new Controller();
//  		list = ctr.RetrieveData("cht_country");
//  		
//  		tableModel = ctr.GenerateTable(list);
//  		
//  		table = new JTable(tableModel);
//  		table.repaint();
//  		
//  		
//          System.out.println(item.toString());
//       }
//    }       
//}
