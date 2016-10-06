package gt.research.xacml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.*;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.finder.*;
import org.wso2.balana.finder.impl.*;

public class DistributedPolicyFinder extends PolicyFinderModule{
	PolicyFinderModule p;


	private PolicyFinder finder = null;

    private Map<URI, AbstractPolicy> policies ;

    private  Set<String> policyStrings;

    private PolicyCombiningAlgorithm combiningAlg;

    /**
     * the logger we'll use for all messages
     */
	private static Log log = LogFactory.getLog(FileBasedPolicyFinderModule.class);

    public static final String POLICY_DIR_PROPERTY = "org.wso2.balana.PolicyDirectory";

	public DistributedPolicyFinder(Set<String> current_policies)
	{
		policyStrings = current_policies;
		policies = new HashMap<URI,AbstractPolicy>();
	}

	@Override
	public void init(PolicyFinder finder) {
		// TODO Auto-generated method stub
		this.finder = finder;
		PreparePolicies();
	}


	public void PreparePolicies()
	{
		for(String policyString : policyStrings)
		{
			AbstractPolicy policy = null;
	        InputStream stream = null;

	        try {
	            // create the factory
	            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            factory.setIgnoringComments(true);
	            factory.setNamespaceAware(true);
	            factory.setValidating(false);

	            // create a builder based on the factory & try to load the policy
	            DocumentBuilder db = factory.newDocumentBuilder();
	            stream = new ByteArrayInputStream(policyString.getBytes("UTF-8"));
	            Document doc = db.parse(stream);

	            // handle the policy, if it's a known type
	            Element root = doc.getDocumentElement();
	            String name = DOMHelper.getLocalName(root);

	            if (name.equals("Policy")) {
	                policy = Policy.getInstance(root);
	            } else if (name.equals("PolicySet")) {
	                policy = PolicySet.getInstance(root, finder);
	            }
	        } catch (Exception e) {
	            // just only logs
	            log.error("Fail to load policy : " + policyString , e);
	        } finally {
	            if(stream != null){
	                try {
	                    stream.close();
	                } catch (IOException e) {
	                    log.error("Error while closing input stream");
	                }
	            }
	        }

	        if(policy != null){
	            policies.put(policy.getId(), policy);
	        }
		}
	}


	@Override
    public PolicyFinderResult findPolicy(EvaluationCtx context) {

        ArrayList<AbstractPolicy> selectedPolicies = new ArrayList<AbstractPolicy>();
        Set<Map.Entry<URI, AbstractPolicy>> entrySet = policies.entrySet();

        // iterate through all the policies we currently have loaded
        for (Map.Entry<URI, AbstractPolicy> entry : entrySet) {

            AbstractPolicy policy = entry.getValue();
            MatchResult match = policy.match(context);
            int result = match.getResult();

            // if target matching was indeterminate, then return the error
            if (result == MatchResult.INDETERMINATE)
                return new PolicyFinderResult(match.getStatus());

            // see if the target matched
            if (result == MatchResult.MATCH) {

                if ((combiningAlg == null) && (selectedPolicies.size() > 0)) {
                    // we found a match before, so this is an error
                    ArrayList<String> code = new ArrayList<String>();
                    code.add(Status.STATUS_PROCESSING_ERROR);
                    Status status = new Status(code, "too many applicable "
                                               + "top-level policies");
                    return new PolicyFinderResult(status);
                }

                // this is the first match we've found, so remember it
                selectedPolicies.add(policy);
            }
        }

        // no errors happened during the search, so now take the right
        // action based on how many policies we found
        switch (selectedPolicies.size()) {
        case 0:
            if(log.isDebugEnabled()){
                log.debug("No matching XACML policy found");
            }
            return new PolicyFinderResult();
        case 1:
             return new PolicyFinderResult((selectedPolicies.get(0)));
        default:
            return new PolicyFinderResult(new PolicySet(null, combiningAlg, null, selectedPolicies));
        }
    }

    @Override
    public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints,
                                         PolicyMetaData parentMetaData) {

        AbstractPolicy policy = policies.get(idReference);
        if(policy != null){
            if (type == PolicyReference.POLICY_REFERENCE) {
                if (policy instanceof Policy){
                    return new PolicyFinderResult(policy);
                }
            } else {
                if (policy instanceof PolicySet){
                    return new PolicyFinderResult(policy);
                }
            }
        }

        // if there was an error loading the policy, return the error
        ArrayList<String> code = new ArrayList<String>();
        code.add(Status.STATUS_PROCESSING_ERROR);
        Status status = new Status(code,
                                   "couldn't load referenced policy");
        return new PolicyFinderResult(status);
    }

    @Override
    public boolean isIdReferenceSupported() {
        return true;
    }

    @Override
    public boolean isRequestSupported() {
        return true;
    }

}
