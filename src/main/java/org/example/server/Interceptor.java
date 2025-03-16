package org.example.server;

import org.example.database.DataBase;
import org.example.utility.Utils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom interceptor to handle pre-processing and post-processing of HTTP requests.
 * This class implements the HandlerInterceptor interface and is annotated with @Component,
 * making it a Spring-managed component.
 */
@Component // Marks this class as a Spring-managed component, allowing it to be automatically detected
public class Interceptor implements HandlerInterceptor {

    private final DataBase dataBase; // Database service for interacting with the database

    /**
     * Constructor to inject the DataBase dependency.
     * @param dataBase the database service instance
     */
    public Interceptor(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    /**
     * Intercepts the request before it reaches the controller.
     * This method is called during the pre-handle phase of the request lifecycle.
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @param handler the handler object (e.g., the controller method) that will process the request
     * @return true to continue processing the request, false to stop further processing
     * @throws Exception if an error occurs during processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Insert the hashed client IP address as a new user in the database
        dataBase.insertNewUser(Utils.calculateHash(request.getRemoteAddr()));

        // Log the intercepted request URL for debugging purposes
        //System.out.println("CustomInterceptor: Before controller - URL = " + request.getRequestURL());

        return true; // Continue processing the request
    }

    /**
     * Intercepts the request after the controller has processed it and the response has been sent.
     * This method is called during the after-completion phase of the request lifecycle.
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @param handler the handler object (e.g., the controller method) that processed the request
     * @param ex an exception thrown during request processing, or null if no exception occurred
     * @throws Exception if an error occurs during processing
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // Log that the response has been sent for debugging purposes
        //System.out.println("CustomInterceptor: After controller - Response sent");
    }
}