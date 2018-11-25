package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.temporal.ChronoUnit;

/**
 * This is {@link ConfirmConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConfirmConsentAction extends AbstractConsentAction {

    public ConfirmConsentAction(final ServicesManager servicesManager,
                                final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                final ConsentEngine consentEngine,
                                final CasConfigurationProperties casProperties) {
        super(casProperties, servicesManager, authenticationRequestServiceSelectionStrategies, consentEngine);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val webService = WebUtils.getService(requestContext);
        val service = this.authenticationRequestServiceSelectionStrategies.resolveService(webService);
        val registeredService = getRegisteredServiceForConsent(requestContext, service);
        val authentication = WebUtils.getAuthentication(requestContext);
        val optionValue = Integer.parseInt(request.getParameter("option"));
        val option = ConsentReminderOptions.valueOf(optionValue);

        val reminder = Long.parseLong(request.getParameter("reminder"));
        val reminderTimeUnit = request.getParameter("reminderTimeUnit");
        val unit = ChronoUnit.valueOf(reminderTimeUnit.toUpperCase());

        consentEngine.storeConsentDecision(service, registeredService, authentication, reminder, unit, option);
        return new EventFactorySupport().success(this);
    }
}
