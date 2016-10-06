package gt.research.xacml.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
//import java.lang.management.ManagementFactory;
//import java.lang.management.ThreadMXBean;
// import java.nio.charset.Charset;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;

public class XacmlTest {



	private static Balana balana;

    private static Map<String,String> priceMap = new HashMap<String, String>();

    private static Map<String,String> idMap = new HashMap<String, String>();

    private static String products = "[1] Food\t[2] Drink\t[3] Fruit\t[4] Liquor\t [5] Medicine";



    private static boolean ResponsePermit(String xacml_response)
    {
    	try {
            ResponseCtx responseCtx = ResponseCtx.getInstance(getXacmlResponse(xacml_response));
            AbstractResult result  = responseCtx.getResults().iterator().next();
            if(AbstractResult.DECISION_PERMIT == result.getDecision()){
            	return true;
                //System.out.println("\n" + "sampleUser" + " is authorized to perform this purchase\n\n");
            } else {
            	return false;
                //System.out.println("\n" + "sampleUser" + " is NOT authorized to perform this purchase\n");
                //List<Advice> advices = result.getAdvices();
                //for(Advice advice : advices){
                    //List<AttributeAssignment> assignments = advice.getAssignments();
                    //for(AttributeAssignment assignment : assignments){
                        //System.out.println("Advice :  " + assignment.getContent() +"\n\n");
                    //}
                //}
            }
        } catch (ParsingException e) {
            e.printStackTrace();
            return false;
        }
    }



/*
    public static void main(String[] args){

    	ThreadMXBean watch = ManagementFactory.getThreadMXBean();
    	long threadID = Thread.currentThread().getId();

    	Vector<String> bobs_atts = new Vector<String>();
        bobs_atts.add("attributeID=urn:fake_urn\nattributeValue=editor_user_0\n");
        bobs_atts.add("attributeID=urn:fake_urn\nattributeValue=lead\n");
        //bobs_atts.add("subjectValue=bob\nattributeID=http://kmarket.com/id/role\nattributeValue=blue\n");
        //bobs_atts.add("subjectValue=alice\nattributeID=http://kmarket.com/id/role\nattributeValue=silver\n");

        MHTNode cert = new MHTNode(bobs_atts,"subjectValue=user\n");
        cert.GenerateHashes();

        String xacml_response = "";
        try
        {


        	String results = "";
        	results += "Num Resources, Time(ns)\n";


        	int num_tests = 110;


        	for(int z = 0; z < 10; z++)
        	{
        		int num_resources = 100*(z+1);
        		System.out.println("Starting round " + num_resources + "\n");

        		String filePath = "C:/Users/ECEUser/Documents/PhD/ExcelPolicies/Generated/Test_" + num_resources + ".xacml";

            	String policy = new String(Files.readAllBytes(Paths.get(filePath)));

            	PDP_LIB testerPdp = new PDP_LIB();
            	testerPdp.InitializePDP(policy,cert.ExportAsXML());
            	results += num_resources + ",";

        	for(int x = 0; x < num_tests; x++)
        	{
        		//NANOSECONDS
            	long startTime = watch.getCurrentThreadCpuTime();
            	String responseList = "";
            	for(int y = 0; y < num_resources; y++)
            	{

            		String xacml_request = CreateXACMLRequestForTagAccess("user","resource_" + y,"edit");

            		xacml_response = testerPdp.handle_request(xacml_request);

                	if(ResponsePermit(xacml_response))
                	{
                		responseList += '1';
                	}
                	else
                	{
                		responseList += '0';
                	}

                	xacml_request = CreateXACMLRequestForTagAccess("user","resource_" + y,"view");

            		xacml_response = testerPdp.handle_request(xacml_request);

                	if(ResponsePermit(xacml_response))
                	{
                		responseList += '1';
                	}
                	else
                	{
                		responseList += '0';
                	}

                	xacml_request = CreateXACMLRequestForTagAccess("user","resource_" + y,"read");

            		xacml_response = testerPdp.handle_request(xacml_request);

                	if(ResponsePermit(xacml_response))
                	{
                		responseList += '1';
                	}
                	else
                	{
                		responseList += '0';
                	}


            	}



            	//NANOSECONDS
            	long endTime = watch.getCurrentThreadCpuTime();
            	results += (endTime - startTime) + ",";
            	//System.out.println((endTime - startTime) + " nanoseconds...");
        	}
        	results += "\n";

        	}

        	PrintWriter out = new PrintWriter("C:/Users/ECEUser/Documents/PhD/FlexibleFrameworkResults/Policies/Results.csv");
        	out.println(results);
        	out.close();




        }
        catch(Exception e)
        {
        	return;
        }



    	if(1 == 1)
    	{
    		return;
    	}


    	Vector<ExcelCell> cell_list = new Vector<ExcelCell>();
    	cell_list.add(new ExcelCell(0,0,"","Test Sheet Header"));
    	cell_list.add(new ExcelCell(0,1,"",3.14f));
    	cell_list.add(new ExcelCell(0,2,"",777.0f));
    	cell_list.add(new ExcelCell(0,3,"",0.0f));
    	String temp_check = ExcelCell.createCellGroup(cell_list);

    	cell_list = new Vector<ExcelCell>();
    	cell_list.add(new ExcelCell(4,5,"","Patient Bob"));
    	cell_list.add(new ExcelCell(4,6,"",10.0f));
    	cell_list.add(new ExcelCell(4,7,"",20.0f));
    	cell_list.add(new ExcelCell(4,8,"",30.0f));
    	cell_list.add(new ExcelCell(4,9,"",40.0f));
    	String leaf2 = ExcelCell.createCellGroup(cell_list);

    	cell_list = new Vector<ExcelCell>();
    	cell_list.add(new ExcelCell(4,5,"Worksheet2","Patient Alice"));
    	cell_list.add(new ExcelCell(4,6,"Worksheet2",30.0f));
    	cell_list.add(new ExcelCell(4,7,"Worksheet2",20.0f));
    	cell_list.add(new ExcelCell(4,8,"Worksheet2",60.0f));
    	cell_list.add(new ExcelCell(4,9,"Worksheet2",10.0f));
    	String leaf3 = ExcelCell.createCellGroup(cell_list);

    	cell_list = new Vector<ExcelCell>();
    	cell_list.add(new ExcelCell(0,0,"Worksheet2","Test Sheet 2"));
    	cell_list.add(new ExcelCell(1,0,"Worksheet2",1.3f));
    	String leaf4 = ExcelCell.createCellGroup(cell_list);

    	Vector<String> cells_to_save = new Vector<String>();
    	cells_to_save.add(temp_check);
    	cells_to_save.add(leaf2);
    	cells_to_save.add(leaf3);
    	cells_to_save.add(leaf4);
    	MHTNode toPrint = new MHTNode(cells_to_save,"");
    	toPrint.GenerateHashes();
    	String stf = toPrint.ExportAsXML();



        Console console;
        String userName = null;
        String productName = null;
        int numberOfProducts = 1;
        int totalAmount = 0;


        printDescription();

        initData();

        initBalana();

        System.out.println("\nYou can select one of following item for your shopping chart : \n");

        System.out.println(products);
        /*console = System.console();

        if (console != null){
            userName = console.readLine("Enter User name : ");
            if(userName == null || userName.trim().length() < 1 ){
                System.err.println("\nUser name can not be empty\n");
                return;
            }

            String productId = console.readLine("Enter Product Id : ");
            if(productId == null || productId.trim().length() < 1 ){
                System.err.println("\nProduct Id can not be empty\n");
                return;
            } else {
                productName = idMap.get(productId);
                if(productName == null){
                    System.err.println("\nEnter valid product Id\n");
                    return;
                }
            }

            String productAmount = console.readLine("Enter No of Products : ");
            if(productAmount == null || productAmount.trim().length() < 1 ){
                numberOfProducts = 1;
            } else {
                numberOfProducts = Integer.parseInt(productAmount);
            }
        }*/

