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

// Java Imports
import java.util.Scanner;
import java.io.File; 

// XML Parser
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.DocumentBuilder;  
import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Node;  
import org.w3c.dom.Element;  

// Compile: javac -cp lib\jade.jar -d src\output\ src\src\agents\*.java
// Executie: java -cp lib\jade.jar;src\output jade.Boot -agents user:UserAgent -gui

// All agents extend from the Agent class
public class ManagerAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private String action = null;
	private String file = null;
	private int requiredFuzzyAgents = 0;
	private String rules = null;
	private String aggregation = null;
	private int currentFuzzyAgents = 0;


    private class ReceiveMessageBehaviour extends CyclicBehaviour {
		private ManagerAgent myAgent;
		
        public ReceiveMessageBehaviour(ManagerAgent a) {
			super(a);
			myAgent = a;
		}

		public void action() {
			// Action of the ManagerAgent is get the user input and read the file
			ACLMessage msg = null;
            msg = myAgent.blockingReceive();

			// Print message to check it was received
            // System.out.println(msg.getContent());

			if (msg.getContent().contains("_")){
				String action = msg.getContent().split("_")[0];
				String file = msg.getContent().split("_")[1];
            	myAgent.setAction(action);
				myAgent.setFile(file);
			} else{
				myAgent.sendMessage("Wrong input.", "user");
				return;
			}
            
			// Now that we have the message we need to decide what to do depending of the action

			if (action.equals("I") || action.equals("D")){
				if (action.equals("I")){
					// Initialization

					// Read Config file
					myAgent.readFile(file);

					// Based on the config file, we have to create the agents 
					myAgent.createFuzzyAgent();

					// Now we need to pass the fcl files to each Fuzzy Agent
					String ru[] = myAgent.getFuzzySettings().split(",");
					for (int i = 1; i <= myAgent.getFuzzy(); i++){
						myAgent.sendMessage(ru[i-1],"fuzzy"+i);
						//System.out.println("FCL:" + ru[i-1]+"fuzzy"+i);
					}
					
					// Now we need to notify the UserAgent that the Fuzzy Agents are ready.
					myAgent.sendMessage("System ready...","user");
				} else{
					// Evaluation
					// To do in the next deliverable

				}
			} else{
				myAgent.sendMessage("Action not recognized.", "user");
			}
		
		}

	}
	

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
			addBehaviour(new ReceiveMessageBehaviour(this));
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
		ContainerController cc = getContainerController();
		int c = currentFuzzyAgents;
		for (int i = requiredFuzzyAgents; i > c ; i--){
			try{
				AgentController ac = cc.createNewAgent("fuzzy" + Integer.toString(i), "FuzzyAgent", null);
				currentFuzzyAgents = currentFuzzyAgents + 1; 
				try{
					ac.start();
				} catch (StaleProxyException e){
					e.printStackTrace();
				}
			} catch (StaleProxyException e){
				e.printStackTrace();
			}
		}
		//System.out.println(currentFuzzyAgents);		
	}

	private void sendMessage(String msg, String agent){
		// Send messages to Agents
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
     	message.addReceiver(new AID(agent, AID.ISLOCALNAME));
    	message.setContent(msg);
      	this.send(message);	
	}

	private void readFile(String file){
		// Reads the XML file
		try{
			File f = new File("src\\config\\" + file);

			// Checks whether the given file exists
			if (!f.isFile()){
				System.out.println("File not found");
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

					setFuzzy(Integer.parseInt(eElement.getElementsByTagName("fuzzyagents").item(0).getTextContent()));
					setRules(eElement.getElementsByTagName("fuzzySettings").item(0).getTextContent());
					setAggregation(eElement.getElementsByTagName("aggregation").item(0).getTextContent());
				}
			}
		} catch (Exception e) {  
			e.printStackTrace();  
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