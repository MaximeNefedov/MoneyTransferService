package ru.netology.money_transfer_service.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.netology.money_transfer_service.converters.TransactionDataConverter;
import ru.netology.money_transfer_service.converters.TransactionDataConverterRaw;
import ru.netology.money_transfer_service.models.transactions.TransactionData;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CustomParamResolver());
    }

    private static class CustomParamResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter methodParameter) {
            return methodParameter.getParameter().getType() == TransactionData.class;
        }

        @Override
        public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                      NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
            final var request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
            TransactionData transactionData = null;
            if (request != null && "POST".equalsIgnoreCase(request.getMethod())) {
                final var requestJson = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                transactionData = TransactionDataConverter.getTransactionData(requestJson);
            }
            return transactionData;
        }
    }
}
