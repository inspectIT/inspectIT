package info.novatec.inspectit.cmr.service;

/**
 * Enumeration used to define the type of the service exporter. Depending on the type, some optional
 * parameters from the {@link ServiceInterface} annotation must be set like service port if RMI is
 * used.
 * 
 * @author Patrice Bouillet
 * 
 */
public enum ServiceExporterType {

	HTTP, RMI; // NOCHK

}
