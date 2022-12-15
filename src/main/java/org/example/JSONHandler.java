package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class JSONHandler {

    private JSONObject obj;
    private JSONArray activities;

    public JSONHandler() {
        try {
            obj = (JSONObject) new JSONParser().parse(new FileReader(CONSTANTS.json_path_og));
            activities = (JSONArray) ((JSONObject) obj.get("model")).get("activities") ;
        } catch(IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean alreadyProcessed() {
        return new File(CONSTANTS.ontology_full).exists() && new File(CONSTANTS.json_path_with_ids).exists();
    }

    public void setKeys() {
        long counter = 0;
        for (Object act : activities) {
            JSONObject activity = (JSONObject) act;

            String act_id_str = "a" + generateID(counter);
            addAttribute(activity, "id", act_id_str);

            HashMap<String, String> obs_ids = new HashMap<>();
            String obs_id_str = act_id_str + ".o";

            for (Object observation : (JSONArray) activity.get("observations")) {
                String obs_content = getContent(observation);

                if (!obs_ids.containsKey(obs_content))
                    obs_ids.put(obs_content, generateID(obs_ids.size()));
                addAttribute((JSONObject) observation, "id", obs_id_str + obs_ids.get(obs_content));
            }
            ++counter;
        }
        try (FileWriter file = new FileWriter(CONSTANTS.json_path_with_ids)) {
            file.write(obj.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateID(long counter) {
        return Long.toHexString(counter);
    }

    private void addAttribute(JSONObject object, String key, String value) {
        object.put(key, value);
    }
    private String getContent(Object jsonObj) {
        return (String) ((JSONObject) jsonObj).get("content");
    }

    public void writeClasses() {
        StringBuilder newTriples = new StringBuilder();
        HashMap<String, HashSet<String>> classes = findClasses();
        for(String superclass : classes.keySet())
            for(String class_ : classes.get(superclass))
                newTriples.append(
                        String.format("%s%s\n%s%s rdf:type owl:Class ;\n\trdfs:subClassOf %s%s .\n\n",
                                CONSTANTS.uri, class_, CONSTANTS.prefix, class_, CONSTANTS.prefix, superclass)
                );
        System.out.println(newTriples);
        write_full_ontology(newTriples);
    }

    private HashMap<String, HashSet<String>> findClasses() {
        HashMap<String, HashSet<String>> classes = new HashMap<>(2);
        classes.put(CONSTANTS.ACT, new HashSet<>());
        classes.put(CONSTANTS.OBS, new HashSet<>());

        for (Object activity : activities) {
            classes.get(CONSTANTS.ACT).add(getContent(activity));
            for (Object observation : (JSONArray) ((JSONObject)activity).get("observations"))
                classes.get(CONSTANTS.OBS).add(getContent(observation));
        }
        System.out.println(classes);
        return classes;
    }

    private void write_full_ontology(StringBuilder newTriples) {
        try {
            File file = new File(CONSTANTS.ontology_full);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false)) ;
            writer.append(
                    Files.readString(Paths.get(CONSTANTS.ontology_og))
            );
            writer.append("\n\n");
            writer.append(newTriples);
            writer.flush();
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
