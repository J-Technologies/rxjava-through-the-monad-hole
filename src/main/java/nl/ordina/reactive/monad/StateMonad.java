package nl.ordina.reactive.monad;

// zie https://dzone.com/articles/do-it-in-java-8-state-monad

/*
Scala impl

object State {
    // unit
    def apply[S, R](v: R): State[S, R] = new State[S, R] {
        def apply(s: S) = (s, v)
    }
    // custom
    def get[S]: State[S, S] = new State[S, S] {
        def apply(s: S) = (s, s)
    }
    def set[S](v: S): State[S, Unit] = new State[S, Unit] {
        def apply(s: S) = (v, ())
    }
    def run[S, R](s: S, st: State[S, R]): R = st(s).state
}

trait State[S, R] extends (S => (S, R)) { self =>
    def flatMap[R2](f: R => State[S, R2]): State[S, R2] =
        new State[S, R2] {
            def apply(s: S) = {
                val (s2, r) = self.apply(s)
                f(r)(s2)
            }
        }
    // Monad rule
    def map[R2](f: R => R2): State[S, R2] = flatMap[R2](r => State(f(r)))
}
 */

import java.util.Objects;
import java.util.function.Function;

class StateTuple<A, S> {
    public final A value;
    public final S state;

    public StateTuple(final A a, final S s) {
        value = Objects.requireNonNull(a);
        state = Objects.requireNonNull(s);
    }
}

// used i.s.o. Void & null; null does not carry its type
final class Nothing {
    public static final Nothing instance = new Nothing();
    private Nothing() {}
}

// state monads encapsulates state and a current value
public class StateMonad<S, A> /* implements Monad<S, Function<S, Tuple(R2,S)>>*/ {
    public final Function<S, StateTuple<A, S>> runState;

    private StateMonad(final Function<S, StateTuple<A, S>> runState) {
        this.runState = runState;
    }

    // unit(x) = s -> (x, s)
    // produces the given value without changing the state
    public static <S, A> StateMonad<S, A> unit(final A a) {
        return new StateMonad<>(s -> new StateTuple<>(a, s));
    }

    // flatMap(f) = r -> let (x, s) = m r in (f x) s
    //            = s -> let (tempValue, tempState) = runState s
    //                   in  (f tempValue) tempState
    // modifies m so that it applies f to its result
    public <B> StateMonad<S, B> flatMap(final Function<A, StateMonad<S, B>> f) {
        return new StateMonad<>(s -> {
            final StateTuple<A, S> temp = runState.apply(s);
            return f.apply(temp.value).runState/*!*/.apply(temp.state);
        });
    }

    // map := flatMap(x -> unit(f(x)))
    public <B> StateMonad<S, B> map(final Function<A, B> f) {
        return flatMap(x -> StateMonad.unit(f.apply(x)));
    }

    // used for composing multiple functions; sb is the accumulator
    public <B, C> StateMonad<S, C> map2(final StateMonad<S, B> sb, final Function<A, Function<B, C>> f) {
        return flatMap(a -> sb.map(b -> f.apply(a).apply(b)));
    }

    // run(s) = t(s)
    // applies state s
    public A eval(S s) {
        return runState.apply(s).value;
    }

    // get() = s -> (s, s)
    // Examine the state at this point in the computation
    public static <S> StateMonad<S, S> get() {
        return new StateMonad<>(s -> new StateTuple<>(s, s));
    }

    // ??? lijkt op map
    public static <S, A> StateMonad<S, A> getState(final Function<S, A> f) {
        return new StateMonad<>(s -> new StateTuple<>(f.apply(s), s));
    }

    // modify(f) = s -> ((), f(s))
    // Update the state
    public static <S> StateMonad<S, Nothing> transition(final Function<S, S> f) {
        return new StateMonad<>(s -> new StateTuple<>(Nothing.instance, f.apply(s)));
    }
    // update state & value
    public static <S, A> StateMonad<S, A> transition(final Function<S, S> f, final A value) {
        return new StateMonad<>(s -> new StateTuple<>(value, f.apply(s)));
    }

    // put(s) = any -> ((), s)
    // Replace the state
    public static <S, Void> StateMonad<S, Nothing> set(final S newState) {
        return new StateMonad<>(s -> new StateTuple<>(Nothing.instance, newState));
    }

}
