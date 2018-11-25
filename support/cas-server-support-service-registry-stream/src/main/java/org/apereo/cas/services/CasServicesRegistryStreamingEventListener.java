package org.apereo.cas.services;

import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

/**
 * This is {@link CasServicesRegistryStreamingEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasServicesRegistryStreamingEventListener {
    private final CasRegisteredServiceStreamPublisher publisher;

    /**
     * Handle cas registered service loaded event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRegisteredServiceLoadedEvent(final CasRegisteredServiceLoadedEvent event) {
        LOGGER.trace("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event);
    }

    /**
     * Handle cas registered service saved event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRegisteredServiceSavedEvent(final CasRegisteredServiceSavedEvent event) {
        LOGGER.trace("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event);
    }

    /**
     * Handle cas registered service deleted event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRegisteredServiceDeletedEvent(final CasRegisteredServiceDeletedEvent event) {
        LOGGER.trace("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event);
    }
}
