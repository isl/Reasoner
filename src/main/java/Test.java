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

import isl.reasoner.OntologyReasoner;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author konsolak
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OntologyReasoner ont = new OntologyReasoner();
        ont.initiateModel("http://139.91.183.3/3MEditor/FetchBinFile?file=cidoc_crm_v6.0-draft-2015January.rdfs");
        ont.initiateModel("http://139.91.183.3/3MEditor/FetchBinFile?file=CRMext4SKOSandLabel_v1.2.rdfs");

        System.out.println("===============================================================================");
        ArrayList<String> listClasses = ont.getAllClasses();
        for (String c : listClasses) {
            System.out.println(c);
        }
        System.out.println("===============================================================================");

        ArrayList<String> listProps = ont.listProperties("http://www.cidoc-crm.org/cidoc-crm/E1_CRM_Entity");
        for (String prop : listProps) {
            System.out.println(prop);
        }

        System.out.println("===============================================================================");
        ArrayList<String> listObjects = ont.listObjects("http://www.w3.org/2004/02/skos/core#broader");
        for (String object : listObjects) {
            System.out.println(object);
        }
        //  ont.listObjects("http://www.cidoc-crm.org/cidoc-crm/P8_took_place_on_or_within");
        System.out.println("===============================================================================");
    }

}
