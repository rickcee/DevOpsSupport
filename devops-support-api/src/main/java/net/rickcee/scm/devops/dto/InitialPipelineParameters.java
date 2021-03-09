/**
 * 
 */
package net.rickcee.scm.devops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rickcee
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InitialPipelineParameters {
	private String tagName;
	private String comments;
}
