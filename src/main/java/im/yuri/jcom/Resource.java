package im.yuri.jcom;


import org.ho.yaml.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;



//resource class, used for reading and writing values to resource files
public class Resource {
    //current properties of the resource file (z, x, y, etc)
    private HashMap properties;
    //name of the file
    private String fileName;
    //id of transaction that is using the resource
    private String transactionId;
    //temporary storage of changed properties, so the actual file is not changed
    private HashMap<String, Integer> changedProperties;
    //random generator
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
        //read the file
        this.fileName = fileName;
        try {
            this.properties = Yaml.loadType(new File(fileName), HashMap.class);
        }
        catch (FileNotFoundException e) {

            e.printStackTrace();
        }

    }

    public boolean setValue(String property, Integer newValue, Float fault) {
        //if the random generated value is larger than fault probability
        //then the writing was unsuccessful
        if (randomGenerator.nextFloat() < fault) {
            return false;
        }
        else {
            //otherwise, change the property
            changedProperties.put(property, newValue);
            return true;
        }
    }


    //save resource, used when commiting
    public void saveResource() {
        Path path = Paths.get(fileName);
        Charset charset = StandardCharsets.UTF_8;
        try {
            //read the file contents
            String content = new String(Files.readAllBytes(path), charset);
            Iterator i = changedProperties.entrySet().iterator();
            //and change the properties that were affected by transaction
            while(i.hasNext()) {
                Map.Entry pairs = (Map.Entry)i.next();
                content = content.replaceAll(pairs.getKey() + ": " + properties.get(pairs.getKey()), pairs.getKey() + ": " + pairs.getValue());
            }
            //save file
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
