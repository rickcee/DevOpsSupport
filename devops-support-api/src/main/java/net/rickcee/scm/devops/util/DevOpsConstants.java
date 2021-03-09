/**
 * 
 */
package net.rickcee.scm.devops.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author rickcee
 *
 */
public class DevOpsConstants {
	public static final String BUILD_WORKSPACE = "buildWorkspace";
	public static final String RELEASE_TICKET_ID = "releaseTicketId";
	public static final String RUN_USER = System.getProperty("user.name");
	public static final String RUN_HOST;
	public static final String STATUS_OK = "SUCCESS";
	public static final String STATUS_ERROR = "ERROR";
	public static final String STATUS_APPROVED = "APPROVED";
	public static final String DEV = "dev";
	public static final String UAT = "uat";
	public static final String STAGE = "stage";
	public static final String PROD = "prod";
	public static final int VCS_TYPE_SVN = 1;
	public static final int VCS_TYPE_GIT = 2;

	static {
		InetAddress localMachine = null;
		try {
			localMachine = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {

		}
		if (localMachine != null) {
			RUN_HOST = localMachine.getHostName();
		} else {
			RUN_HOST = "N/A";
		}
	}
}
