package com.jpmh.curso.web.security.filter;

import com.jpmh.curso.persistence.service.CursoUserDetailService;
import com.jpmh.curso.web.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilterRequest extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CursoUserDetailService cursoUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        // Se valida que en el header venga el Authorization
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer")){
            // Como siempre debe tener el prefijo "Bearer ", se toma desde el caracter 7 (6 + espacio)
            String jwt = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);

            // Se valida que venga el usuario y que éste ya no se encuentre logeado
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = cursoUserDetailService.loadUserByUsername(username);
                // Se valida token
                if(jwtUtil.validateToken(jwt, userDetails)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    // Se manda el request para tener la info (hora que se logeo, en que navegador, en que so, etc)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
