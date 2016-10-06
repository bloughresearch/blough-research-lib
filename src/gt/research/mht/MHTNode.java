package gt.research.mht;

import gt.research.crypto.Crypto_Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MHTNode {

	public static void main(String[] args)
	{

		byte[] check_hash = Crypto_Utils.hashString("Test String this should work in Java and C#");

		try
		{
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.parse("C:/Users/ECEUser/Documents/PhD/XACML_Workspace/samplemht.nmht");
			Node main_node = doc.getDocumentElement();
			MHTNode toCheck = new MHTNode(main_node);
			if(toCheck.VerifyHashes())
			{
				System.out.println("Hashes verified");
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public static final int NUM_CHILDREN = 2;
	private static final String NODE_XML = "MHTNODE";
    public static final String DATA_XML = "DATA";
    public static final String HASH_XML = "HASH";
    public static final String CERT_XML = "CERTIFICATE";

	Vector<MHTNode> children;
	X509Certificate source;
	String data;
	byte[] hash;

	public byte[] getNodeHash()
    {
		return hash;
    }

	public X509Certificate getDataSource()
	{
		if(source != null)
		{
			return source;
		}
		return null;
	}

	public String getData()
	{
		if(data != null)
		{
			return data;
		}
		return "";
	}

	public Vector<MHTNode> getChildren()
	{
		return children;
	}

	public List<String> getIncludedData()
	{
		List<String> toRet = new ArrayList<String>();

		if(data != null && data != "")
		{
			toRet.add(data);
		}

		for(MHTNode child : children)
		{
			toRet.addAll(child.getIncludedData());
		}

		return toRet;
	}

    public MHTNode(Vector<String> leaves, String depends_on)
    {
        children = new Vector<MHTNode>();
        if(leaves.size() <= 0)
        {
            return;
        }

        if(depends_on != null && depends_on != "")
        {
            data = depends_on;
        }

        if (leaves.size() == 1)
        {
            data = leaves.elementAt(0);
            return;
        }
        else
        {
            for(int x = 0; x < NUM_CHILDREN; x++)
            {
            	int leaves_start = x*leaves.size()/NUM_CHILDREN;
            	int leaves_end = (x+1)*leaves.size()/NUM_CHILDREN;
                MHTNode toAdd = new MHTNode(new Vector<String>(leaves.subList(leaves_start,leaves_end)),"");
                children.add(toAdd);
            }
        }


    }

    public MHTNode(Vector<MHTNode> leaves)
    {
    	children = new Vector<MHTNode>();
    	if(leaves.size() < NUM_CHILDREN)
    	{
    		children = leaves;
    	}
    	else
    	{
    		if(leaves.size()/NUM_CHILDREN < NUM_CHILDREN)
    		{
    			int current_leaf = 0;
    			int nodes_remaining = NUM_CHILDREN;
    			for(int x = 0; x < NUM_CHILDREN; x++)
    			{
    				if(leaves.size() - current_leaf == nodes_remaining)
    				{
    					children.add(leaves.get(x));
    					current_leaf++;
    				}
    				else if((nodes_remaining - 1) > (leaves.size() - current_leaf - NUM_CHILDREN))
    				{
    					int numToAdd = (leaves.size() - current_leaf - (nodes_remaining - 1) );
    					children.add(new MHTNode(new Vector<MHTNode>(leaves.subList(current_leaf, current_leaf+numToAdd))));
    					current_leaf += numToAdd;
    				}
    				else
    				{
    					children.add(new MHTNode(new Vector<MHTNode>(leaves.subList(current_leaf,current_leaf+NUM_CHILDREN))));
    					current_leaf += NUM_CHILDREN;
    				}
    				nodes_remaining--;

    			}
    		}
    		else
    		{
    			for(int x = 0; x < NUM_CHILDREN; x++)
                {
                    MHTNode toAdd = new MHTNode(new Vector<MHTNode>(leaves.subList(x*leaves.size()/NUM_CHILDREN,(x+1)*leaves.size()/NUM_CHILDREN -1)));
                    children.add(toAdd);
                }
    		}
    	}

    }

    private static byte[] byteArrayFromHexString(String s)
    {
    	int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public MHTNode(Node node)
    {
    	children = new Vector<MHTNode>();
    	NodeList xml_children = node.getChildNodes();
    	for(int child_num = 0; child_num < xml_children.getLength(); child_num++ )
    	{
    		Node current_child = xml_children.item(child_num);
    		switch(current_child.getNodeName())
    		{
    		case(NODE_XML):
    			children.add(new MHTNode(current_child));
    			break;
    		case(DATA_XML):
    			data = current_child.getTextContent();
    			break;
    		case(HASH_XML):
    			hash = byteArrayFromHexString(current_child.getTextContent().toUpperCase());
    			break;
    		case(CERT_XML):
    			try
				{
    				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    				InputStream iS = new ByteArrayInputStream(byteArrayFromHexString(current_child.getTextContent()));

    				source = (X509Certificate)certFactory.generateCertificate(iS);
    			}
    			catch(Exception e)
    			{
    				System.out.println("Unable to decode certificate");
    				System.out.println(e.getMessage());
    			}
    			break;
    		default:

    		}
    	}

    }

    public static MHTNode buildTreeFromString(String xml_version)
    {
    	try
    	{
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder builder = dbFactory.newDocumentBuilder();
    		InputStream stream = new ByteArrayInputStream(xml_version.getBytes("UTF-8"));
    		Document toRet = builder.parse(stream);
    		return new MHTNode(toRet.getDocumentElement());
    	}
    	catch(Exception e)
    	{
    		return null;
    	}
    }

    public String ExportAsXML()
    {
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document toRet = dBuilder.newDocument();
			toRet.appendChild(ExportXML(toRet));
			if (toRet != null)
			{
				Transformer tf = TransformerFactory.newInstance()
						.newTransformer();
				tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				tf.setOutputProperty(OutputKeys.INDENT, "yes");
				Writer out = new StringWriter();
				tf.transform(new DOMSource(toRet), new StreamResult(out));

				return out.toString();
			}
		}
		catch (Exception e)
		{

		}

        return "";




    }

    private Node ExportXML(Document docToRet)
    {
        try
        {
            Node toRet = docToRet.createElement(NODE_XML);
            if (source != null)
            {
                Node source_node = docToRet.createElement(CERT_XML);
                source_node.setTextContent(bytesToHex(source.getEncoded()));
                //source_node.InnerText = source.GetRawCertDataString();// Constants.ByteArrayToString(source.Export(X509ContentType.Cert));
                toRet.appendChild(source_node);
            }
            if (data != null)
            {
                Node data_node = docToRet.createElement(DATA_XML);
                data_node.setTextContent(data);
                toRet.appendChild(data_node);
            }

            if (hash != null)
            {
                Node hash_node = docToRet.createElement(HASH_XML);
                hash_node.setTextContent(bytesToHex(hash));
                toRet.appendChild(hash_node);
            }
            else
            {
                //ERROR!!!
            }

            for(MHTNode child : children)
            {
                try
                {
                    toRet.appendChild(child.ExportXML(docToRet));
                }
                catch (Exception e)
                {
                    int y = 0;
                    y = 5 + 3;
                }
            }

            return toRet;
        }
        catch (Exception e)
        {
            return null;
        }


    }


    public void GenerateHashes()
    {
    	if(source != null)
    	{
    		return;
    	}

        hash = new byte[0];
        if (children != null)
        {
            for(MHTNode child : children)
            {
                child.GenerateHashes();
                hash = Crypto_Utils.concat(hash, child.hash);
            }
        }

        if (hash.length <= 0)
        {
            if (data != null)
            {
                hash = Crypto_Utils.hashString(data);
            }
            else
            {
                //ERROR
                hash = "ERRORERRORERRORERRORERRORshouldn'tgetthisblahblahblah".getBytes();
            }
            return;
        }

        if (data != null)
        {
            hash = Crypto_Utils.concat(hash, data.getBytes());
        }
        hash = Crypto_Utils.hashByteArray(hash);
    }

    public boolean SignRoot(X509Certificate identity, byte[] signed_hash)
    {
        if (VerifyHashes())
        {
            boolean valid_signature = Crypto_Utils.VerifyHash(hash, signed_hash, identity.getPublicKey());
            if (valid_signature)
            {
                hash = signed_hash;
                source = identity;
                return true;
            }
        }
        return false;
    }

    public boolean VerifyHashes()
    {
        byte[] temp_hash = new byte[0];
        if (children != null)
        {
            for(MHTNode child : children)
            {
                if (!child.VerifyHashes())
                {
                    return false;
                }
                //child.GenerateHashes();
                temp_hash = Crypto_Utils.concat(temp_hash, child.hash);
            }
        }

        if (temp_hash.length <= 0)
        {
            if (data != null)
            {
                temp_hash = Crypto_Utils.hashString(data);
                return Crypto_Utils.hashesEqual(hash, temp_hash);
            }
            else
            {
                return true;
            }
        }
        else
        {

            if (data != null)
            {
                temp_hash = Crypto_Utils.concat(temp_hash, data.getBytes());
            }

        }

        temp_hash = Crypto_Utils.hashByteArray(temp_hash);

        if (source != null)
        {
            return Crypto_Utils.VerifyHash(temp_hash, hash, source.getPublicKey());
        }
        else
        {
            return Crypto_Utils.hashesEqual(hash, temp_hash);
        }
    }

}
