// All agents extend from the Agent class
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

import java.util.Scanner;

// java -cp lib\jade.jar;src\test jade.Boot -agents user:UserAgent -gui

public class UserAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());

    private class ReadUserInputBehaviour extends CyclicBehaviour {
		/*
		Behaviour that reads the user input via the console/command line

		It asks for a command in the form: {Action}_{file} and pass the message
		directly to the ManagerAgent that will do the heavylifting.

		*/
		private UserAgent myAgent;
		//private boolean complete = false;

		public ReadUserInputBehaviour(UserAgent a) {
			super(a);
			myAgent = a;
		}

		public void action() {
			// Action of the UserAgent is read the UserInput
			Scanner scanner = new Scanner(System.in);
			System.out.println("Please enter your action:");
			String input = scanner.nextLine();
			/*
			String action = input.split("_")[0];
			String file = input.split("_")[1];
			myAgent.setAction(action);
			myAgent.setFile(file);
			*/
			// Now this info needs to be passed to the ManagerAgent
			ACLMessage message = new ACLMessage(ACLMessage.INFORM);
     		message.addReceiver(new AID("manager", AID.ISLOCALNAME));
      		message.setContent(input);
      		myAgent.send(message);
			//this.complete = true; 
			// Validation
			//System.out.println("Action is " + action);
			//System.out.println("File is " + file);
		}

		// The below is only necessary if SimpleBehaviour is used
		/*
		public boolean done(){
			if (complete){
				return true;
			} else{
				return false;
			}
		}
		*/
	}

	private class ReceiveMessageBehaviour extends CyclicBehaviour{
		private UserAgent myAgent;
		
        public ReceiveMessageBehaviour(UserAgent a) {
			super(a);
			myAgent = a;
		}

		public void action() {
			// Action of the ManagerAgent is get the user input and read the file
			ACLMessage msg = null;
            msg = myAgent.blockingReceive();

			// Print message to check it was received
            System.out.println(msg.getContent());
            //msg = myAgent.blockingReceive();
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

		createManagerAgent();

        try {
			DFService.register(this, dfd);
			ReadUserInputBehaviour ReadUserBehaviour = new  ReadUserInputBehaviour(this);
			addBehaviour(ReadUserBehaviour);
			addBehaviour(new ReceiveMessageBehaviour(this));
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent " + getLocalName()+ " - Cannot register with DF", e);
			doDelete();
		}
    }

	/* No need for now
	public void setAction(String act){
		this.action = act;
	} 

	public void setFile(String f){
		this.file = f;
	} 
	*/
	
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