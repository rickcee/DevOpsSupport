/**
 * 
 */
package net.rickcee.scm.devops.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.rickcee.scm.devops.dto.NexusFileView;

/**
 * @author rickcee
 *
 */
@Component
@Slf4j
public class DevOpsUtils {

	@Autowired
	private SSLUtils sslUtils;
	@Value("${app.prod.ticketRequired}")
	private boolean prodTicketRequired;

	public boolean isProductionTicketRequired() {
		return prodTicketRequired;
	}

	public List<NexusFileView> extractNexusData(String _url) throws Exception {

		// Deal w/ any potential SSL issues by ignoring certs (UNSAFE!)
		// TODO: Find another way
		sslUtils.trustAllHosts();

		URL url = new URL(_url);
		URLConnection conn = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String html = br.lines().collect(Collectors.joining(System.lineSeparator()));

		// Use Jsoup to parse and correct syntax.
		final Document doc = Jsoup.parse(html);
		doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

		List<NexusFileView> files = new ArrayList<>();
		NexusFileView file;
		boolean first = true;
		for (Element table : doc.select("table")) {
			for (Element row : table.select("tr")) {
				if (first) {
					first = false;
					continue;
				}
				Elements tds = row.select("td");
				if (tds.size() > 3) {
					String name = tds.get(0).text();
					String lastModified = tds.get(1).text();
					String size = tds.get(2).text();
					log.debug("Name: " + name + " / Last Modified: " + lastModified + " / Size: " + size);
					file = new NexusFileView(name, lastModified, size);
					files.add(file);
				}
			}
		}

		log.info("Extracted from Nexus: " + files);
		return files;
	}
}
