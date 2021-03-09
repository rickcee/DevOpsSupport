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
public class BuildDetailsId implements Serializable {

	private static final long serialVersionUID = -82561540747014256L;
	
	private String jobId;
	private Long buildNumber;
	
}
