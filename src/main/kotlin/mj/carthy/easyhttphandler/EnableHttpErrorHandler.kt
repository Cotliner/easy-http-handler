package mj.carthy.easyhttphandler

import mj.carthy.easyhttphandler.configuration.HttpErrorHandlerConfig
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Import(HttpErrorHandlerConfig::class)
annotation class EnableHttpErrorHandler