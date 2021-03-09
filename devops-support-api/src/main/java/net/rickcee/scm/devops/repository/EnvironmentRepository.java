/**
 * 
 */
package net.rickcee.scm.devops.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.rickcee.scm.devops.model.ReleaseEnvironment;
import net.rickcee.scm.devops.model.ReleaseEnvironmentId;

/**
 * @author rickcee
 *
 */
public interface EnvironmentRepository extends JpaRepository<ReleaseEnvironment, ReleaseEnvironmentId> {

}
