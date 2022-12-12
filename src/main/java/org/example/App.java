package org.example;


public class App {
    public static final String prefix = "act:";
    public static final String uri = "###  http://www.semanticweb.org/xristina/ontologies/2022/11/act#";
    public static final String OBS = "Observation";
    public static final String ACT = "Activity";
    public static final String RESOURCES = "./src/main/resources/";
    public static final String json_path_with_ids = RESOURCES + "processed/observations_with_ids.json";
    public static final String ontology_full = RESOURCES + "processed/activities_onto_full.ttl";

    private void process_json_and_ontology() {
        JSONHandler handler = new JSONHandler(null);
        if (! handler.alreadyProcessed()) {
            handler.setKeys();
            handler.writeClasses();
        }
    }

    public static void main(String[] args ) {
        App app = new App();
        app.process_json_and_ontology();
        //new Mapping();
    }
}
