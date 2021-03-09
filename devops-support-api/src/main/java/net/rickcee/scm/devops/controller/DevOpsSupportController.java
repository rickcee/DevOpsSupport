package net.rickcee.scm.devops.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.rickcee.scm.devops.dto.InitialPipelineParameters;
import net.rickcee.scm.devops.model.AuditEntry;
import net.rickcee.scm.devops.model.BuildDetails;
import net.rickcee.scm.devops.model.BuildDetailsId;
import net.rickcee.scm.devops.model.ReleaseEnvironmentId;
import net.rickcee.scm.devops.model.VCSStorage;
import net.rickcee.scm.devops.repository.BuildDetailsRepository;
import net.rickcee.scm.devops.repository.EnvironmentRepository;
import net.rickcee.scm.devops.repository.VCSStorageRepo;
import net.rickcee.scm.devops.util.DevOpsConstants;
import net.rickcee.scm.devops.util.FileSystemUtils;
import net.rickcee.scm.devops.util.PDFUtils;

@RestController
@RequestMapping("/jenkins/")
@Slf4j
public class DevOpsSupportController {
	@Autowired
	private Environment env;
	@Autowired
	private BuildProperties props;
	@Autowired
	private EnvironmentRepository envRepo;
	@Autowired
	private VCSStorageRepo vcsStorageRepo;
	@Autowired
	private BuildDetailsRepository bdRepo;
	@Autowired
	private FileSystemUtils fsu;
	@Autowired
	private PDFUtils pdfUtils;

