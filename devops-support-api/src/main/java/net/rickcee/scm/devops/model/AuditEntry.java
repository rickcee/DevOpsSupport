/**
 * 
 */
package net.rickcee.scm.devops.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Data;
import net.rickcee.scm.devops.util.DevOpsConstants;

/**
 * @author rickcee
 *
 */
@Data
@Entity
@Table(name = "BUILD_AUDIT")
public class AuditEntry implements Comparable<AuditEntry> {
	@Id
	@GeneratedValue
	public Integer id;
	private String environment;
	private String status;
	private String approver;
	private String releaseTicketId;
	private String exceptionMsg;
	private String modifiedBy;
	private String modifiedByHost;
	private Date modifiedOn;
	
	@PrePersist
	public void prePersist() {
		this.modifiedOn = new Date();
		this.modifiedBy = DevOpsConstants.RUN_USER;
		this.modifiedByHost = DevOpsConstants.RUN_HOST;
	}

	@Override
	public int compareTo(AuditEntry o) {
		return this.modifiedOn.compareTo(o.getModifiedOn());
	}

}
