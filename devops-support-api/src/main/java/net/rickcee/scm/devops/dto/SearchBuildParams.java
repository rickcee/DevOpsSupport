/**
 * 
 */
package net.rickcee.scm.devops.dto;

import java.util.Date;

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
public class SearchBuildParams {
	private Long buildNumber;
	private String buildDescription;
	private Date buildDateFrom;
	private Date buildDateTo;
	private Boolean finalized;
	private String triggeredBy;
	private String approvedBy;
}
