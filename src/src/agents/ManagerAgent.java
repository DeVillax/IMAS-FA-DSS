package agents;

// Jade Imports
import jade.core.Agent;
import jade.util.Logger;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.domain.JADEAgentManagement.KillAgent;

// Java Imports
import java.util.Scanner;
import java.io.File; 
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

// XML Parser
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.DocumentBuilder;  
import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Node;  
import org.w3c.dom.Element;  

import behaviours.MAReceiveMessageBehaviour;

// Compile: javac -cp lib\jade.jar;lib\jFuzzyLogic.jar -d src\output\ src\src\agents\*.java src\src\behaviours\*.java
// Executie: java -cp lib\jade.jar;lib\jFuzzyLogic.jar;src\output jade.Boot -agents user:agents.UserAgent -gui

// All agents extend from the Agent class
public class ManagerAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private String action = null;
	private String file = null;
	private int requiredFuzzyAgents = 0;
	private String rules = null;
	private String aggregation = null;
	private String application = null;
	private ArrayList<String> evaluationData = null;
	private int currentFuzzyAgents = 0;
	public double finalResults[] = null;
	public int resultsCount = 0;
	public int resultsexpected = 0;
	private int difference = 1;
	Map<String, ArrayList<String>> dictionary = null;

    protected void setup(){
        // Method to register with the DF 
        
        DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		sd.setType("ManagerAgent"); 
		sd.setName(getName());
		sd.setOwnership("IMAS_course");
		dfd.setName(getAID());
		dfd.addServices(sd);

        try {
			DFService.register(this, dfd);
			addBehaviour(new MAReceiveMessageBehaviour(this));
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent " + getLocalName()+ " - Cannot register with DF", e);
			doDelete();
		}
    }

	// -------------------------------------- Setters ------------------------------------------
	
	public void setAction(String act){
		this.action = act;
	} 

	public void setFile(String f){
		this.file = f;
	}

	public void setFuzzy(int num){
		this.requiredFuzzyAgents = num;
	}

	public void setRules(String fcl){
		this.rules = fcl;
	}

	public void setAggregation(String ag){
		this.aggregation = ag;
	}

	public void setApplication(String app){
		this.application = app;
	}

	public void setEvaluationData(ArrayList<String>  data){
		this.evaluationData = data;
	}

	public void setFinalResults(int rows){
		this.finalResults = new double[rows];
	}

	public void setExpectedResults(int expected){
		this.resultsexpected = expected;
	}

	// -------------------------------------- Getters ------------------------------------------
	public int getFuzzy(){
		return this.requiredFuzzyAgents;
	}

	public String getAction(){
		return this.action;
	}

	public String getFuzzySettings(){
		return this.rules;
	}
	
	// -------------------------------------- Other Methods ------------------------------------------

	private void createFuzzyAgent(){
		// Dynamically creates the Fuzzy Agent
		if (currentFuzzyAgents == 0){
			System.out.println("Creating FA for domain " + application);
			dictionary = new HashMap<String,ArrayList<String>>();
			ContainerController cc = getContainerController();
			int c = currentFuzzyAgents;
			ArrayList<String> agentResults = new ArrayList<String>();
			for (int i = requiredFuzzyAgents; i > c ; i--){
				try{
					AgentController ac = cc.createNewAgent("fuzzy" + Integer.toString(i), "agents.FuzzyAgent", null);
					currentFuzzyAgents = currentFuzzyAgents + 1; 
					agentResults.add("fuzzy" + Integer.toString(i));
					try{
						ac.start();
					} catch (StaleProxyException e){
						e.printStackTrace();
					}
				} catch (StaleProxyException e){
					e.printStackTrace();
				}
			}
			dictionary.put(application,agentResults);
			//System.out.println(dictionary);
		} else {
			System.out.println("Creating FA for domain " + application);
			int c = currentFuzzyAgents;
			difference = requiredFuzzyAgents - currentFuzzyAgents;
			ContainerController cc = getContainerController();
			ArrayList<String> agentResults = new ArrayList<String>();
			for (int i = requiredFuzzyAgents; i > c ; i--){
				try{
					AgentController ac = cc.createNewAgent("fuzzy" + Integer.toString(i), "agents.FuzzyAgent", null);
					currentFuzzyAgents = currentFuzzyAgents + 1; 
					agentResults.add("fuzzy" + Integer.toString(i));
					try{
						ac.start();
					} catch (StaleProxyException e){
						e.printStackTrace();
					}
				} catch (StaleProxyException e){
					e.printStackTrace();
				}
			}
			dictionary.put(application,agentResults);
		}
		
	}

	public void sendMessage(String msg, String agent){
		// Send messages to Agents
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
     	message.addReceiver(new AID(agent, AID.ISLOCALNAME));
    	message.setContent(msg);
      	this.send(message);	
	}

	public String readDataFile(String file){
		// Reads the Data file
		String app = null;
		try {
      		File f = new File("src\\data\\" + file);

			if (!f.isFile()){
				sendMessage("File not found.", "user");
				return app;
			}

			Scanner reader = new Scanner(f);
			ArrayList<String> results = new ArrayList<String>();
			
			while (reader.hasNextLine()) {
				String data = reader.nextLine();
				if(data.contains(",")){
					results.add(data);
				} else{
					app = data;
				}
      		}
			setFinalResults(results.size());
			setEvaluationData(results);
			setExpectedResults(results.size());
      		reader.close();
			
    	} catch (FileNotFoundException e) {
      		System.out.println("An error occurred.");
     		 e.printStackTrace();
    	}
		return app;
	}

	private void readXMLFile(String file){
		// Reads the XML file
		try{
			File f = new File("src\\config\\" + file);

			// Checks whether the given file exists
			if (!f.isFile()){
				sendMessage("File not found.", "user");
				//System.out.println("File not found");
				return;
			}
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
			DocumentBuilder db = dbf.newDocumentBuilder();  
			Document doc = db.parse(f);  
			
			doc.getDocumentElement().normalize();  
			NodeList nodeList = doc.getElementsByTagName("SimulationSettings");  
			
			for (int i = 0; i < nodeList.getLength(); i++){  
				Node node = nodeList.item(i);  
				
				if (node.getNodeType() == Node.ELEMENT_NODE){  
					Element eElement = (Element) node;  

					setApplication(eElement.getElementsByTagName("application").item(0).getTextContent());
					if (currentFuzzyAgents== 0){
						setFuzzy(Integer.parseInt(eElement.getElementsByTagName("fuzzyagents").item(0).getTextContent()));
					} else{
						requiredFuzzyAgents = requiredFuzzyAgents + Integer.parseInt(eElement.getElementsByTagName("fuzzyagents").item(0).getTextContent());
					}
					
					setRules(eElement.getElementsByTagName("fuzzySettings").item(0).getTextContent());
					setAggregation(eElement.getElementsByTagName("aggregation").item(0).getTextContent());
				}
			}
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}	

	public void produceResults(){
		try {
			//System.out.println("Here");
			String filename = ".\\src\\results\\" + application + "_results.txt";
			File f = new File(filename);
			f.createNewFile();
			FileWriter myWriter = new FileWriter(filename);
			myWriter.write("Domain: " + application +"\n");
			for(int i=1; i <= finalResults.length;i++){
				// As the model only uses average I have performed this operation directly here
				// by simply dividing the sum of the outcomes per row by the number of agents in 
				// that domain
				myWriter.write("Row " + i + " : Decision is " + finalResults[i-1]/dictionary.get(application).size() + "\n");
			}	
	
			resultsCount = 0;
			resultsexpected = 0;
			
			myWriter.close();
			sendMessage("Completed! Results can be found at " + filename, "user");
    	} catch (IOException e) {
      		System.out.println("An error occurred.");
      		e.printStackTrace();
    	}


		
	}

	public void performAction(){
		// Now that we have the message we need to decide what to do depending of the action

		if (action.equals("I") || action.equals("D")){
			if (action.equals("I")){
				// Initialization

				// Read Config file
				readXMLFile(file);

				if (getFuzzySettings() == null){
					return;
				}

				// Based on the config file, we have to create the agents 
				createFuzzyAgent();

				// Now we need to pass the fcl files to each Fuzzy Agent
				String ru[] = getFuzzySettings().split(",");
				int size = dictionary.get(application).size();
				for (int i = 0; i < size; i++){
					sendMessage(ru[i],dictionary.get(application).get(size-i-1));
					System.out.println("FCL " + ru[i]+ " sent to " + dictionary.get(application).get(size-i-1));
				}
				
				sendMessage("System ready...","user");
			} else{
				// Action D
				String d = readDataFile(file);
				application = d;

				if(d != null){
					if (!dictionary.containsKey(d)){
						sendMessage("The data belong to a domain that has not been initialized.", "user");
						return;
					}
					resultsexpected = resultsexpected * dictionary.get(d).size();
					for (int j = 0; j < evaluationData.size(); j++){
						for (int i = 0; i < dictionary.get(d).size(); i++){
							sendMessage(d+"_row"+j+"_"+evaluationData.get(j),dictionary.get(d).get(i));
						}
					}
				} else{
					return;
				}
				
			}
		} else{
			sendMessage("Action not recognized.", "user");
		}
	}

    protected void takeDown(){
        // Method to unregister with the DF 
		try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        super.takeDown();
    }


}