package org.example;


import com.ontotext.trree.config.OWLIMSailSchema;
import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.RepositoryConnectionWrapper;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.repository.sail.config.SailRepositorySchema;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import javax.swing.*;
import java.io.*;
import java.util.Collections;
import java.util.Map;

import static org.example.App.RESOURCES;

public class Graph_DB implements Closeable {
    private RepositoryManager repoManager;
    private static final String repoId = "repo-test-1";
    private static final String repoConfigFile = RESOURCES + "processed/graphDB_repoConfig.ttl";

    public Graph_DB() throws Exception {
        repoManager = RepositoryProvider.getRepositoryManager("http://localhost:7200/");
        repoManager.init();
        createRepository();
        printRepos();
    }

    private void printRepos() {
        System.out.println(repoManager.getRepositoryIDs());
    }

    private boolean repoExists() {
        return repoManager.hasRepositoryConfig(repoId);
    }

    public void createRepository() throws Exception{
        if (repoExists()) {
            repoManager.removeRepository(repoId);
            System.out.println("Repo found and removed.");
            printRepos();
        }

        TreeModel graph = new TreeModel();

        InputStream config = new FileInputStream(repoConfigFile);
        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(graph));
        rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
        config.close();

        Resource repositoryNode = Models.subject(graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY)).orElse(null);

        graph.add(repositoryNode, RepositoryConfigSchema.REPOSITORYID,
                SimpleValueFactory.getInstance().createLiteral(repoId));


        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);

        repoManager.addRepositoryConfig(repositoryConfig);
    }


    @Override
    public void close() throws IOException {
        repoManager.shutDown();
    }

    public static void main(String[] args) throws Exception {
        new Graph_DB();
    }

}