package org.example.amaizon.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public FilterRegistrationBean<StaffMarketAccessFilter> staffMarketAccessFilterRegistration(
            StaffMarketAccessFilter filter
    ) {
        FilterRegistrationBean<StaffMarketAccessFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    // =========================
    // STAFF
    // =========================
    @Bean
    @Order(1)
    SecurityFilterChain staffChain(
            HttpSecurity http,
            StaffAuthenticationSuccessHandler staffSuccessHandler,
            StaffAuthenticationFailureHandler staffFailureHandler,
            StaffMarketAccessFilter staffMarketAccessFilter
    ) throws Exception {

        return http
                .securityMatcher("/staff/**", "/home/staff", "/staff/logout", "/home-staff", "/home-staff/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/home/staff", "/staff/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/home/staff")
                        .loginProcessingUrl("/home/staff")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(staffSuccessHandler)
                        .failureHandler(staffFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/staff/logout")
                        .logoutSuccessUrl("/home/staff?logout=1")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .addFilterAfter(staffMarketAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(Customizer.withDefaults())
                .build();
    }

    // =========================
    // USER
    // =========================
    @Bean
    @Order(2)
    SecurityFilterChain userChain(
            HttpSecurity http,
            UserAuthenticationSuccessHandler userSuccessHandler,
            UserAuthenticationFailureHandler userFailureHandler
    ) throws Exception {

        return http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/", "/home",
                                "/p/**",
                                "/login", "/register",
                                "/auth/**",
                                "/checkout", "/checkout/**",
                                "/cart", "/cart/**",
                                "/orders/**",
                                "/about", "/about/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
                        ).permitAll()


                        .requestMatchers("/staff/**", "/home/staff", "/staff/logout", "/home-staff/**").denyAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(userSuccessHandler)
                        .failureHandler(userFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=1")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/cart/**"))
                .build();
    }
}
