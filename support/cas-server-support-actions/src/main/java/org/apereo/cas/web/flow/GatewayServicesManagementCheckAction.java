package org.apereo.cas.web.flow;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Performs an authorization check for the gateway request if there is no Ticket Granting Ticket.
 *
 * @author Scott Battaglia
 * @since 3.4.5
 */
@Slf4j
@RequiredArgsConstructor
public class GatewayServicesManagementCheckAction extends AbstractAction {
    private final ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext context) {
        val service = WebUtils.getService(context);
        val registeredService = this.servicesManager.findServiceBy(service);

        if (registeredService == null) {
            val msg = String.format("Unauthorized Service Access: Service [%s] does not match entries in service registry.", service.getId());
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }

        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            val msg = String.format("Access to service [%s] is disabled by the service registry.", service.getId());
            LOGGER.warn(msg);
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        return success();
    }
}
