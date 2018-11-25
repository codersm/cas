package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ScriptingUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

/**
 * Return a collection of allowed attributes for the principal, but additionally,
 * offers the ability to rename attributes on a per-service level.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ReturnMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;

    private Map<String, Object> allowedAttributes = new TreeMap<>();

    private static void mapSingleAttributeDefinition(final String attributeName, final String mappedAttributeName,
                                                     final Object attributeValue, final Map<String, Object> resolvedAttributes,
                                                     final Map<String, Object> attributesToRelease) {
        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(mappedAttributeName);
        val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(mappedAttributeName);
        if (matcherInline.find()) {
            LOGGER.debug("Mapped attribute [{}] is an inlined groovy script", mappedAttributeName);
            processInlineGroovyAttribute(resolvedAttributes, attributesToRelease, matcherInline, attributeName);
        } else if (matcherFile.find()) {
            LOGGER.debug("Mapped attribute [{}] is an external groovy script", mappedAttributeName);
            processFileBasedGroovyAttributes(resolvedAttributes, attributesToRelease, matcherFile, attributeName);
        } else {
            if (attributeValue != null) {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                    attributeName, mappedAttributeName);
                attributesToRelease.put(mappedAttributeName, attributeValue);
            } else {
                LOGGER.warn("Could not find value for mapped attribute [{}] that is based off of [{}] in the allowed attributes list. "
                        + "Ensure the original attribute [{}] is retrieved and contains at least a single value. Attribute [{}] "
                        + "will and can not be released without the presence of a value.", mappedAttributeName, attributeName,
                    attributeName, mappedAttributeName);
            }
        }
    }

    private static void processFileBasedGroovyAttributes(final Map<String, Object> resolvedAttributes,
                                                         final Map<String, Object> attributesToRelease,
                                                         final Matcher matcherFile, final String key) {
        try {
            LOGGER.debug("Found groovy script to execute for attribute mapping [{}]", key);
            val file = new File(matcherFile.group(2));
            val script = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            val result = getGroovyAttributeValue(script, resolvedAttributes);
            if (result != null) {
                LOGGER.debug("Mapped attribute [{}] to [{}] from script", key, result);
                attributesToRelease.put(key, result);
            } else {
                LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", key);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void processInlineGroovyAttribute(final Map<String, Object> resolvedAttributes,
                                                     final Map<String, Object> attributesToRelease,
                                                     final Matcher matcherInline, final String attributeName) {
        LOGGER.debug("Found inline groovy script to execute for attribute mapping [{}]", attributeName);
        val result = getGroovyAttributeValue(matcherInline.group(1), resolvedAttributes);
        if (result != null) {
            LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
            attributesToRelease.put(attributeName, result);
        } else {
            LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
        }
    }

    private static Object getGroovyAttributeValue(final String groovyScript, final Map<String, Object> resolvedAttributes) {
        val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
        return ScriptingUtils.executeGroovyShellScript(groovyScript, args, Object.class);
    }

    /**
     * Gets the allowed attributes.
     *
     * @return the allowed attributes
     */
    public Map<String, Object> getAllowedAttributes() {
        return new TreeMap<>(this.allowedAttributes);
    }

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attrs, final RegisteredService service) {
        val resolvedAttributes = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = new HashMap<String, Object>();
        /*
         * Map each entry in the allowed list into an array first
         * by the original key, value and the original entry itself.
         * Then process the array to populate the map for allowed attributes
         */
        this.allowedAttributes.forEach((attributeName, value) -> {
            val mappedAttributes = CollectionUtils.wrap(value);
            LOGGER.debug("Attempting to map allowed attribute name [{}]", attributeName);
            val attributeValue = resolvedAttributes.get(attributeName);
            mappedAttributes.forEach(mapped -> {
                val mappedAttributeName = mapped.toString();
                LOGGER.debug("Mapping attribute [{}] to [{}] with value [{}]", attributeName, mappedAttributeName, attributeValue);
                mapSingleAttributeDefinition(attributeName, mappedAttributeName, attributeValue, resolvedAttributes, attributesToRelease);
            });
        });
        return attributesToRelease;
    }

}
