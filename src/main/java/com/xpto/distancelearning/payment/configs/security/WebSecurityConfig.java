package com.xpto.distancelearning.payment.configs.security;

import com.xpto.distancelearning.payment.configs.security.jwt.AuthenticationJwtFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
// @EnableGlobalMethodSecurity(prePostEnabled = true) // Setting global config of the authentication manager. This is used to enable the @PreAuthorize annotation. // Commenting this line and using the @EnableMethodSecurity annotation.
@EnableMethodSecurity
@EnableWebSecurity
public class WebSecurityConfig { // henrique: Maybe the class name should be SecurityConfig

    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public AuthenticationJwtFilter authenticationJwtFilter() {
        return new AuthenticationJwtFilter();
    }

    /**
     * This is used to set the role hierarchy and I is set automatically by the Spring Security framework.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        // This is the deprecated way to set the role hierarchy.
//        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
//        String hierarchy = "ROLE_ADMIN > ROLE_INSTRUCTOR \n ROLE_INSTRUCTOR > ROLE_STUDENT \n ROLE_STUDENT > ROLE_USER";
//        roleHierarchy.setHierarchy(hierarchy);
//        return roleHierarchy;

        // This is the new way to set the role hierarchy. See https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html#authz-hierarchical-roles
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("INSTRUCTOR")
                .role("INSTRUCTOR").implies("STUDENT")
                .role("STUDENT").implies("USER")
                .build();
    }

//    // This method was added by Michelli at the end of the course.
//    // The idea is to set the role hierarchy, which means that the ADMIN role implies the INSTRUCTOR role, which implies the STUDENT role, which implies the USER role.
//    // Henrique: I am not sure if this is necessary. I think the hierarchy is already set by the RoleHierarchyImpl class and was working properly.
//    @Bean
//    public DefaultMethodSecurityExpressionHandler expressionHandler() {
//        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
//        expressionHandler.setRoleHierarchy(roleHierarchy());
//        return expressionHandler;
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Allow users/clients (ie Postman, frontend, etc.) to access the error pages without the need of authentication.
                        // Without this line, the user/client would need to authenticate to access the error pages and the return would be a 401 Unauthorized error.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable());

        http.addFilterBefore(authenticationJwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ====================================================================================================================
    // This is my implementation of the authenticationManagerBean method. I am not extending the WebSecurityConfigurerAdapter class.
    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }
//    // The method below is the implementation suggested by Michelli Brito, during the course.
//    @Bean
//    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration auth) throws Exception {
//        return auth.getAuthenticationManager();
//    }
    // ====================================================================================================================

//    This method is using the InMemoryUserDetailsManager class to create a user with the username "admin" and password "123456.
//    However, this is not the best way to create a user. The best way is to use the UserDetailsServiceImpl class to create a user.
//    @Bean
//    public InMemoryUserDetailsManager userDetailsService() {
//        UserDetails user = User.withUsername("admin")
//                .password(passwordEncoder().encode("123456"))
//                .roles("ADMIN")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }

    // This is used on the AuthenticationController class to encrypt the password before saving it.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}