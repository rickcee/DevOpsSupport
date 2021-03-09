/**
 * 
 */
package net.rickcee.scm.devops.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.rickcee.scm.devops.model.VCSPermission;

/**
 * @author rickcee
 *
 */
public interface VCSPermissionRepo extends JpaRepository<VCSPermission, String> {

}
