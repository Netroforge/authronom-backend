package com.github.netroforge.authronom_backend.config;

import com.github.netroforge.authronom_backend.properties.CorsProperties;
import com.github.netroforge.authronom_backend.properties.DbSchedulerUiSecurityProperties;
import com.github.netroforge.authronom_backend.properties.SecurityProperties;
import com.github.netroforge.authronom_backend.properties.SpringdocSecurityProperties;
import com.github.netroforge.authronom_backend.service.*;
import com.github.netroforge.authronom_backend.service.dto.AuthorizationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.web.cors.CorsConfiguration;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new CookieHttpSessionIdResolver();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicResourceServerSecurityFilterChain(
            HttpSecurity http,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer
    ) throws Exception {
        return http
                .securityMatcher("/public/**")
                .cors(corsConfigurerCustomizer)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .sessionManagement((customizer) ->
                        customizer.sessionCreationPolicy(SessionCreationPolicy.NEVER)
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain swaggerSecurityFilterChain(
            HttpSecurity http,
            UserDetailsService swaggerUsers,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer
    ) throws Exception {
        return http
                .securityMatcher(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error"
                )
                .cors(corsConfigurerCustomizer)
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(swaggerUsers)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .sessionManagement((customizer) ->
                        customizer.sessionCreationPolicy(SessionCreationPolicy.NEVER)
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain dbSchedulerUiSecurityFilterChain(
            HttpSecurity http,
            UserDetailsService dbSchedulerUiUsers,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer
    ) throws Exception {
        return http
                .securityMatcher("/db-scheduler/**", "/db-scheduler-api/**")
                .cors(corsConfigurerCustomizer)
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(dbSchedulerUiUsers)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .sessionManagement((customizer) ->
                        customizer.sessionCreationPolicy(SessionCreationPolicy.NEVER)
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain authResourceServerSecurityFilterChain(
            HttpSecurity http,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer
    ) throws Exception {
        http
                .securityMatcher("/user/**", "/auth/**")
                .cors(corsConfigurerCustomizer)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        )
                );

        // Transfer access token in HTTP parameters of reqeust
        DefaultBearerTokenResolver tokenResolver = new DefaultBearerTokenResolver();
        tokenResolver.setAllowUriQueryParameter(true);

        http.oauth2ResourceServer(configurer -> {
            configurer.bearerTokenResolver(tokenResolver);
            configurer.jwt(Customizer.withDefaults());
        });
        return http.build();
    }

    @Bean
    @Order(5)
    public SecurityFilterChain registrationSecurityFilterChain(
            HttpSecurity http,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer
    ) throws Exception {
        return http
                .securityMatcher("/registration/**")
                .cors(corsConfigurerCustomizer)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .sessionManagement((customizer) ->
                        customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Order(6)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer,
            SecurityProperties securityProperties
    ) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .cors(corsConfigurerCustomizer)
                .csrf(AbstractHttpConfigurer::disable)
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                // Redirect to the OAuth 2.0 Login endpoint when not authenticated from the authorization endpoint
                .exceptionHandling((exceptions) ->
                        exceptions
                                .defaultAuthenticationEntryPointFor(
                                        new LoginUrlAuthenticationEntryPoint(securityProperties.getLoginUrlAuthenticationEntryPoint()),
                                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                                )
                );
        return http.build();
    }

    @Bean
    @Order(7)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            FormLoginAuthenticationSuccessHandler formLoginAuthenticationSuccessHandler,
            FormLoginAuthenticationFailureHandler formLoginAuthenticationFailureHandler,
            Oauth2LoginAuthenticationSuccessHandler oauth2LoginAuthenticationSuccessHandler,
            CustomOAuth2UserService customOAuth2UserService,
            CustomOidcUserService customOidcUserService,
            CustomUserDetailsService customUserDetailsService,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer,
            SecurityProperties securityProperties
    ) throws Exception {
        http
                .sessionManagement(session ->
                        session
                                // Optional: Set session timeout
                                .maximumSessions(1)
                                // Optional: Prevent new logins if max sessions reached
                                .maxSessionsPreventsLogin(true)
                )
                .cors(corsConfigurerCustomizer)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated()
                )
                // OAuth2 Login handles the redirect to the OAuth 2.0 Login endpoint
                // from the authorization server filter chain
                .oauth2Login(new Customizer<>() {
                    @Override
                    public void customize(OAuth2LoginConfigurer<HttpSecurity> httpSecurityOAuth2LoginConfigurer) {
                        httpSecurityOAuth2LoginConfigurer.userInfoEndpoint(userInfoEndpointConfig -> {
                            userInfoEndpointConfig.userService(customOAuth2UserService);
                            userInfoEndpointConfig.oidcUserService(customOidcUserService);
                        });
                        httpSecurityOAuth2LoginConfigurer.successHandler(oauth2LoginAuthenticationSuccessHandler);
                        httpSecurityOAuth2LoginConfigurer.loginPage(securityProperties.getOauth2LoginPage());
                        httpSecurityOAuth2LoginConfigurer.failureUrl(securityProperties.getOauth2LoginFailureUrl());
                    }
                })
                .cors(corsConfigurerCustomizer)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(new Customizer<>() {
                    @Override
                    public void customize(FormLoginConfigurer<HttpSecurity> httpSecurityFormLoginConfigurer) {
                        httpSecurityFormLoginConfigurer.usernameParameter("email");
                        httpSecurityFormLoginConfigurer.successHandler(formLoginAuthenticationSuccessHandler);
                        httpSecurityFormLoginConfigurer.failureHandler(formLoginAuthenticationFailureHandler);
                        httpSecurityFormLoginConfigurer.loginPage(securityProperties.getFormLoginPage());
                        httpSecurityFormLoginConfigurer.loginProcessingUrl(securityProperties.getFormLoginProcessingPath());
                    }
                })
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer(CorsProperties corsProperties) {
        return httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();

            corsConfiguration.setAllowedMethods(corsProperties.getAllowedMethods());
            corsConfiguration.setAllowedHeaders(corsProperties.getAllowedHeaders());
            corsConfiguration.setExposedHeaders(corsProperties.getExposedHeaders());
            corsConfiguration.setAllowCredentials(corsProperties.isAllowCredentials());
            corsConfiguration.setMaxAge(corsProperties.getMaxAge());

            // Either add allowOrigins or allowedOriginPatterns depending on the profile activated (local or production)
            if (corsProperties.getAllowedOriginPatterns() != null && !corsProperties.getAllowedOriginPatterns().isEmpty()) {
                corsConfiguration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
            } else if (corsProperties.getAllowedOrigins() != null && !corsProperties.getAllowedOrigins().isEmpty()) {
                corsConfiguration.setAllowedOrigins(corsProperties.getAllowedOrigins());
            }
            return corsConfiguration;
        });
    }

    @Bean
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(
            RedisTemplate<String, OAuth2AuthorizationConsent> redisTemplate,
            SecurityProperties securityProperties
    ) {
        return new RedisOAuth2AuthorizationConsentService(
                redisTemplate,
                securityProperties.getAuthorizationTtl()
        );
    }

    @Bean
    public OAuth2AuthorizationService oAuth2AuthorizationService(
            RedisTemplate<String, OAuth2Authorization> redisTemplate,
            RedisTemplate<String, AuthorizationInfo> redisTemplateAuthInfo,
            SecurityProperties securityProperties
    ) {
        return new RedisOAuth2AuthorizationService(
                redisTemplate,
                redisTemplateAuthInfo,
                securityProperties.getAuthorizationTtl()
        );
    }

    /**
     * More info:
     * https://docs.spring.io/spring-authorization-server/reference/core-model-components.html#oauth2-token-customizer
     * https://docs.spring.io/spring-authorization-server/reference/guides/how-to-userinfo.html#customize-id-token
     */
    @Bean
    public FederatedIdentityIdTokenCustomizer federatedIdentityIdTokenCustomizer() {
        return new FederatedIdentityIdTokenCustomizer();
    }

    @Bean
    public PasswordEncoder passwordEncoder(
            SecurityProperties securityProperties
    ) {
        return new BCryptPasswordEncoder(securityProperties.getBcryptPasswordEncoderStrength());
    }

    @Bean
    public UserDetailsService swaggerUsers(SpringdocSecurityProperties springdocSecurityProperties) {
        UserDetails admin = User.builder()
                .username(springdocSecurityProperties.getAdminUsername())
                .password(springdocSecurityProperties.getAdminPassword())
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public UserDetailsService dbSchedulerUiUsers(DbSchedulerUiSecurityProperties dbSchedulerUiSecurityProperties) {
        UserDetails admin = User.builder()
                .username(dbSchedulerUiSecurityProperties.getAdminUsername())
                .password(dbSchedulerUiSecurityProperties.getAdminPassword())
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
