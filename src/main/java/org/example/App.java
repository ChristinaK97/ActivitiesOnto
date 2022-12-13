package org.example;


public class App {

    private void process_json_and_ontology() {
        JSONHandler handler = new JSONHandler();
        if (! handler.alreadyProcessed()) {
            handler.setKeys();
            handler.writeClasses();
        }
    }

    public static void main(String[] args ) {
        App app = new App();
        app.process_json_and_ontology();
        new Mapping();
        new Graph_DB();
    }
}
