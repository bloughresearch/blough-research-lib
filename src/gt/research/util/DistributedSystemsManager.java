package gt.research.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.ResponseCtx;
import org.xml.sax.InputSource;

import gt.research.mht.MHTNode;
import gt.research.xacml.PDP_LIB;
import gt.research.xacml.test.XacmlTest;

public class DistributedSystemsManager {
	
	public static boolean alreadyRun;
	public static int count;
	private String username;
	private PDP_LIB pdp;
	private MHTNode node;
	private MHTNode original_node;
	private boolean hashesVerified;

	private int numCellGroups = 0;
	private LinkedList<DataEntity> worksheet;
	private LinkedList<DataEntity> attributes;
	private LinkedList<DataEntity> averages;

	private String authorizedTags;
	private String unauthorizedTags;
	public static String policyString;
	public static String certXML;

	private Vector<MHTNode> nodeBuffers;
	private int maxRow = 0;

	private boolean decrypted;
	private File dataFile;
	private File policyFile;
	private File certificateFile;

	private static String WORKSHEET_XML = "WORKSHEET";
	private static final String ROW_XML = "ROW";
	private static final String COL_XML = "COL";
	private static final String DATA_XML = "DATA";
	private static final String TAG_XML = "TAG";

	private static final String TAG = "DISTRIBUTED_SYSTEMS_MANAGER";


	public DistributedSystemsManager(Properties prop) {
		this.username = prop.getProperty("username");
		pdp = new PDP_LIB();
		nodeBuffers = new Vector<MHTNode>();
		worksheet = new LinkedList<DataEntity>();
		attributes = new LinkedList<DataEntity>();
		averages = new LinkedList<DataEntity>();

        try {
			dataFile = new File(new File(".").getCanonicalPath() + prop.getProperty("dataFileDir"));
            policyFile = new File(new File(".").getCanonicalPath() + prop.getProperty("policyFileDir"));
            certificateFile = new File(new File(".").getCanonicalPath() + prop.getProperty("certificateFileDir"));
        } catch (IOException e){
            e.printStackTrace();
        }
	}

	public void parseDataFile() {
        // Build the node from our data file
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = dbfac.newDocumentBuilder();
			InputStream stream = new FileInputStream(dataFile);
			InputSource inputsource = new InputSource(stream);
			Document doc = docBuilder.parse(inputsource);

			node = new MHTNode(doc.getDocumentElement());
		} catch (Exception e) {
			e.printStackTrace();
		}

		clear();

		original_node = node; // Why is this here?

		InputStream policyFileInputStream;
		InputStream certificateFileInputStream;

		try {

			policyFileInputStream = new FileInputStream(policyFile);
			certificateFileInputStream = new FileInputStream(certificateFile);

			policyString = IOUtils.toString(policyFileInputStream, "UTF-8");
			certXML = IOUtils.toString(certificateFileInputStream, "UTF-8");

			PDPinit(policyString, certXML);

            // Verify that the data has not been tempered with
			hashesVerified = verifyHashes();

            // Load data into some data structures
            // Traverse down the hash tree to get to the data nodes
			findChildren(node);

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}


	public void PDPinit(String policyString, String certXML) {
		pdp.InitializePDP(policyString, certXML);
	}

	private boolean verifyHashes() {
		return node != null && node.VerifyHashes();
	}

