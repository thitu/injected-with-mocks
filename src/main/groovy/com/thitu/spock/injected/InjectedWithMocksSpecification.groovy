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
            } else if (depth < 1 || depth > 4) {
                throw new InjectedWithMocksException("Are we sure about the configured depth of ${depth}? Expected range is 1...4")
            }

            if (injected && notShared) {
                field.accessible = true

                def fields = [] as Set<Field>
                fields.addAll field.type.declaredFields

                if (depth == 4) {
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