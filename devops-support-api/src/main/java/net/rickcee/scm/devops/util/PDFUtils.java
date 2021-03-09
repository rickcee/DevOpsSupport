/**
 * 
 */
package net.rickcee.scm.devops.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import lombok.extern.slf4j.Slf4j;
import net.rickcee.scm.devops.dto.NexusFileView;
import net.rickcee.scm.devops.model.AuditEntry;
import net.rickcee.scm.devops.model.BuildDetails;
import net.rickcee.scm.devops.model.ReleaseEnvironment;
import net.rickcee.scm.devops.model.ReleaseEnvironmentId;
import net.rickcee.scm.devops.repository.EnvironmentRepository;

/**
 * @author rickcee
 *
 */
@Component
@Slf4j
@SuppressWarnings("unused")
public class PDFUtils {
	@Autowired
	private FileSystemUtils fsu;
	@Autowired
	private DevOpsUtils utils;
	@Autowired
	private EnvironmentRepository envRepo;	
	
	public ByteArrayOutputStream generateEvidence(BuildDetails bd) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
		
		ByteArrayOutputStream pdfContent = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfContent));
        Document doc = new Document(pdfDoc);
 
		doc.add(new Paragraph().add(getText("Job ID: ")).add(getHighlightTextBlue(bd.getJobId()))
				.add(getText(" // Build #: ")).add(getHighlightTextBlue(bd.getBuildNumber().toString())).setFontSize(16)
				.setTextAlignment(TextAlignment.CENTER));
		
		doc.add(new Paragraph().add(getText("Description: ")).add(getHighlightTextGreen(bd.getBuildDescription()))
				.setFontSize(12).setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(""));
        doc.add(new Paragraph("Software Configuration Management: ").setFontSize(12).setUnderline());
        doc.add(new Paragraph(""));

