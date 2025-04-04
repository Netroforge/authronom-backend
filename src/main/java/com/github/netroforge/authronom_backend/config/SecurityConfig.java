package com.github.netroforge.authronom_backend.config;

import com.github.netroforge.authronom_backend.properties.CorsProperties;
import com.github.netroforge.authronom_backend.properties.SecurityProperties;
import com.github.netroforge.authronom_backend.repository.UserRepository;
import com.github.netroforge.authronom_backend.service.CustomUserDetailsService;
import com.github.netroforge.authronom_backend.service.FederatedIdentityIdTokenCustomizer;
import com.github.netroforge.authronom_backend.service.FormLoginAuthenticationSuccessHandler;
import com.github.netroforge.authronom_backend.service.Oauth2LoginAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

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
                        customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain authResourceServerSecurityFilterChain(
            HttpSecurity http,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer
    ) throws Exception {
        http
                .securityMatcher("/auth/**")
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
    @Order(3)
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
    @Order(4)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer,
            SecurityProperties securityProperties
    ) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .cors(corsConfigurerCustomizer)
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
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
    @Order(5)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            FormLoginAuthenticationSuccessHandler formLoginAuthenticationSuccessHandler,
            Oauth2LoginAuthenticationSuccessHandler oauth2LoginAuthenticationSuccessHandler,
            Customizer<CorsConfigurer<HttpSecurity>> corsConfigurerCustomizer,
            SecurityProperties securityProperties
    ) throws Exception {
        http
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
                        httpSecurityFormLoginConfigurer.loginPage(securityProperties.getFormLoginPage());
                        httpSecurityFormLoginConfigurer.failureUrl(securityProperties.getFormLoginFailureUrl());
                        httpSecurityFormLoginConfigurer.loginProcessingUrl(securityProperties.getFormLoginProcessingPath());
                    }
                });

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
    public CustomUserDetailsService users(
            UserRepository userRepository
    ) {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder(
            SecurityProperties securityProperties
    ) {
        return new BCryptPasswordEncoder(securityProperties.getBcryptPasswordEncoderStrength());
    }
}
