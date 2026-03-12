package com.kareem.GitMatch.security;

import com.kareem.GitMatch.core.entity.AppUser;
import com.kareem.GitMatch.core.enums.AuthProvider;
import com.kareem.GitMatch.core.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles the OAuth2 callback after a user successfully authenticates with GitHub or Google.
 * Creates or updates the AppUser, generates a JWT, and redirects to the mobile app deep link.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${gitmatch.security.app-redirect-uri:gitmatch://login}")
    private String appRedirectUri;

    public OAuth2AuthenticationSuccessHandler(AppUserRepository appUserRepository,
                                              JwtService jwtService,
                                              OAuth2AuthorizedClientService authorizedClientService) {
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        AuthProvider provider = registrationId.equalsIgnoreCase("github")
                ? AuthProvider.GITHUB
                : AuthProvider.GOOGLE;

        log.info("OAuth2 login success: provider={}, attributes={}", provider, oAuth2User.getAttributes().keySet());

        AppUser user;

        if (provider == AuthProvider.GITHUB) {
            user = handleGitHubLogin(oAuth2User, oauthToken);
        } else {
            user = handleGoogleLogin(oAuth2User);
        }

        String jwt = jwtService.generateToken(user.getId(), user.getEmail(), provider.name());

        String redirectUrl = appRedirectUri + "?token=" + jwt;
        log.info("Redirecting to app: {}", appRedirectUri + "?token=***");

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private AppUser handleGitHubLogin(OAuth2User oAuth2User, OAuth2AuthenticationToken oauthToken) {
        String githubUsername = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");
        String displayName = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        // Retrieve the OAuth2 access token from the authorized client
        String accessToken = null;
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());
            if (client != null && client.getAccessToken() != null) {
                accessToken = client.getAccessToken().getTokenValue();
            }
        } catch (Exception e) {
            log.warn("Could not retrieve GitHub access token from authorized client: {}", e.getMessage());
        }

        AppUser user = appUserRepository.findByGithubUsername(githubUsername).orElse(null);

        if (user == null) {
            user = new AppUser(githubUsername, email, displayName, avatarUrl);
            user.setAuthProvider(AuthProvider.GITHUB);
            if (accessToken != null) {
                user.setGithubAccessToken(accessToken);
            }
            user = appUserRepository.save(user);
            log.info("Created new GitHub user: id={}, username={}", user.getId(), githubUsername);
        } else {
            // Update profile info on each login
            user.setEmail(email);
            user.setDisplayName(displayName);
            user.setAvatarUrl(avatarUrl);
            user.setAuthProvider(AuthProvider.GITHUB);
            if (accessToken != null) {
                user.setGithubAccessToken(accessToken);
            }
            user = appUserRepository.save(user);
            log.info("Updated existing GitHub user: id={}, username={}", user.getId(), githubUsername);
        }

        return user;
    }

    private AppUser handleGoogleLogin(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String displayName = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        AppUser user = appUserRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new AppUser(null, email, displayName, avatarUrl);
            user.setAuthProvider(AuthProvider.GOOGLE);
            user = appUserRepository.save(user);
            log.info("Created new Google user: id={}, email={}", user.getId(), email);
        } else {
            user.setDisplayName(displayName);
            user.setAvatarUrl(avatarUrl);
            user.setAuthProvider(AuthProvider.GOOGLE);
            user = appUserRepository.save(user);
            log.info("Updated existing Google user: id={}, email={}", user.getId(), email);
        }

        return user;
    }
}
