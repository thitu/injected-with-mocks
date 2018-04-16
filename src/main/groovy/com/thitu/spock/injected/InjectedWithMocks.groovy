package com.thitu.spock.injected

import com.google.common.annotations.Beta
import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.*

@Beta
@Documented
@Target([ElementType.FIELD])
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(value = InjectedWithMocksExtension)
@interface InjectedWithMocks {
    int depth() default 2;
}
