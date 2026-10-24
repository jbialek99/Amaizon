package org.example.amaizon.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.resolvers.MarketResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MarketContextInterceptor implements HandlerInterceptor {

    private final MarketResolver marketResolver;

    public MarketContextInterceptor(MarketResolver marketResolver) {
        this.marketResolver = marketResolver;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest req,
                             @NonNull HttpServletResponse res,
                             @NonNull Object handler) {

      if(req.getAttribute(MarketResolver.REQ_ATTR) instanceof MarketContextDto)
          return true;

        MarketContextDto ctx = marketResolver.resolve(req, res);
        req.setAttribute(MarketResolver.REQ_ATTR, ctx);
        return true;
    }
}
