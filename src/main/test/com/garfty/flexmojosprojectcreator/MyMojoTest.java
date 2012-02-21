package com.garfty.flexmojosprojectcreator;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: garethparker
 * Date: 20/02/2012
 * Time: 17:01
 */
public class MyMojoTest {

    private File effectivePom;
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document XMLDocument;
    private Element XMLRoot;
    private NodeList XMLNodes;

    /*
    * @Before
    *
    * */
    
    @Before
    public void setUp() throws Exception, IOException {

        effectivePom = new File("src/main/test/resources/effective-pom.xml");
        //
        try{
            // Declare a new Document Factory Builder / Document Builder for the XML
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // Parse the XML and normalise it
            XMLDocument = documentBuilder.parse(effectivePom);
            XMLDocument.getDocumentElement().normalize();

            // Get the root XML Node
            XMLRoot = XMLDocument.getDocumentElement();

            // Set the children nodes from the root node
            XMLNodes = XMLRoot.getChildNodes();

        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    /*
    * @After
    *
    * */

    @After
    public void tearDown() throws Exception {

        effectivePom = null;
        //
        documentBuilderFactory = null;
        documentBuilder = null;
        //
        XMLDocument = null;
        XMLRoot = null;
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /*
    * @Test
    *
    * */

    @Test
    public void testWriteActionScriptProperties() throws Exception, IOException {

        // Create .actionScriptProperties in temporary folder
        File actionScriptProperties = tempFolder.newFile(".actionScriptProperties");
        FileWriter fileWriter = null;

        try{
            fileWriter = new FileWriter( actionScriptProperties );
            fileWriter.write("<?xml version='1.0' encoding='UTF-8' standalone='no'?>");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if( fileWriter != null ){
                try{
                    fileWriter.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testWriteProjectProperties() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testWriteFlexLibraryProperties() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testParseBuildElements() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testParsePlugins() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testParseDependencies() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testDependencyExclusion() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testParseResources() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testConditionalCompilerSettings() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testCheckManifestFile() throws Exception {
    }

   /*
   * @Test
   *
   * */

    @Test
    public void testNameSpaceURI() throws Exception {
    }
}