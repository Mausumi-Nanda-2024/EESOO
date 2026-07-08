package com.eesoo.EESOO.shared.application.cqrs;

public interface QueryHandler<Q, R> {
    R handle(Q query);
}
