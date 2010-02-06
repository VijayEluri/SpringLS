/*
 * Created on 2006.10.21
 */

package com.springrts.tasserver;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Note that it is vital that everything here is synchronized with main server thread.
 * Currently dupAccounts list is cloned from original accounts list so we don't have
 * to worry about thread-safety here (this is the easiest way since otherwise we would
 * have to make sure no account is added or removed while we are saving accounts to disk).
 * The second thing for which we must ensure thread-safety is Account.toString() method.
 * The only problem with Account.toString() method is calling MapGradeList.toString()
 * method, which could potentially cause problems (other fields used in Account.toString()
 * are mostly atomic or if they are not it doesn't hurt us really - example is 'long' type,
 * which consists of two 32 bit ints and is thus not atomic, but it won't cause corruption
 * in accounts file as it is used right now). So it is essential to ensure that MapGrading
 * class is thread-safe (or at least its toString() method is).
 *
 * @author Betalord
 */
public class FSSaveAccountsThread extends Thread {

	private static final Log s_log  = LogFactory.getLog(FSSaveAccountsThread.class);

	/**
	 * Duplicated accounts.
	 * Needed to ensure thread safety as well as accounts state consistency
	 */
	private List<Account> dupAccounts;

	public FSSaveAccountsThread(List<Account> dupAccounts) {
		this.dupAccounts = dupAccounts;
	}

	@Override
	public void run() {

		s_log.info("Dumping accounts to disk in a separate thread ...");
		long time = System.currentTimeMillis();

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(TASServer.ACCOUNTS_INFO_FILEPATH)));

			for (int i = 0; i < dupAccounts.size(); i++) {
				out.println(FSAccountsService.toPersistentString(dupAccounts.get(i)));
			}

			out.close();
		} catch (IOException e) {
			s_log.error("Failed writing accounts info to " + TASServer.ACCOUNTS_INFO_FILEPATH + "!", e);

			// add server notification:
			ServerNotification sn = new ServerNotification("Error saving accounts");
			sn.addLine("Serious error: accounts info could not be saved to disk. Exception trace:" + Misc.exceptionToFullString(e));
			ServerNotifications.addNotification(sn);

			return;
		}

		s_log.info(dupAccounts.size() + " accounts information written to " + TASServer.ACCOUNTS_INFO_FILEPATH + " successfully (" + (System.currentTimeMillis() - time) + " ms).");

		// let garbage collector free the duplicate accounts list:
		dupAccounts = null;
	}
}