	private void findChildren(MHTNode currNode) {

        // Keep recursively traversing down the node if there are children
		if ((currNode.getChildren().size() != 0)) {
			findChildren(currNode.getChildren().elementAt(0));
			findChildren(currNode.getChildren().elementAt(1));
		}

        // Extract data out of the leaf nodes
		try {
			byte[] nodeByteArray = currNode.getData().getBytes("UTF-8");
			String rawString = new String(nodeByteArray, Charset.forName("UTF-8"));

			if (!rawString.isEmpty()) {
			    System.out.println("Found: " + rawString);
                parseCellGroup(rawString);
            }

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void parseCellGroup(String rawString) {

		DocumentBuilder db;

		try {
		    // Parse the string as a document
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(rawString));
			Document doc = db.parse(is);

            // Check if the document's root node is of type "CellGroup"
            if ("CELLGROUP".equals(doc.getDocumentElement().getNodeName())) {
                // If there are child nodes from CellGroup, keep traversing the document
                if (doc.getDocumentElement().hasChildNodes()) {
                    numCellGroups++;
                    NodeList list = doc.getElementsByTagName("CELL");
                    for (int j = 0; j < list.getLength(); j++) {
                        Element element = (Element) list.item(j);
                        parseCell(element);
                    }
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseCell(Element element) {

	    // Get row,col coords
        int row = Integer.parseInt(element.getElementsByTagName(ROW_XML)
                .item(0)
                .getChildNodes()
                .item(0)
                .getNodeValue());
        int col = Integer.parseInt(element.getElementsByTagName(COL_XML)
                .item(0)
                .getChildNodes()
                .item(0)
                .getNodeValue());

        // Keep tracking the maximum row
        maxRow = Math.max(row, maxRow);

        // Which worksheet this data entity is in
        String wrkshtNum = element.getElementsByTagName(WORKSHEET_XML).item(0).getChildNodes().item(0).getNodeValue();
        String type;
        String value;

        try {
            type = "float";
            value = "" + Float.parseFloat(element.getElementsByTagName(DATA_XML).item(0).getChildNodes().item(0).getNodeValue());
        } catch (Exception f) {
            type = "string";
            value = element.getElementsByTagName(DATA_XML).item(0).getChildNodes().item(0).getNodeValue();
        }

        DataEntity<String> dataEntity = new DataEntity(type, value, wrkshtNum, row, col, numCellGroups - 1);

        boolean hasAverageTag = false;
        for (int m = 0; m < element.getElementsByTagName(TAG_XML).getLength(); m++) {
            String newTag = element.getElementsByTagName(TAG_XML).item(m).getChildNodes().item(0).getNodeValue();
            if(newTag.equals("Average")){
                hasAverageTag = true;
            }
            dataEntity.tags.add(newTag);
            if(m == 0){
                dataEntity.isVisible = requestForAccess(newTag);
            }
            else {
                dataEntity.isVisible = dataEntity.isVisible & requestForAccess(newTag);
            }
        }

        if (!hasAverageTag) {
            worksheet.add(dataEntity);
            if (worksheet.getLast().row == 0) {
                dataEntity.isVisible = requestForAccess(dataEntity.value);
                attributes.add(dataEntity);
            }
        }

    }

	public boolean requestForAccess(String tagname) {
		if (username == null) {
			//System.out.println("Could not determine username, no permission given.");
		}
		String request = XacmlTest.CreateXACMLRequestForTagAccess(username, tagname, "buy");
		String response = pdp.handle_request(request);
		try {
			ResponseCtx responseCtx = ResponseCtx.getInstance(XacmlTest.getXacmlResponse(response));
			AbstractResult result = responseCtx.getResults().iterator().next();
			return AbstractResult.DECISION_PERMIT == result.getDecision();
		} catch (ParsingException e) {
			e.printStackTrace();
		}
		return false;
	}

	public MHTNode createParentNode(Vector<MHTNode> child_nodes) {
		int index = 0;
		Vector<MHTNode> parent_nodes = new Vector<MHTNode>();
		while (index < child_nodes.size()-1){
			Vector<MHTNode> two_child_nodes = new Vector<MHTNode>();
			two_child_nodes.add(child_nodes.get(index));
			two_child_nodes.add(child_nodes.get(index+1));
			MHTNode new_mht_node = new MHTNode(two_child_nodes);
			parent_nodes.add(new_mht_node);
			index += 2;
		}
		if (child_nodes.size() % 2 != 0) {
			MHTNode new_mht_node = child_nodes.get(child_nodes.size()-1);
			parent_nodes.add(new_mht_node);
		}
		if (parent_nodes.size() == 1) {
			return parent_nodes.get(0);
		}
		return createParentNode(parent_nodes);
	}

	public void saveChanges(boolean logout) {
		if (nodeBuffers.size() == 0) {
		    /*
			if (logout) {
				fileManager.encrypt();
			}
			*/
			System.out.println("Empty node buffers, unable to save.");
		} else {
			MHTNode session_node = createParentNode(nodeBuffers);
			Vector<MHTNode> trees_to_merge = new Vector<MHTNode>();
			trees_to_merge.add(session_node);
			trees_to_merge.add(original_node);
			node = new MHTNode(trees_to_merge);
			node.GenerateHashes();


            //TODO: Implement signing
			//node = signRoot(node, userName);


			FileManager.updateFile(dataFile, node.ExportAsXML().getBytes());
			FileManager.updateFile(policyFile,policyString.getBytes());


			if (logout) {
				//fileManager.encrypt();
			} else {
				parseDataFile();
			}
		}
	}

	public void export(File exportTo) { 
		FileManager.updateFile(exportTo, original_node.ExportAsXML().getBytes());
	}

	public void clear() {
		attributes.clear();
		worksheet.clear();
		nodeBuffers.clear();
		authorizedTags="";
		unauthorizedTags="";
		hashesVerified = false;
		maxRow = 0;
	}

	public void showStats(){
		System.out.println(dataFile);
		System.out.println(policyFile);
		System.out.println(certificateFile);

		System.out.println("Hashes verifed: " + hashesVerified);
		System.out.println("Attributes");
		for (int i = 0; i < attributes.size(); i++) {
			System.out.print(attributes.get(i).value + ", ");
		}
		for (int i = 0; i < worksheet.size(); i++) {
				System.out.print("(" + worksheet.get(i).row + " , " + worksheet.get(i).col + "), val: " + worksheet.get(i).value  + ", tags: ");
				for (int j = 0; j < worksheet.get(i).tags.size(); j++) {
					System.out.print(worksheet.get(i).tags.get(j) + ", ");
				}
				System.out.print(", visible: " + worksheet.get(i).isVisible + "\n");
		}
		System.out.println("Averages");
		for (int i = 0; i < averages.size(); i++) {
			System.out.println(averages.get(i).value + ", " );
		}
	}


	public String exportAsCSV() {
		File file = new File("sdcard" + File.separator + "Download" + File.separator + "." + username + ".csv");
		String content = "";
		int tags = 0;
		for (int i = 0; i < worksheet.size(); i++) {
			if (worksheet.get(i).isVisible || (i >= 0 && i <= 4)) {
				content += worksheet.get(i).value + ",";
			} else {
				content += "DELETED,";
			}
			if (tags == 4) {
				content += "\n";
			}
			tags++;
			if (tags >= 5) {
				try {
					tags = 0;
					FileUtils.writeStringToFile(file, content, true);
					content = "";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return file.getAbsolutePath();
	}

}
