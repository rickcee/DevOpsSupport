/**
 * 
 */
package net.rickcee.scm.devops.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import net.rickcee.scm.devops.dto.SearchBuildParams;
import net.rickcee.scm.devops.model.BuildDetails;
import net.rickcee.scm.devops.model.BuildDetailsId;
import net.rickcee.scm.devops.model.ReleaseEnvironment;
import net.rickcee.scm.devops.model.ReleaseEnvironmentId;
import net.rickcee.scm.devops.model.VCSPermission;
import net.rickcee.scm.devops.model.VCSStorage;
import net.rickcee.scm.devops.repository.BuildDetailsRepository;
import net.rickcee.scm.devops.repository.EnvironmentRepository;
import net.rickcee.scm.devops.repository.VCSPermissionRepo;
import net.rickcee.scm.devops.repository.VCSStorageRepo;
import net.rickcee.scm.devops.repository.VCSStorageTypeRepo;
import net.rickcee.scm.devops.util.PDFUtils;

/**
 * @author rickcee
 *
 */
@Slf4j
@RestController
@RequestMapping("/secured/")
public class DevOpsUIController {
	@Autowired
	private VCSStorageRepo vcsStorageRepo;
	@Autowired
	private VCSStorageTypeRepo vcsStorageTypeRepo;
	@Autowired
	private EnvironmentRepository envRepo;
	@Autowired
	private VCSPermissionRepo permRepo;
	@Autowired
	private BuildDetailsRepository bdRepo;
	@Autowired
	private PDFUtils pdfUtils;
	
