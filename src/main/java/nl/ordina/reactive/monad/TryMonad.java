package nl.ordina.reactive.monad;

import java.util.function.Function;

// wrap and pass on errors
public interface TryMonad<A> {
    boolean isFailure();
    <B> TryMonad<B> map(Function<A, B> f); // wrap / construct / leaf / apply / 'lift' a normal function into the monad
    <B> TryMonad<B> flatMap(Function<A, TryMonad<B>> f); // compose / maintain monad 'state'

    // was sort-of missing apply() method? How to get the value or error out?
    A apply();
}

class Success<A> implements TryMonad<A> {
    private final A value;

    public Success(final A value) {
        this.value = value;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public <B> TryMonad<B> map(Function<A, B> f) {
        return new Success<>(f.apply(value)); // pas f toe op waarde & wrap
    }

    @Override
    public <B> TryMonad<B> flatMap(Function<A, TryMonad<B>> f) {
        return f.apply(value); // f already takes care of wrapping
    }

    @Override
    public A apply() {
        return value;
    }
}

class Failure<A> implements TryMonad<A> {
    private final Object error;

    public Failure(final Object error) {
        this.error = error;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public <B> TryMonad<B> map(Function<A, B> f) {
        // A != B maar maakt niet uit want error heeft type Object. A & B hebben in deze scope geen betekenis
        // nothing to do - cannot apply f since we have no value A
        return (Failure<B>) this;
    }

    @Override
    public <B> TryMonad<B> flatMap(Function<A, TryMonad<B>> f) {
        return (Failure<B>) this; // nothing to apply
    }

    @Override
    public A apply() {
        return null; // could also throw Exc with error object??
    }

    public Object getError() {
        return error;
    }
}