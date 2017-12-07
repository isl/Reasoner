/*
 Copyright 2015 Institute of Computer Science,
 Foundation for Research and Technology - Hellas

 Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 by the European Commission - subsequent versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in writing, software distributed
 under the Licence is distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the Licence for the specific language governing permissions and limitations
 under the Licence.

 Contact:  POBox 1385, Heraklio Crete, GR-700 13 GREECE
 Tel:+30-2810-391632
 Fax: +30-2810-391638
 E-mail: isl@ics.forth.gr
 http://www.ics.forth.gr/isl

 Authors : Konstantina Konsolaki, Georgios Samaritakis
 This file is part of the Reasoner project.
 */
package isl.reasoner;

import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;

/**
 *
 * @author konsolak
 */
public class OntologyReasoner {

    OntModel modelAll = ModelFactory.createOntologyModel(
            PelletReasonerFactory.THE_SPEC, null);
    ;

    private static final HashMap<String, String> langs = new HashMap<String, String>();

    static {
        langs.put(".ttl", "Turtle");
        langs.put(".nt", "N-Triples");
        langs.put(".nq", "N-Quads");
        langs.put(".trig", "TriG");
        langs.put(".rdf", "RDF/XML");
        langs.put(".owl", "N-Triples");
        langs.put(".jsonld", "JSON-LD");
        langs.put(".trdf", "RDF Thrift");
        langs.put(".rt", "RDF Thrift");
        langs.put(".rj", "RDF/JSON");
        langs.put(".trix", "TriX");
    }

    /**
     * Return all properties that can be applied to instances of this class.
     *
     * @param subject
     * @return An arrayList with the properties that can be applied to the
     * specific subject
     */
    public ArrayList<String> listProperties(String subject) {
        disableLogging();
        ArrayList<String> listProps = new ArrayList();
        OntClass c = modelAll.getOntClass(subject);
        if (c != null) {
            ExtendedIterator itq = c.listDeclaredProperties(false);
            while (itq.hasNext()) {
                OntProperty property = (OntProperty) itq.next();
                if (property.getDomain() != null) {
                    listProps.add(property.toString());
                }
            }
            itq = modelAll.listAnnotationProperties();
            while (itq.hasNext()) {
                OntProperty property = (OntProperty) itq.next();
                listProps.add(property.toString());
            }
        }

        //remove duplicates
        Set setItems = new LinkedHashSet(listProps);
        listProps.clear();
        listProps.addAll(setItems);
        //sort list
        Collections.sort(listProps);
        /*   for (String prop : listProps) {
         System.out.println(prop);
         }*/
        return listProps;
    }

    /**
     * Initiates the ontology and checks the consistency of the model
     *
     * @param modelNS
     * @return true or false according to the consistency of the model
     */
    public boolean initiateModel(String modelNS) {
        // read the ontology with its imports
        disableLogging();

        //disable logging
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for (Logger logger : loggers) {
            logger.setLevel(Level.OFF);
        }
        String ext = modelNS.substring(modelNS.lastIndexOf("."));
        langs.get(ext);
        OntModel model = ModelFactory.createOntologyModel(
                PelletReasonerFactory.THE_SPEC, null);
        try {
            model.setDerivationLogging(false);
            model.read(modelNS, langs.get(ext));

        } catch (com.hp.hpl.jena.shared.JenaException e) {
            e.printStackTrace();
            System.out.println("Connection refused!");
            // e.printStackTrace();
        }

        model.prepare();

        OntModel tmp = modelAll;
        modelAll = model;
        modelAll.add(tmp);

        KnowledgeBase kb = ((PelletInfGraph) model.getGraph()).getKB();

        boolean consistent = kb.isConsistent();
        return consistent;
    }

