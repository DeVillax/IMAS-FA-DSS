package behaviours;

// Jade Imports
import jade.core.behaviours.*;

// Jade imports
import jade.lang.acl.ACLMessage;

// Agent imports
import agents.FuzzyAgent;

public class FAReceiveMessageBehaviour extends CyclicBehaviour{
		/*
		Behaviour that handles the recival of messages from other agents

		It was assumed that the userAgent only receives messages from the ManagerAgent
		for notification purposes. As such, it will display the content of these messages
		to the user.

		*/
		private FuzzyAgent myAgent;
		
        public FAReceiveMessageBehaviour(FuzzyAgent a) {
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
			if (msg.getContent().contains("_")){
				//System.out.println(msg.getContent());
				// Evaluate
				if (msg.getContent().contains("tipper")){
					myAgent.EvaluateDataTipper(msg.getContent());
				} else if (msg.getContent().contains("quality")){
					myAgent.EvaluateDataQuality(msg.getContent());
				}
				
			} else {
				String filename = msg.getContent();
				myAgent.setRules(filename);
				myAgent.InitializeFuzzyAgent();
			}
			
			
			
		}
	}