package net.rickcee.scm.devops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NexusFileView {
	private String name;
	private String lastModified;
	private String size;
}