        //totalAmount = calculateTotal(productName, numberOfProducts);
        //System.err.println("\nTotal Amount is  : " + totalAmount + "\n");


        //String request = createXACMLRequest(userName, productName, numberOfProducts, totalAmount);
		/*
        userName = "user";

        String request =  CreateXACMLRequestForTagAccess("user","Health","buy");
        PDP pdp = getPDPNewInstance();

        System.out.println("\n======================== XACML Request ====================");
        System.out.println(request);
        System.out.println("===========================================================");

        String response = pdp.evaluate(request);

        System.out.println("\n======================== XACML Response ===================");
        System.out.println(response);
        System.out.println("===========================================================");

        try {
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
        }

    }*/


    private static void initData(){

        idMap.put("1" , "Food");
        idMap.put("2" , "Drink");
        idMap.put("3" , "Fruit");
        idMap.put("4" , "Liquor");
        idMap.put("5" , "Medicine");

        priceMap.put("Food" , "20");
        priceMap.put("Drink" , "5");
        priceMap.put("Fruit" , "15");
        priceMap.put("Liquor" , "80");
        priceMap.put("Medicine" , "50");
    }

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

    /**
     * Returns a new PDP instance with new XACML policies
     *
     * @return a  PDP instance
     */
	 /*
    private static PDP getPDPNewInstance(){

        PDPConfig pdpConfig = balana.getPdpConfig();

        // registering new attribute finder. so default PDPConfig is needed to change
        AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
        List<AttributeFinderModule> finderModules = attributeFinder.getModules();

        Vector<String> bobs_atts = new Vector<String>();
        bobs_atts.add("subjectValue=user\nattributeID=urn:gtTest:user-name\nattributeValue=doctor\n");
        bobs_atts.add("subjectValue=user\nattributeID=urn:gtTest:user-role\nattributeValue=lead\n");
        //bobs_atts.add("subjectValue=bob\nattributeID=http://kmarket.com/id/role\nattributeValue=blue\n");
        //bobs_atts.add("subjectValue=alice\nattributeID=http://kmarket.com/id/role\nattributeValue=silver\n");

        MHTNode bobsCert = new MHTNode(bobs_atts,"");

        Vector<MHTNode> subCerts = new Vector<MHTNode>();
        subCerts.add(bobsCert);

        MHTNode fullCert = new MHTNode(subCerts);
        fullCert.GenerateHashes();
        Set<MHTNode> certs = new HashSet<MHTNode>();
        certs.add(fullCert);

        finderModules.add(new SampleAttributeFinderModule(certs));
        attributeFinder.setModules(finderModules);

        PolicyFinder policyFinder = pdpConfig.getPolicyFinder();
        Set<PolicyFinderModule> policyFindModules = new HashSet<PolicyFinderModule>();
        Set<String> policySet = new HashSet<String>();
        String check = getPolicy(0);
        policySet.add(check);
        policyFindModules.add(new DistributedPolicyFinder(policySet));
        policyFinder.setModules(policyFindModules);

        return new PDP(new PDPConfig(attributeFinder, policyFinder, null, true));
    }*/

