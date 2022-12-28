package org.example;

import com.ontotext.graphdb.repository.http.GraphDBHTTPRepository;
import com.ontotext.graphdb.repository.http.GraphDBHTTPRepositoryBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.GraphDBValidationException;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;

import java.io.IOException;
import java.io.StringReader;


// to upload shacl shape and validate after data have already been uploaded
public class Validation {

    //private SailRepositoryConnection connection;
    private RepositoryConnection connection;
    public Validation() {
        GraphDBHTTPRepository repository = new GraphDBHTTPRepositoryBuilder()
                .withServerUrl("http://localhost:7200")
                .withRepositoryId(CONSTANTS.repoId)
                .build();
        connection = repository.getConnection();

        /*ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository sailRepository = new SailRepository(shaclSail);
        sailRepository.init();
        connection = sailRepository.getConnection();*/

    }

    private void commitShaclShapesGraph() throws IOException, GraphDBValidationException {
        connection.begin();
        // test rule
        StringReader shaclRules = new StringReader(
                String.join("\n", "",

                "PREFIX act: <http://www.semanticweb.org/xristina/ontologies/2022/11/act#>",
                "PREFIX sh: <http://www.w3.org/ns/shacl#>",
                "act:TestShape",
                    "a sh:NodeShape ;",
                    "sh:targetClass act:Interval;",
                    "sh:property [",
                        "sh:path act:start ;",
                        "sh:minCount 2",
                    "]."
                )
        );
        System.out.println("commit shapes graph ");
        connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

    public void clearShaclShapesGraph() {
        System.out.println("Clear shapes graph");
        connection.begin();
        connection.clear(RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

    public void validate() {
        try {
            commitShaclShapesGraph();
        } catch (RepositoryException exception) {
            System.out.println("Exception caught");
            Throwable cause = exception.getCause();
            if (cause instanceof GraphDBValidationException) {
                Model validationReportModel = ((GraphDBValidationException) cause).validationReportAsModel();
                Rio.write(validationReportModel, System.out, RDFFormat.TURTLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //clearShaclShapesGraph();
    }

    public static void main(String[] args) {
        new Validation().validate();
        //new Validation().clearShaclShapesGraph();
    }

}

