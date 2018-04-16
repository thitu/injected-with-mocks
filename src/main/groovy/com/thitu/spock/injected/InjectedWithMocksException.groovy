package com.thitu.spock.injected

import org.spockframework.runtime.SpockExecutionException

class InjectedWithMocksException extends SpockExecutionException {
    InjectedWithMocksException(String msg) {
        super(msg)
    }
}