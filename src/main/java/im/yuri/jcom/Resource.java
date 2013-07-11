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
        properties.put("X", randomGenerator.nextInt(10));
        properties.put("Y", randomGenerator.nextInt(10));
        properties.put("Z", randomGenerator.nextInt(10));
        saveResource();
    }

    public boolean setValue(String property, Integer newValue, Float fault) {
        if (randomGenerator.nextFloat() < fault) {
            return false;
        }
        else {
            reloadResource();
            this.properties.put(property, newValue);
            //need to rewrite because of transactions
            //first, make all actions, get COMMIT and then save
            saveResource();
            return true;
        }
    }

    private void saveResource() {
        try {
            Yaml.dump(this.properties, new File("resource " + id + ".yaml"));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //gets values from YAML file
    private void reloadResource() {
        try {
            this.properties = Yaml.loadType(new File("resource " + this.id + ".yaml"), HashMap.class);
        }
        catch (FileNotFoundException e) {

            e.printStackTrace();
        }
    }

    public Integer getValue(String property) {
        reloadResource();
        return (Integer)this.properties.get(property);
    }
}
