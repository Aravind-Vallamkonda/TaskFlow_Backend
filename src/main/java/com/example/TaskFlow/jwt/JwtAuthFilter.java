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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

// JWT authentication filter implementation
// This filter will intercept requests to validate JWT tokens
// and set the authentication context accordingly.
//This will work between the client and server to ensure secure communication.
//This class will be added to the security filter chain in SecurityConfig.
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository){
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }


    public boolean shouldNotFilter(HttpServletRequest req){
        String uri = req.getRequestURI();
        return uri.equals("/auth/login") ||
                uri.equals("/auth/identify");

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        String authHeader = request.getHeader("Authorization");
        String auth_token = jwtService.extractTokenFromHeader(authHeader).orElse(null);
        try{
            if(auth_token != null){
                Claims claims = jwtService.parse(auth_token);
                String username = jwtService.extractUsername(claims);
                if(username ==  null || SecurityContextHolder.getContext().getAuthentication() == null){
                    User user = userRepository.findByUsername(username).orElse(null);
                    if(user !=null ){
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
             //   List<SimpleGrantedAuthority> auths = jwtService.roles(c).stream()
              //          .map(SimpleGrantedAuthority::new).toList();

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username,null);
                    SecurityContext sc = SecurityContextHolder.createEmptyContext();
                    sc.setAuthentication(auth);
                    SecurityContextHolder.setContext(sc);
                }
            }
        }catch(Exception e){

        }
        // Implementation for JWT authentication filtering
        filterChain.doFilter(request, response);
    }
}
