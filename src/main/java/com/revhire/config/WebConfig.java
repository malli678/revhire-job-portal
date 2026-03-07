package com.revhire.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig is used to configure Spring MVC settings for the application.
 *
 * Responsibilities:
 * - Configure view controllers if required.
 * - Configure resource handlers for serving static files.
 *
 * In this project, it is mainly used to expose uploaded files so they can be
 * accessed through HTTP URLs.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures simple automated controllers that map URLs directly to views.
     *
     * In this project, no direct view mappings are defined because the
     * HomeController handles the landing page request.
     *
     * @param registry registry used to add view controller mappings
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Removed redirect to allow HomeController to serve landing page
    }

    /**
     * Configures resource handlers used to serve static resources.
     *
     * This configuration allows files stored in the "uploads" directory
     * on the server to be accessed using the URL pattern "/uploads/**".
     *
     * Example:
     * If a file is stored as uploads/resume.pdf
     * it can be accessed using: http://localhost:8080/uploads/resume.pdf
     *
     * @param registry registry used to register resource handlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}