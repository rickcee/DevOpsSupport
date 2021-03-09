/**
 * 
 */
package net.rickcee.scm.devops.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.rickcee.scm.devops.model.VCSStorage;

/**
 * @author rickcee
 *
 */
public interface VCSStorageRepo extends JpaRepository<VCSStorage, Long> {
	
	@Query("SELECT obj FROM VCSStorage obj WHERE obj.type.id = :type")
	List<VCSStorage> getByVCSType(@Param("type") Long type);

}
