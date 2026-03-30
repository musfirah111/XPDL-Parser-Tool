import java.io.File;
import java.util.Random;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TotalCycleTime {
	// Function to generate random number between 1 and 15 to assign them as durations.
	public static int generateRandomDuration() {
    	int min = 5;
    	int max = 15;
    	int randomNumber = (int) (Math.random() * (max - min + 1) + min);
        return  randomNumber;
    }
	
	// Function to save the updated XML document to a file.
    public static void SaveUpdateXML(Document document, String filePath) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }
    
    // Function to display tasks and their durations.
    public static void displayTasksAndDurations(Document document) {
        System.out.println("Tasks and Durations:");
        NodeList mxCellList = document.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String style = mxCell.getAttribute("style");
            if (style != null && style.contains("taskMarker")) {
                String taskId = mxCell.getAttribute("id");
                String taskName = mxCell.getAttribute("value");
                String durationStr = mxCell.getAttribute("duration");
                int duration = Integer.parseInt(durationStr);
                String isTraversed = mxCell.getAttribute("isTraversed");
                System.out.println("Task ID: " + taskId + ", Name: " + taskName + ", Duration: " + duration + " minutes" + ", isTraversed: " + isTraversed);
            }
        }
    }
    
    public static void printGatewayTraversalStatus(Document doc) 
    {
        Element rootElement = doc.getDocumentElement();
        NodeList mxCellList = rootElement.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) 
        {
            Element mxCell = (Element) mxCellList.item(i);
            String style = mxCell.getAttribute("style");
            if (style != null && style.contains("gateway")) 
            {
                // Check if isTraversed attribute exists and print its value.
                String isTraversed = mxCell.getAttribute("isTraversed");
                if (!isTraversed.isEmpty()) 
                {
                    System.out.println("Gateway ID: " + mxCell.getAttribute("id") + ", isTraversed: " + isTraversed);
                }
            }
        }
    }
    
    public static void calculateSequentialCT(Document newDoc) {
    	int sumOfSequentialDurations = 0;
    	Element rootElement = newDoc.getDocumentElement();
        NodeList mxCellList = rootElement.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String style = mxCell.getAttribute("style");

            // Check if the style contains "taskMarker". It is a task if it does.
            if (style != null && style.contains("taskMarker")) {
            	String durationStr = mxCell.getAttribute("duration");
            	String taskId = mxCell.getAttribute("id");
                String taskName = mxCell.getAttribute("value");
                String durationString = mxCell.getAttribute("duration");
                int duration = Integer.parseInt(durationStr);
                String isTraversed = mxCell.getAttribute("isTraversed");
            	sumOfSequentialDurations += Integer.parseInt(durationStr);
            	
            	//System.out.println("Task ID: " + taskId + ", Name: " + taskName + ", Duration: " + duration + " minutes" + ", isTraversed: " + isTraversed);
            }              
        }
    }
    
    // Method to find the start event.
    public static Element findStartEvent(Document newDoc) {
        NodeList mxCellList = newDoc.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String style = mxCell.getAttribute("style");
            String value = mxCell.getAttribute("value");
            // Check if the element is a start event.
            if (style != null && style.contains("shape=mxgraph.bpmn.event") && style.contains("outline=standard")) {
            	System.out.println("----- Start Event Found -----");
                return mxCell;
            }
        }
        return null;
    }
    
    public static boolean isParallelGateway(Element gateway) {
    	String style = gateway.getAttribute("style");
    	return style.contains("gwType=parallel");
    }
    
    public static boolean isInclusiveGateway(Element gateway) {
    	String style = gateway.getAttribute("style");
    	return style.contains("bpmn.gateway") && !style.contains("gwType=parallel") && !style.contains("gwType=exclusive");
    }
    
    public static boolean isExclusiveGateway(Element gateway) {
    	String style = gateway.getAttribute("style");
    	return style.contains("gwType=exclusive");
    }
    
    // Method to read tasks in sequence.
    public static double readTasks(Document doc, Element currentElement) {               
        // Print task details.
    	double totalDuration = 0;
    	double totalDuration1 = 0;
        String taskId = currentElement.getAttribute("id");
        String taskName = currentElement.getAttribute("value");
        String durationStr = currentElement.getAttribute("duration");
        if (!durationStr.isEmpty()) {
            int duration = Integer.parseInt(durationStr);
            //System.out.println("Duration within Read Tasks: " + duration);
            totalDuration += duration; // Add duration to total.
        }
        //System.out.println("Task ID: " + taskId + ", Name: " + taskName + ", Duration: " + durationStr);

        // Find outgoing edges.
        NodeList outgoingEdges = doc.getElementsByTagName("mxCell");
        // Loop through outgoing edges to filter based on current element as the source.
        for (int i = 0; i < outgoingEdges.getLength(); i++) {
            Element edge = (Element) outgoingEdges.item(i);
            String edgeSource = edge.getAttribute("source");
            
            // Check if the current edge originates from the current element.
            if (edgeSource != null && edgeSource.equals(currentElement.getAttribute("id"))) 
            {
                // Recursive call to process next task.
                String edgeTarget = edge.getAttribute("target");
                Element nextElement = findElementById(currentElement.getOwnerDocument(), edgeTarget);
                if (nextElement != null && !isGateway(nextElement)) 
                {
                    // Recursively read tasks from the next element and accumulate duration.
                	//System.out.println(" READ TASKS FUNCTION TASK ID: " + nextElement.getAttribute("id") + ", isTraversed: " + nextElement.getAttribute("isTraversed"));	
                		
                		
                		nextElement.setAttribute("isTraversed", "True");
                		//System.out.println("Total Durartion (if): " + totalDuration);
                		totalDuration += readTasks(doc, nextElement);
                		totalDuration1 += readTasks(doc, nextElement);
                }
                else if (isGateway(nextElement) && (isParallelGateway(nextElement))){
            		if(!Boolean.parseBoolean(nextElement.getAttribute("isTraversed")))
            		{
            			nextElement.setAttribute("isTraversed", "true");
            			int ParallelReturned = calculateCTWithParallel1(doc, nextElement);  
            			//System.out.println("----->>>>> Parallel Duration: " + ParallelReturned);
            			totalDuration += ParallelReturned;  
            			totalDuration1 += ParallelReturned;
            		}
                }
                else if (isGateway(nextElement) && isInclusiveGateway(nextElement)) {
                	if(!Boolean.parseBoolean(nextElement.getAttribute("isTraversed")))
            		{
            			nextElement.setAttribute("isTraversed", "true");
            			int ParallelReturned = calculateCTWithParallel1(doc, nextElement);  
            			//System.out.println("----->>>>> Inclusive Duration (One Path): " + ParallelReturned);
            			totalDuration += ParallelReturned;               	              	
            		}
                	                	
                	double inclusiveDuration = getEdge(doc, nextElement);
                	totalDuration1 += inclusiveDuration;
                	//System.out.println("----->>>>> Inclusive Duration (All Paths): " + inclusiveDuration);
                	
                	
                }
                else if (isGateway(nextElement) && isExclusiveGateway(nextElement)) {
                	double inclusiveDuration = getEdge(doc, nextElement);
                	//System.out.println("----->>>>> Exclusive Duration (All Paths): " + inclusiveDuration);
                	totalDuration += inclusiveDuration;
                	totalDuration1 += inclusiveDuration;
                	//System.out.println("----->>>>> Exclusive Duration (TOTAL): " + totalDuration);
                }
            }
        }
        
        System.out.println("INCLUSIVE -- EXCLUSIVE: " + totalDuration);
        System.out.println("INCLUSIVE -- PARALLEL: " + totalDuration1);
        
        //printGatewayTraversalStatus(doc);
        
        System.out.println();
        // Return total duration for this path.
        return totalDuration;
    }
    
    // Function to check is the gateway is split (one incoming and multiple outgoing flows).
    public static boolean isSplitGateway(Document doc, Element gatewayElement) {	
        // Count incoming and outgoing edges.
        int incomingCount = 0;
        int outgoingCount = 0;

        // Get the ID of the gateway element.
        String gatewayId = gatewayElement.getAttribute("id");
        //System.out.println(gatewayId);

        // Find all edges in the document.
        NodeList mxCellList = doc.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) 
        {
            Element mxCell = (Element) mxCellList.item(i);
            String edgeSource = mxCell.getAttribute("source");
            String edgeTarget = mxCell.getAttribute("target");

            // Check if the edge is connected to the gateway.
            if (edgeSource.equals(gatewayId)) 
            {
                outgoingCount++;
            } 
            else if (edgeTarget.equals(gatewayId)) 
            {
                incomingCount++;
            }
        }
        // Check if it is a split gateway.
        return incomingCount == 1 && outgoingCount > 1;
    }
    
    // Function to check is the gateway is join (multiple incoming and one outgoing flow).
    public static boolean isJoinGateway(Document doc, Element gatewayElement) {
    	 // Count incoming and outgoing edges.
        int incomingCount = 0;
        int outgoingCount = 0;

        // Get the ID of the gateway element.
        String gatewayId = gatewayElement.getAttribute("id");

        // Find all edges in the document.
        NodeList mxCellList = doc.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String edgeSource = mxCell.getAttribute("source");
            String edgeTarget = mxCell.getAttribute("target");

            // Check if the edge is connected to the gateway.
            if (edgeSource.equals(gatewayId)) {
                outgoingCount++;
            } else if (edgeTarget.equals(gatewayId)) {
                incomingCount++;
            }
        }    
        // Check if it is a join gateway
        return incomingCount > 1 && outgoingCount == 1;
    }    
 
	private static int calculateCTWithParallel1(Document document, Element gatewayElement) 
    {
	    int maxPathDuration = 0;

	    NodeList outgoingEdges = document.getElementsByTagName("mxCell");
	    for (int i = 0; i < outgoingEdges.getLength(); i++) 
        {
	        Element edge = (Element) outgoingEdges.item(i);
	        String edgeSource = edge.getAttribute("source");
	        if (edgeSource != null && edgeSource.equals(gatewayElement.getAttribute("id"))) 
            {
	            // Get the target vertex of the edge.
	            String edgeTargetId = edge.getAttribute("target");
	            Element targetVertex = findElementById(document, edgeTargetId);
	            if (targetVertex != null && !isGateway(targetVertex)) 
                {
	                // If the target vertex is not a gateway, calculate its duration.
	                int pathDuration = calculatePathDuration(document, edge);
	                if (pathDuration > maxPathDuration) {
	                    maxPathDuration = pathDuration;
	                }
	            }
	        }
	    }
	    return maxPathDuration;
	}
	
	// Function to get edge to multiply probabilities.
	public static double getEdge(Document document, Element nextElement) {
		//System.out.println("getEdge()");
		double totalDuration = 0;
		NodeList outgoingEdges = document.getElementsByTagName("mxCell");
	    for (int i = 0; i < outgoingEdges.getLength(); i++) {
	        Element edge = (Element) outgoingEdges.item(i);
	        String edgeSource = edge.getAttribute("source");
	        if (edgeSource != null && edgeSource.equals(nextElement.getAttribute("id"))) {
	            // Get the target vertex of the edge.
	            String edgeTargetId = edge.getAttribute("target");
	            Element targetVertex = findElementById(document, edgeTargetId);
	            if (targetVertex != null && !isGateway(targetVertex)) {
	                // If the target vertex is not a gateway, calculate its duration.
	            	totalDuration += calculatePathDurationForInclusive(document, edge);
	            }
	        }
	    }
	    return totalDuration;
	}

    private static int calculatePathDuration(Document document, Element edge) 
    {
        int pathDuration = 0;
        // Traverse the target vertex and its children recursively.
        Element targetVertex = getTargetVertex(document, edge.getAttribute("target"));
        
        String targetVertexId = targetVertex.getAttribute("id"); // Get the ID of the target vertex.
        //System.out.print("Target Vertex within the gateways : " + targetVertexId + " " + targetVertex.getAttribute("value"));
        pathDuration = traverseForDuration(targetVertex, 0, document);  
        return pathDuration;
    }

    private static Element getTargetVertex(Document document, String targetId) {
    	
        NodeList mxCellList = document.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String targetVertexId = mxCell.getAttribute("id"); // Get the ID of the target vertex.
            if (mxCell.getAttribute("id").equals(targetId)) {
                return mxCell;
            }
        }
        return null;
    }
    
    private static int traverseForDuration(Element targetVertex, int currentDuration, Document doc) {
        // Consider the duration of the current vertex.
        String vertexDurationStr = targetVertex.getAttribute("duration");
        int vertexDuration = 0;
        if (vertexDurationStr != null && !vertexDurationStr.isEmpty()) {
            vertexDuration = Integer.parseInt(vertexDurationStr);
        }

        NodeList mxCellList = targetVertex.getOwnerDocument().getElementsByTagName("mxCell");
        int maxChildDuration = 0;
        boolean hasOutgoingEdges = false;
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String edgeSource = mxCell.getAttribute("source");
            String edgeTarget = mxCell.getAttribute("target");
            // Check if the edge is connected to the current vertex.
            if (edgeSource.equals(targetVertex.getAttribute("id"))) {
                hasOutgoingEdges = true;
                Element nextVertex = findElementById(targetVertex.getOwnerDocument(), edgeTarget);
                if (nextVertex != null) {
                    int childDuration = traverseForDuration(nextVertex, currentDuration + vertexDuration, doc);
                    if (childDuration > maxChildDuration) {
                        maxChildDuration = childDuration;
                    }
                }
            }
        }
        
        
        // If the vertex is an end event or has no outgoing edges, return the vertex duration.
        if (!hasOutgoingEdges || isEndEvent(targetVertex)) 
        {
            return vertexDuration + currentDuration;
        } 
        else if (isSplitGateway(doc, targetVertex)) 
        {
        	 //System.out.println(" Traverse for duration FUNCTION Gateway ID: " +  targetVertex.getAttribute("id") + ", isTraversed: " +  targetVertex.getAttribute("isTraversed"));
        	
        	if(!Boolean.parseBoolean(targetVertex.getAttribute("isTraversed")))
    		{
    			targetVertex.setAttribute("isTraversed", "true");
    			//System.out.println(" Traverse for duration FUNCTION Gateway ID: " +  targetVertex.getAttribute("id") + ", isTraversed: " +  targetVertex.getAttribute("isTraversed"));
        	
        	
	        	//System.out.println("Split Gateway: " + targetVertex.getAttribute("id"));
	            // If the current vertex is a split gateway within a parallel path, repeat the calculation for each branch.
	            int splitGatewayDuration = 0;
	            for (int i = 0; i < mxCellList.getLength(); i++) {
	                Element mxCell = (Element) mxCellList.item(i);
	                String edgeSource = mxCell.getAttribute("source");
	                String edgeTarget = mxCell.getAttribute("target");
	                // Check if the edge originates from the current split gateway.
	                if (edgeSource.equals(targetVertex.getAttribute("id"))) {
	                    Element nextVertex = findElementById(targetVertex.getOwnerDocument(), edgeTarget);
	                    if (nextVertex != null) {
	                        int branchDuration = traverseForDuration(nextVertex, currentDuration, doc);
	                        //System.out.println("Branch Duration: " + branchDuration);
	                        
	                        splitGatewayDuration = Math.max(splitGatewayDuration, branchDuration);
	                    }
	                }
	            }
	            return splitGatewayDuration;
            
    		}
    
        }
        
        // Return the maximum of child duration and current duration.
        return Math.max(maxChildDuration, currentDuration);
        
       
    }

    private static boolean isEndEvent(Element vertex) {
        // Check if the vertex is an end event
        String type = vertex.getAttribute("type");
        return type != null && type.equals("EndEvent");
    }
    
    // Function to check if element is a gateway.
    public static boolean isGateway(Element element) {
        String style = element.getAttribute("style");
        return style.contains("bpmn.gateway");
    }


    // Method to find an element by its ID.
    private static Element findElementById(Document document, String id) {
        NodeList mxCellList = document.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            if (mxCell.getAttribute("id").equals(id)) {
                return mxCell;
            }
        }
        return null;
    }
    
    public static void assignProbabilitesToSequenceFlows(Document document) {
        NodeList gatewayNodes = document.getElementsByTagName("mxCell");

        for (int i = 0; i < gatewayNodes.getLength(); i++) {
            Element gateway = (Element) gatewayNodes.item(i);

            // Check if the element represents a gateway
            if (isGateway(gateway)) {
            	
                // Check if the gateway is a split gateway
                if (isSplitGateway(document, gateway)) {
                	
                    // Assign probabilities to outgoing edges.
                    assignProbabilitiesToOutgoingEdges(gateway, document);
                }
            }
        }
    }

    private static void assignProbabilitiesToOutgoingEdges(Element gateway, Document document) {
        // Find all mxCell elements representing edges in the document
        NodeList edgeNodes = document.getElementsByTagName("mxCell");

        // Initialize a counter for outgoing edges of the gateway
        int totalOutgoingEdges = 0;

        // Count the total outgoing edges of the gateway
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            Element edge = (Element) edgeNodes.item(i);
            String edgeSource = edge.getAttribute("source");

            // Check if the edge originates from the gateway
            if (gateway.getAttribute("id").equals(edgeSource)) {
                totalOutgoingEdges++;
            }
        }

        // Calculate probabilities for each outgoing edge
        double[] probabilities = calculateProbabilities(totalOutgoingEdges);

        // Assign probabilities to outgoing edges
        int probabilityIndex = 0;
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            Element edge = (Element) edgeNodes.item(i);
            String edgeSource = edge.getAttribute("source");

            // Check if the edge originates from the gateway
            if (gateway.getAttribute("id").equals(edgeSource)) {
                // Assign probability to the outgoing edge
                edge.setAttribute("probability", String.valueOf(probabilities[probabilityIndex]));
                probabilityIndex++;
            }
        }
    }


    private static double[] calculateProbabilities(int totalEdges) {
        // Initialize an array to store probabilities
        double[] probabilities = new double[totalEdges];
        // Generate random probabilities that sum up to 1
        double sum = 0;
        for (int i = 0; i < totalEdges - 1; i++) {
            probabilities[i] = Math.random() * (1 - sum);
            sum += probabilities[i];
        }
        // Ensure the sum of probabilities equals 1
        probabilities[totalEdges - 1] = 1 - sum;
        return probabilities;
    }


    public static void displayGatewayInfo(Document document) {
        // Get all mxCell elements representing edges
        NodeList edgeNodes = document.getElementsByTagName("mxCell");

        // Iterate through edge nodes
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            Element edge = (Element) edgeNodes.item(i);

            // Check if the element represents an edge
            String edgeStyle = edge.getAttribute("style");
            if (edgeStyle != null && edgeStyle.contains("edge")) {
                // Check if the edge is an outgoing edge from a gateway
                String sourceId = edge.getAttribute("source");
                String targetId = edge.getAttribute("target");
                Element sourceElement = findElementById(document, sourceId);
                Element targetElement = findElementById(document, targetId);

                // Check if the source element is a gateway
                if (sourceElement != null && isGateway(sourceElement)) {
                    // Print gateway ID and name
                    String gatewayId = sourceElement.getAttribute("id");
                    String gatewayName = sourceElement.getAttribute("value");
                    System.out.println("Gateway ID: " + gatewayId);
                    System.out.println("Gateway Name: " + gatewayName);

                    // Print outgoing edge ID and probability if the target is not a gateway
                    if (targetElement != null && !isGateway(targetElement)) {
                        String edgeId = edge.getAttribute("id");
                        String probability = edge.getAttribute("probability");
                        System.out.println("  Outgoing Edge ID: " + edgeId + ", Probability: " + probability);
                    }
                }
            }
        }
    }
    
    private static double calculatePathDurationForInclusive(Document document, Element edge) {
        double pathDuration = 0;
        // Get the probability of the edge
        double probability = Double.parseDouble(edge.getAttribute("probability"));

        // Traverse the target vertex and its children recursively.
        Element targetVertex = getTargetVertex(document, edge.getAttribute("target"));
        
        String targetVertexId = targetVertex.getAttribute("id");
        //System.out.print("Target Vertex within the gateways : " + targetVertexId + " " + targetVertex.getAttribute("value"));

        // Recursively calculate the duration of the branch
        pathDuration = traverseForDurationForInclusive(targetVertex, 0, document) * probability;

        return pathDuration;
    }

    private static int traverseForDurationForInclusive(Element targetVertex, int currentDuration, Document doc) {
        // Consider the duration of the current vertex.
        String vertexDurationStr = targetVertex.getAttribute("duration");
        int vertexDuration = 0;
        if (vertexDurationStr != null && !vertexDurationStr.isEmpty()) {
            vertexDuration = Integer.parseInt(vertexDurationStr);
        }

        NodeList mxCellList = targetVertex.getOwnerDocument().getElementsByTagName("mxCell");
        int maxChildDuration = 0;
        boolean hasOutgoingEdges = false;
        for (int i = 0; i < mxCellList.getLength(); i++) {
            Element mxCell = (Element) mxCellList.item(i);
            String edgeSource = mxCell.getAttribute("source");
            String edgeTarget = mxCell.getAttribute("target");
            // Check if the edge is connected to the current vertex.
            if (edgeSource.equals(targetVertex.getAttribute("id"))) {
                hasOutgoingEdges = true;
                Element nextVertex = findElementById(targetVertex.getOwnerDocument(), edgeTarget);
                if (nextVertex != null) {
                    // Recursively calculate duration for the next vertex and its children
                    int childDuration = traverseForDuration(nextVertex, currentDuration + vertexDuration, doc);
                    if (childDuration > maxChildDuration) {
                        maxChildDuration = childDuration;
                    }
                }
            }
        }

        // If the vertex is an end event or has no outgoing edges, return the vertex duration.
        if (!hasOutgoingEdges || isEndEvent(targetVertex)) {
            return vertexDuration + currentDuration;
        } else if (isSplitGateway(doc, targetVertex)) {
            // If the current vertex is a split gateway, consider the maximum duration of its branches.
            return maxChildDuration;
        }

        // Return the maximum of child duration and current duration.
        return Math.max(maxChildDuration, currentDuration);
    }


}



























