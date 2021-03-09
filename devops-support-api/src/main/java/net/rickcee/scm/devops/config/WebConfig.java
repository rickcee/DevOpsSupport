/**
 * 
 */
package net.rickcee.scm.devops.config;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * @author rickcee
 *
 */
@Configuration
@EnableWebMvc
//@EnableAutoConfiguration
//@ComponentScan(basePackages = { "net.rickcee.scm.devops" })
public class WebConfig implements WebMvcConfigurer {

	@Value("${server.timeout}")
	private Integer sessionTimeout;
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/DevOpsSupportAPI").setViewName("DevOpsSupportAPI");
		registry.addViewController("/login").setViewName("login");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
	}

	@Bean
	public ViewResolver getViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		return resolver;
	}
	
	@Bean
	public HttpSessionListener httpSessionListener() {
		return new HttpSessionListener() {

			@Override
			public void sessionCreated(HttpSessionEvent se) {
				se.getSession().setMaxInactiveInterval(sessionTimeout);
			}

			@Override
			public void sessionDestroyed(HttpSessionEvent se) {
				HttpSessionListener.super.sessionDestroyed(se);
			}
			
		};
	}

}