//		doc.add(new Paragraph(getText(bd.getRepository().getType().getName())));
//		doc.add(new Paragraph(getText(bd.getRepository().getUrl() + bd.getRepository().getTagBase() + bd.getTagName())));
        float[] cols0 = { 20, 80 };
        Table table0 = new Table(UnitValue.createPercentArray(cols0)).useAllAvailableWidth();
        table0.setFontSize(9);
		table0.addCell(getHeaderColumn("VCS Type"));
		table0.addCell(new Paragraph(getText(bd.getRepository().getType().getName())));
		table0.addCell(getHeaderColumn("URL (Tag/Branch)"));
		String vcsUrl;
		if(DevOpsConstants.VCS_TYPE_SVN == bd.getRepository().getType().getId()) {
			vcsUrl = bd.getRepository().getUrl() + bd.getRepository().getTagBase() + bd.getTagName();
		} else {
			vcsUrl = bd.getRepository().getUrl() + " // " + bd.getTagName();
		}
		
		table0.addCell(new Paragraph(getText(vcsUrl)));

        doc.add(table0);
        
        doc.add(new Paragraph(""));
        
        float[] cols = { 10, 10, 10, 20, 50 };
        Table table = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();
        table.setFontSize(8);
		table.addCell(getHeaderColumn("Environment"));
		table.addCell(getHeaderColumn("Status"));
		table.addCell(getHeaderColumn("Approver"));
		table.addCell(getHeaderColumn("TimeStamp"));
		table.addCell(getHeaderColumn("Release Ticket ID"));
        for (AuditEntry ae : new TreeSet<AuditEntry>(bd.getAuditEntries())) {
            table.addCell(ae.getEnvironment());
            table.addCell(ae.getStatus());
			table.addCell(ae.getApprover() != null ? ae.getApprover() : "");
			table.addCell(sdf.format(ae.getModifiedOn()));
			table.addCell(ae.getReleaseTicketId() != null ? ae.getReleaseTicketId() : "");
        }

        doc.add(new Paragraph("Evidence of Release Approvals: ").setFontSize(12).setUnderline());
        doc.add(new Paragraph(""));

        doc.add(table);
        
		ReleaseEnvironment prodEnvironment = envRepo.getOne(new ReleaseEnvironmentId(bd.getRepository().getEnvironmentGroup(), DevOpsConstants.PROD));
        
        /* Maven (Nexus) Releases */
		try {
			/*[ "net/rickcee/scm/devops/simple-project-api/1.0.0/simple-project-api-1.0.0.jar"] */
			if (bd.getNexusFiles() != null && bd.getNexusFiles().size() > 0) {
				Set<String> uniquePaths = new HashSet<String>();
				String path;
				for(String file : bd.getNexusFiles()) {
					path = file.substring(0, file.lastIndexOf('/'));
					uniquePaths.add(path);
				}
				
				String nexusBrowseUrl = prodEnvironment.getNexusBrowseUrl();
				if (!nexusBrowseUrl.endsWith("/")) {
					nexusBrowseUrl = nexusBrowseUrl + "/";
				}
				
				float[] cols2 = { 30, 50, 10 };
				Table table2 = new Table(UnitValue.createPercentArray(cols2)).useAllAvailableWidth();
				table2.setFontSize(8);
				table2.addCell(getHeaderColumn("Name"));
				table2.addCell(getHeaderColumn("Last Modified"));
				table2.addCell(getHeaderColumn("Size"));
				
				String finalNexusUrl;
				for(String nexusPath : uniquePaths) {
					finalNexusUrl = nexusBrowseUrl + nexusPath;
					for (NexusFileView nfw : utils.extractNexusData(finalNexusUrl)) {
						table2.addCell(nfw.getName());
						table2.addCell(nfw.getLastModified());
						table2.addCell(nfw.getSize());
					}
				}

				doc.add(new Paragraph(""));
				doc.add(new Paragraph("Evidence of [Nexus] Uploads: ").setFontSize(12).setUnderline().setBold());
				doc.add(new Paragraph(""));
				doc.add(table2);
			}
		} catch (Exception e1) {
			log.warn("Error getting NEXUS evidence: " + e1.getMessage(), e1);
		}
        
        /* File System Releases */
        try {
			if (bd.getReleaseFiles() != null && bd.getReleaseFiles().size() > 0) {
				String fsFile, allDestFiles = "";
				for (String file : bd.getReleaseFiles()) {
					fsFile = prodEnvironment.getBaseLocation() + "/" + bd.getRepository().getFsLocation() + "/" + file;
					allDestFiles = allDestFiles.concat(" " + fsFile);
				}

				Table fsOutput = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
				log.info("AllDestFiles: [" + allDestFiles + "]");
				StringBuilder sb = fsu.runCmd("ls -ltr " + allDestFiles);
				fsOutput.addCell(getFileListingColumn(sb.toString()));

				doc.add(new Paragraph(""));
				doc.add(new Paragraph("Evidence of Files Released: ").setFontSize(12).setUnderline());
				doc.add(new Paragraph(""));
				doc.add(fsOutput);
			}
		} catch (Exception e) {
			log.error("Error getting File System Evidence: " + e.getMessage(), e);
		}
        
        
        /* Footer */
        doc.add(new Paragraph(""));
		doc.add(new Paragraph("Generated by " + getUser() + " on " + sdf.format(new Date()))
				.setFontColor(ColorConstants.BLUE).setTextAlignment(TextAlignment.RIGHT));

		
        doc.close();
        
        return pdfContent;
	}
	
	private Cell getFileListingColumn(String content) {
		return new Cell().setBackgroundColor(ColorConstants.BLACK).setFontColor(ColorConstants.WHITE)
				.add(new Paragraph(content).setFontSize(8)).setTextAlignment(TextAlignment.LEFT);
	}
	
	private Cell getHeaderColumn(String name) {
		return new Cell().setBackgroundColor(ColorConstants.BLACK).setFontColor(ColorConstants.WHITE)
				.add(new Paragraph(name)).setTextAlignment(TextAlignment.CENTER);
	}
	
	private Cell getHighlightCell(String name) {
		return new Cell().setFontColor(ColorConstants.BLACK).setBold()
				.add(new Paragraph(name)).setTextAlignment(TextAlignment.CENTER);
	}
	
	private Text getHighlightTextBlue(String value) {
		return new Text(value).setFontColor(ColorConstants.BLUE).setBold();
	}
	
	private Text getHighlightTextGreen(String value) {
		return new Text(value).setFontColor(ColorConstants.DARK_GRAY).setBold();
	}
	
	private Text getText(String value) {
		return new Text(value);
	}
	
	private Text getCmdLineText(String value) {
		try {
			return new Text(value).setBackgroundColor(ColorConstants.BLACK).setFontColor(ColorConstants.WHITE)
					.setFont(PdfFontFactory.createFont(StandardFonts.COURIER));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	private String getUser() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
}
