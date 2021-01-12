package behaviours;

// Jade Imports
import jade.core.behaviours.*;


// Java imports
import java.util.Scanner;
import jade.lang.acl.ACLMessage;

// Agent imports
import agents.UserAgent;


public class UserReceiveMessageBehaviour extends CyclicBehaviour{
    /*
    Behaviour that handles the recival of messages from other agents

    It was assumed that the userAgent only receives messages from the ManagerAgent
    for notification purposes. As such, it will display the content of these messages
    to the user.

    */
    enum State{
		IDLE,
		WAITING
	}
    private UserAgent myAgent;
    private State state;
     
    
    public UserReceiveMessageBehaviour(UserAgent a) {
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