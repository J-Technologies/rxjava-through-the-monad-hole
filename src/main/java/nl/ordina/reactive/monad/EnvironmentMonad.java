package nl.ordina.reactive.monad;

import java.util.function.Function;

// aka ReaderMonad
// aka FunctionMonad
// wrap abstract computation r ('lookup') without evaluating it
// 'E' kun je zien als inputwaarde, maar ook als 'context' of 'resource'
public class EnvironmentMonad<E,T> /*implements Monad<E,T>*/ {
    private final Function<E,T> r;

    // unit??(t)?? = e -> t
    public EnvironmentMonad(final Function<E,T> r) {
        this.r = r;
    }

    // flatMap(f) = e -> f(r(e))(e)
    public <T2> EnvironmentMonad<E,T2> flatMap(final Function<T, EnvironmentMonad<E,T2>> f) {
        return new EnvironmentMonad<>(e -> f.apply(/*r.*/apply(e)).apply(e));
    }

    // map := flatMap(x -> unit(f(x)))
    public <T2> EnvironmentMonad<E,T2> map(final Function<T,T2> f) {
        // 'manual' implementation
        return new EnvironmentMonad<>(e -> f.apply(/*r.*/apply(e)));
        // monad rules impl.
        //return flatMap(e -> new EnvironmentMonad<>(e1 -> f.apply(e)));
    }

    // ask() = e -> e
    // retrieve current context
    public EnvironmentMonad<E,E> ask() {
        return new EnvironmentMonad<>(e -> e);
    }

    // local(f:E->E) = e -> self(f(e))
    // executes a computation in a modified subcontext
    public EnvironmentMonad<E,T> local(final Function<E,E> f) {
        return new EnvironmentMonad<>((E e) -> r.apply(f.apply(e)));
    }

    // unwind the computation
    public T apply(final E e) {
        return r.apply(e);
    }
}
