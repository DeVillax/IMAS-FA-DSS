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

// Compile: javac -cp lib\jade.jar -d src\test\ src\src\agents\*.java
// Executie: java -cp lib\jade.jar;src\test jade.Boot -agents pa:UserAgent -gui

// All agents extend from the Agent class
public class ManagerAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private String action = null;
	private String file = null;
	private int fuzzy = 0;
	private String rules = null;
	private String aggregation = null;


    private class ReadConfigFileBehaviour extends CyclicBehaviour {
		private ManagerAgent myAgent;
		
        public ReadConfigFileBehaviour(ManagerAgent a) {
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
				ACLMessage message = new ACLMessage(ACLMessage.INFORM);
     			message.addReceiver(new AID("user", AID.ISLOCALNAME));
      			message.setContent("Wrong input");
      			myAgent.send(message);
				return;
			}
            
			// Now that we have the message we need to decide what to do depending of the action

			if (action.equals("I") || action.equals("D")){
				if (action.equals("I")){
					// Initialization

					// Read Config file
					readFile(file);

					// Based on the config file, we have to create the agents 
					myAgent.createFuzzyAgent();
					
					// Now we need to notify the UserAgent that the Fuzzy Agents are ready.
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
					message.addReceiver(new AID("user", AID.ISLOCALNAME));
					message.setContent("System ready...");
					myAgent.send(message);
				} else{
					// Evaluation
					// To do in the next deliverable

				}
			} else{
				ACLMessage message = new ACLMessage(ACLMessage.INFORM);
     			message.addReceiver(new AID("user", AID.ISLOCALNAME));
      			message.setContent("Action not recognized.");
      			myAgent.send(message);
			}
		
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

						myAgent.setFuzzy(Integer.parseInt(eElement.getElementsByTagName("fuzzyagents").item(0).getTextContent()));
						myAgent.setRules(eElement.getElementsByTagName("fuzzySettings").item(0).getTextContent());
						myAgent.setAggregation(eElement.getElementsByTagName("aggregation").item(0).getTextContent());
					}
				}
			} catch (Exception e) {  
				e.printStackTrace();  
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
			ReadConfigFileBehaviour ReadConfigBehaviour = new ReadConfigFileBehaviour(this);
			addBehaviour(ReadConfigBehaviour);
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent " + getLocalName()+ " - Cannot register with DF", e);
			doDelete();
		}
    }

	private void createFuzzyAgent(){
		// Dynamically creates the Fuzzy Agent
		ContainerController cc = getContainerController();
		for (int i = 0; i < fuzzy; i++){
			try{
				AgentController ac = cc.createNewAgent("fuzzy" + Integer.toString(i+1), "FuzzyAgent", null);
				try{
					ac.start();
				} catch (StaleProxyException e){
					e.printStackTrace();
				}
			} catch (StaleProxyException e){
				e.printStackTrace();
			}
		}		
	}

	public void setAction(String act){
		this.action = act;
	} 

	public void setFile(String f){
		this.file = f;
	}

	public void setFuzzy(int num){
		this.fuzzy = num;
	}

	public void setRules(String fcl){
		this.rules = fcl;
	}

	public void setAggregation(String ag){
		this.aggregation = ag;
	}

	public int getFuzzy(){
		return this.fuzzy;
	}

	public String getAction(){
		return this.action;
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