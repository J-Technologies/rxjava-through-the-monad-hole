package nl.ordina.reactive.monad;

import java.util.function.Function;

// combine Reader and Try
public class TryReaderMonad<R, A> {
    private final Function<R,TryMonad<A>> run;

    public TryReaderMonad(final Function<R,TryMonad<A>> run) {
        this.run = run;
    }

    public <B> TryReaderMonad<R, B> map(Function<A, B> f) {
        return new TryReaderMonad<>((R r) -> apply(r).map(a -> f.apply(a)));
        // without Try: f.apply(apply(r)); need map to handle Success/Failure of the Try
    }

    public <B> TryReaderMonad<R, B> flatMap(Function<A, TryReaderMonad<R, B>> f) {
        return new TryReaderMonad<>((R r) -> apply(r).flatMap(a -> f.apply(a).apply(r)));
        // without Try: f.apply(apply(r)).apply(r)
        // brain hurts!
    }

    // 'lift' ReaderMonad to TryReaderMonad
    public <B> TryReaderMonad<R, B> mapReader(Function<A, EnvironmentMonad<R, B>> f) {
        return new TryReaderMonad<>((R r) -> apply(r).map(a -> f.apply(a).apply(r)));
        // only difference with flatMap(): use map i.s.o. flatMap on TryMonad
    }

    public TryMonad<A> apply(R r) {
        return run.apply(r); // same
    }
}
