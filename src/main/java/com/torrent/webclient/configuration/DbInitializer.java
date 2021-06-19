package com.torrent.webclient.configuration;

import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.r2dbc.OptionsCapableConnectionFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.security.Security;

@Configuration
@EnableR2dbcRepositories
public class DbInitializer extends AbstractR2dbcConfiguration {
    @Value(value = "${h2.data.filepath}")
    private String h2DataFilePath;
    @Value(value = "${h2.data.username}")
    private String h2UserName;
    @Value(value = "${h2.data.password}")
    private String h2Password;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.USER, h2UserName)
                .option(ConnectionFactoryOptions.DRIVER, "h2")
                .option(ConnectionFactoryOptions.PASSWORD, h2Password)
                .option(ConnectionFactoryOptions.PROTOCOL, "file")
                .option(Option.valueOf(H2ConnectionOption.DB_CLOSE_DELAY.name()), "-1")
                .option(Option.valueOf(H2ConnectionOption.DB_CLOSE_ON_EXIT.name()), "true")
                .option(ConnectionFactoryOptions.DATABASE, String.format("r2dbc:h2:file//%s;DB_CLOSE_DELAY=-1;", h2DataFilePath))
                .build();
        return new OptionsCapableConnectionFactory(options, ConnectionFactories.get(options));
    }

    @Bean
    public TomcatServletWebServerFactory tomcatEmbeddedServletContainerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public void init() {
        String key = "crypto.policy";
        String value = "unlimited";
        Security.setProperty(key, value);
    }

//    @Bean
//    public DataSourceInitializer dataSourceInitializer(@Qualifier("dataSource") final DataSource dataSource) {
//        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
//        resourceDatabasePopulator.addScript(new ClassPathResource("/data.sql"));
//        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
//        dataSourceInitializer.setDataSource(dataSource);
//        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
//        return dataSourceInitializer;
//    }
}
