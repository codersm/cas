package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.flow.OAuth20RegisteredServiceUIAction;
import org.apereo.cas.support.oauth.web.flow.OAuth20WebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasOAuthWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casOAuthWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthWebflowConfiguration implements CasWebflowExecutionPlanConfigurer {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> logoutFlowDefinitionRegistry;

    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private ObjectProvider<AuthenticationServiceSelectionStrategy> oauth20AuthenticationServiceSelectionStrategy;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "oauth20LogoutWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer oauth20LogoutWebflowConfigurer() {
        val c = new OAuth20WebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry.getIfAvailable(),
            oauth20RegisteredServiceUIAction(), applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry.getIfAvailable());
        return c;
    }

    @ConditionalOnMissingBean(name = "oauth20RegisteredServiceUIAction")
    @Bean
    public Action oauth20RegisteredServiceUIAction() {
        return new OAuth20RegisteredServiceUIAction(servicesManager.getIfAvailable(), oauth20AuthenticationServiceSelectionStrategy.getIfAvailable());
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(oauth20LogoutWebflowConfigurer());
    }
}
