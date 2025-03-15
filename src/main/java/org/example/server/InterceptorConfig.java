package org.example.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to register custom interceptors for the Spring MVC framework.
 * This class is annotated with @Configuration, indicating that it contains Spring configuration.
 */
@Configuration // Marks this class as a configuration class, enabling it to define beans and other settings
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private Interceptor customInterceptor; // Custom interceptor to be registered

    /**
     * Registers the custom interceptor and specifies the URL patterns it should apply to.
     * This method is part of the WebMvcConfigurer interface, which allows customization of Spring MVC behavior.
     *
     * @param registry the InterceptorRegistry used to register interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the custom interceptor and apply it to all URL patterns ("/**")
        registry.addInterceptor(customInterceptor).addPathPatterns("/**");
    }
}