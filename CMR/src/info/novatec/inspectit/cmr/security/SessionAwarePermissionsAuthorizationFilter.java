package info.novatec.inspectit.cmr.security;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

/**
 * This class is taken and modified from <a href="https://shiro.apache.org/static/1.2.4/shiro-web/cobertura/org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter.html" />
 * to handle the session id manually.
 * The originally class is under the Apache 2 license.
 * 
 * The authors who modified it are
 * @author Clemens Geibel
 * 
 * Filter that allows access if the current user has the permissions specified by the mapped value, or denies access
 * if the user does not have all of the permissions specified.
 *
 * @since 0.9
 */
public class SessionAwarePermissionsAuthorizationFilter extends AuthorizationFilter {

	/**
	 * Is the subject who created the request permitted?
	 * 
	 * @param request
	 *            Servlet request
	 * @param response
	 *            Servlet response
	 * @param mappedValue
	 *            Permissions
	 * @throws IOException
	 *             IOException
	 * @return Returns whether request has permission to proceed
	 * 
	 */
	@Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
		
		// This is the most relevant modified part, where the sessionid is extracted from the header and the subject is build manually.
		if (!(request instanceof HttpServletRequest)) {
			throw new IOException("Invalid http request.");
		}
		
        String sessionid = ((HttpServletRequest) request).getHeader("sessionid");
        Subject subject = new Subject.Builder(SecurityUtils.getSecurityManager()).sessionId((Serializable) sessionid).buildSubject();
        
        String[] perms = (String[]) mappedValue;

        boolean isPermitted = true;
        if (perms != null && perms.length > 0) {
            if (perms.length == 1) {
                if (!subject.isPermitted(perms[0])) {
                    isPermitted = false;
                }
            } else {
                if (!subject.isPermittedAll(perms)) {
                    isPermitted = false;
                }
            }
        }

        return isPermitted;
    }
	
}