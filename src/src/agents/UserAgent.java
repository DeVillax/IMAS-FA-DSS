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

// Compile: javac -cp lib\jade.jar -d src\output\ src\src\agents\*.java
// Execute: java -cp lib\jade.jar;src\output jade.Boot -agents user:UserAgent -gui


// All agents extend from the Agent class
public class UserAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private boolean pendingAction = false;
	private String userInput = null;

    private class ReadUserInputBehaviour extends CyclicBehaviour {
		/*
		Behaviour that reads the user input via the console/command line

		It asks for a command in the form: {Action}_{file} and pass the message
		directly to the ManagerAgent that will do the heavylifting.

		*/
		private UserAgent myAgent;

		public ReadUserInputBehaviour(UserAgent a) {
			super(a);
			myAgent = a;
		}

		public void action() {
			// Action of the UserAgent is read the UserInput
			Scanner scanner = new Scanner(System.in);
			System.out.println("Please enter your action:");
			String input = scanner.nextLine();
			
			// Now this info needs to be passed to the ManagerAgent
			myAgent.setPendingAction(true);
			myAgent.setUserInput(input);
			
		}
	}

	enum State{
		IDLE,
		WAITING
	}

	private class ReceiveMessageBehaviour extends CyclicBehaviour{
		/*
		Behaviour that handles the recival of messages from other agents

		It was assumed that the userAgent only receives messages from the ManagerAgent
		for notification purposes. As such, it will display the content of these messages
		to the user.

		*/
		private UserAgent myAgent;
		private State state; 
		
        public ReceiveMessageBehaviour(UserAgent a) {
			super(a);
			myAgent = a;
			state = State.IDLE;
		}

		public void action() {
			switch(state){
				case IDLE:
					if (myAgent.getPendingAction()){
						String msg = myAgent.getUserInput();
						state = State.WAITING;
						myAgent.sendMessage(msg,"manager");
					}
				case WAITING:
					ACLMessage msg = null;
            		msg = myAgent.blockingReceive();
					state = State.IDLE;
					myAgent.setPendingAction(false);
					System.out.println(msg.getContent());
			} 
		}
	}
	

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
			addBehaviour(new ReceiveMessageBehaviour(this));
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
			AgentController ac = cc.createNewAgent("manager", "ManagerAgent", null);
			try{
				ac.start();
			} catch (StaleProxyException e){
				e.printStackTrace();
			}
		} catch (StaleProxyException e){
			e.printStackTrace();
		}	
	}

	private void sendMessage(String msg, String agent){
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