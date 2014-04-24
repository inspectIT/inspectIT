package info.novatec.inspectit.cmr.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that all our client-server services can use to declare if the default value should be
 * returned to the caller if any problem occur in communication between UI and CMR.
 * <p>
 * Note that this only relates to the calls executed on the UI. The complete intercepting is done on
 * the UI as well.
 * <p>
 * Annotation can be placed on the type and/or methods. If placed on the type then all methods are
 * considered to have the annotation as well.
 * <p>
 * <B>IMPORTANT:</b> The annotation should only be used on the service interfaces we define.
 * 
 * @author Ivan Senic
 * 
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnDefaultValue {
}
