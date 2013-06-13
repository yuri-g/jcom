package im.yuri.jcom;


import org.ho.yaml.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class Resource {
    private HashMap properties;
    private Random randomGenerator = new Random();
    private Integer id;


    public Resource(Integer id) {
        properties = new HashMap();
        this.id = id;
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

    public Integer getValue(String property) {
        try {
            properties = Yaml.loadType(new File("resource " + this.id + ".xml"), HashMap.class);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return (Integer)properties.get(property);
    }
}
