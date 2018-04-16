package com.thitu.spock.injected

import com.google.common.annotations.Beta
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

@Beta
@Slf4j(value = "logger")
class InjectedWithMocksExtension extends AbstractAnnotationDrivenExtension<InjectedWithMocks> {

    @Override
    void visitFieldAnnotation(InjectedWithMocks annotation, FieldInfo fieldInfo) {
        logger.trace("@visitFieldAnnotation(InjectedWithMocks, FieldInfo)")
    }

    @Override
    void visitSpec(SpecInfo specInfo) {
        logger.trace("@visitSpec(SpecInfo)")

        specInfo.features.each { feature ->
            feature.featureMethod.addInterceptor(new InjectionInterceptor())
        }
    }


    @Slf4j(value = "logger")
    private static class InjectionInterceptor implements IMethodInterceptor {

        @Override
        void intercept(IMethodInvocation invocation) throws Throwable {
            logger.trace("@intercept(IMethodInvocation)")

            def method = invocation.spec.reflection.getMethod("inject", null)
            method.accessible = true
            method.invoke(invocation.instance, null)

            invocation.proceed()

            logger.trace("intercept completed!")
        }
    }
}