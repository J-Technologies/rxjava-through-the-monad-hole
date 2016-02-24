package nl.ordina.reactive.monad;

import java.util.function.Function;

// unusable due to limited Java type system. Does work in Scala.
public interface Monad<A,B> {
    <B2> /* self type of.. */ Monad<A,B2> flatMap(Function<B, /* self type of.. */ Monad<A, B2>> f);
    <B2> /* self type of.. */ Monad<A,B2> map(Function<B, B2> f);
}
