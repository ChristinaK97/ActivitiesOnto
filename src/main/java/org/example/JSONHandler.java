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

import static org.example.App.*;

public class JSONHandler {

    private JSONObject obj;
    private JSONArray activities;
    private String json_path_og = RESOURCES + "input/example_observations.json";
    private String ontology_og = RESOURCES + "input/activities_onto.owl";

    public JSONHandler(String json_path) {
        if(json_path != null)
            this.json_path_og = json_path;
        try {
            obj = (JSONObject) new JSONParser().parse(new FileReader(this.json_path_og));
            activities = (JSONArray) ((JSONObject) obj.get("model")).get("activities") ;
        } catch(IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean alreadyProcessed() {
        return new File(ontology_full).exists() && new File(json_path_with_ids).exists();
    }

    public void setKeys() {
        for (int i=0; i< activities.size(); ++i) {
            String act_id_str = "a" + i;
            JSONObject activity = (JSONObject) activities.get(i);
            addAttribute(activity, "id", act_id_str);

            HashMap<String, Integer> obs_ids = new HashMap<>();
            String obs_id_str = act_id_str + ".o";

            for (Object observation : (JSONArray) activity.get("observations")) {
                String obs_content = getContent(observation);

                if (!obs_ids.containsKey(obs_content))
                    obs_ids.put(obs_content, obs_ids.size());
                addAttribute((JSONObject) observation, "id", obs_id_str + obs_ids.get(obs_content));
            }
        }
        try (FileWriter file = new FileWriter(App.json_path_with_ids)) {
            file.write(obj.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                App.uri, class_, App.prefix, class_, App.prefix, superclass)
                );
        System.out.println(newTriples);
        write_full_ontology(newTriples);
    }

    private HashMap<String, HashSet<String>> findClasses() {
        HashMap<String, HashSet<String>> classes = new HashMap<>(2);
        classes.put(App.ACT, new HashSet<>());
        classes.put(App.OBS, new HashSet<>());

        for (Object activity : activities) {
            classes.get(App.ACT).add(getContent(activity));
            for (Object obsevation : (JSONArray) ((JSONObject)activity).get("observations"))
                classes.get(App.OBS).add(getContent(obsevation));
        }
        System.out.println(classes);
        return classes;
    }

    private void write_full_ontology(StringBuilder newTriples) {
        try {
            File file = new File(App.ontology_full);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false)) ;
            writer.append(
                    Files.readString(Paths.get(ontology_og))
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