    /**
     * Return all classes of the model
     *
     * @return an arrayList with all the classes of the model
     */
    public ArrayList<String> getAllClasses() {
        disableLogging();
        ArrayList<String> listClasses = new ArrayList();
        ExtendedIterator<OntClass> listClassesIt = modelAll.listClasses();
        try {

            while (listClassesIt.hasNext()) {
                OntClass c = (OntClass) listClassesIt.next();
                if (c.getURI() != null) {
                    listClasses.add(c.getURI());
                }

            }
        } catch (Exception ex) {
        }
        //remove duplicates
        Set setItems = new LinkedHashSet(listClasses);
        listClasses.clear();
        listClasses.addAll(setItems);
        //sort
        Collections.sort(listClasses);
        /*for (String c : listClasses) {
         System.out.println(c);
         }*/

        return listClasses;

    }

    /**
     * Return all objects that can be applied to a specific property
     *
     * @param property
     * @return An arrayList with the objects that can be applied to the specific
     * property
     */
    public ArrayList<String> listObjects(String property) {
        disableLogging();
        ArrayList<String> listObjects = new ArrayList();
//        String query = "select ?range \n"
//                + "where {\n"
//                + "<" + property + "> <" + RDFS.range + "> ?range .\n"
//                + "}                                                    ";
        OntProperty p2 = modelAll.getOntProperty(property);

        //    StmtIterator it = modelAll.listStatements(p, RDFS.range, (RDFNode) null);
        if (p2 != null) {
//            com.hp.hpl.jena.query.Query q = QueryFactory.create(query);
//
//            // Create a SPARQL-DL query execution for the given query and
//            // ontology model
//            QueryExecution qe = QueryExecutionFactory.create(q, modelAll);
//
//            // We want to execute a SELECT query, do it, and return the result set
//            ResultSet rs = qe.execSelect();
//            while (rs.hasNext()) {
//                QuerySolution s = rs.next();
//                System.out.println("sss->" + s.toString());
//            }
            // Print the query for better understanding
            ExtendedIterator it = p2.listSuperProperties(true);
            List<Property> list = new ArrayList<Property>();
            while (it.hasNext()) {
                Property node = (Property) it.next();
                list.add(node);
                // System.out.println("----> " + node.toString());

            }
            //remove super proprties to get the direct range
            //otherwise we get also the range of the super proprties
            for (Property list1 : list) {
                p2.removeSuperProperty(list1);
            }

            RDFNode range = p2.getRange();
            if (range != null) {
                System.out.println("range  " + range.toString());
                //   RDFNode node = (RDFNode) it.next().getObject();
                //  listObjects.add(node.toString());
                listObjects.add(range.toString());
                OntClass objectClass = modelAll.getOntClass(range.toString());
                if (objectClass != null) {
                    ExtendedIterator<OntClass> listSubClasses = objectClass.listSubClasses();
                    while (listSubClasses.hasNext()) {
                        OntClass subClass = listSubClasses.next();
                        listObjects.add(subClass.toString());
                    }
                }

            } else {
                for (Property list1 : list) {
                    p2.addSuperProperty(list1);
                }
                //Return also the ranges of the super properties of property
                //For example at skos prefLabel did not have declared the edfs:range but it 
                //was subproperty of label. So we should return the range of label as a result.
                StmtIterator it2 = modelAll.listStatements(p2, RDFS.subPropertyOf, (RDFNode) null);
                while (it2.hasNext()) {
                    RDFNode node = (RDFNode) it2.next().getObject();
                    OntClass subProperty = modelAll.getOntClass(node.toString());
                    if (subProperty != null) {
                        StmtIterator rangeClasses = modelAll.listStatements(subProperty, RDFS.range, (RDFNode) null);

                        while (rangeClasses.hasNext()) {
                            RDFNode node2 = (RDFNode) rangeClasses.next().getObject();
                            listObjects.add(node2.toString());
                        }
                    }
                }
            }

        }
        //remove duplicates
        Set setItems = new LinkedHashSet(listObjects);
        listObjects.clear();
        listObjects.addAll(setItems);
        //sort list
        Collections.sort(listObjects);
        /*    for (String prop : listObjects) {
         System.out.println(prop);
         }*/

        return listObjects;
    }

    private static void disableLogging() {
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for (Logger logger : loggers) {
            logger.setLevel(Level.OFF);
        }
    }
}
