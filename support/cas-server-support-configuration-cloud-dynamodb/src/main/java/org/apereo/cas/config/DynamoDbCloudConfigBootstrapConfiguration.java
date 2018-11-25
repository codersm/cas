package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.Properties;

/**
 * This is {@link DynamoDbCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("dynamoDbCloudConfigBootstrapConfiguration")
@Slf4j
@Getter
public class DynamoDbCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    private static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.dynamodb";
    private static final String TABLE_NAME = "DynamoDbCasProperties";

    private static final long PROVISIONED_THROUGHPUT = 10;

    private static Pair<String, Object> retrieveSetting(final Map<String, AttributeValue> entry) {
        val name = entry.get(ColumnNames.NAME.getColumnName()).getS();
        val value = entry.get(ColumnNames.VALUE.getColumnName()).getS();
        return Pair.of(name, value);
    }

    @SneakyThrows
    private static void createSettingsTable(final AmazonDynamoDB amazonDynamoDBClient, final boolean deleteTables) {
        val request = createCreateTableRequest();
        if (deleteTables) {
            val delete = new DeleteTableRequest(request.getTableName());
            LOGGER.debug("Sending delete request [{}] to remove table if necessary", delete);
            TableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
        }
        LOGGER.debug("Sending delete request [{}] to create table", request);
        TableUtils.createTableIfNotExists(amazonDynamoDBClient, request);
        LOGGER.debug("Waiting until table [{}] becomes active...", request.getTableName());
        TableUtils.waitUntilActive(amazonDynamoDBClient, request.getTableName());
        val describeTableRequest = new DescribeTableRequest().withTableName(request.getTableName());
        LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest);
        val tableDescription = amazonDynamoDBClient.describeTable(describeTableRequest).getTable();
        LOGGER.debug("Located newly created table with description: [{}]", tableDescription);
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private static CreateTableRequest createCreateTableRequest() {
        val name = ColumnNames.ID.getColumnName();
        return new CreateTableRequest()
            .withAttributeDefinitions(new AttributeDefinition(name, ScalarAttributeType.S))
            .withKeySchema(new KeySchemaElement(name, KeyType.HASH))
            .withProvisionedThroughput(new ProvisionedThroughput(PROVISIONED_THROUGHPUT, PROVISIONED_THROUGHPUT))
            .withTableName(TABLE_NAME);
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();

        try {
            val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            val amazonDynamoDBClient = builder.build(AmazonDynamoDBClient.builder(), AmazonDynamoDB.class);
            val preventTableCreationOnStartup = builder.getSetting("preventTableCreationOnStartup", Boolean.class);
            if (!preventTableCreationOnStartup) {
                createSettingsTable(amazonDynamoDBClient, false);
            }
            val scan = new ScanRequest(TABLE_NAME);
            LOGGER.debug("Scanning table with request [{}]", scan);
            val result = amazonDynamoDBClient.scan(scan);
            LOGGER.debug("Scanned table with result [{}]", scan);

            result.getItems()
                .stream()
                .map(DynamoDbCloudConfigBootstrapConfiguration::retrieveSetting)
                .forEach(p -> props.put(p.getKey(), p.getValue()));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }

    @Getter
    private enum ColumnNames {

        ID("id"), NAME("name"), VALUE("value");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }
}
