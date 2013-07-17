package im.yuri.jcom;


import org.ho.yaml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Resource {
    private HashMap properties;
    private String fileName;
    private String transactionId;
    private HashMap<String, Integer> changedProperties;
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
        changedProperties = new HashMap<>();
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
            changedProperties.put(property, newValue);
//            this.properties.put(property, newValue);
            System.out.println(properties.values());
            //need to rewrite because of transactions
            //first, make all actions, get COMMIT and then save
            return true;
        }
    }

    public void saveResource() {
        Path path = Paths.get(fileName);
        Charset charset = StandardCharsets.UTF_8;
        try {
            String content = new String(Files.readAllBytes(path), charset);
            System.out.println("has next!");
            Iterator i = changedProperties.entrySet().iterator();
            while(i.hasNext()) {
                System.out.println("really?");
                Map.Entry pairs = (Map.Entry)i.next();
                System.out.println(pairs.getKey() + ": " + properties.get(pairs.getKey()));
                content = content.replaceAll(pairs.getKey() + ": " + properties.get(pairs.getKey()), pairs.getKey() + ": " + pairs.getValue());
                System.out.println(content);
            }
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
//            Yaml.dump(this.properties, new File(fileName));
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
