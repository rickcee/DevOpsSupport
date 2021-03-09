/**
 * 
 */
package net.rickcee.scm.devops.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.rickcee.scm.devops.model.VCSStorage;
import net.rickcee.scm.devops.model.VCSStorageType;

/**
 * @author rickcee
 *
 */
public interface VCSStorageTypeRepo extends JpaRepository<VCSStorageType, Long> {

}
