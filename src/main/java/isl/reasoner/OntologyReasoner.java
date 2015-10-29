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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.util.ArrayList;
import java.util.Collections;
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
    

    private OntModel modelAll = ModelFactory.createOntologyModel(
            PelletReasonerFactory.THE_SPEC, null);

    /**
     * Return  all properties that can be applied to instances of this class.
     * @param subject
     * @return An arrayList with the properties that can be applied to the specific subject
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
        
        OntModel model = ModelFactory.createOntologyModel(
                PelletReasonerFactory.THE_SPEC, null);
        try {
            model.setDerivationLogging(false);
            model.read(modelNS);
        } catch (com.hp.hpl.jena.shared.JenaException e) {
            e.printStackTrace();
            System.out.println("Connection refused!");
            // e.printStackTrace();
        }

        model.prepare();
        modelAll = (OntModel) modelAll.add(model);
        KnowledgeBase kb = ((PelletInfGraph) model.getGraph()).getKB();
        boolean consistent = kb.isConsistent();
        
        return consistent;
    }

    /**
     * Return all classes of the model
     * @return an arrayList with all the classes of the model
     */
    public ArrayList<String> getAllClasses() {
        disableLogging();
        ArrayList<String> listClasses = new ArrayList();

        ExtendedIterator<OntClass> listClassesIt = modelAll.listClasses();
        while (listClassesIt.hasNext()) {
            OntClass c = (OntClass) listClassesIt.next();
            if (c.getURI() != null) {
                listClasses.add(c.getURI());
            }
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
     * Return  all objects that can be applied to a specific property
     * @param property 
     * @return An arrayList with the objects that can be applied to the specific property
     */
    public ArrayList<String> listObjects(String property) {
        disableLogging();
        ArrayList<String> listObjects = new ArrayList();
        Property p = modelAll.getProperty(property);
        StmtIterator it = modelAll.listStatements(p, RDFS.range, (RDFNode) null);
        if (p != null) {
            while (it.hasNext()) {
                RDFNode node = (RDFNode) it.next().getObject();
                listObjects.add(node.toString());
                OntClass objectClass = modelAll.getOntClass(node.toString());
                if (objectClass != null) {
                    ExtendedIterator<OntClass> listSubClasses = objectClass.listSubClasses();
                    while (listSubClasses.hasNext()) {
                        OntClass subClass = listSubClasses.next();
                        listObjects.add(subClass.toString());
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
