package gt.research.util;

import java.util.LinkedList;

public class DataEntity<V> {

	public String type;
	public V value;
	public String worksheet;

	public int row, col, group;
	public boolean isVisible;

	public LinkedList<String> tags = new LinkedList<String>();

	public DataEntity(String type, V value, String worksheet, int row, int col, int group){
		this.type = type;
		this.value = value;
		this.worksheet = worksheet;
		this.row = row;
		this.col = col;
		this.group = group;
	}

	public String toXMLString(){
		String xml = "<CELLGROUP><CELL><WORKSHEET>" + worksheet	+ "</WORKSHEET><ROW>" + row + "</ROW><COL>"
	    + col + "</COL><DATA node_type=\"" + type+ "\">" + value + "</DATA>";

		for(int i = 0; i < tags.size(); i++){
			xml = xml + "<TAG>" + tags.get(i) + "</TAG>";
		}

		xml = xml + "</CELL></CELLGROUP>";
		return xml;
	}
}
