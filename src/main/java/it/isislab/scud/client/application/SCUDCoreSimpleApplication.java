package it.isislab.scud.client.application;



import it.isislab.scud.core.engine.hadoop.sshclient.connection.HadoopFileSystemManager;
import it.isislab.scud.core.engine.hadoop.sshclient.connection.ScudManager;
import it.isislab.scud.core.engine.hadoop.sshclient.utils.environment.EnvironmentSession;
import it.isislab.scud.core.engine.hadoop.sshclient.utils.simulation.Simulation;
import it.isislab.scud.core.engine.hadoop.sshclient.utils.simulation.Simulations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;


public class SCUDCoreSimpleApplication {

	public static int PORT=22;
	public static String host= "127.0.0.1";
	public static String pstring="password";
	public static String bindir="/isis/hadoop-2.4.0";  
	public static String homedir="/isis/"; 
	public static String javabindir ="/usr/local/java/bin/";
	public static String name="isis";
	public static String scudhomedir="/";

	public static  String toolkit="netlogo";
	public static String simulation_name="aids";
	public static String domain_pathname="examples-sim-aids/domain.xml";
	public static String bashCommandForRunnableFunction="/usr/local/java/bin/java";
	public static String output_description_filename="examples-sim-aids//output.xml";
	public static String executable_selection_function_filename="examples-sim-aids/selection.jar";
	public static String executable_rating_function_filename="examples-sim-aids/evaluate.jar";
	public static String description_simulation="this a simple simulation optimization process for AIDS NetLogo simulation example";
	public static String executable_simulation_filename="examples-sim-aids/aids.nlogo";

	/**
	 * @param args
	 * @throws SftpException 
	 */

	public static EnvironmentSession session;

	public static void main(String[] args) throws SftpException{

		Simulations sims=null;
		try {

			ScudManager.setFileSystem(bindir,System.getProperty("user.dir"), scudhomedir, homedir, javabindir ,name);
			if ((session=ScudManager.connect(name, host, pstring, bindir,PORT,
					new FileInputStream(System.getProperty("user.dir")+File.separator+"scud-resources"+File.separator+"SCUD.jar"),
					new FileInputStream(System.getProperty("user.dir")+File.separator+"scud-resources"+File.separator+"SCUD-RUNNER.jar")
					))!=null)
			{
				System.out.println("Connected. Type \"help\", \"usage <command>\" or \"license\" for more information.");

			}else{
				System.err.println("Login Correct but there are several problems in the hadoop environment, please contact your hadoop admin.");
				System.exit(-1);
			}
		} catch (Exception e) {
			System.err.println("Login Error. Check your credentials and ip:port of your server and try again .. ");

		}
		//CREATE SIMULATION FROM EXAMPLE IN SO MODE
		try {
			ScudManager.makeSimulationFolderForLoop(session, toolkit, simulation_name, domain_pathname, bashCommandForRunnableFunction, output_description_filename, 
					executable_selection_function_filename, executable_rating_function_filename, description_simulation, executable_simulation_filename);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("SIMULATION AVAILABLE LIST: ");
		sims = ScudManager.getSimulationsData(session);
		if(sims == null){
			System.err.println("No such simulations.");
		}
		System.out.println("******************************************************");
		
		for(int i=1; i<=sims.getSimulations().size(); i++){
			int simID= i-1;
			Simulation s = sims.getSimulations().get(simID);
			System.err.println("sim-id:"+i+") name: "+s.getName()+" state: "+s.getState()+" time: "+s.getCreationTime()+" id: "+s.getId()+"\n");
		}

		System.out.println("******************************************************");

		System.out.println("Submit the simulation with sim-id "+(sims.getSimulations().size()));
		sims = ScudManager.getSimulationsData(session);
		
		
		Simulation s = sims.getSimulations().get(sims.getSimulations().size()-1);
		if(s == null){
			System.err.println("No such simulation with ID "+sims.getSimulations().size());
			System.exit(-1);
		}

		ScudManager.runAsynchronousSimulation(session,s);

		System.out.println("Waiting for simulation ends.");
		Simulation sim=null;
		
		
		do{
			sims = ScudManager.getSimulationsData(session);
			sim = sims.getSimulations().get(sims.getSimulations().size()-1);
			
			
		}while(!(sim.getState().equals(Simulation.FINISHED)));
		System.exit(0);

	}
}
