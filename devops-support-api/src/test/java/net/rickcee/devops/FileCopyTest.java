/**
 * 
 */
package net.rickcee.devops;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.rickcee.scm.devops.model.BuildDetails;
import net.rickcee.scm.devops.model.VCSStorage;
import net.rickcee.scm.devops.util.FileSystemUtils;

/**
 * @author rickcee
 *
 */
public class FileCopyTest {
//	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
//	private static String separator = "=====================================================================================================================";
//
//	private static Map<String, String> environmentBaseLocation = new HashMap<>();
//	static {
//		environmentBaseLocation.put("dev", "/opt/devops_work_dir/dev_apps");
//		environmentBaseLocation.put("stage", "/opt/devops_work_dir/stage_apps");
//		environmentBaseLocation.put("uat", "/opt/devops_work_dir/uat_apps");
//		environmentBaseLocation.put("prod", "/opt/devops_work_dir/prod_apps");
//	}
	
	public static void main(String args[]) throws Exception {
		//String originalFile = "/opt/devops_work_dir/dev_apps/file";
		//String destinationFile = "/opt/devops_work_dir/uat_apps/file";
		
		FileSystemUtils fsu = new FileSystemUtils();
		
		BuildDetails bd = new BuildDetails();
		
		bd.setRepository(new VCSStorage());
		bd.getRepository().setFsLocation("app_1");
		
		bd.setReleaseFiles(new ArrayList<String>());
		bd.getReleaseFiles().add("file1");
		bd.getReleaseFiles().add("file2");
		bd.getReleaseFiles().add("file3");
		bd.getReleaseFiles().add("shell/script.sh");

		//deployToEnvironment("/opt/devops_work_dir/dummy_workspace", "dev", bd);
		fsu.deployToEnvironment("/opt/devops_work_dir/dummy_workspace", "uat", bd);
		
		
		//runCmd("ls -ltr " + originalFile + " " + destinationFile);
		//System.out.println(separator);
	}
	
//	static void deployToEnvironment(String workspace, String environment, BuildDetails bd) throws Exception {
//		
//		String srcFile, destFile, allDestFiles = "";
//		File destFileLocation;
//		for(String file : bd.getReleaseFiles()) {
//
//			srcFile = workspace + "/" + file;
//			destFile = environmentBaseLocation.get(environment) + "/" + bd.getRepository().getFsLocation() + "/" + file;
//			destFileLocation = new File(destFile);
//			if(!destFileLocation.exists()) {
//				destFileLocation.mkdirs();
//			}
//			
//			System.out.println("Copy File: " + srcFile + " --> " + destFile);
//			allDestFiles = allDestFiles.concat(" " + destFile);
//			copyFile(srcFile, destFile);
//		}
//		
//		runCmd("ls -ltr " + allDestFiles);
//	}
//	
//	static void copyFile(String source, String destination) throws Exception {
//		Path original = Paths.get(source);
//		Path copied = Paths.get(destination);
//		
//		Files.copy(original, copied, StandardCopyOption.REPLACE_EXISTING);
//	}
//	
//	static void runCmd(String command) throws Exception {
//		System.out.println(separator);
//		System.out.println("= Audit Record");
//		System.out.println("=");
//		System.out.println("= Timestamp: " + sdf.format(new Date()));
//		System.out.println("=");
//		System.out.println("= Running:");
//		System.out.println("=");
//		System.out.println("= [" + command + "]");
//		System.out.println("=");
//		System.out.println(separator);
//		System.out.println("");
//		try {
//			String [] cmd =  {"sh", "-c", command};
//		    Process process = Runtime.getRuntime().exec( cmd );
//		 
//		    BufferedReader reader = new BufferedReader(
//		            new InputStreamReader(process.getInputStream()));
//		    String line;
//		    while ((line = reader.readLine()) != null) {
//		        System.out.println(line);
//		    }
//		 
//		    reader.close();
//		 
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}
//		System.out.println("");
//	}
}
