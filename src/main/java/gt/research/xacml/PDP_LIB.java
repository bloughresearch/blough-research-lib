package gt.research.xacml;
import gt.research.mht.MHTNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import com.sun.xacml.PDP;
import org.wso2.balana.*;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.AttributeAssignment;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.xacml3.Advice;

public class PDP_LIB {

	private static Balana balana;
	private PDP decisionPoint;
	private static Log logger = LogFactory.getLog(SampleAttributeFinderModule.class);


	private static void initBalana(){

        try{
            // using file based policy repository. so set the policy location as system property
            String policyLocation = (new File(".")).getCanonicalPath() + File.separator + "resources";
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
        } catch (IOException e) {
            System.err.println("Can not locate policy repository");
        }
        // create default instance of Balana
        balana = Balana.getInstance();
    }

	public void InitializePDP(String xacml_policy, String xml_mht_attributes)
	{
		if(balana == null)
		{
			initBalana();
		}
		if(balana != null)
		{
			decisionPoint = getPDPNewInstance(xacml_policy,xml_mht_attributes);
		}
	}

	/**
     * Returns a new PDP instance with new XACML policies
     *
     * @return a  PDP instance
     */
    private static PDP getPDPNewInstance(String xacml_policy, String attribute_cert){

        PDPConfig pdpConfig = balana.getPdpConfig();

        // registering new attribute finder. so default PDPConfig is needed to change
        AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
        List<AttributeFinderModule> finderModules = attributeFinder.getModules();


        //logger.info("PDP LIB WAS PASSED THIS TREE: \n" + attribute_cert);
        MHTNode relevant_cert = MHTNode.buildTreeFromString(attribute_cert);



        Vector<MHTNode> subCerts = new Vector<MHTNode>();
        subCerts.add(relevant_cert);

        MHTNode fullCert = new MHTNode(subCerts);
        fullCert.GenerateHashes();
        Set<MHTNode> certs = new HashSet<MHTNode>();
        certs.add(fullCert);

        finderModules.add(new SampleAttributeFinderModule(certs));
        attributeFinder.setModules(finderModules);

        PolicyFinder policyFinder = pdpConfig.getPolicyFinder();
        Set<PolicyFinderModule> policyFindModules = new HashSet<PolicyFinderModule>();
        Set<String> policySet = new HashSet<String>();
        policySet.add(xacml_policy);
        policyFindModules.add(new DistributedPolicyFinder(policySet));
        policyFinder.setModules(policyFindModules);

        return new PDP(new PDPConfig(attributeFinder, policyFinder, null, true));
    }

    public String handle_request(String xacml_request)
    {
    	String toRet = decisionPoint.evaluate(xacml_request);

/*    	try {
            ResponseCtx responseCtx = ResponseCtx.getInstance(getXacmlResponse(response));
            AbstractResult result  = responseCtx.getResults().iterator().next();
            if(AbstractResult.DECISION_PERMIT == result.getDecision()){
                System.out.println("\n" + userName + " is authorized to perform this purchase\n\n");
            } else {
                System.out.println("\n" + userName + " is NOT authorized to perform this purchase\n");
                List<Advice> advices = result.getAdvices();
                for(Advice advice : advices){
                    List<AttributeAssignment> assignments = advice.getAssignments();
                    for(AttributeAssignment assignment : assignments){
                        System.out.println("Advice :  " + assignment.getContent() +"\n\n");
                    }
                }
            }
        } catch (ParsingException e) {
            e.printStackTrace();
        }*/

    	return toRet;
    }

    public static String createXACMLRequest(String userName, String resource, int amount, int totalAmount){

        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">buy</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + userName +"</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"http://kmarket.com/category\">\n" +
                "<Attribute AttributeId=\"http://kmarket.com/id/amount\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + amount + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "<Attribute AttributeId=\"http://kmarket.com/id/totalAmount\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + totalAmount + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "</Request>";

    }

}
