/**
 * 
 */
package net.rickcee.scm.devops.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author rickcee
 *
 */
@Entity
@Table(name = "PERMISSION")
@Data
public class VCSPermission {

	@Id
	private String username;
	private String fullName;
	@Column(nullable = true)
	private Boolean disabled;
	@OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	private List<VCSStorage> repositories;

}
