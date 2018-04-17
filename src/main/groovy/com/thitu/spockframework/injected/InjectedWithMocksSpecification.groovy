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
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Field

import static java.lang.reflect.Modifier.isFinal
import static java.lang.reflect.Modifier.isStatic

@Beta
@Slf4j(value = "logger")
class InjectedWithMocksSpecification extends Specification {

    def inject() {
        this.class.declaredFields.each { declaredField ->
            def annotations = declaredField.declaredAnnotations

            def injected = annotations.any { it instanceof InjectedWithMocks }
            def notShared = annotations.every { !(it instanceof Shared) }

            int depth = annotations.find { it instanceof InjectedWithMocks }?.depth() ?: 2

            if (injected && !notShared) {
                throw new InjectedWithMocksException("@${InjectedWithMocks} cannot be used alongside @${Shared}")
            } else if (depth < 1 || depth > 5) {
                throw new InjectedWithMocksException("Are we sure about the configured depth of ${depth}? Expected range is 1...5")
            }

            if (injected && notShared) {
                declaredField.accessible = true

                def fields = [] as Set<Field>
                fields.addAll declaredField.type.declaredFields

                if (depth == 5) {
                    fields.addAll extractFields(declaredField.type?.superclass?.superclass?.superclass?.superclass)
                    fields.addAll extractFields(declaredField.type?.superclass?.superclass?.superclass)
                    fields.addAll extractFields(declaredField.type?.superclass?.superclass)
                    fields.addAll extractFields(declaredField.type?.superclass)
                } else if (depth == 4) {
                    fields.addAll extractFields(declaredField.type?.superclass?.superclass?.superclass)
                    fields.addAll extractFields(declaredField.type?.superclass?.superclass)
                    fields.addAll extractFields(declaredField.type?.superclass)
                } else if (depth == 3) {
                    fields.addAll extractFields(declaredField.type?.superclass?.superclass)
                    fields.addAll extractFields(declaredField.type?.superclass)
                } else if (depth == 2) {
                    fields.addAll extractFields(declaredField.type?.superclass)
                }

                fields.each { field ->
                    if (!field.type.primitive) {
                        int modifiers = field.modifiers

                        if (isFinal(modifiers) || isStatic(modifiers)) {
                            logger.debug "Not Mocking static or final field: {} -> {}", field.name, field.type
                        } else {
                            field.accessible = true

                            if (field.type == String) {
                                field.set declaredField.get(this), field.name
                            } else if (!isFinal(field.type.modifiers)) {
                                field.set declaredField.get(this), Mock(field.type)
                            }
                        }
                    }
                }
            }
        }
    }

    private def extractFields = { clazz ->
        clazz?.declaredFields ?: []
    }
}