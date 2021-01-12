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


// Java imports
import java.util.Scanner;

// Behaviours
import behaviours.ReadUserInputBehaviour;
import behaviours.UserReceiveMessageBehaviour;

// Compile: javac -cp lib\jade.jar -d src\output\ src\src\agents\*.java
// Execute: java -cp lib\jade.jar;src\output jade.Boot -agents user:UserAgent -gui


// All agents extend from the Agent class
public class UserAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private boolean pendingAction = false;
	private String userInput = null;

    protected void setup(){
        // Method to register with the DF 
        DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		sd.setType("UserAgent"); 
		sd.setName(getName());
		sd.setOwnership("IMAS_course");
		dfd.setName(getAID());
		dfd.addServices(sd);

		// Creates Manager Agent dynamically
		createManagerAgent();

        try {
			DFService.register(this, dfd);
			addBehaviour(new ReadUserInputBehaviour(this));
			addBehaviour(new UserReceiveMessageBehaviour(this));
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent " + getLocalName()+ " - Cannot be registered with DF", e);
			doDelete();
		}
    }

	// Setters
	public void setPendingAction(boolean x){
		this.pendingAction = x;
	}

	public void setUserInput(String input){
		this.userInput = input;
	}

	// Getters

	public boolean getPendingAction(){
		return this.pendingAction;
	}

	public String getUserInput(){
		return this.userInput;
	}

	// Other methods
	
	private void createManagerAgent(){
		// Dynamically creates the Manager Agent
		ContainerController cc = getContainerController();
		try{
			AgentController ac = cc.createNewAgent("manager", "agents.ManagerAgent", null);
			try{
				ac.start();
			} catch (StaleProxyException e){
				e.printStackTrace();
			}
		} catch (StaleProxyException e){
			e.printStackTrace();
		}	
	}

	public void sendMessage(String msg, String agent){
		// Send messages to other agents
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
     	message.addReceiver(new AID(agent, AID.ISLOCALNAME));
    	message.setContent(msg);
      	this.send(message);	
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