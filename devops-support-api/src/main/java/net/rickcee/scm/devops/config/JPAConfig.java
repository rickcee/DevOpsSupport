/**
 * 
 */
package net.rickcee.scm.devops.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author rickcee
 *
 */
@Configuration
@ConditionalOnClass(DataSource.class)
@ComponentScan(basePackages = { "net.rickcee.scm.devops.model" })
@EnableJpaRepositories(basePackages = {"net.rickcee.scm.devops.repository"})
@EnableTransactionManagement
public class JPAConfig {
	
//	@Autowired
//	private DataSource dataSource;
	
//	@Bean
//	@ConditionalOnBean(name = "dataSource")
//	@ConditionalOnMissingBean
//	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//		em.setDataSource(dataSource);
//		em.setPackagesToScan("net.rickcee.scm.devops.model");
//		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
////		if (additionalProperties() != null) {
////			em.setJpaProperties(additionalProperties());
////		}
//		return em;
//	}
//
//	@Bean
//	public JpaVendorAdapter jpaVendorAdapter() {
//	    HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
//	    hibernateJpaVendorAdapter.setShowSql(true);
//	    hibernateJpaVendorAdapter.setGenerateDdl(true); //Auto creating scheme when true
//	    hibernateJpaVendorAdapter.setDatabase(Database.H2);//Database type
//	    return hibernateJpaVendorAdapter;
//	}
//	
//	@Bean
//	@ConditionalOnMissingBean(type = "JpaTransactionManager")
//	JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
//		JpaTransactionManager transactionManager = new JpaTransactionManager();
//		transactionManager.setEntityManagerFactory(entityManagerFactory);
//		return transactionManager;
//	}
}
