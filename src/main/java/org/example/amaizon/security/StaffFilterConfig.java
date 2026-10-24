package org.example.amaizon.security;

import org.example.amaizon.resolvers.MarketResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaffFilterConfig {

    @Bean
    public StaffMarketAccessFilter staffMarketAccessFilter(
            MarketResolver marketResolver,
            AuthorizationService authorizationService,
            org.example.amaizon.repositories.UserRepository userRepository) {
        return new StaffMarketAccessFilter(marketResolver, authorizationService, userRepository);
    }
}
