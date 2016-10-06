package gt.research.xacml.test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ExcelCell
{
	private enum d_type {float_type, string_type};

	private int row;
	private int col;
	private String worksheet;
	private String s_data;
	private float f_data;

	private d_type data_type;

	public ExcelCell(int r, int c, String ws, String d)
	{
		row = r;
		col = c;
		worksheet = ws;
		s_data = d;
		data_type = d_type.string_type;
	}

	public ExcelCell(int r, int c, String ws, float f)
	{
		row = r;
		col = c;
		worksheet = ws;
		f_data = f;
		data_type = d_type.float_type;
	}

	public static String createCellGroup(Vector<ExcelCell> cells)
    {
    	String toRet = "";


    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory
				.newInstance();
    	try
    	{
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		Document group_doc = dBuilder.newDocument();
		Node temp_leaf = group_doc.createElement("CELLGROUP");
		for(ExcelCell cell : cells)
		{

			Node temp_cell = group_doc.createElement("CELL");
			Node temp_work = group_doc.createElement("WORKSHEET");
			temp_work.setTextContent(cell.worksheet);
			Node temp_row = group_doc.createElement("ROW");
			temp_row.setTextContent(("" + cell.row));
			Node temp_col = group_doc.createElement("COL");
			temp_col.setTextContent(""+ cell.col);
			Node temp_data = group_doc.createElement("DATA");
			if(cell.data_type == d_type.string_type)
			{
				((Element)temp_data).setAttribute("node_type", "string");
				temp_data.setTextContent(cell.s_data);
			}
			else if(cell.data_type == d_type.float_type)
			{
				((Element)temp_data).setAttribute("node_type", "float");
				temp_data.setTextContent("" + cell.f_data);
			}
			temp_cell.appendChild(temp_work);
			temp_cell.appendChild(temp_row);
			temp_cell.appendChild(temp_col);
			temp_cell.appendChild(temp_data);
			temp_leaf.appendChild(temp_cell);
		}

		group_doc.appendChild(temp_leaf);

		if (group_doc != null)
		{
			try
			{
			Transformer tf = TransformerFactory.newInstance()
					.newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(temp_leaf), new StreamResult(out));

			return out.toString();
			}
			catch(Exception e)
			{

			}
		}
    	}
    	catch(Exception e)
    	{

    	}

    	return toRet;
    }
}
