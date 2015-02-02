package network.broadcast;

import java.util.ArrayList;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;

public class BroadcastMessageThread extends Thread{
	
	private BroadcastHandler broadcastHandler;
	private ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
	private long[] timeLastMsg;
	
	public BroadcastMessageThread(BroadcastHandler broadcastHandler, ArrayList<BroadcastMessage> broadcastMessages) {
		this.broadcastHandler = broadcastHandler;
		this.broadcastMessages = broadcastMessages;
		timeLastMsg = new long[broadcastMessages.size()];
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				
				long minToWait = Long.MAX_VALUE;
				
				for(int i = 0 ; i < broadcastMessages.size() ; i++) {
					BroadcastMessage msg = broadcastMessages.get(i);
					
					long timeUntilMessage = System.currentTimeMillis() - timeLastMsg[i] - msg.getUpdateTimeInMiliseconds();
					
					if(timeUntilMessage >= 0) {
						broadcastHandler.sendMessage(msg.encode());
						timeLastMsg[i] = System.currentTimeMillis();
					} else {
						minToWait = Math.min(timeUntilMessage, minToWait);
					}
				}
				
				Thread.sleep(minToWait);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
