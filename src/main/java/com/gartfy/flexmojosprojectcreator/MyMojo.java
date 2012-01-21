package com.gartfy.flexmojosprojectcreator;

/*
* Copyright 2001-2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
* Goal which initialises the project
* 
* @goal initialiseProject
*/
public class MyMojo extends AbstractMojo {

  /**
  * The Project Location.
  *
  * @parameter expression="${initialiseProject.projectLocation}"
  * default-value=""
  */
  
  private String projectLocation;
  /**
  * The Workspace Location.
  *
  * @parameter expression="${initialiseProject.workspaceLocation}"
  * default-value=""
  */
  private String workspaceLocation;
  
  /**
  * The Project Source Directory.
  */
  private String sourceDirectory;
  
  /**
  * The Project Test Source Directory.
  */
  private String testSourceDirectory;
  
  /**
  * The Project Resource Directories.
  */
  private ArrayList<String> resourceDirectories;
  
  /**
  * System HOME Environment string
  */
  private String homeDirectory;
  
  /**
  * local repository path
  */
  private String localRepositoryPath;
  
  /**
  * Main application file
  */
  private String mainApplicationFile = "Empty.as";
  
  /**
  * Set if the project is a library project
  */
  private Boolean isLibraryProject = false;

  public void execute() throws MojoExecutionException {

    // Add the following slash to the project location
    projectLocation = addFollowingSlash(projectLocation);

    // Get the effective pom and parse the dependencies
    File effectivePom = new File(projectLocation + "target/classes/effective-pom.xml");

    // Set the homdirectory from the system variable
    homeDirectory = System.getenv("HOME");

    try {

      // Declare a new Document Factory Builder / Document Builder for the XML
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

      // Parse the XML and normalise it
      Document XMLDoc = dBuilder.parse(effectivePom);
      XMLDoc.getDocumentElement().normalize();

      // Get the root XML Node
      Element root = XMLDoc.getDocumentElement();

      // Set the children nodes from the root node
      NodeList nodes = root.getChildNodes();

      // Loop through the Child Nodes to find required elements
      for (int i = 0; i < nodes.getLength(); i++) {

        Node node = nodes.item(i);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
          if ("build".equals(node.getNodeName().toLowerCase())) {
            parseBuildElements(node);
          } else if ("dependencies".equals(node.getNodeName().toLowerCase())) {
            parseDependencies(node);
          } else if ("properties".equals(node.getNodeName().toLowerCase())) {
            libraryProjectCheck(node);
          }
        }
      }

      // Write the main application file
      File dir = new File(projectLocation + sourceDirectory);
      if (dir.isDirectory()) {
        File[] files = dir.listFiles();
        for (File f : files) {
          if (f.getName().contains(".as")) {
            mainApplicationFile = f.getName();
          }
        }
      }

    } catch (SAXParseException err) {
      getLog().error("// XML Parsing Error: " + err.getLineNumber() + ", uri: " + err.getSystemId() + "\n// Error Message:\n\n" + err.getMessage());
    } catch (SAXException e) {

      Exception x = e.getException();

      if (x == null) {       
        getLog().error( e.getMessage() );
      } else {
        getLog().error( x.getMessage() );
      }

    } catch (Throwable t) {
      getLog().error( t.getMessage() );
    }
  }  
  
  /**
  * Parse the Build node of the effective POM
  */
  private void parseBuildElements(Node buildNode) {

    // Set the children nodes from the buildNode node
    NodeList nodes = buildNode.getChildNodes();

    // Loop through the Child Nodes to find required elements
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);

      if (node.getNodeType() == Node.ELEMENT_NODE) {

        if ("sourceDirectory".equals(node.getNodeName())) {
          sourceDirectory = node.getFirstChild().getNodeValue();
        } else if ("sourceDirectory".equals(node.getNodeName())) {
          testSourceDirectory = node.getFirstChild().getNodeValue();
        } else if ("resources".equals(node.getNodeName())) {

          NodeList resourceNodes = node.getChildNodes();

          for (int a = 0; a < resourceNodes.getLength(); a++) {
            parseResourceElement(resourceNodes.item(a));
          }
        }
      }
    }
  }

  /**
  * Parse the dependencies
  */
  private void parseDependencies(Node dependenciesNode) {

    String artifactID = "";
    String version = "";
    String groupID = "";
    String scope = "";
    String attachmentSourcePath = "";

    // Set the children nodes from the dependenciesNode
    NodeList nodes = dependenciesNode.getChildNodes();

    // Loop through the child nodes to find the required elements
    for (int i = 0; i < nodes.getLength(); i++) {

      Node node = nodes.item(i);

      if (node.getNodeType() == Node.ELEMENT_NODE) {

        NodeList dependencyNodes = node.getChildNodes();

        for (int a = 0; a < dependencyNodes.getLength(); a++) {
          Node dependencyNode = dependencyNodes.item(a);

          if (dependencyNode.getNodeType() == Node.ELEMENT_NODE) {
            if ("artifactId".equals(dependencyNode.getNodeName())) {
              artifactID = dependencyNode.getFirstChild().getNodeValue();
            } else if ("groupId".equals(dependencyNode.getNodeName())) {
              groupID = dependencyNode.getFirstChild().getNodeValue();
            } else if ("version".equals(dependencyNode.getNodeName())) {
              version = dependencyNode.getFirstChild().getNodeValue();
            } else if ("scope".equals(dependencyNode.getNodeName())) {
              scope = dependencyNode.getFirstChild().getNodeValue();
            }

            // Run the check to see if the dependency is to be excluded
            // Set the exclude flag to determine if the dependency is needed
            Boolean exclude = checkDependencyExclusion(groupID);

            if (!exclude) {
              localRepositoryPath = homeDirectory + "/.m2/repository/"
                      + convertToPath(groupID) + artifactID + "/" + version + "/"
                      + artifactID + "-" + version + ".swc";
            }

            // Check if the scope is internal, and add it to the Includes ArrayList
            if ("internal".equals(scope)) {
              if (mIncludes == null) {
                mIncludes = new ArrayList<String>();
              }

              mIncludes.add(localRepositoryPath);
            }
          }
        }

        File dir = new File(projectLocation + "../.metadata/.plugins/org.eclipse.core.resources/.projects/" + artifactID);

        if (dir.isDirectory()) {
          attachmentSourcePath = "${DOCUMENTS}/" + artifactID + "/" + sourceDirectory;
          mAttachments.add("<sourceAttachmentPathEntry kind='3' linkType='1' path='"
                  + localRepositoryPath + "' sourcePath='" + attachmentSourcePath + "' useDefaultLinkType='false' />");
        }

        if (mLibraries == null) {
          mLibraries = new ArrayList<String>();
        }

        mLibraries.add("<libraryPathEntry kind='3' linkType='1' path='" + localRepositoryPath
                + "' sourcePath='" + attachmentSourcePath + "' useDefaultLinkType='false' />");
      }
    }
  }

  /**
  * Check if the dependency is meant to be excluded
  */
  private Boolean checkDependencyExclusion(String value) {

    Boolean exclude = false;

    for (int i = 0; i < mExcludes.size(); i++) {

      Boolean stringComparison = value.equals(mExcludes.get(i));

      if (stringComparison == true) {
        exclude = true;
        break;
      }
    }
    //
    return exclude;
  }

  /**
  * Add the following slash to any given string
  */
  private String addFollowingSlash(String path) {

    if (!"/".equals(Character.toString(path.charAt(path.length() - 1)))) {
      return path + "/";
    } else {
      return path;
    }
  }

  /**
  *
  */
  private String convertToPath(String value) {
    return value.replace(".", "/");
  }

  /**
  *
  */
  private Boolean libraryProjectCheck(Node propertiesNode) {

    NodeList nodes = propertiesNode.getChildNodes();

    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);

      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if ("library".equals(node.getNodeName()) && "true".equals(node.getFirstChild().getNodeValue())) {
          isLibraryProject = true;
        } else {
          isLibraryProject = false;
        }
      }
    }
    return isLibraryProject;
  }

  /**
  * Parse the resource path directory
  */
  private void parseResourceElement(Node resourceNode) {

    NodeList resourceNodes = resourceNode.getChildNodes();

    String[] splits;
    String loc = "";

    for (int i = 0; i < resourceNodes.getLength(); i++) {
      Node directoryNode = resourceNodes.item(i);
      if (directoryNode.getNodeType() == Node.ELEMENT_NODE && "directory".equals(directoryNode.getNodeName())) {
        splits = directoryNode.getFirstChild().getNodeValue().split(projectLocation);
        if (splits.length > 1) {
          for (String s : splits) {
            loc += s;
          }
        } else {
          splits = directoryNode.getFirstChild().getNodeValue().split(workspaceLocation);
          if (splits.length > 1) {
            loc = "../";
            for (String s : splits) {
              loc += s;
            }
          }
        }
      }
    }

    if (resourceDirectories == null) {
      resourceDirectories = new ArrayList<String>();
    }
    resourceDirectories.add("<compilerSourcePathEntry kind='1' linkType='1' path='" + loc + "' />");
  }
  
  /**
  * The Libraries ArrayList
  *
  * @parameter property="libraries"
  */
  private ArrayList<String> mLibraries;

  public void setLibraries(ArrayList<String> libraries) {
    mLibraries = libraries;
  }
  
  /**
  * The Excludes ArrayList
  *
  * @parameter property="excludes"
  */
  private ArrayList<String> mExcludes;

  public void setExcludes(ArrayList<String> excludes) {
    mExcludes = excludes;
  }
  
  /**
  * The Includes ArrayList
  *
  * @parameter property="includes"
  */
  private ArrayList<String> mIncludes;

  public void setIncludes(ArrayList<String> includes) {
    mIncludes = includes;
  }
  
  /**
  * The Attachments ArrayList
  *
  * @parameter property="attachments"
  */
  private ArrayList<String> mAttachments;

  public void setAttachments(ArrayList<String> attachments) {
    mAttachments = attachments;
  }
}