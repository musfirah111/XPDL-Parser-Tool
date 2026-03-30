package bpe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

public class XPDLParser {
	
    public void parseBPMN(String fileName) {
        try {
            File inputFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            int totalEvents = countElementsWithStyle(doc, "bpmn.event"); // correct
            int startEvents = countStartEvents(doc); // correct
            int endEvents = countEndEvents(doc); // correct
            int intermediateEvents = totalEvents - startEvents - endEvents;
            int totalActivities = countElementsWithStyle(doc, "taskMarker"); // correct
            int tasks = totalActivities; // correct
            int userTasks = countElementsWithStyle(doc, "taskMarker=user"); // correct
            int serviceTasks = countElementsWithStyle(doc, "taskMarker=service"); // correct
            int scriptTasks = countElementsWithStyle(doc, "taskMarker=script"); // correct
            int manualTasks = countElementsWithStyle(doc, "taskMarker=manual"); // correct
            int abstractTasks = countElementsWithStyle(doc, "taskMarker=abstract"); // correct
            int subprocess = countElementsWithStyle(doc, "isLoopSub=1"); //correct // Added the subprocess 
            
            if(subprocess > 0 && abstractTasks > 0)//Additional code body because both taskMarker are abstract
            {
            	abstractTasks = abstractTasks - subprocess;
            }
            
            int sendTasks = countElementsWithStyle(doc, "taskMarker=send"); // correct
            int totalGateways = countElementsWithStyle(doc, "bpmn.gateway"); // correct
            int exclusiveGatewaysXOR = countElementsWithStyle(doc, "gwType=exclusive"); // correct
            int parallelGatewaysAND = countElementsWithStyle(doc, "gwType=parallel"); // correct
            //int inclusiveGatewaysOR = countElementsWithStyle(doc, "symbol=general"); // correct //Edited from gwType=inclusive
            int inclusiveGatewaysOR = totalGateways - exclusiveGatewaysXOR - parallelGatewaysAND;
            int totalArtifacts = countArtifacts(doc); // correct
            int dataObjects = countElementsWithStyle(doc, "shape=mxgraph.bpmn.data"); // correct
            int groups = countElementsWithStyle(doc, "rounded=1;arcSize=10;dashed=1"); //Edited //Also works with point[]
            int annotations = countElementsWithStyle(doc, "shape=mxgraph.flowchart.annotation_2"); // correct         
            int associations = countAssociations(doc); // correct      
            int sequenceFlows = countSequenceFlows(doc); // correct      
            int messageFlows = countMessageFlows(doc); // correct         
            int totalConnectingObjects = associations + sequenceFlows + messageFlows; // correct
            int totalSwimlanes = countElementsWithStyle(doc, "swimlane"); // correct
            int Pool = 0;
            int Lane = 0;
            if (totalSwimlanes != 0) { // correct
            	Pool = countElementsWithStyle(doc, "childLayout=stackLayout");     
            	Lane = totalSwimlanes - Pool;	
            }

            System.out.println("BPMN_Model_Elements:");
            System.out.println("Total_Events: " + totalEvents);
            System.out.println("  Start_Events: " + startEvents);
            System.out.println("  Intermediate_Events: " + intermediateEvents);
            System.out.println("  End_Events: " + endEvents);
            System.out.println("Total_Activities: " + totalActivities);
            System.out.println("Tasks: " + tasks);
            System.out.println("  User_Tasks: " + userTasks);
            System.out.println("  Service_Tasks: " + serviceTasks);
            System.out.println("  Script_Tasks: " + scriptTasks);
            System.out.println("  Manual_Tasks: " + manualTasks);
            System.out.println("  Abstract_Tasks: " + abstractTasks);
            System.out.println("  Sub_Process: " + subprocess);
            System.out.println("  Send_Tasks: " + sendTasks);
            System.out.println("Total_Gateways: " + totalGateways);
            System.out.println("  Exclusive_Gateways_XOR: " + exclusiveGatewaysXOR);
            System.out.println("  Parallel_Gateways_AND: " + parallelGatewaysAND);
            System.out.println("  Inclusive_Gateways_OR: " + inclusiveGatewaysOR);
            System.out.println("Total_Artifacts: " + totalArtifacts);
            System.out.println("  Data_Objects: " + dataObjects);
            System.out.println("  Groups: " + groups);
            System.out.println("  Annotations: " + annotations);
            System.out.println("Total_Connecting_Objects: " + totalConnectingObjects);
            System.out.println("  Sequence_Flows: " + sequenceFlows);
            System.out.println("  Message_Flows: " + messageFlows);
            System.out.println("  Associations: " + associations);
            System.out.println("Total_Swimlanes: " + totalSwimlanes);
            System.out.println("  Lanes: " + Lane);
            System.out.println("  Pools: " + Pool);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static int countSequenceFlows(Document doc) {
        int count = 0;
        NodeList mxCellList = doc.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String style = mxCell.getAttribute("style");
            if (style.contains("edgeStyle=elbowEdgeStyle") && style.contains("endArrow=blockThin")) {
                count++;
            }
        }
        return count;
    }
    
    public static int countMessageFlows(Document doc) {
        int count = 0;
        NodeList mxCellList = doc.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String style = mxCell.getAttribute("style");
            if (style.contains("endArrow=blockThin") && style.contains("startArrow=oval")) {
                count++;
            }
        }
        return count;
    }

    public static int countAssociations(Document doc) {
        int count = 0;
        NodeList mxCellList = doc.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String style = mxCell.getAttribute("style");
            if (style.contains("edgeStyle=elbowEdgeStyle") && style.contains("dashed=1")) {
                count++;
            }
        }
        return count;
    }
    
    public static int countArtifacts(Document doc) {
        NodeList mxCellList = doc.getElementsByTagName("mxCell");
        int count = 0;
        for (int temp = 0; temp < mxCellList.getLength(); temp++) {
            Element mxCell = (Element) mxCellList.item(temp);
            String style = mxCell.getAttribute("style");
            if (style != null && (style.contains("shape=mxgraph.bpmn.data") || style.contains("shape=mxgraph.flowchart.annotation_2") || style.contains("rounded=1;arcSize=10;dashed=1"))) {
                count++;
            }
        }
        return count;
    }
    
    public static int countStartEvents(Document doc) {
        NodeList cellList = doc.getElementsByTagName("mxCell");
        int startEventCount = 0;
        for (int i = 0; i < cellList.getLength(); i++) {
            Element cell = (Element) cellList.item(i);
            String style = cell.getAttribute("style");
            if (isStartEvent(style)) {
                startEventCount++;
            }
        }
        return startEventCount;
    }

    public static int countEndEvents(Document doc) {
        NodeList cellList = doc.getElementsByTagName("mxCell");
        int endEventCount = 0;
        for (int i = 0; i < cellList.getLength(); i++) {
            Element cell = (Element) cellList.item(i);
            String style = cell.getAttribute("style");
            if (isEndEvent(style)) {
                endEventCount++;
            }
        }
        return endEventCount;
    }

    public static boolean isStartEvent(String style) {
        return style.contains("shape=mxgraph.bpmn.event") && style.contains("outline=standard");
    }

    public static boolean isEndEvent(String style) {
        return style.contains("shape=mxgraph.bpmn.event") && style.contains("outline=end");
    }
    
    public static int countElementsWithStyle(Document doc, String styleName) {
        NodeList nodeList = doc.getElementsByTagName("mxCell");
        int count = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            if (element.hasAttribute("style") && element.getAttribute("style").contains(styleName)) {
                count++;
            }
        }
        return count;
    }
   
}