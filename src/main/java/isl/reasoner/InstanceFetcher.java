package isl.reasoner;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.apache.commons.lang3.tuple.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** This class reads a resource containing instances of an ontology and 
 * retrieves their classes, as well as the corresponding instances.
 *
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 */
public class InstanceFetcher {
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM );
    
    public InstanceFetcher(String fileContents, String extension) throws UnsupportedExtensionException{
       if(!OntologyReasoner.langs.keySet().contains(extension.toLowerCase())){
            throw new UnsupportedExtensionException("The given file extension ("+extension+") is not supported. "
                                                   +"The list of accepted file extensions is "+OntologyReasoner.langs.keySet());
        }
        model.read(new StringReader(fileContents),null,OntologyReasoner.langs.get(extension.toLowerCase()));
    }
    
    /** Retrieves the URIs of the classes that are in the given instance resource. 
     * Practically it retrieves the URIs that appear as object in triples of the form
     * [?subject rdf:type ?object] 
     * Notice that the method does not apply inference. 
     * 
     * @return the URIs of the classes of the given resource */ 
    public Collection<String> getClassUris(){
        Set<String> retCollection=new HashSet<>();
        String selectQuery="SELECT DISTINCT ?class "
                          +"WHERE { "
                            +"?subject <"+RDF.type+"> ?class "
                          +"}";
        QueryExecution qe = QueryExecutionFactory.create(selectQuery, this.model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.next();
            retCollection.add(result.get("class").toString());
        }
        return retCollection;
    }
    
    /** Given the URI of a class, it retrieves the URIs of its instances.
     * Practically, it retrieves of the subjects in triples of the form
     * [?subject rdf:type GIVEN_CLASS_URI]
     * Notice that the method does not apply inference. 
     * If a given instance does not have an rdfs:label then an empty string is returned
     * 
     * @param classUri the URI of the class
     * @return the URIs and the corresponding labels (if they exist) of the instances of the given class */
    public Collection<Pair<String,String>> getInstanceUris(String classUri){
        Set<Pair<String,String>> retCollection=new HashSet<>();
        String selectQuery="SELECT DISTINCT ?subject ?label "
                          +"WHERE { "
                            +"?subject <"+RDF.type+"> <"+classUri+">. "
                            +"OPTIONAL{ "
                                +"?subject <"+RDFS.label+"> ?label. " 
                            +"} "
                          +"}";
        QueryExecution qe = QueryExecutionFactory.create(selectQuery, this.model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.next();
            retCollection.add(Pair.of(result.get("subject").toString(),
                              (result.get("label")!=null)?result.get("label").toString():""));
        }
        return retCollection;
    }
    
    /** The method retrieves the URIs of the classes that appear in the model
     * and for each one of them it retrieves its instances as well. 
     * Practically, it retrieves of the subjects and the objects in triples of the form
     * [?subject rdf:type ?object]
     * The results are returned in the form of a multimap where keys are the URIs of the classes 
     * and values are corresponding instances URIs. 
     * If a given instance does not have an rdfs:label then an empty string is returned
     * Notice that the method does not apply inference. 
     * 
     * @return a multimap with class and instance URIs [key: class Uri, values: instances URIs and their corresponding labels] */
    public Multimap<String,Pair<String,String>> getClassAndInstanceUris(){
        Multimap<String,Pair<String,String>> retMap=TreeMultimap.create();
        String selectQuery="SELECT DISTINCT ?subject ?object ?label "
                          +"WHERE { "
                            +"?subject <"+RDF.type+"> ?object. "
                            +"OPTIONAL{ "
                                +"?subject <"+RDFS.label+"> ?label. "
                            +"} "
                          +"}";
        QueryExecution qe = QueryExecutionFactory.create(selectQuery, this.model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.next();
            retMap.put(result.get("object").toString(),
                       Pair.of(result.get("subject").toString(),
                              (result.get("label")!=null)?result.get("label").toString():""));
        }
        return retMap;
    }
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException, UnsupportedExtensionException{
        String contents="";
        String line="";
        BufferedReader br=new BufferedReader(new FileReader(new File("sample.ttl")));
        while((line=br.readLine())!=null){
            contents+=line+"\n";
        }
        InstanceFetcher instance=new InstanceFetcher(contents, ".ttl");
        for(String classUri : instance.getClassUris()){
            System.out.println(classUri);
            for(Pair<String,String> instanceUri : instance.getInstanceUris(classUri)){
                System.out.println("\t"+instanceUri);
            }
        }
        Multimap<String,Pair<String,String>> multimap=instance.getClassAndInstanceUris();
        for(String classUri : multimap.keySet()){
            System.out.println(classUri+":\t"+multimap.get(classUri));
        }
    }
}