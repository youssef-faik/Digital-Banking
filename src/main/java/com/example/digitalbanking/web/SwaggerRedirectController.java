package com.example.digitalbanking.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to redirect users to the Swagger UI documentation
 */
@Controller
public class SwaggerRedirectController {

    /**
     * Redirects the root path to the Swagger UI
     * This makes the API documentation easily accessible
     * 
     * @return redirect to Swagger UI
     */
    @GetMapping("/api-docs")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui.html";
    }
}
