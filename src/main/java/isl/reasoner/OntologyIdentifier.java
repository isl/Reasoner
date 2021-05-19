package isl.reasoner;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;

/**
 *  This class is responsible for identifying if a given resource contains schema resources 
 * (e.g. definitions of classes, properties, etc.).
 * Technically, it checks if a given resource contains instantiations of a class (found in rdf, rdfs, owl) 
 * or property. 
 *
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 */
public class OntologyIdentifier {
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM );
    
    /** Checks if the given resource contains schema-related elements (e.g. definition of classes and properties
     * w.r.t. RDF, RDFS, and OWL). Apart from the actual contents, the extension of the file that contained them is
     * required (so that the model can properly parse them). 
     * Notice that the extension should be given using the beginning dot (e.g. .rdfs, .owl, etc.)
     * Although several extensions are supported, if a non-supported extension is given an 
     * exception is thrown.
     * 
     * @param fileContents the actual contents of the resource to be checked
     * @param extension the extension of the file containing the resources
     * @return true if the resource contains schema elements, otherwise false
     * @throws UnsupportedExtensionException if the given extension is not supported */ 
    public boolean isSchema(String fileContents, String extension) throws UnsupportedExtensionException{ 
        if(!OntologyReasoner.langs.keySet().contains(extension.toLowerCase())){
            throw new UnsupportedExtensionException("The given file extension ("+extension+") is not supported. "
                                                   +"The list of accepted file extensions is "+OntologyReasoner.langs.keySet());
        }
        model.read(new StringReader(fileContents),null,OntologyReasoner.langs.get(extension.toLowerCase()));
         String query = "ASK "
                + "WHERE { "
                + "?s ?p ?type."
                 + "FILTER("
                 + "?type=<https://www.w3.org/1999/02/22-rdf-syntax-ns#Class> || "
                 + "?type=<http://www.w3.org/1999/02/22-rdf-syntax-ns#Class> || "
                 + "?type=<https://www.w3.org/2000/01/rdf-schema#Class> ||  "
                 + "?type=<http://www.w3.org/2000/01/rdf-schema#Class> || "
                 + "?type=<http://www.w3.org/2002/07/owl#Class> ||  "
                 + "?type=<https://www.w3.org/2002/07/owl#Class> || "
                 + "?type=<https://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || "
                 + "?type=<http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || "
                 + "?type=<https://www.w3.org/2000/01/rdf-schema#Property> ||  "
                 + "?type=<http://www.w3.org/2000/01/rdf-schema#Property> || "
                 + "?type=<http://www.w3.org/2002/07/owl#Property> ||  "
                 + "?type=<https://www.w3.org/2002/07/owl#Property>) "
                + "}";
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        return qe.execAsk();
    }
}
