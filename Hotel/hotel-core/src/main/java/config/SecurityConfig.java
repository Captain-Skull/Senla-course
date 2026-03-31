package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import hotel.security.CustomAccessDeniedHandler;
import hotel.security.JwtAuthenticationEntryPoint;
import hotel.security.JwtAuthenticationFilter;
import hotel.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@PropertySource("classpath:security.properties")
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            UserDetailsService userDetailsService,
            JwtTokenProvider jwtTokenProvider,
            ObjectMapper objectMapper
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public JwtAuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public CustomAccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/api/rooms/**", HttpMethod.GET.name())).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/rooms/**/checkIn", HttpMethod.POST.name())).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/rooms/**", HttpMethod.POST.name())).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/rooms/**", HttpMethod.PATCH.name())).hasRole("ADMIN")

                        .requestMatchers(new AntPathRequestMatcher("/api/guests/**", HttpMethod.GET.name())).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/guests/**", HttpMethod.POST.name())).authenticated()

                        .requestMatchers(new AntPathRequestMatcher("/api/services/**", HttpMethod.GET.name())).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/services/**", HttpMethod.POST.name())).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/services/**", HttpMethod.PATCH.name())).hasRole("ADMIN")

                        .requestMatchers(new AntPathRequestMatcher("/api/hotel/**", HttpMethod.GET.name())).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/hotel/**", HttpMethod.POST.name())).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}