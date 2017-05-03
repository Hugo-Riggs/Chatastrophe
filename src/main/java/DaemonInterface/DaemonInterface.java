package DaemonInterface;

import akka.actor.ActorRef;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import Chatastrophe.Actors.server.ChatServer;
import Chatastrophe.Protocol.Shutdown;

public class DaemonInterface implements Daemon  {

	private Thread chatastropheServerThread; 
	private boolean stopped = false;
	private boolean lastOneWasATick = false;
	private ActorRef chatServer;
	private Shutdown shutdown;

	@Override
	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
		/*
		* Construct objects and initialize variables here.
		* You can access the command line arguments that would normally be passed to your main() 
		* method as follows:
		*/
		String[] args = daemonContext.getArguments(); 
		chatServer = ChatServer.actor();

		chatastropheServerThread = new Thread(){
			private long lastTick = 0;

			@Override
			public synchronized void start() {
				DaemonInterface.this.stopped = false;
				super.start();
			}

			@Override
			public void run() {            
			}
		};
	}

	@Override
	public void start() throws Exception {
		chatastropheServerThread.start();
	}

	@Override
	public void stop() throws Exception {
		stopped = true;
		chatServer.tell(shutdown, chatServer);
		try{
			chatastropheServerThread.join(1000);
		}catch(InterruptedException e){
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Override
	public void destroy() {
		chatastropheServerThread = null;
	}
}
