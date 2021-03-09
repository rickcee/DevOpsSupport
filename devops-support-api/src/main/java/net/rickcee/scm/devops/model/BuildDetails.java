/**
 * 
 */
package net.rickcee.scm.devops.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import net.rickcee.scm.devops.util.JsonAttributeConverter;

/**
 * @author rickcee
 *
 */
@Data
@Entity
@Table(name = "BUILD_DETAILS")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BuildDetails {
//	@Id
//	private Long buildNumber;
	@EmbeddedId
	@JsonIgnore
	private BuildDetailsId id;
	
	@Column(insertable = false, updatable = false)
	private String jobId;
	@Column(insertable = false, updatable = false)
	private Long buildNumber;
	
	@OneToOne
	private VCSStorage repository;
	
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "BUILD_DETAILS_AUDIT_ENTRIES", joinColumns = { @JoinColumn(name = "job_id", referencedColumnName = "jobId"),
			@JoinColumn(name = "build_number", referencedColumnName = "buildNumber") })
	private Set<AuditEntry> auditEntries;
	
	/* DESCRIPTION entered by the person that triggered the build */
	private String buildDescription;
	/* WHO triggered the build */
	private String modifiedBy;
	/* WHEN was the build triggered */
	private Date modifiedOn;
	/* SERVER that served the request */
	private String modifiedByServer;
	/* LOCATION of workspace on server */
	private String fsLocation;
	/* TAG checked out */
	private String tagName;
	/* FULLY RELEASED */
	private Boolean fullyReleased;
	
	@Lob
	@Column(name="RELEASE_FILES", columnDefinition = "clob")
	@Convert(converter = JsonAttributeConverter.class)
	private List<String> releaseFiles;
	
	@Lob
	@Column(name="NEXUS_FILES", columnDefinition = "clob")
	@Convert(converter = JsonAttributeConverter.class)
	private List<String> nexusFiles;
}
