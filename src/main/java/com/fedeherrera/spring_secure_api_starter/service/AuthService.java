package com.fedeherrera.spring_secure_api_starter.service;

import com.fedeherrera.spring_secure_api_starter.dto.AdminCreateUserRequest;
import com.fedeherrera.spring_secure_api_starter.dto.AuthProviderEnum;
import com.fedeherrera.spring_secure_api_starter.dto.LoginRequest;
import com.fedeherrera.spring_secure_api_starter.dto.LoginResponse;
import com.fedeherrera.spring_secure_api_starter.dto.PublicRegisterRequest;
import com.fedeherrera.spring_secure_api_starter.entity.Role;
import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.entity.UserPrincipal;
import com.fedeherrera.spring_secure_api_starter.entity.VerificationToken;
import com.fedeherrera.spring_secure_api_starter.exception.AuthException;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifierService   googleTokenVerifierService;
    /**
     * Registro de usuario base
     * - Password encriptado
     * - ROLE_USER asignado
     * - enabled = false (requiere verificación)
     */
    public void registerPublic(PublicRegisterRequest request) {

        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();

        if (userService.existsByUsername(username)) {
            throw new RegistrationException("Username ya registrado.");
        }
        if (userService.existsByEmail(email)) {
            throw new RegistrationException("Email ya registrado.");
        }

        Role roleUser = roleService.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .roles(Set.of(roleUser))
                .build();

        userService.save(user);

        // 1️⃣ Generar token de verificación
    VerificationToken token = verificationService.createToken(user);
   

    // 2️⃣ Construir magic link
    String verificationLink = "http://localhost:3000/auth/verify?token=" + token.getToken();

    // 3️⃣ Enviar email
    emailService.sendEmail(
            user.getEmail(),
            "Verify your account",
            "Click the link to verify your account: " + verificationLink
    );

    }

    public void registerInternal(AdminCreateUserRequest request) {

        if (userService.existsByUsername(request.getUsername())) {
            throw new RegistrationException("Username ya registrado.");
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new RegistrationException("Email ya registrado.");
        }

        Set<Role> roles = request.getRoles().stream()
                .map(roleService::findByName)
                .map(opt -> opt.orElseThrow(() -> new RegistrationException("Invalid user data")))
                .collect(Collectors.toSet());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true) // admin puede habilitar
                .roles(roles)
                .build();

        userService.save(user);

        
    }

    public LoginResponse login(LoginRequest request) {

        try{
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        User user = userPrincipal.getUser();
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);
        return new LoginResponse(user.getUsername(), accessToken, refreshToken, user.getRoles().iterator().next().getName());
    }
    catch (BadCredentialsException e) {
        throw new AuthException("Credenciales inválidas");
    } catch (DisabledException e) {
        throw new AuthException("Usuario no verificado");
    }
    }
public LoginResponse loginWithGoogle(String googleToken) {
    try {
        // 1. Validar token de Google
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(googleToken);
        if (payload == null) {
            throw new AuthException("Token de Google inválido");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // 2. Buscar o crear usuario
        // Usamos .orElseGet para que sea mucho más limpio
        User user = userService.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, name));

        // 3. Verificar que el usuario esté habilitado
        if (!user.isEnabled()) {
            throw new AuthException("El usuario está deshabilitado");
        }

        // 4. Generar JWT tokens
        // Convertimos nuestra entidad User a UserPrincipal (que sí implementa UserDetails)
        UserDetails userDetails = new UserPrincipal(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // 5. Obtener el nombre del rol de forma segura
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("ROLE_USER");

        return new LoginResponse(
                user.getUsername(),
                accessToken,
                refreshToken,
                roleName
        );

    } catch (AuthException e) {
        // Re-lanzamos nuestra propia excepción para que el Handler la capture
        throw e;
    } catch (Exception e) {
        // Logueamos el error real para nosotros
        log.error("Error crítico en autenticación de Google: ", e);
        // Enviamos un mensaje genérico al cliente
        throw new AuthException("Error durante la autenticación con Google");
    }
}

    public User createGoogleUser(String email, String name) {
    // Split name into first and last name
    String[] names = name.split(" ", 2);
    String firstName = names[0];
    String lastName = names.length > 1 ? names[1] : "";
    
    // Create user with Google details
    User user = User.builder()
            .email(email)
            .username(email) // Using email as username for Google users
            .firstName(firstName)
            .lastName(lastName)
            .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password for Google users
            .enabled(true) // Google users are auto-verified
            .provider(AuthProviderEnum.GOOGLE)
            .build();
    
    // Add ROLE_USER by default
    Role userRole = roleService.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    user.setRoles(Set.of(userRole));
    
    return userService.save(user);
}

public LoginResponse refreshToken(String refreshToken) {
    // 1. Validar firma y extraer email sin ir a la DB aún
    if (!jwtService.isTokenSignatureValid(refreshToken)) {
        throw new AuthException("Refresh token inválido o expirado");
    }

    String userEmail = jwtService.extractUsername(refreshToken);

    // 2. Buscar usuario
    var user = userService.findByUsername(userEmail)
            .orElseThrow(() -> new AuthException("Usuario no encontrado"));

    // 3. Validación final (incluyendo el passwordChangedAt que hicimos antes)
    UserPrincipal principal = new UserPrincipal(user);
    if (!jwtService.isTokenValid(refreshToken, principal)) {
        throw new AuthException("Sesión inválida, por favor inicie sesión nuevamente");
    }

    // 4. Generar nuevo Access Token
    String accessToken = jwtService.generateToken(principal);

    return new LoginResponse(
            user.getUsername(),
            accessToken,
            refreshToken, // Reutilizamos el mismo Refresh Token
            user.getRoles().stream().findFirst().map(Role::getName).orElse("ROLE_USER")
    );
}


}
