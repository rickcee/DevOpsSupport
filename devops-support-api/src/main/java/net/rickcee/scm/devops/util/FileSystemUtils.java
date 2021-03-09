/**
 * 
 */
package net.rickcee.scm.devops.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
		File destFileLocation;
		
		String baseLocation = envRepo
				.getOne(new ReleaseEnvironmentId(bd.getRepository().getEnvironmentGroup(), environment))
				.getBaseLocation();
		
		for(String file : bd.getReleaseFiles()) {

			srcFile = workspace + "/" + file;
			destFile = baseLocation + "/" + bd.getRepository().getFsLocation() + "/" + file;
			destFileLocation = new File(destFile);
			if(!destFileLocation.exists()) {
				destFileLocation.mkdirs();
			}
			
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
		log.info("Source File: [" + source + "]");
		log.info("Source File Exists: " + new File(source).exists());
		log.info("Source File to URI: [" + new File(source).toURI() + "]");
		Path original = Paths.get(new File(source).toURI());
		Path copied = Paths.get(new File(destination).toURI());

		Files.copy(original, copied, StandardCopyOption.REPLACE_EXISTING);
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
