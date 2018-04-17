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
package com.thitu.spock.injected

import com.google.common.annotations.Beta
import groovy.util.logging.Slf4j
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Field

import static java.lang.reflect.Modifier.isFinal
import static java.lang.reflect.Modifier.isStatic

@Beta
@Slf4j(value = "logger")
class InjectedWithMocksSpecification extends Specification {

    def inject() {
        for (def field : this.class.declaredFields) {
            def injected = field.declaredAnnotations.any { it instanceof InjectedWithMocks }
            def notShared = field.declaredAnnotations.every { !(it instanceof Shared) }

            int depth = field.declaredAnnotations.find {
                it instanceof InjectedWithMocks
            }?.depth() ?: 2

            if (injected && !notShared) {
                throw new InjectedWithMocksException("@${InjectedWithMocks} cannot be used alongside @${Shared}")
            } else if (depth < 1 || depth > 5) {
                throw new InjectedWithMocksException("Are we sure about the configured depth of ${depth}? Expected range is 1...5")
            }

            if (injected && notShared) {
                field.accessible = true

                def fields = [] as Set<Field>
                fields.addAll field.type.declaredFields

                if (depth == 5) {
                    fields.addAll extractFields(field.type?.superclass?.superclass?.superclass?.superclass)
                    fields.addAll extractFields(field.type?.superclass?.superclass?.superclass)
                    fields.addAll extractFields(field.type?.superclass?.superclass)
                    fields.addAll extractFields(field.type?.superclass)
                } else if (depth == 4) {
                    fields.addAll extractFields(field.type?.superclass?.superclass?.superclass)
                    fields.addAll extractFields(field.type?.superclass?.superclass)
                    fields.addAll extractFields(field.type?.superclass)
                } else if (depth == 3) {
                    fields.addAll extractFields(field.type?.superclass?.superclass)
                    fields.addAll extractFields(field.type?.superclass)
                } else if (depth == 2) {
                    fields.addAll extractFields(field.type?.superclass)
                }

                for (def f : fields) {
                    if (f.type.primitive) {
                        continue
                    }

                    int modifiers = f.modifiers

                    if (isFinal(modifiers) || isStatic(modifiers)) {
                        logger.debug("Not Mocking static or final field: {} -> {}", f.name, f.type)
                        continue
                    }

                    f.accessible = true

                    if (f.type == String) {
                        f.set(field.get(this), f.name)
                    } else if (!isFinal(f.type.modifiers)) {
                        f.set(field.get(this), Mock(f.type))
                    }

                    logger.trace("injection completed")
                }
            }
        }
    }

    private def extractFields = { clazz ->
        clazz?.declaredFields ?: []
    }
}