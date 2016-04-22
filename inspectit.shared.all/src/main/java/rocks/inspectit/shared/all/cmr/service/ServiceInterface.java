package rocks.inspectit.shared.all.cmr.service;

import java.awt.Component;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a "Service" interface (e.g. a business service facade).
 * 
 * <p>
 * This annotation serves as a specialization of {@link Component @Component}, allowing for
 * interface classes to be autodetected through classpath scanning.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from
 * <a href="http://jira.springframework.org/browse/SPR-3926">Spring JIRA (SPR-3926)</a>. Original
 * authors are James Douglas and Henno Vermeulen.
 * 
 * @author Henno Vermeulen
 * @author James Douglas
 * @author Patrice Bouillet
 * @see Component
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceInterface {

	// Note on Checkstyle ignores:
	// Due to bug http://sourceforge.net/p/checkstyle/bugs/641/ it is currently not possible to add
	// @return tags to methods within an @interface definition, thus we currently ignore these
	// incorrect findings.

	/**
	 * The value may indicate a suggestion for a logical component name, to be turned into a Spring
	 * bean in case of an autodetected component.
	 * 
	 * @return the suggested component name, if any //NOCHK
	 */
	String name() default "";

	/**
	 * Defines the exporter used for exposing this service. Valid values are defined in the
	 * enumeration {@link ServiceExporterType}.
	 * 
	 * @return the defined exporter type. //NOCHK
	 */
	ServiceExporterType exporter();

	/**
	 * Service id for kryonet remote export. This must be a unique value, thus two different
	 * services must not share the same id.
	 * 
	 * @return Service id for kryonet remote export. //NOCHK
	 */
	int serviceId() default 0;

}