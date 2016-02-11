package info.novatec.inspectit.cmr.jetty;

import info.novatec.inspectit.spring.logger.Log;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.mortbay.jetty.servlet.Context;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * This class binds an empty Spring {@link GenericWebApplicationContext} to the
 * <code>ServletContext</code> of a given {@link Context}.
 * 
 * The newly created web application context is required, so that servlets managed via Jetty can
 * attach to it. While the context itself is usually empty, it is bound to the parent context
 * provided through {@link ApplicationContextAware#setApplicationContext(ApplicationContext)}. Thus,
 * all Jetty managed servlets may obtain the web application context through the configured
 * {@link #setContextAttribute(String) context attribute}.
 * 
 * The configured {@link #setJettyContext(Context) Jetty context} is automatically started upon
 * {@link #initialize() initialization}.
 * 
 * @author NovaProvisioning
 */
public class JettyWebApplicationContextInitializer implements ApplicationContextAware {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The context attribute.
	 */
	private String contextAttribute;

	/**
	 * The jetty context being inject.
	 */
	private Context jettyContext;

	/**
	 * The real application context.
	 */
	private ApplicationContext ctx;

	/**
	 * Initializes the context attribute used to bind the web application context to the
	 * <code>ServletContext</code>.
	 * 
	 * @param contextAttribute
	 *            identifier to be used for binding
	 */
	public void setContextAttribute(String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}

	/**
	 * Injects the Jetty context object to be initialized.
	 * 
	 * @param jettyContext
	 *            the context
	 */
	public void setJettyContext(Context jettyContext) {
		this.jettyContext = jettyContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.ctx = applicationContext;
	}

	/**
	 * Performs initialization of the web context binding it to Jetty.
	 * 
	 * @throws Exception
	 *             in case of an error while starting the Jetty context
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		ServletContext servletContext = jettyContext.getServletContext();

		GenericWebApplicationContext webCtx = new GenericWebApplicationContext();
		webCtx.setServletContext(servletContext);
		webCtx.setParent(ctx);
		webCtx.refresh();

		servletContext.setAttribute(contextAttribute, webCtx);
		jettyContext.start();

		if (log.isInfoEnabled()) {
			log.info("| Jetty Web Application Context started!");
		}
	}

}