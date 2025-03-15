package org.example.server;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.stereotype.Component;

/**
 * Aspect class to handle asynchronous operations in the CertificateController.
 * This class is annotated with @Aspect and @Component, making it a Spring-managed aspect.
 */
@Aspect // Marks this class as an Aspect, enabling it to define cross-cutting concerns
@Component // Marks this class as a Spring component, allowing it to be detected during component scanning
public class AsyncControllerAspect {

    /**
     * Advice method that wraps the execution of methods in the CertificateController with DeferredResult handling.
     * This advice is applied around all methods in the CertificateController class.
     *
     * @param joinPoint the ProceedingJoinPoint representing the method being intercepted
     * @return the result of the intercepted method, potentially wrapped in a DeferredResult
     * @throws Throwable if an error occurs during the execution of the intercepted method
     */
    @Around("execution(* org.example.server.CertificateController.*(..))") // Pointcut expression targeting all methods in CertificateController
    public Object wrapWithDeferredResult(ProceedingJoinPoint joinPoint) throws Throwable {
        // Proceed with the execution of the intercepted method
        Object result = joinPoint.proceed();

        // Check if the result is already an instance of DeferredResult
        if (result instanceof DeferredResult) {
            // If it is, return it directly without further modification
            return result;
        }

        // Otherwise, return the result as-is
        return result;
    }
}