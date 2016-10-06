package gt.research.xacml;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.AttributeFinderModule;

import gt.research.mht.MHTNode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Sample attribute finder module. Actually this must be the point that calls to K-Market user store
 * and retrieve the customer attributes.  But here we are not talking any user store and values has
 * been hard corded in the source.
 */
public class SampleAttributeFinderModule extends AttributeFinderModule{

    private URI defaultSubjectId;

    private static Log logger = LogFactory.getLog(SampleAttributeFinderModule.class);

    private class AttributePair
    {
    	public String attributeID;
    	public String attributeValue;

    	public AttributePair(String id, String value)
    	{
    		attributeID = id;
    		attributeValue = value;
    	}

    	public AttributePair()
    	{
    		attributeID = "";
    		attributeValue = "";
    	}
    }

    private class AttributeList
    {
    	public String subject_name;
    	public Set<AttributePair> list = new HashSet<AttributePair>();

    	public AttributeList(String sub_name)
    	{
    		subject_name = sub_name;
    		list = new HashSet<AttributePair>();
    	}

    	public AttributeList()
    	{
    		subject_name = "";
    		list = new HashSet<AttributePair>();
    	}
    }

    Set<AttributeList> attributes = new HashSet<AttributeList>();

    private Set<AttributeList> getAttributesFromCert(MHTNode cert)
    {
    	Set<AttributeList> toRet = new HashSet<AttributeList>();

    	if(cert.VerifyHashes())
    	{
    		logger.info("Cert passed verification");
    		List<MHTNode> toCheck = new ArrayList<MHTNode>();
    		toCheck.add(cert);
    		//logger.info(cert.ExportAsXML());
    		while(toCheck.size() > 0)
    		{
    			MHTNode checking = toCheck.get(0);
    			String subject_name = getSubjectName(toCheck.get(0));
    			if(subject_name.length() > 0)
    			{
    				//logger.info("Subject tracked:   " + subject_name);
    				AttributeList listToAdd = new AttributeList(subject_name);
    				listToAdd.list.addAll(getEmbeddedAttributes(checking));
    				toRet.add(listToAdd);
    			}
    			else
    			{
    				toCheck.addAll(checking.getChildren());
    			}
    			toCheck.remove(0);
    		}
    	}
    	else
    	{
    		logger.info("Cert failed verification");
    	}


    	return toRet;
    }

    private String getSubjectName(MHTNode node)
    {
    	String nodeData = node.getData();
    	if(nodeData.length() > 0)
    	{
    		String subjectMatch = "subjectValue=";
    		int subjectAt = nodeData.indexOf(subjectMatch);
    		//logger.info("Expected subjectValue   got   " + nodeData);
    		if(subjectAt >= 0)
    		{

    			String toRet = nodeData.substring(subjectAt + subjectMatch.length(),nodeData.indexOf('\n',subjectAt));

    			//logger.info("Sample attribute finder found subject of: " + toRet);
    			return toRet;
    		}
    	}
    	//logger.info("Searching for subject name and found: " + nodeData + " <- if that is empty then node has no data....");
    	return "";
    }

    private Set<AttributePair> getEmbeddedAttributes(MHTNode node)
    {
    	//logger.info("Sample Attribute finder now looking for attributes in the tree....");
    	List<String> treeData = node.getIncludedData();
    	for(String data : treeData)
    	{

    	}
    	Set<AttributePair> atrbts = new HashSet<AttributePair>();
    	for(MHTNode child : node.getChildren())
    	{
    		atrbts.addAll(getEmbeddedAttributes(child));
    	}
    	if(node.getData().length() > 0)
    	{
    		String IDMatch = "attributeID=";
    		String ValueMatch = "attributeValue=";
    		String nodeData = node.getData();
    		int idAt = nodeData.indexOf(IDMatch);
    		int valueAt = nodeData.indexOf(ValueMatch);
    		if(idAt >= 0 && valueAt >= 0)
    		{
    			String tempID = nodeData.substring(idAt + IDMatch.length(),nodeData.indexOf('\n',idAt));
    			String tempValue = nodeData.substring(valueAt + ValueMatch.length(), nodeData.indexOf('\n',valueAt));
    			AttributePair toAdd = new AttributePair(tempID,tempValue);
    			//logger.info("Attribute tracked:    " + tempID + "  =  " + tempValue);
    			atrbts.add(toAdd);
    		}
    	}

    	return atrbts;
    }

    public SampleAttributeFinderModule(Set<MHTNode> attributeCerts) {

    	//logger.info("Testing that the logger works from inside SampleAttributeFinder...");

        try {

        	for(MHTNode single_cert : attributeCerts)
        	{
        		attributes.addAll(getAttributesFromCert(single_cert));
        	}

        	/*AttributeList bobs_attributes = new AttributeList("bob");
        	AttributePair bobs_group = new AttributePair("http://kmarket.com/id/role","blue");
        	bobs_attributes.list.add(bobs_group);

        	AttributePair alices_group = new AttributePair("http://kmarket.com/id/role","silver");
        	AttributeList alices_attributes = new AttributeList("alice");
        	alices_attributes.list.add(alices_group);

        	AttributePair peters_group = new AttributePair("http://kmarket.com/id/role","gold");
        	AttributeList peters_attributes = new AttributeList("peter");
        	peters_attributes.list.add(peters_group);


        	attributes.add(bobs_attributes);
        	attributes.add(alices_attributes);
        	attributes.add(peters_attributes);*/



            defaultSubjectId = new URI("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        } catch (URISyntaxException e) {
           //ignore
        }

    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<String>();
        categories.add("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        return categories;
    }

    @Override
    public Set getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        for(AttributeList list : attributes)
        {
        	for(AttributePair single_attribute: list.list)
        	{
        		if(!ids.contains(single_attribute.attributeID))
        		{
        			ids.add(single_attribute.attributeID);
        		}
        	}
        }
        return ids;
    }

    @Override
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
                                                            URI category, EvaluationCtx context) {
        //String roleName = null;
        List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

        EvaluationResult result = context.getAttribute(attributeType, defaultSubjectId, issuer, category);
        if(result != null && result.getAttributeValue() != null && result.getAttributeValue().isBag()){
            BagAttribute bagAttribute = (BagAttribute) result.getAttributeValue();
            if(bagAttribute.size() > 0){
                String userName = ((AttributeValue) bagAttribute.iterator().next()).encode();
                for(AttributeList at_list : attributes)
                {
                	if(at_list.subject_name.equals(userName))
                	{
                		//logger.info("Getting attribute " + attributeId + " for " + userName);
                		for(AttributePair single_attribute : at_list.list)
                		{
                			String attributeId_ToString = attributeId.toString();
                			if (((String)(single_attribute.attributeID)).equals(attributeId_ToString)) {
                				//logger.info(userName + " has attribute of " + single_attribute.attributeValue);
                            	attributeValues.add(new StringAttribute(single_attribute.attributeValue));
                        	}
                		}
                	}
                }
            }
        }

        return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
    }

    @Override
    public boolean isDesignatorSupported() {
        return true;
    }

    private String findRole(String userName){

        if(userName.equals("bob")){
            return "blue";
        } else if(userName.equals("alice")){
            return "silver";
        } else if(userName.equals("peter")){
            return "gold";
        }

        return null;
    }
}
