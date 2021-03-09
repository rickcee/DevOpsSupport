/**
 * 
 */
package net.rickcee.scm.devops.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rickcee
 *
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseEnvironmentId implements Serializable {

	private static final long serialVersionUID = -2796416835873845189L;
	
	private String environmentGroup;
	private String environmentId;
	
}
