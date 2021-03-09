/**
 * 
 */
package net.rickcee.scm.devops.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@Table(name = "REPOSITORY")
public class VCSStorage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.EAGER)
	private VCSStorageType type;
	private String url;
	private String description;
	@Column(nullable = true)
	private Boolean disabled = false;
	private String extAlias;
	private String fsLocation;
	private String tagBase;
	private String environmentGroup;
	private String groupEmailAddress;

}
