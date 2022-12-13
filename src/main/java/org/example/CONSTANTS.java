package org.example;

public class CONSTANTS {

    public static final String prefix = "act:";
    public static final String uri = "###  http://www.semanticweb.org/xristina/ontologies/2022/11/act#";
    public static final String OBS = "Observation";
    public static final String ACT = "Activity";
    public static final String RESOURCES = "./src/main/resources/";

    // ontology and data input files
    public static final String ontology_og = RESOURCES + "input/activities_onto.owl";
    public static final String json_path_og = RESOURCES + "input/example_observations.json";

    // ontology and data processed files
    public static final String ontology_full = RESOURCES + "processed/activities_onto_full.ttl";
    public static final String json_path_with_ids = RESOURCES + "processed/observations_with_ids.json";
    public static final String dataFilePath = RESOURCES + "processed/data.ttl";
    public static final String mappingFilePath = RESOURCES + "processed/activities_mapping.ttl";

    // repository
    public static final String repoConfigFile = RESOURCES + "processed/graphDB_repoConfig.ttl";
    public static final String repoId = "repo-test-1";

    // queries
    public static final String resultsFile = RESOURCES + "queries/results";
    public static final String queriesFile = RESOURCES + "queries/queries.txt";

}
