package gt.research.contactprovider;

import gt.research.mht.MHTNode;
import gt.research.util.DistributedSystemsManager;
import gt.research.util.FileManager;
import gt.research.xacml.PDP_LIB;
import gt.research.xacml.test.XacmlTest;
import org.apache.commons.io.IOUtils;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.ResponseCtx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by sang on 10/25/16.
 * Demo for Dr. Blough
 *
 */
public class Test {

    private PDP_LIB pdp;

    public Test(File policyFile, File certFile) {
        pdp = new PDP_LIB();
        try {
            pdp.InitializePDP(IOUtils.toString(new FileInputStream(policyFile), "UTF-8"),
                    IOUtils.toString(new FileInputStream(certFile), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean requestAccess(String appname, String tag) {
        String request = XacmlTest.CreateXACMLRequestForTagAccess(appname, tag, "buy");
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

    public static void main(String[] args) {
        //generateCert();
        String[] apps = {"whatsapp", "facebook", "snapchat"};
        HashMap<String, String> pkgMap = new HashMap<String, String>();
        loadPkgMap(pkgMap);

        for (String key: pkgMap.keySet()) {
            System.out.println(key + "->" + pkgMap.get(key));
        }

        HashMap<String, Test> parsers = new HashMap<>();
        for (String app: apps) {
            Test test = new Test(new File("data/app-policy/app-policy.xml"), new File("data/app-policy/" + app + ".xml"));
            parsers.put(app, test);
        }

        String[] groups = {"Friend", "Family", "Colleague", "School", "Work", "Alumni",
                "Supervisor", "Medical", "Sales", "Repair", "Professor", "None"};

        HashMap<String, Boolean> permissions = new HashMap<>();
        for (String app: apps) {
            for (String tag: groups) {
                boolean perm = parsers.get(app).requestAccess(app, tag);
                System.out.println(app + ":" + tag + "=" + perm);
                permissions.put(app + ":" + tag, perm);
            }
        }



    }

    public static void loadPkgMap(HashMap<String, String> map) {
        map.put("facebook", "com.facebook");
        map.put("snapchat", "com.snapchat");
        map.put("whatsapp", "com.whatsapp");
    }




    public static void generateCert() {
        String node1Data = "attributeID=urn:gtTEST:fake-attribute\n" +
                "attributeValue=dontCare";
        String node2Data = "attributeID=urn:gtTEST:user-name\n" +
                "attributeValue=bloodtech";
        String parentData = "subjectValue=bloodtech";
        MHTNode node1 = new MHTNode(node1Data);
        MHTNode node2 = new MHTNode(node2Data);
        Vector<MHTNode> children = new Vector<MHTNode>();
        children.add(node1);
        children.add(node2);
        MHTNode parent = new MHTNode(children);

        parent.data = parentData;
        parent.GenerateHashes();
        System.out.println(parent.getData());
        System.out.println(MHTNode.bytesToHex(parent.getNodeHash()));
        for (MHTNode child: parent.getChildren()) {
            System.out.println(child.getData());
            System.out.println(MHTNode.bytesToHex(child.getNodeHash()));
        }
        String xml = parent.ExportAsXML();
        System.out.println(xml);
    }
}
