import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



//This is for just to handle password saving, saves user object onto h drive
public class FileIO {
	
	StrongAES enc;
	public FileIO(){
		enc = new StrongAES();
	}

	
	public void setValues(User u,boolean savePassword){
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
		     fout = new FileOutputStream("H:\\user_indexing.ser", false);
		     oos = new ObjectOutputStream(fout);
		     
		     //encripting password and username
		     u.username = enc.encript(u.username);
		     u.password = enc.encript(u.password);
		     
		     if (!savePassword){
		    	 String tmp = u.password;
		    	 u.password = "";
		    	 oos.writeObject(u);
		    	 u.password = tmp;
		     }
		     else{
		    	 oos.writeObject(u);
		     }
		    
		    oos.close();
		} catch (Exception ex) {
		    ex.printStackTrace();
		} finally {
		    if(oos  != null){
		        try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    } 
		}
	}
	public User getValues(){
		
		ObjectInputStream objectinputstream = null;
		User u = null;
		
		try {
			FileInputStream streamIn = new FileInputStream("H:\\user_indexing.ser");
		     objectinputstream = new ObjectInputStream(streamIn);
		    u = (User) objectinputstream.readObject();
		    
		    
		     u.username = enc.decript(u.username);
		     u.password = enc.decript(u.password);
		    
		    
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    if(objectinputstream != null){
		        try {
					objectinputstream .close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    } 
		}
		return u;
	}
	
}
