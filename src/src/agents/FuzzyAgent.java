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
public class FuzzyAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    // TODO in the next deliverable: Add variables

	private class ReceiveMessageBehaviour extends CyclicBehaviour{
		/*
		Behaviour that handles the recival of messages from other agents

		It was assumed that the userAgent only receives messages from the ManagerAgent
		for notification purposes. As such, it will display the content of these messages
		to the user.

		*/
		private FuzzyAgent myAgent;
		
        public ReceiveMessageBehaviour(FuzzyAgent a) {
			super(a);
			myAgent = a;
		}

		public void action() {
			// It gets the message and print its content 
			// For this deliverable, we are simply checking that the fuzzy agents receive messages from the manager agent
			// In the next iteration, this will be expanded to set the fcls and start the fuzzy logic
			ACLMessage msg = null;
            msg = myAgent.blockingReceive();
            //System.out.println(msg.getContent()); 
			
		}
	}

    protected void setup(){
        // Method to register with the DF 
        
        DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		sd.setType("FuzzyAgent"); 
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