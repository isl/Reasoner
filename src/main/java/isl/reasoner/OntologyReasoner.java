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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    private static final HashMap<String, String> langs = new HashMap<String, String>();

    static {
        langs.put(".ttl", "Turtle");
        langs.put(".nt", "N-TRIPLES");
        langs.put(".nq", "N-Quads");
        langs.put(".trig", "TriG");
        langs.put(".rdf", "RDF/XML");
        langs.put(".owl", "RDF/XML");
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
    public ArrayList<String> listProperties(String subject) throws java.lang.Exception, InvalidParameterException, NullPointerException {
        disableLogging();
        ArrayList<String> listProps = new ArrayList();
        if (Character.isWhitespace(subject.charAt(0)) || Character.isWhitespace(subject.charAt(subject.length() - 1))) {
            throw new InvalidParameterException("The subject has Leading/Trailing Whitespaces: \"" + subject + "\"");
        } else if (subject == null) {
            throw new NullPointerException("The subject is null: " + subject);

        }
        OntClass c = modelAll.getOntClass(subject);
        if (c != null) {
            try {
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
            } catch (Exception ex) {
                throw new Exception("Something went wrong: " + ex.getMessage());

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
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("exception subject " + subject);
//        }
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

        String ext = modelNS.substring(modelNS.lastIndexOf("."));
        OntModel model = ModelFactory.createOntologyModel(
                PelletReasonerFactory.THE_SPEC, null);
        // PelletOptions.FREEZE_BUILTIN_NAMESPACES =true;
        //     PelletOptions.IGNORE_UNSUPPORTED_AXIOMS =true;
        try {
            model.setDerivationLogging(false);
            model.read(modelNS, langs.get(ext));
        } catch (com.hp.hpl.jena.shared.JenaException e) {
            //   e.printStackTrace();
            if (e.getMessage().contains("java.io.IOExceptio")) {
                throw new com.hp.hpl.jena.shared.JenaException("Connection refused to connect: " + e.getMessage());
            } else if (e.toString().contains("com.hp.hpl.jena.shared.SyntaxError")) {
                throw new com.hp.hpl.jena.shared.SyntaxError("Wrong file format for extention: " + ext);
            } else {
                throw new com.hp.hpl.jena.shared.JenaException("Error: " + e.getMessage());

            }
        }

        model.prepare();
        OntModel tmp = modelAll;
        modelAll = model;
        modelAll.addSubModel(tmp); //test if with subModel works as with add
        //Reason for this change was that rdfs schema was not loaded with some  schemata. For example skos
        KnowledgeBase kb = ((PelletInfGraph) model.getGraph()).getKB();

        boolean consistent = kb.isConsistent();
        return consistent;
    }

    /**
     * Initiates the ontology and checks the consistency of the model
     *
     * @param modelNS
     * @param extention
     * @return true or false according to the consistency of the model
     */
    public boolean initiateModelUrl(String modelNS, String extention) {
        // read the ontology with its imports
        disableLogging();

        OntModel model = ModelFactory.createOntologyModel(
                PelletReasonerFactory.THE_SPEC, null);
        try {
            model.setDerivationLogging(false);
            model.read(modelNS, langs.get(extention));

        } catch (com.hp.hpl.jena.shared.JenaException e) {
            if (e.getMessage().contains("java.io.IOExceptio")) {
                throw new com.hp.hpl.jena.shared.JenaException("Connection refused to connect: " + e.getMessage());
            } else if (e.toString().contains("com.hp.hpl.jena.shared.SyntaxError")) {
                throw new com.hp.hpl.jena.shared.SyntaxError("Wrong file format for extention: " + extention);
            } else {
                throw new com.hp.hpl.jena.shared.JenaException("Error: " + e.getMessage());
            }
        }

        model.prepare();
        OntModel tmp = modelAll;
        modelAll = model;
        modelAll.addSubModel(tmp); //test if with subModel works as with add
        //Reason for this change was that rdfs schema was not loaded with some  schemata. For example skos
        KnowledgeBase kb = ((PelletInfGraph) model.getGraph()).getKB();
        boolean consistent = kb.isConsistent();
        return consistent;
    }

    /**
     * Initiates the ontology and checks the consistency of the model
     *
     * @param schemaFile
     *
     * @return true or false according to the consistency of the model
     */
    public Map<String, String> initiateModel(File schemaFile) throws FileNotFoundException {
        // read the ontology with its imports
        disableLogging();
        InputStream targetStream = new FileInputStream(schemaFile);
        String filePath = schemaFile.getPath();
        String extention = filePath.substring(filePath.lastIndexOf("."));

        OntModel model = ModelFactory.createOntologyModel(
                PelletReasonerFactory.THE_SPEC, null);
        try {
            model.setDerivationLogging(false);
            model.read(targetStream, null, langs.get(extention));
            NsIterator listNameSpaces = model.listNameSpaces();
                String ns = listNameSpaces.next();
                String prefix = model.getNsURIPrefix(ns);
                nsPrefixMap.put(ns, prefix);
            }
        } catch (com.hp.hpl.jena.shared.JenaException e) {
            if (e.getMessage().contains("java.io.IOExceptio")) {
                throw new com.hp.hpl.jena.shared.JenaException("Connection refused to connect: " + e.getMessage());
            } else if (e.toString().contains("com.hp.hpl.jena.shared.SyntaxError")) {
                throw new com.hp.hpl.jena.shared.SyntaxError("Wrong file format for extention: " + extention);
            } else {
                throw new com.hp.hpl.jena.shared.JenaException("Error: " + e.getMessage());
            }
        }

        model.prepare();
        OntModel tmp = modelAll;
        modelAll = model;
        modelAll.addSubModel(tmp); //test if with subModel works as with add
        //Reason for this change was that rdfs schema was not loaded with some  schemata. For example skos
        //    KnowledgeBase kb = ((PelletInfGraph) model.getGraph()).getKB();
        //    boolean consistent = kb.isConsistent();
        return nsPrefixMap;
    }

    /**
     * Initiates the ontology and checks the consistency of the model
     *
     * @param modelNS
     * @param extention
     * @return true or false according to the consistency of the model
     */
    public boolean initiateModelFileContent(String fileContent, String extention) {
        // read the ontology with its imports
        disableLogging();

        OntModel model = ModelFactory.createOntologyModel(
                PelletReasonerFactory.THE_SPEC, null);
        InputStream in = FileManager.get().open(fileContent);

        try {
            model.setDerivationLogging(false);
            model.read(in, langs.get(extention));

        } catch (com.hp.hpl.jena.shared.JenaException e) {
            if (e.getMessage().contains("java.io.IOExceptio")) {
                throw new com.hp.hpl.jena.shared.JenaException("Connection refused to connect: " + e.getMessage());
            } else if (e.toString().contains("com.hp.hpl.jena.shared.SyntaxError")) {
                throw new com.hp.hpl.jena.shared.SyntaxError("Wrong file format for extention: " + extention);
            } else {
                throw new com.hp.hpl.jena.shared.JenaException("Error: " + e.getMessage());
            }
        }

        model.prepare();
        OntModel tmp = modelAll;
        modelAll = model;
        modelAll.addSubModel(tmp); //test if with subModel works as with add
        //Reason for this change was that rdfs schema was not loaded with some  schemata. For example skos
        KnowledgeBase kb = ((PelletInfGraph) model.getGraph()).getKB();
        boolean consistent = kb.isConsistent();
        return consistent;
    }

    /**
     * Return all classes of the model
     *
     * @return an arrayList with all the classes of the model
     * @throws java.lang.Exception
     */
    public ArrayList<String> getAllClasses() throws Exception {
        disableLogging();
        ArrayList<String> listClasses = new ArrayList();

        try {
            ExtendedIterator<OntClass> listClassesIt = modelAll.listClasses();

            while (listClassesIt.hasNext()) {
                OntClass c = (OntClass) listClassesIt.next();
                if (c.getURI() != null) {
                    listClasses.add(c.getURI());
                }

            }
        } catch (Exception ex) {
            throw new Exception("Something went wrong :" + ex.getMessage());
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
     * @throws java.lang.Exception
     */
    public ArrayList<String> listObjects(String property) throws Exception, InvalidParameterException, NullPointerException, QueryParseException {

        disableLogging();
        ArrayList<String> listObjects = new ArrayList();
        if (Character.isWhitespace(property.charAt(0)) || Character.isWhitespace(property.charAt(property.length() - 1))) {
            throw new InvalidParameterException("The property has Leading/Trailing Whitespaces: \"" + property + "\"");
            throw new NullPointerException("The property is null: " + property);

        }
        //   try {
        //    StmtIterator it = modelAll.listStatements(p, RDFS.range, (RDFNode) null);
//                }
//                //remove super proprties to get the direct range
//                //otherwise we get also the range of the super proprties
                    }

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
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("exception property " + property);
//        }
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
