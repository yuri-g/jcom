package im.yuri.jcom;


import org.ho.yaml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.nio.channels.FileChannel;

class Resource {
    private HashMap properties;
    private Random randomGenerator = new Random();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName;
    private Integer id;

    public Resource() {

    }

    public Resource(String fileName) throws IOException {
        this.fileName = fileName;
        try {
            this.properties = Yaml.loadType(new File(fileName), HashMap.class);
        }
        catch (FileNotFoundException e) {

            e.printStackTrace();
        }

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
            return true;
        }
    }

    public void saveResource() {
        try {
            Yaml.dump(this.properties, new File(fileName));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //gets values from YAML file
    private void reloadResource() {
        try {
            this.properties = Yaml.loadType(new File(fileName), HashMap.class);
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
