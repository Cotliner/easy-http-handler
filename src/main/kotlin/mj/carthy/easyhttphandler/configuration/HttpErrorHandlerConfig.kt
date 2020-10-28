package mj.carthy.easyhttphandler.configuration

import mj.carthy.easyhttphandler.handler.CustomResponseExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration class HttpErrorHandlerConfig {
    @Bean fun customResponseExceptionHandler(): CustomResponseExceptionHandler = CustomResponseExceptionHandler()
}