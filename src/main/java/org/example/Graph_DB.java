package org.example;

import com.ontotext.graphdb.repository.http.GraphDBHTTPRepository;
import com.ontotext.graphdb.repository.http.GraphDBHTTPRepositoryBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Graph_DB {

    private RepositoryManager repoManager;
    private RepositoryConnection connection;

    public Graph_DB() {
        try {
            // create repository
            repoManager = RepositoryProvider.getRepositoryManager("http://localhost:7200/");
            repoManager.init();
            createRepository();
            printRepos();
            repoManager.shutDown();

            // upload data and run queries
            connection = startConnection();
            uploadData();
            runQueries();
            connection.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createRepository() throws Exception{
        System.out.println(">>> Create Repository");

        if (repoExists()) // remove and create again. Alt return
            repoManager.removeRepository(CONSTANTS.repoId);

        TreeModel graph = new TreeModel();

        InputStream config = new FileInputStream(CONSTANTS.repoConfigFile);
        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(graph));
        rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
        config.close();

        Resource repositoryNode = Models.subject(graph.filter(null, RDF.TYPE,
                RepositoryConfigSchema.REPOSITORY)).orElse(null);

        graph.add(repositoryNode, RepositoryConfigSchema.REPOSITORYID,
                SimpleValueFactory.getInstance().createLiteral(CONSTANTS.repoId));

        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
        repoManager.addRepositoryConfig(repositoryConfig);
    }

    public void uploadData() throws IOException {
        System.out.println(">>> Upload Data");
        // When adding data we need to start a transaction
        connection.begin();
        connection.add(new FileInputStream(CONSTANTS.ontology_full), "urn:base", RDFFormat.TURTLE);
        connection.add(new FileInputStream(CONSTANTS.dataFilePath), "urn:base", RDFFormat.TURTLE);
        // Committing the transaction persists the data
        connection.commit();
    }

    public void runQueries() {
        System.out.println(">>> Run Queries");

        Queries queries = new Queries();
        queries.readQueries();

        for(int i=0; i< queries.numOfQueries(); ++i) {
            ArrayList<ArrayList<String>> records = new ArrayList<>();
            TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    queries.getQuery(i)).evaluate();

            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                ArrayList<String> bindings = new ArrayList<>();

                for (String var : queries.getVariables(i)) {
                    Object binding = bindingSet.getBinding(var).getValue();
                    if (binding instanceof IRI)
                        binding = ((IRI) binding).getLocalName();
                    bindings.add(binding.toString());
                }
                records.add(bindings);
            }
            result.close();
            queries.writeQueryResults(i, records);
        }
    }

    private RepositoryConnection startConnection() {
        GraphDBHTTPRepository repository = new GraphDBHTTPRepositoryBuilder()
                .withServerUrl("http://localhost:7200")
                .withRepositoryId(CONSTANTS.repoId)
                .build();
        return repository.getConnection();
    }
    private void printRepos() {
        System.out.println(repoManager.getRepositoryIDs());
    }
    private boolean repoExists() {
        return repoManager.hasRepositoryConfig(CONSTANTS.repoId);
    }

}
