package com.nexus.backend.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Embrulha o DataSource já auto-configurado pelo Spring Boot (Hikari, com
 * toda a configuração/métricas que o Boot aplica) num TenantAwareDataSource,
 * sem precisar declarar o bean manualmente e perder esse auto-config.
 */
@Configuration
public class TenantDataSourceConfig {

    @Bean
    static BeanPostProcessor tenantAwareDataSourcePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSource dataSource && !(bean instanceof TenantAwareDataSource) && "dataSource".equals(beanName)) {
                    return new TenantAwareDataSource(dataSource);
                }
                return bean;
            }
        };
    }
}
