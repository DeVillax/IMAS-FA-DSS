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

// Java Imports
import java.util.Scanner;
import java.io.File; 
import java.util.ArrayList;

// XML Parser
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.DocumentBuilder;  
import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Node;  
import org.w3c.dom.Element;  

// FIS Imports
import net.sourceforge.jFuzzyLogic.FIS;

import behaviours.FAReceiveMessageBehaviour;

// Compile: javac -cp lib\jade.jar;lib\jFuzzyLogic.jar -d src\output\ src\src\agents\*.java
// Executie: java -cp lib\jade.jar;lib\jFuzzyLogic.jar;src\output jade.Boot -agents user:UserAgent -gui

// All agents extend from the Agent class
public class FuzzyAgent extends Agent {
	private String rules = null;
	private FIS fis = null;
	private ArrayList<String> queue = new ArrayList<String>();

    private Logger myLogger = Logger.getMyLogger(getClass().getName());

	

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
			addBehaviour(new FAReceiveMessageBehaviour(this));

		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent " + getLocalName()+ " - Cannot register with DF", e);
			doDelete();
		}
    }

	// -------------------------------------- Setters ------------------------------------------
	public void setRules(String fcl){
		this.rules = fcl;
	} 

	public void setFIS(String filename){
		this.fis = FIS.load(filename);
	}
	
	// -------------------------------------- Getters ------------------------------------------
	
	public String getFuzzySettings(){
		return this.rules;
	}

	// -------------------------------------- Other Methods ------------------------------------------

    protected void takeDown(){
        // Method to unregister with the DF 
		try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        super.takeDown();
    }

	public void InitializeFuzzyAgent(){
		// Method to load the fuzzy inference engine
		// the agent will receive the name of the FCL to load
		// fis is the fuzzy inference System
		
		String filename ="src\\fuzzyrules\\" + rules + ".fcl";
		// System.out.println(filename);
		setFIS(filename);

		// Error while loading?
        if( fis == null ) { 
            System.err.println("Can't load file: '" + rules + ".fcl'");
            return;
        }
	}

	public void EvaluateDataTipper(String data){
		String[] content = data.split("_");
		String row = Character.toString(content[1].charAt(content[1].length()-1));
		float service = Float.parseFloat(content[2].split(",")[0]); 
		float food = Float.parseFloat(content[2].split(",")[1]);

		fis.setVariable("service", service);
        fis.setVariable("food", food);

        // Evaluate
        fis.evaluate();

		double tip = fis.getVariable("tip").getLatestDefuzzifiedValue();

		String msg = "Tipper" + ";" + row+ ";" + Double.toString(tip);
		//System.out.println(msg); 
		sendMessage(msg,"manager");
	}

	private void sendMessage(String msg, String agent){
		// Send messages to Agents
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
     	message.addReceiver(new AID(agent, AID.ISLOCALNAME));
    	message.setContent(msg);
      	this.send(message);	
	}

	public void EvaluateDataQuality(String data){
		String[] content = data.split("_");
		String row = Character.toString(content[1].charAt(content[1].length()-1));
		float commitment = Float.parseFloat(content[2].split(",")[0]); 
		float clarity = Float.parseFloat(content[2].split(",")[1]);
		float influence = Float.parseFloat(content[2].split(",")[2]);

		fis.setVariable("commitment", commitment);
        fis.setVariable("clarity", clarity);
		fis.setVariable("influence", influence);
        // Evaluate
        fis.evaluate();

		double quality = fis.getVariable("service_quality").getLatestDefuzzifiedValue();

		String msg = "Quality" + ";" + row+ ";" + Double.toString(quality);
		//System.out.println(msg); 
		sendMessage(msg,"manager");
	}

}