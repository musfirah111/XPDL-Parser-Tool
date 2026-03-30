import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main_Function {

	public static void main(String[] args) {
		try {
			TotalCycleTime cycleTime = new TotalCycleTime();
			
			//String Existing_XML_File = "HARD.xml"; // Path to the existing XML file.
			String Existing_XML_File = "DoubleParallelGates.xml"; // Path to the existing XML file.
			//String Existing_XML_File = "SingleParallel.xml"; // Path to the existing XML file.
			//String Existing_XML_File = "DoubleInclusive.xml"; // Path to the existing XML file.
			//String Existing_XML_File = "SingleExclusive.xml"; // Path to the existing XML file.
	        String updated_XML_File = "Updated.xml"; // Path for the new modified XML file.
	        
	        // Load the existing XML document.
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document existingDoc = dBuilder.parse(new File(Existing_XML_File));
	        existingDoc.getDocumentElement().normalize();
	
	        // Create a new XML document called newDoc.
	        Document newDoc = dBuilder.newDocument();
	        Node copiedNode = newDoc.importNode(existingDoc.getDocumentElement(), true);
	        newDoc.appendChild(copiedNode);
	
	        // Make changes to the new document_ add durations to each task.
	        Element rootElement = newDoc.getDocumentElement();
	        NodeList mxCellList = rootElement.getElementsByTagName("mxCell");
	        for (int i = 0; i < mxCellList.getLength(); i++) 
			{
	            Element mxCell = (Element) mxCellList.item(i);
	            String style = mxCell.getAttribute("style");
	            if (style != null && style.contains("taskMarker")) 
				{
	                // Add a duration attribute to task cells.
	                int duration = TotalCycleTime.generateRandomDuration();
	                mxCell.setAttribute("duration", String.valueOf(duration));
	                mxCell.setAttribute("isTraversed", "false");
	            }
	        }
	        
	       System.out.println("------------------------------------------");
	       TotalCycleTime.assignProbabilitesToSequenceFlows(newDoc); 
	       TotalCycleTime.displayGatewayInfo(newDoc);
	       System.out.println("------------------------------------------");
	       
	        
	        
	        Element rootElement1 = newDoc.getDocumentElement();
	        NodeList mxCellList1 = rootElement.getElementsByTagName("mxCell");
	        for (int i = 0; i < mxCellList1.getLength(); i++) {
	            Element mxCell = (Element) mxCellList1.item(i);
	            String style = mxCell.getAttribute("style");
	            if (style != null && style.contains("gateway")) {
	                // Add isTraversed attribute to gateway cells.
	                mxCell.setAttribute("isTraversed", "false");
	            }
	        }        
	
	        // Save the modified XML document to a file.
	        TotalCycleTime.SaveUpdateXML(newDoc, updated_XML_File);
	
	        System.out.println("New Updated xml file created.");
	
	        // Load the updated XML document.
	        Document updatedDoc = dBuilder.parse(new File(updated_XML_File));
	        updatedDoc.getDocumentElement().normalize();
	
	        // Display tasks and their durations.
	        TotalCycleTime.displayTasksAndDurations(updatedDoc);	       

            // Find the start event.
            Element startEvent = TotalCycleTime.findStartEvent(newDoc);
            String taskId = startEvent.getAttribute("id");
            String taskName = startEvent.getAttribute("value");
            System.out.println("Task ID: " + taskId + ", Name: " + taskName);
            
            double tct = 0;
            // Read tasks in sequence starting from the start event.
            if (startEvent != null) {
                System.out.println("Tasks in sequence:");
                tct = TotalCycleTime.readTasks(newDoc, startEvent);
            } 
            
            System.out.println("");
            System.out.println("Sequential cycle time: " + tct + " minutes.");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
