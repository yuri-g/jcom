package im.yuri.jcom;


import org.ho.yaml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Resource {
    private HashMap properties;
    private String fileName;
    private String transactionId;
    private Random randomGenerator = new Random();

    public String getFileName() {
        return fileName;
    }



    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
//            reloadResource();
            this.properties.put(property, newValue);
            System.out.println(properties.values());
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
