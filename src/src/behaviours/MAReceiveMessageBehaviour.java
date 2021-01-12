package behaviours;

// Jade Imports
import jade.core.behaviours.*;

// Jade imports
import jade.lang.acl.ACLMessage;

// Agent imports
import agents.ManagerAgent;

public class MAReceiveMessageBehaviour extends CyclicBehaviour {
		private ManagerAgent myAgent;
		
        public MAReceiveMessageBehaviour(ManagerAgent a) {
			super(a);
			myAgent = a;
		}

		public void action() {
			// Action of the ManagerAgent is get the user input and read the file
			ACLMessage msg = null;
            msg = myAgent.blockingReceive();

			// Print message to check it was received
            // System.out.println(msg.getContent());
			if (msg != null){
				if (msg.getContent().contains("_")){
					if (msg.getContent().split("_").length == 2 || msg.getContent().split("_").length == 3 ) {
						
						String action = msg.getContent().split("_")[0];
						String file = msg.getContent().split("_")[1];
						if (msg.getContent().split("_").length == 3){
							file = msg.getContent().split("_")[1] + "_" +msg.getContent().split("_")[2];
						}
						myAgent.setAction(action);
						myAgent.setFile(file);
						myAgent.performAction();
					} else{
						myAgent.sendMessage("Wrong input.", "user");
						return;
					}
				} else if (msg.getContent().contains(";")){
						if (msg.getContent().contains("Tipper")){
						//System.out.println("Received:  " + msg.getContent());
						int row = Integer.parseInt(msg.getContent().split(";")[1]);
						double tip = Double.parseDouble(msg.getContent().split(";")[2]);
						myAgent.finalResults[row] = myAgent.finalResults[row] + tip;
						myAgent.resultsCount = myAgent.resultsCount + 1;
						//System.out.println("Count " + myAgent.resultsCount);
						if(myAgent.resultsCount == myAgent.resultsexpected){
							//System.out.println("Count in " + myAgent.resultsCount);
							myAgent.produceResults();
						}
					} else{
						// System.out.println("Received:  " + msg.getContent());
						int row = Integer.parseInt(msg.getContent().split(";")[1]);
						double quality = Double.parseDouble(msg.getContent().split(";")[2]);
						myAgent.finalResults[row] = myAgent.finalResults[row] + quality;
						myAgent.resultsCount = myAgent.resultsCount + 1;
						if(myAgent.resultsCount == myAgent.resultsexpected){
							myAgent.produceResults();
						}
					}
				} else{
					myAgent.sendMessage("Wrong input.", "user");
					return;
				}
			} else{
				block();
			}
		}
	}