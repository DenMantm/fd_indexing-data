import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JCheckBox;


public class GuiInserIndexes extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private JTextField f_range_min;
	private JTextField f_range_max;
	private JTextField f_index_count;
	private JTextField f_institution_number;
	Controller ctr;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the dialog.
	 * @throws SQLException 
	 */
	
	
	public GuiInserIndexes(String tableName,final Controller ctr, final Gui gui) throws SQLException {
		setTitle("Book New Indexes");
		
		//initializing
		this.ctr = ctr;
		
		
		setBounds(100, 100, 891, 402);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(10, 121, 863, 210);
			contentPanel.add(scrollPane);
			{
				table = new JTable();
				scrollPane.setViewportView(table);
			}
		}
		{
			JLabel lblTable = new JLabel(tableName);
			lblTable.setBounds(379, 11, 154, 14);
			contentPanel.add(lblTable);
		}
		
		f_range_min = new JTextField();
		f_range_min.setText("0");
		f_range_min.setBounds(201, 49, 32, 20);
		contentPanel.add(f_range_min);
		f_range_min.setColumns(10);
		
		JLabel lblRangemin = new JLabel("Range_max:");
		lblRangemin.setBounds(243, 51, 80, 14);
		contentPanel.add(lblRangemin);
		
		JLabel label = new JLabel("Range_min:");
		label.setBounds(124, 52, 67, 14);
		contentPanel.add(label);
		
		f_range_max = new JTextField();
		f_range_max.setText("999");
		f_range_max.setBounds(333, 49, 32, 20);
		contentPanel.add(f_range_max);
		f_range_max.setColumns(10);
		
		JLabel lblIndexcount = new JLabel("Index_count:");
		lblIndexcount.setBounds(394, 52, 76, 14);
		contentPanel.add(lblIndexcount);
		
		f_index_count = new JTextField();
		f_index_count.setText("1");
		f_index_count.setBounds(471, 49, 32, 20);
		contentPanel.add(f_index_count);
		f_index_count.setColumns(10);
		
		JButton btnPreview = new JButton("Preview");
		btnPreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					getPreview();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		btnPreview.setBounds(555, 48, 91, 23);
		contentPanel.add(btnPreview);
		
		f_institution_number = new JTextField();
		f_institution_number.setText("00000000");
		f_institution_number.setBounds(303, 92, 67, 20);
		contentPanel.add(f_institution_number);
		f_institution_number.setColumns(10);
		
		JLabel lblNumber = new JLabel("Number:");
		lblNumber.setBounds(244, 96, 52, 14);
		contentPanel.add(lblNumber);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Save");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						
						
						//promptiong for the action
						int val	= JOptionPane.showConfirmDialog(contentPanel, "Are you sure you want to save these items?");
						System.out.println(val);
						if(val==1||val==2){return;}

						//DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
						
						//converting to an object
						Object[][] obj = ctr.getTableData(table);
						
						//putting information into object of the row--
						ArrayList <ChtIndexTableRow> rowList = new ArrayList();
						try{
						for (int i = 0; i < obj.length;i++){
						
							ChtIndexTableRow row = new ChtIndexTableRow(obj[i][0].toString(),obj[i][1].toString(),
																	obj[i][2].toString(),obj[i][3].toString(),
																	obj[i][4].toString(),obj[i][5].toString(),null,"Booked");
							
							rowList.add(row);
						}
						}
						catch(java.lang.NullPointerException ex){
							
							JOptionPane.showMessageDialog(contentPanel, "Please Populate All Necesarry fields!");
						}
						
						try {
							ctr.addToChtTable(rowList);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						gui.refreshTable();
						gui.setEnabled(true);
						dispose();
						
					}
				});
				
				JButton btnGenerateInserts = new JButton("Generate Inserts");
				btnGenerateInserts.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						String inserts = "";
						
						//DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
						
						//converting to an object
						Object[][] obj = ctr.getTableData(table);
						
						//putting information into object of the row--
						ArrayList <ChtIndexTableRow> rowList = new ArrayList();
						try{
						for (int i = 0; i < obj.length;i++){
						
							ChtIndexTableRow row = new ChtIndexTableRow(obj[i][0].toString(),obj[i][1].toString(),
																	obj[i][2].toString(),obj[i][3].toString(),
																	obj[i][4].toString(),obj[i][5].toString(),null,"Booked");
							
							rowList.add(row);
						}
						}
						catch(java.lang.NullPointerException ex){
							
							JOptionPane.showMessageDialog(contentPanel, "Please Populate All Necesarry fields!");
						}
						
						try {
							
							inserts = ctr.generateInserts(rowList);
							
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						gui.refreshTable();
						
						//dispose();

						displayInsertsGui g = new displayInsertsGui(inserts);
						g.setVisible(true);
						
					}
				});
				buttonPane.add(btnGenerateInserts);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						gui.setEnabled(true);
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		this.addWindowListener(
			    new WindowAdapter() 
			    {
			        
			        public void windowClosing(WindowEvent e) 
			        {
			        	
			        	gui.setEnabled(true);
			            
			        }

			    });
		
		
		
		
		getPreview();
	}
	
	
	public void getPreview() throws SQLException{
		
		//f_index_count.getText()
		
		NewIndexEntity ent = new NewIndexEntity(null,f_index_count.getText(),f_institution_number.getText(),f_range_min.getText(),f_range_max.getText(),null);

	//	ctr.getNewChtValues();
		DefaultTableModel tableModel = null;
		try {
			tableModel = ctr.getNewChtValues(ent);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		table.setAutoCreateRowSorter(true);
       	table.setModel(tableModel);
       	table.setRowHeight(20);
       	
//        ArrayList<String> values = new ArrayList<String>();
//        values.add("Bananas");
//        values.add("Apples");
//        values.add("Oranages");
//        values.add("Grapes");
//        values.add("Pears");
//
//        ComboBoxTableCellEditor editor = new ComboBoxTableCellEditor(values);
//        table.getColumnModel().getColumn(0).setCellEditor(editor);
       	
       	
       	ArrayList<String> envList = ctr.getEnvoirmentsList();
       	
       	String[] values = new String[envList.size()];
       	values = envList.toArray(values);
       	
       	
       	//String[] values = new String[] { "1", "2", "3" };
       	
	    TableColumn col = table.getColumnModel().getColumn(0);
	    col.setCellEditor(new MyComboBoxEditor(values));
	    col.setCellRenderer(new MyComboBoxRenderer(values));
       	
       	table.updateUI();
		
		
		
	}
	
	
}




class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {
	  public MyComboBoxRenderer(String[] items) {
	    super(items);
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	      boolean hasFocus, int row, int column) {
	    if (isSelected) {
	      setForeground(table.getSelectionForeground());
	      super.setBackground(table.getSelectionBackground());
	    } else {
	      setForeground(table.getForeground());
	      setBackground(table.getBackground());
	    }
	    setSelectedItem(value);
	    return this;
	  }
	}

	class MyComboBoxEditor extends DefaultCellEditor {
	  public MyComboBoxEditor(String[] items) {
	    super(new JComboBox(items));
	  }
	}


