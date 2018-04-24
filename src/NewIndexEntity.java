import oracle.jdbc.OracleTypes;


public class NewIndexEntity {

	String table_name;
	String occurances;
	String range_min;
	String institution;
	String range_max;
	String new_index;
	
	public NewIndexEntity(	String table_name, String occurances, String institution, String range_min, String range_max, String new_index){
		this.table_name = table_name;
		this.occurances = occurances;
		this.range_min = range_min;
		this.institution = institution;
		this.range_max = range_max;
		this.new_index = new_index;
	}
	
}
