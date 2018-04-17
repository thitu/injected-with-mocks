/*
 * MIT License
 * Copyright (c) 2018 Nikolas Mangu-Thitu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/
package com.thitu.spockframework.injected

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
        logger.trace "@visitFieldAnnotation(InjectedWithMocks, FieldInfo)"
    }

    @Override
    void visitSpec(SpecInfo specInfo) {
        logger.trace "@visitSpec(SpecInfo)"

        specInfo.features.each { feature ->
            feature.featureMethod.addInterceptor new InjectionInterceptor()
        }
    }

    @Slf4j(value = "logger")
    private static class InjectionInterceptor implements IMethodInterceptor {

        @Override
        void intercept(IMethodInvocation invocation) throws Throwable {
            logger.trace "@intercept(IMethodInvocation)"

            def method = invocation.spec.reflection.getMethod("inject", null)
            method.accessible = true
            method.invoke invocation.instance, null

            invocation.proceed()

            logger.trace "intercept completed!"
        }
    }
}