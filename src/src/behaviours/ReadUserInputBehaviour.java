package behaviours;

// Jade Imports
import jade.core.behaviours.*;


// Java imports
import java.util.Scanner;

// Agent imports
import agents.UserAgent;


public class ReadUserInputBehaviour extends CyclicBehaviour {
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