import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Label;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JPasswordField;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JCheckBox;


public class LoginGui extends JDialog {

	private final JPanel contentPanel = new JPanel();
	User u = null;
	FileIO io = new FileIO();
	private JPasswordField f_db_password;
	/**
	 * Launch the application.
	 */
	
	//getting value if file exists

	
	

	/**
	 * Create the dialog.
	 */
	public LoginGui() {
		
		 try {
	            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }

		
		setTitle("Login");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setToolTipText("Login");
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			Label label = new Label("Sysimp DB Login:");
			label.setBounds(206, 29, 98, 22);
			contentPanel.add(label);
		}
		
		Label label = new Label("Login:");
		label.setBounds(143, 75, 43, 22);
		contentPanel.add(label);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(136, 111, 50, 14);
		contentPanel.add(lblPassword);
		
		final TextField f_db_login = new TextField();
		f_db_login.setBounds(192, 75, 112, 22);
		contentPanel.add(f_db_login);
		
		Label label_1 = new Label("CHT_INDEXING_APPLICATION V1.1");
		label_1.setBounds(28, 213, 225, 22);
		contentPanel.add(label_1);
		
		f_db_password = new JPasswordField();
		f_db_password.setBounds(192, 108, 112, 20);
		f_db_password.setEchoChar('*');
		f_db_password.setColumns(10);
		contentPanel.add(f_db_password);
		
		final JCheckBox chk_save_password = new JCheckBox("save password");
		chk_save_password.setSelected(true);
		chk_save_password.setBounds(191, 135, 113, 23);
		contentPanel.add(chk_save_password);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
					 	//saving object to the file to remember username and password
						String username = f_db_login.getText();
						String password = f_db_password.getText();
						
						
						//saving to the file username
						User u = new User(username,password);
						
						io.setValues(u, chk_save_password.isSelected());
						
						//try to connect to the database
						
						
						
						//setting up visible gui
						try{
							
						Connection c = new Connection(u);
						c.testConnection();
							
						Gui g = new Gui(c);
						g.setVisible(true);
						
						dispose();
						}
						catch(Exception e1){
							JOptionPane.showMessageDialog(null, "There was a problem connecting to the database, please check your usere crerdentials!");
							
						}
						
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		
		if(new File("H:\\user_indexing.ser").isFile()){
			u = io.getValues();
			f_db_login.setText(u.username);
			f_db_password.setText(u.password);
			}
		
		
		
	}
}
