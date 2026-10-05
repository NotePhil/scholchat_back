package cmr.notep.business.config;

import cmr.notep.business.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${front.endpoint}")
    private String frontEndpoint;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Client web (navigateur) : SockJS, origines du front uniquement.
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontEndpoint, "https://scholchat-front-1.onrender.com")
                .withSockJS();

        // Client mobile (React Native) : WebSocket brut, ws(s)://<hôte>/scholchat/ws/native.
        // Une app native n'envoie pas d'Origin fiable : le contrôle d'origine n'apporte rien ici,
        // la protection repose sur le JWT exigé à la trame CONNECT (WebSocketAuthInterceptor).
        registry.addEndpoint("/ws/native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // CONNECT : JWT obligatoire ; SUBSCRIBE/SEND : destinations autorisées selon l'utilisateur.
        registration.interceptors(webSocketAuthInterceptor);
    }
}