	@RequestMapping(method = RequestMethod.GET, path = "/HealthCheck", produces = { "application/json" })
	public Object healthCheck() {
		HashMap<String, String> result = new HashMap<>();
		result.put("result", "OK");
		result.put("application", props.getArtifact());
		result.put("version", props.getVersion());
		result.put("timeout", env.getProperty("server.timeout"));
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/nexus/url/{environmentGroup}/{environmentId}", produces = { "application/json" })
	public Object getNexusUrl(@PathVariable("environmentGroup") String environmentGroup, @PathVariable("environmentId") String environmentId) {
		return envRepo.getOne(new ReleaseEnvironmentId(environmentGroup, environmentId)).getNexusUploadUrl();
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
	
//	@RequestMapping(method = RequestMethod.GET, path = "/register/{username}/{repositoryId}/{build}")
//	@Transactional
//	public void registerBuild(@PathVariable("username") String username,
//			@PathVariable("repositoryId") Long repositoryId, @PathVariable("build") Long build,
//			@RequestParam(value = "description", required = false) String description) {
//		VCSStorage repo = vcsStorageRepo.getOne(repositoryId);
//		BuildDetails bd = new BuildDetails();
//		bd.setRepository(repo);
//		bd.setBuildNumber(build);
//		bd.setModifiedBy(username);
//		bd.setModifiedOn(new Date());
//		bd.setModifiedByServer(DevOpsContants.RUN_HOST);
//		bd.setBuildDescription(description);
//		bdRepo.save(bd);
//		log.info("Registered Build Number [" + build + "] w/ VCS Repository [" + repo + "]");
//	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/register/{username}/{repositoryId}/{jobId}/{build}")
	@Transactional
	public void registerBuildPost(@PathVariable("username") String username,
			@PathVariable("repositoryId") Long repositoryId, @PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber,
			@RequestBody InitialPipelineParameters initialData) {
		VCSStorage repo = vcsStorageRepo.getOne(repositoryId);
		BuildDetails bd = new BuildDetails();
		bd.setId(new BuildDetailsId(jobId, buildNumber));
		bd.setRepository(repo);
		//bd.setJobId(jobId);
		//bd.setBuildNumber(build);
		bd.setModifiedBy(username);
		bd.setModifiedOn(new Date());
		bd.setModifiedByServer(DevOpsConstants.RUN_HOST);
		bd.setTagName(initialData.getTagName());
		bd.setBuildDescription(initialData.getComments());
		bdRepo.save(bd);
		log.info("Registered Build Number [" + buildNumber + "] w/ VCS Repository [" + repo + "]");
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/register/{jobId}/{build}")
	@Transactional
	public void registerBuildFiles(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber, @RequestBody String fileList) {
		BuildDetails db = bdRepo.findById(new BuildDetailsId(jobId, buildNumber)).orElseThrow(NoSuchElementException::new);
		StringTokenizer st = new StringTokenizer(fileList, "\n");
		if(st.countTokens() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No content found in the release file!");
		}
		String fileToRelease;
		List<String> files = new ArrayList<String>();
		while(st.hasMoreTokens()) {
			fileToRelease = st.nextToken();
			if(fileToRelease == null || fileToRelease.trim().equals("")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No or bad content found in the release file!");
			}
			files.add(fileToRelease);
			log.info("Registered File [" + fileToRelease + "] w/ Build [" + buildNumber + "]");
		}
		db.setReleaseFiles(files);
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/register/maven/{jobId}/{build}")
	@Transactional
	public void registerMavenBuildFiles(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber, @RequestBody String fileList) {
		BuildDetails db = bdRepo.findById(new BuildDetailsId(jobId, buildNumber)).orElseThrow(NoSuchElementException::new);
		StringTokenizer st = new StringTokenizer(fileList, "\n");
		if(st.countTokens() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No content found in the data received!");
		}
		String fileToRelease;
		List<String> files = new ArrayList<String>();
		StringTokenizer st2;
		String groupId, artifactId, version, type, finalPath;
		while(st.hasMoreTokens()) {
			fileToRelease = st.nextToken();
			if(fileToRelease == null || fileToRelease.trim().equals("")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No or bad content found in the release file!");
			}
			
			/* net.rickcee jdcompare 2.0.0 war */
			/*
			net.rickcee.scm.devops devops-support 1.0.0 pom
			net.rickcee.scm.devops devops-support-api 1.0.0 jar
			net.rickcee.scm.devops devops-support-ui 1.0.0 jar
			*/
			st2 = new StringTokenizer(fileToRelease," ");
			log.info("Number of Tokens: " + st2.countTokens());
			if(st2.countTokens() == 4) { 
				groupId = st2.nextToken();
				artifactId = st2.nextToken();
				version = st2.nextToken();
				type = st2.nextToken();
				finalPath = groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "." + type;
				files.add(finalPath);
				log.info("Registered File [" + finalPath + "] w/ Build [" + buildNumber + "]");
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Invalid content found in the data received: [" + fileToRelease + "]");
			}
			//files.add(fileToRelease);
		}
		db.setNexusFiles(files);
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/release/{jobId}/{build}/{environment}")
	@Transactional
	public void releaseFiles(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber, @PathVariable("environment") String environment) {
		BuildDetails db = bdRepo.findById(new BuildDetailsId(jobId, buildNumber)).orElseThrow(NoSuchElementException::new);
		if (db.getReleaseFiles() == null) {
			throw new RuntimeException("No files to release!");
		}
		for (String fileToRelease : db.getReleaseFiles()) {
			log.info("Build [" + buildNumber + "] // Releasing File [" + fileToRelease + "] to [" + environment + "]");
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/audit/{jobId}/{build}/{environment}/{approver}")
	@Transactional
	public void auditApprover(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber, @PathVariable("environment") String environment,
			@PathVariable("approver") String approver, @RequestBody String requestBody) {
		log.info("Registered Audit Entry " + buildNumber + " / " + environment + " / " + approver);
		
		BuildDetails bd = bdRepo.getOne(new BuildDetailsId(jobId, buildNumber));
		
		AuditEntry ae = new AuditEntry();
		//ae.setId(new AuditEntryId(bd, environment));
		ae.setApprover(approver);
		ae.setEnvironment(environment);
		ae.setReleaseTicketId(requestBody);
		ae.setStatus(DevOpsConstants.STATUS_APPROVED);
		
		bd.getAuditEntries().add(ae);

	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/audit/{jobId}/{build}")
	@Transactional
	public void auditFullyReleased(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber, @RequestBody String requestBody) {
		log.info("Registered Audit Entry - Fully Released" + jobId + " / " + buildNumber);
		
		BuildDetails bd = bdRepo.getOne(new BuildDetailsId(jobId, buildNumber));
		bd.setFullyReleased(true);

	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/audit/success/{jobId}/{build}/{environment}")
	@Transactional
	public void auditSuccess(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber, @PathVariable("environment") String environment, 
			@RequestBody String requestBody) {
		log.info("Registered Success Audit Entry " + buildNumber + " / " + environment);
		
		BuildDetails bd = bdRepo.getOne(new BuildDetailsId(jobId, buildNumber));
		
		AuditEntry ae = new AuditEntry();
		//ae.setId(new AuditEntryId(bd, environment));
		//ae.setApprover(approver);
		ae.setEnvironment(environment);
		ae.setStatus(DevOpsConstants.STATUS_OK);
		//ae.setReleaseTicketId(requestBody);
		
		bd.getAuditEntries().add(ae);

	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/audit/exception/{jobId}/{build}/{environment}")
	@Transactional
	public void auditException(@PathVariable("jobId") String jobId, @PathVariable("build") Long buildNumber, @PathVariable("environment") String environment, 
			@RequestBody String requestBody) {
		log.info("Registered Exception Audit Entry " + buildNumber + " / " + environment);
		
		BuildDetails bd = bdRepo.getOne(new BuildDetailsId(jobId, buildNumber));
		
		AuditEntry ae = new AuditEntry();
		//ae.setId(new AuditEntryId(bd, environment));
		//ae.setApprover(approver);
		ae.setEnvironment(environment);
		ae.setStatus(DevOpsConstants.STATUS_ERROR);
		ae.setExceptionMsg(requestBody);
		
		bd.getAuditEntries().add(ae);

	}
	
//	@RequestMapping(method = RequestMethod.GET, path = "/git/repository", produces = { "application/json" })
//	public Object getVCSRepositoriesAsStringListOrig() {
//		List<String> result = new ArrayList<>();
//		List<VCSStorage> repos = vcsStorageRepo.findAll();
//		for(VCSStorage repo : repos) {
//			result.add(repo.getId() + " || " + repo.getUrl());
//		}
//		return result;
//	}
//	
//	@RequestMapping(method = RequestMethod.GET, path = "/vcs/repository/{type}", produces = { "application/json" })
//	public Object getVCSRepositoriesAsStringList(@PathVariable("type") Long type) {
//		List<String> result = new ArrayList<>();
//		List<VCSStorage> repos = vcsStorageRepo.getByVCSType(type);
//		for(VCSStorage repo : repos) {
//			result.add(repo.getId() + " || " + repo.getUrl());
//		}
//		return result;
//	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/vcs/repository/json/{type}", produces = { "application/json" })
	public Object getVCSRepositoriesAsList(@PathVariable("type") Long type) {
		return vcsStorageRepo.getByVCSType(type);
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/filecopy/{jobId}/{buildNumber}/{environment}", produces = {
			"application/json" })
	@Transactional
	public void releaseFilesToEnvironment(@PathVariable("jobId") String jobId,
			@PathVariable("buildNumber") Long buildNumber, @PathVariable("environment") String environment,
			@RequestBody String jsonParams) throws Exception {
		log.info("[POST] Content: " + jsonParams);
		ObjectMapper mapper = new ObjectMapper();
		
		Map<String, String> mapParams = mapper.readValue(jsonParams, new TypeReference<Map<String, String>>() {});
		
		BuildDetails bd = bdRepo.findById(new BuildDetailsId(jobId, buildNumber)).orElseThrow(NoSuchElementException::new);
		
//		AuditEntry ae = new AuditEntry();
//		//ae.setId(new AuditEntryId(bd, environment));
//		ae.setApprover(approver);
//		ae.setEnvironment(environment);
//		ae.setReleaseTicketId(mapParams.get(DevOpsContants.RELEASE_TICKET_ID));
//		
//		bd.getAuditEntries().add(ae);
		
		fsu.deployToEnvironment(mapParams.get(DevOpsConstants.BUILD_WORKSPACE), environment, bd);
	}
}