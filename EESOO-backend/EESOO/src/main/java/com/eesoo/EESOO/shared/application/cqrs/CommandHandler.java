package com.eesoo.EESOO.shared.application.cqrs;

public interface CommandHandler<TCommand, TResult> {

    TResult handle(TCommand command);
    
}
