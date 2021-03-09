/**
 * 
 */
package net.rickcee.scm.devops.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rickcee
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ENVIRONMENT")
public class ReleaseEnvironment {

	@EmbeddedId
	@JsonIgnore
	private ReleaseEnvironmentId id;
	
	@Column(insertable = false, updatable = false)
	private String environmentGroup;
	@Column(insertable = false, updatable = false)
	private String environmentId;
	
	private String description;
	private String baseLocation;
	private String nexusUploadUrl;
	private String nexusBrowseUrl;

}
