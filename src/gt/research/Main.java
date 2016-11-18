package gt.research;

import gt.research.util.DistributedSystemsManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by sang on 10/4/16.
 */
public class Main {

    public static void main(String[] args) {
        Properties prop = new Properties();
        try {
            File propFile = new File(new File(".").getCanonicalPath() + "/test.properties");
            System.out.println(propFile);
            FileInputStream in = new FileInputStream(propFile);
            prop.load(in);
            in.close();
            DistributedSystemsManager m = new DistributedSystemsManager(prop);
            m.parseDataFile();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
