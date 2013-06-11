package im.yuri.jcom;


import org.ho.yaml.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class Resource {
    private HashMap properties;
    Random randomGenerator = new Random();

    public Resource(Integer id) {
        properties = new HashMap();

        properties.put("X", randomGenerator.nextInt(20));
        properties.put("Y", randomGenerator.nextInt(75));
        properties.put("Z", randomGenerator.nextInt(200));
        try {
            Yaml.dump(properties, new File("resource " + id + ".xml"));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}
