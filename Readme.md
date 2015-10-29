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

 

Reasoner
====

Reasoner is a Java API used by [3MEditor](https://github.com/isl/3MEditor) to fill the the target nodes with the valid subjects,properties and objects.


Build - Run
====
Folder src contain all the files needed to build and create a jar file. This project is a Maven project, providing all the libs in pom.xml.

Usage
====

Basic usage:

 OntologyReasoner ont = new OntologyReasoner();
 
 ont.initiateModel("http://139.91.183.3/3MEditor/FetchBinFile?file=cidoc_crm_v6.0-draft-2015January.rdfs");
 
  ArrayList<String> listClasses = ont.getAllClasses();
 
  ArrayList<String> listProps = ont.listProperties("http://www.cidoc-crm.org/cidoc-crm/E1_CRM_Entity");
  
  ArrayList<String> listObjects = ont.listObjects("http://www.w3.org/2004/02/skos/core#broader");
