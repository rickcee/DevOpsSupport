/**
 * 
 */
package net.rickcee.scm.devops.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.rickcee.scm.devops.model.BuildDetails;
import net.rickcee.scm.devops.model.ReleaseEnvironmentId;
import net.rickcee.scm.devops.repository.EnvironmentRepository;

/**
 * @author rickcee
 *
 */
@Component
@Slf4j
public class FileSystemUtils {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS a");
	private static String separator = "===============================================================================================================";
	
	@Autowired
	private EnvironmentRepository envRepo;
	
	public void deployToEnvironment(String workspace, String environment, BuildDetails bd) throws Exception {
		String srcFile, destFile, allDestFiles = "";
		
		String baseLocation = envRepo
				.getOne(new ReleaseEnvironmentId(bd.getRepository().getEnvironmentGroup(), environment))
				.getBaseLocation();
		
		for(String file : bd.getReleaseFiles()) {

			srcFile = workspace + "/" + file;
			destFile = baseLocation + "/" + bd.getRepository().getFsLocation() + "/" + file;
			
			log.info("Copy File: " + srcFile + " --> " + destFile);
			allDestFiles = allDestFiles.concat(" " + destFile);
			copyFile(srcFile, destFile);
		}
		
		StringBuilder sb = runCmd("ls -ltr " + allDestFiles);
		if (sb != null) {
			log.info(sb.toString());
		}
	}
	
	public void copyFile(String source, String destination) throws Exception {
		source = replaceInvalidChars(source);
		destination = replaceInvalidChars(destination);
		
		log.info("Source File: [" + source + "]");
		log.info("Destination File: [" + destination + "]");

		Path srcPath = Paths.get(new URI("file://" + source));
		Path destPath = Paths.get(new URI("file://" + destination));
		
		log.info("Source Path: [" + srcPath + "]");
		log.info("Destination Path: [" + destPath + "]");
		
		Files.createDirectories(destPath.getParent());

		Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
	}

	private String replaceInvalidChars(String source) {
		return source.replace("\r", "").replace("\n", "").replace("\t", "");
	}
	
	public StringBuilder runCmd(String command) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(separator).append("\n");
		sb.append("= Audit Record").append("\n");
		sb.append("=").append("\n");
		sb.append("= Timestamp: " + sdf.format(new Date())).append("\n");
		//sb.append("=").append("\n");
		/*
		sb.append("= Running:").append("\n");
		sb.append("=").append("\n");
		sb.append("= [" + command + "]").append("\n");
		sb.append("=").append("\n");
		*/
		sb.append(separator).append("\n");
		//sb.append("").append("\n");
		sb.append("> " + command).append("\n");
		sb.append("").append("\n");
		try {
			String [] cmd =  {"sh", "-c", command};
		    Process process = Runtime.getRuntime().exec( cmd );
		 
		    BufferedReader reader = new BufferedReader(
		            new InputStreamReader(process.getInputStream()));
		    String line;
		    while ((line = reader.readLine()) != null) {
		        sb.append(line).append("\n");;
		    }
		 
		    reader.close();
		 
		} catch (IOException e) {
		    log.error(e.getMessage(), e);
		    return null;
		}
		sb.append(separator);
		//System.out.println("");
		return sb;
	}
}