	@RequestMapping("/user")
	public Object user() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/repository")
	@Transactional
	public VCSStorage processRepoChanges(@RequestBody VCSStorage repo) {
		log.info("[POST] Repository: " + repo);
		return vcsStorageRepo.save(repo);
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/repository", produces = { "application/json" })
	public Object getRepositories() {
		return vcsStorageRepo.findAll();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/repository/type", produces = { "application/json" })
	public Object getRepositoryTypes() {
		return vcsStorageTypeRepo.findAll();
	}

	@RequestMapping(method = RequestMethod.POST, path = "/environment")
	@Transactional
	public ReleaseEnvironment processRepoChanges(@RequestBody ReleaseEnvironment environment) {
		log.info("[POST] ReleaseEnvironment: " + environment);
		environment.setId(new ReleaseEnvironmentId(environment.getEnvironmentGroup(), environment.getEnvironmentId()));
		return envRepo.save(environment);
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/environment", produces = { "application/json" })
	public Object getEnvironment() {
		return envRepo.findAll();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/permission/add/{username}/{vcsId}")
	@Transactional
	public void registerPermission(@PathVariable("username") String username, @PathVariable("vcsId") Long vcsId) {
		registerPermissionPost(username, vcsId);
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/permission/add/{username}/{vcsId}")
	@Transactional
	public VCSPermission registerPermissionPost(@PathVariable("username") String username, @PathVariable("vcsId") Long vcsId) {
		VCSPermission perm;
		try {
			perm = permRepo.findById(username).get();
		} catch (Exception e) {
			perm = new VCSPermission();
			perm.setUsername(username);
			perm.setRepositories(new ArrayList<VCSStorage>());
		}
		VCSStorage repository;
		try {
			repository = vcsStorageRepo.findById(vcsId).get();
		} catch (Exception e) {
			log.warn("VCS Repository [" + vcsId + "] doesn't exist.");
			return null;
		}
		if (!perm.getRepositories().contains(repository)) {
			perm.getRepositories().add(repository);
		}
		return permRepo.save(perm);
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/permission/remove/{username}/{vcsId}")
	@Transactional
	public void deregisterPermission(@PathVariable("username") String username, @PathVariable("vcsId") Long vcsId) {
		log.info("Removing Permission for User [" + username + "], Repository ID [" + vcsId + "].");
		VCSPermission perm;
		try {
			perm = permRepo.findById(username).get();
		} catch (Exception e) {
			log.warn("VCS Permission for [" + username + "] doesn't exist.");
			return;
		}
		VCSStorage repository;
		try {
			repository = vcsStorageRepo.findById(vcsId).get();
		} catch (Exception e) {
			log.warn("VCS Repository [" + vcsId + "] doesn't exist.");
			return;
		}
		if (perm.getRepositories().contains(repository)) {
			perm.getRepositories().remove(repository);
		}
		permRepo.save(perm);
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/permission/{username}", produces = { "application/json" })
	public Object getPermission(@PathVariable("username") String username) {
		try {
			VCSPermission perm = permRepo.findById(username).get();
			return perm;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No permissions found for " + username);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/builds", produces = { "application/json" })
	public Object getBuilds() {
		List<BuildDetails> bds = bdRepo.findAll();
		return bds;
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/builds/search", produces = { "application/json" })
	public Object buildSearch(@RequestBody SearchBuildParams params) {
		log.info("[POST] Content: " + params);
		if (params.getBuildNumber() != null) {
			return bdRepo.getByBuildNumber(params.getBuildNumber());
		}
		if (params.getBuildDateFrom() != null && params.getBuildDateTo() != null && (params.getApprovedBy() != null && !params.getApprovedBy().trim().equals("") && params.getFinalized().booleanValue() == false)) {
			return bdRepo.getByDateRangeApproved(params.getBuildDateFrom(), params.getBuildDateTo(), params.getApprovedBy());
		}
		if (params.getBuildDateFrom() != null && params.getBuildDateTo() != null && params.getTriggeredBy() != null && !params.getTriggeredBy().trim().equals("") && params.getFinalized().booleanValue() == false) {
			return bdRepo.getByDateRangeTriggered(params.getBuildDateFrom(), params.getBuildDateTo(), params.getTriggeredBy());
		}
		if (params.getBuildDateFrom() != null && params.getBuildDateTo() != null && params.getTriggeredBy() != null && !params.getTriggeredBy().trim().equals("") && params.getFinalized().booleanValue() == true) {
			return bdRepo.getByDateRangeTriggeredAndFinalized(params.getBuildDateFrom(), params.getBuildDateTo(), params.getTriggeredBy(), params.getFinalized());
		}
		if (params.getBuildDateFrom() != null && params.getBuildDateTo() != null && params.getFinalized() != null && params.getFinalized()) {
			return bdRepo.getByDateRangeFinalized(params.getBuildDateFrom(), params.getBuildDateTo(), params.getFinalized());
		}
		if (params.getBuildDateFrom() != null && params.getBuildDateTo() != null) {
			return bdRepo.getByDateRange(params.getBuildDateFrom(), params.getBuildDateTo());
		}
		return new ArrayList<BuildDetails>();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/audit/{jobId}/{build}", produces = { "application/json" })
	public Object getBuildDetails(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber) {
		BuildDetails bd = bdRepo.getOne(new BuildDetailsId(jobId, buildNumber));
		log.info("Build ID: " + bd.getBuildNumber());
		log.info("Repository: " + bd.getRepository());
		log.info("Audit Entries: " + bd.getAuditEntries());
		return bd;
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/audit/PDF/{jobId}/{buildId}", produces = { "application/pdf" })
	public ResponseEntity<InputStreamResource> generateEvidence(@PathVariable("jobId") String jobId, @PathVariable("buildId") Long buildNumber) throws Exception {
		BuildDetails bd = bdRepo.findById(new BuildDetailsId(jobId, buildNumber)).orElseThrow(NoSuchElementException::new);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add("Content-Disposition", "fileName=evidence.pdf");
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Expires", "0");
		
		ResponseEntity<InputStreamResource> response = new ResponseEntity<InputStreamResource>(
				new InputStreamResource(new ByteArrayInputStream(pdfUtils.generateEvidence(bd).toByteArray())), headers, HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/dashboard/data")
	public Object getDashboardData() {
		Map<String, Object> result = new HashMap<>();
		result.put("topDevs", bdRepo.getTopDevs());
		result.put("lifetimeReleases", bdRepo.getLifetimeProdReleases());
		result.put("past30Releases", bdRepo.getProdReleasesLast30());
		return result;
	}
}
