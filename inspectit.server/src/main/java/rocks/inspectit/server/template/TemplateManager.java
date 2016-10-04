package rocks.inspectit.server.template;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.util.ResourcesPathResolver;

/**
 * Manages text templates.
 *
 * @author Alexander Wert
 *
 */
@Component
public class TemplateManager {

	/**
	 * Folder where all templates are saved.
	 */
	public static final String DEFAULT_TEMPLATE_FOLDER = "templates";

	/**
	 * Used with {@link ResourcesPathResolver} to get the file of the templates dir.
	 */
	File templatesDir;

	/**
	 * Resolves a template for the given template type and set of placeholder to replace.
	 *
	 * @param templateType
	 *            {@link AlertEMailTemplateType} of the template to use.
	 * @param replacements
	 *            Key-Value pairs representing the properties to insert.
	 * @return The resolved template with inserted variables.
	 * @throws IOException
	 *             Thrown if a template file cannot be opened.
	 */
	public String resolveTemplate(ITemplateType templateType, Map<String, String> replacements) throws IOException {
		Path path = getTemplatePath(templateType);
		String template = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		return insertProperties(template, replacements);
	}

	/**
	 * Inserts properties to the template.
	 *
	 * @param template
	 *            Template string where to insert the properties to.
	 * @param properties
	 *            Key-Value pairs representing the properties to insert.
	 * @return The modified template.
	 */
	private String insertProperties(String template, Map<String, String> properties) {
		for (Entry<String, String> entry : properties.entrySet()) {
			template = template.replace(entry.getKey(), entry.getValue());
		}
		return template;
	}

	/**
	 * Returns the default template folder.
	 *
	 * @return Returns the default template folder.
	 */
	private Path getDefaultTemplatesPath() {
		return templatesDir.toPath();
	}

	/**
	 * Resolves the path to the given template.
	 *
	 * @param templateType
	 *            {@link AlertEMailTemplateType} of the template.
	 * @return The path to the given template.
	 */
	private Path getTemplatePath(ITemplateType templateType) {
		return getDefaultTemplatesPath().resolve(templateType.getFileName());
	}

	/**
	 * Initializes {@link #configDirFile}.
	 */
	@PostConstruct
	protected void init() {
		try {
			templatesDir = ResourcesPathResolver.getResourceFile(DEFAULT_TEMPLATE_FOLDER);
		} catch (IOException exception) {
			throw new BeanInitializationException("Template manager can not locate templates directory.", exception);
		}
	}
}
