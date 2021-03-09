/**
 * 
 */
package net.rickcee.scm.devops.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.rickcee.scm.devops.model.BuildDetails;
import net.rickcee.scm.devops.model.BuildDetailsId;
import net.rickcee.scm.devops.util.DevOpsConstants;

/**
 * @author rickcee
 *
 */
public interface BuildDetailsRepository extends JpaRepository<BuildDetails, BuildDetailsId> {

	@Query("SELECT obj FROM BuildDetails obj WHERE obj.buildNumber = :buildNumber")
	List<BuildDetails> getByBuildNumber(@Param("buildNumber") Long buildNumber);

	@Query("SELECT obj FROM BuildDetails obj WHERE obj.modifiedOn BETWEEN :dateFrom AND :dateTo")
	List<BuildDetails> getByDateRange(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);

	@Query("SELECT obj FROM BuildDetails obj LEFT JOIN obj.auditEntries ae WHERE obj.modifiedOn BETWEEN :dateFrom AND :dateTo AND ae.approver = :approvedBy and ae.environment = '"
			+ DevOpsConstants.PROD + "'")
	List<BuildDetails> getByDateRangeApproved(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("approvedBy") String approvedBy);

	@Query("SELECT obj FROM BuildDetails obj WHERE obj.modifiedOn BETWEEN :dateFrom AND :dateTo AND obj.modifiedBy = :triggeredBy")
	List<BuildDetails> getByDateRangeTriggered(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("triggeredBy") String triggeredBy);

	@Query("SELECT obj FROM BuildDetails obj WHERE obj.modifiedOn BETWEEN :dateFrom AND :dateTo AND obj.fullyReleased = :finalized")
	List<BuildDetails> getByDateRangeFinalized(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("finalized") Boolean finalized);

	@Query("SELECT obj FROM BuildDetails obj WHERE obj.modifiedOn BETWEEN :dateFrom AND :dateTo AND obj.modifiedBy = :triggeredBy AND obj.fullyReleased = :finalized")
	List<BuildDetails> getByDateRangeTriggeredAndFinalized(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("triggeredBy") String triggeredBy, @Param("finalized") Boolean finalized);

	@Query(nativeQuery = true, value="select a.modified_by, b.full_name, count(*) from build_details a join permission b on a.modified_by = b.username where DATEDIFF(DY, modified_on, NOW()) <= 360 group by a.modified_by, b.full_name")
	String[][] getTopDevs();
	
	@Query(nativeQuery = true, value="select count(*) from build_details a \n"
			+ "join build_details_audit_entries b on a.build_number = b.build_number and a.job_id = b.job_id\n"
			+ "join build_audit c on b.audit_entries_id = c.id\n"
			+ "where c.environment = 'prod' and c.status = 'APPROVED'")
	int getLifetimeProdReleases();
	
	@Query(nativeQuery = true, value="select count(*) from build_details a \n"
			+ "join build_details_audit_entries b on a.build_number = b.build_number and a.job_id = b.job_id\n"
			+ "join build_audit c on b.audit_entries_id = c.id\n"
			+ "where c.environment = 'prod' and c.status = 'APPROVED' and DATEDIFF(DY, c.modified_on, NOW()) <= 30")
	int getProdReleasesLast30();
	
}