    public static int calculateTotal(String productName, int amount){

        String priceString = priceMap.get(productName);
        return Integer.parseInt(priceString)*amount;

    }

    /**
     * Creates DOM representation of the XACML request
     *
     * @param response  XACML request as a String object
     * @return XACML request as a DOM element
     */
    public static Element getXacmlResponse(String response) {

        ByteArrayInputStream inputStream;
        DocumentBuilderFactory dbf;
        Document doc;

        inputStream = new ByteArrayInputStream(response.getBytes());
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            doc = dbf.newDocumentBuilder().parse(inputStream);
        } catch (Exception e) {
            System.err.println("DOM of request element can not be created from String");
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
               System.err.println("Error in closing input stream of XACML response");
            }
        }
        return doc.getDocumentElement();
    }

    public static void printDescription(){

        System.out.println("\nK-Market is on-line trading company. They have implemented some access " +
                "control over the on-line trading using XACML policies. K-Martket has separated their " +
                "customers in to three groups and has put limit on on-line buying items.\n");

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

    public static String CreateXACMLRequestForTagAccess(String userName, String tagName, String action)
    {
        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + action + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + userName + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + tagName + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "</Request>";
    }

    // public static String getPolicy(int policyNum)
    // {
    // 	switch(policyNum)
    // 	{
    // 	case 0:
    // 		return readFile("C:/Users/ECEUser/Documents/PhD/ExcelPolicies/sample-doc-policy.xml",Charset.defaultCharset());
    // 	case 1:
    // 		return readFile("./resources/kmarket-sliver-policy.xml",Charset.defaultCharset());
    // 	case 2:
    // 		return readFile("./resources/kmarket-gold-policy.xml",Charset.defaultCharset());
    // 	default:
    // 			return "";
    // 	}
    // }

    // private static String readFile(String path, Charset encoding)
    // {
    // 	try
    // 	{
    // 		byte[] encoded = Files.readAllBytes(Paths.get(path));
    // 		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    // 	}
    // 	catch(IOException e)
    // 	{
    // 		return "";
    // 	}
    // }



}
