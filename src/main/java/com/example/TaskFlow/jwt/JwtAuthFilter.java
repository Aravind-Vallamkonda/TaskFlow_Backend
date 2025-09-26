package com.example.TaskFlow.jwt;

import java.util.List;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import com.example.TaskFlow.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

// JWT authentication filter implementation
// This filter will intercept requests to validate JWT tokens
// and set the authentication context accordingly.
//This will work between the client and server to ensure secure communication.
//This class will be added to the security filter chain in SecurityConfig.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository){
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean shouldNotFilter(HttpServletRequest req){
        String uri = req.getRequestURI();
        return uri.equals("/auth/login") ||
                uri.equals("/auth/identify") ||
                uri.equals("/auth/register") ||
                uri.equals("/auth/refresh") ||
                uri.equals("/v3/api-docs.yaml") ||
                uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/swagger-ui");

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header : "+authHeader);
        String auth_token = jwtService.extractTokenFromHeader(authHeader).orElse(null);
        System.out.println("Auth_Token : "+auth_token);
        try{
            if(auth_token != null){
                Claims claims = jwtService.parse(auth_token);
                // Block if token is expired
                if (jwtService.isTokenExpired(claims)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                String username = jwtService.extractUsername(claims);
                if(username !=  null || SecurityContextHolder.getContext().getAuthentication() == null){
                    User user = userRepository.findByUsername(username).orElse(null);
                    if(user ==null ){
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    // We create a List of Authorities from the claims objects
                    // that will be set into the auth token for Spring Security's internal Memory
                    // We are Using Java 8 Streams here for decreasing the code length
                    List<SimpleGrantedAuthority> auths = jwtService.roles(claims)
                            .stream().map(SimpleGrantedAuthority::new).toList();

                    // SecurityContextHolder is the local Memory of the Spring Security.
                    // It can be used to store the username and his roles or authentication related Data.
                    // This is stored in thread memory of each request/thread.
                    // The advantage of using this is the application is not required to parse the JWT again on the Internal Controllers to
                    // to know the user specific details.
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username,null,auths);
                    SecurityContext sc = SecurityContextHolder.createEmptyContext();
                    sc.setAuthentication(auth);
                    SecurityContextHolder.setContext(sc);
                }
            } else {
                // No token provided, block
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // Only call filterChain if authentication is set
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
        }
        // Otherwise, do not continue the filter chain
    }
}
