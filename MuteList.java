/*
 * Created on 3.12.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Betalord
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.util.Vector;

public class MuteList {
	private Vector usernames;
	private Vector mutedUntil; // time (in milliseconds, refers to System.currentTimeMillis()) when it will expire. Expired records are automatically removed in certain methods. Use 0 to mute user for indefinite time.
	private Channel channel;
	
	public MuteList(Channel channel) {
		usernames = new Vector();
		mutedUntil = new Vector();
		this.channel = channel;
	}
	
	private void clearExpiredOnes() {
		// remove any expired records (those with expire time 0 are persistent, we won't remove them):
		for (int i = 0; i < usernames.size(); i++)
			if ((((Long)mutedUntil.get(i)).longValue() <= System.currentTimeMillis()) && (((Long)mutedUntil.get(i)).longValue() != 0)) {
				channel.broadcast("<" + (String)usernames.get(i) + "> has been unmuted (mute expired)");
				usernames.remove(i);
				mutedUntil.remove(i);
				i--;
			}		
	}
	
	public boolean isMuted(String username) {
		clearExpiredOnes();
		
		for (int i = 0; i < usernames.size(); i++)
			if (((String)usernames.get(i)).equals(username)) {
				return true;
			}

		return false;
	}	
	
	// returns false if already on the list. Use "seconds" to tell for how long should user be muted. 
	public boolean mute(String username, int seconds) {
		for (int i = 0; i < usernames.size(); i++)
			if (((String)usernames.get(i)).equals(username)) return false;
			
		usernames.add(username);
		Long until = new Long(0);
		if (seconds != 0) until = new Long(System.currentTimeMillis() + seconds*1000); 
		mutedUntil.add(until);
		return true;	
	}	
	
	// returns false if user is not on the list
	public boolean unmute(String username) {
		for (int i = 0; i < usernames.size(); i++)
			if (((String)usernames.get(i)).equals(username)) {
				usernames.remove(i);
				mutedUntil.remove(i);
				return true;
			}
			
		return false;
	}	
	
	public int size() {
		clearExpiredOnes();
		return usernames.size();
	}
	
	public String getUsername(int index) {
		if (index > usernames.size()-1) return ""; else return (String)usernames.get(index);
	}

	public long getRemainingSeconds(int index) {
		if (index > mutedUntil.size()-1) return -1; 
		if (((Long)mutedUntil.get(index)).longValue() == 0) return 0;
		else return (((Long)mutedUntil.get(index)).longValue() - System.currentTimeMillis()) / 1000; 
	}
	
}
