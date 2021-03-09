/**
 * 
 */
package net.rickcee.scm.devops.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author rickcee
 *
 */
@Embeddable
@Data
@ToString(exclude = { "build" })
@AllArgsConstructor
@NoArgsConstructor
public class AuditEntryId implements Serializable {

	private static final long serialVersionUID = -8271369566985960542L;
	
	@ManyToOne
	@JsonIgnore
	private BuildDetails build;
	private String environment;
}
