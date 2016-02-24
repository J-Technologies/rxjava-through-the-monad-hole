package nl.ordina.reactive.monad;

import java.util.function.Function;

/*
http://lampwww.epfl.ch/~emir/bqbase/2005/01/20/monad.html

type Cont[A] = (A => Unit) => Unit;
case class M[+A](in: (A=>Any)=>Any)

 */
public class ContinuationMonad<A> {
    private final Function<Function<A,Void>,Void> in;

    ContinuationMonad(final Function<Function<A,Void>,Void> in) {
        this.in = in;
    }

    public static <A> ContinuationMonad<A> unit(final A x) {
        return new ContinuationMonad<>(k -> k.apply(x));
    }

    public <B> ContinuationMonad<B> map(final Function<A,B> f) {
        return bind(this, x -> unit(f.apply(x)));
    }

    public <B> ContinuationMonad<B> flatMap(final Function<A,ContinuationMonad<B>> f) {
        return bind(this, f);
    }

    static <A, B> ContinuationMonad<B> bind(ContinuationMonad<A> m, final Function<A,ContinuationMonad<B>> f) {
        return new ContinuationMonad<>(k -> m.in.apply(x -> f.apply(x).in.apply(k)));
    }

    public static <A> ContinuationMonad<A> callCC(final Function<Function<A, ContinuationMonad<A>>, ContinuationMonad<A>> e) {
        return new ContinuationMonad<>(k -> e.apply(a -> new ContinuationMonad<>(ignored -> k.apply(a))).in.apply(k));
    }

    // 3 convenience functions
    public static <A> Function<A, Void> show() {
        return x -> { System.out.println(x); return null; };
    }

     public static <A> void show(final ContinuationMonad<A> m) {
         m.in.apply(show());
     }

    public static <A> void showshow(final ContinuationMonad<ContinuationMonad<A>> m) {
        m.in.apply(k -> k.in.apply(show()));
    }

}
