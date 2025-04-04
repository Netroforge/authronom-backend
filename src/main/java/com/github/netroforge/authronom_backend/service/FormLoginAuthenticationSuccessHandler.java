package com.github.netroforge.authronom_backend.service;

import com.github.netroforge.authronom_backend.controller.dto.FormLoginAuthenticationSuccessDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public final class FormLoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final HttpMessageConverter<Object> converter = new MappingJackson2HttpMessageConverter();
    private final HttpSessionRequestCache httpSessionRequestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        SavedRequest savedRequest = httpSessionRequestCache.getRequest(request, response);
        if (savedRequest != null) {
            httpSessionRequestCache.removeRequest(request, response);
            clearAuthenticationAttributes(request);
        }

        converter.write(
                new FormLoginAuthenticationSuccessDto(true),
                MediaType.APPLICATION_JSON,
                new ServletServerHttpResponse(response)
        );
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
