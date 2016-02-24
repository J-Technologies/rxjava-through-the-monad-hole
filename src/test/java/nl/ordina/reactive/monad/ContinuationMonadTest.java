package nl.ordina.reactive.monad;

import org.junit.Test;

import java.util.function.Function;

import static nl.ordina.reactive.monad.ContinuationMonad.*;

public class ContinuationMonadTest {

    @Test
    public void should_show_3() {
        final ContinuationMonad<Integer> cont = unit(new Integer(3));
        show(cont);
    }

    @Test
    public void should_show_MoL_42_using_bind() {
        final ContinuationMonad<String> m = unit("42");
        final Function<String, ContinuationMonad<String>> f = s -> new ContinuationMonad<>(k -> k.apply("meaning of life: " + s));
        final ContinuationMonad<String> n = bind(m, f);
        show(n);
    }

    @Test
    public void should_show_MoL_42_and_null_using_flat_map() {
        final ContinuationMonad<String> m = unit("42");

        final ContinuationMonad<Void> n = m
                .flatMap(s -> new ContinuationMonad<>(k -> k.apply("meaning of life: " + s)))
                .map(x -> { System.out.println(x); return null;});
        show(n);
    }

    ContinuationMonad<ContinuationMonad<String>> meaning(final Function<ContinuationMonad<String>, ContinuationMonad<ContinuationMonad<String>>> x) {
        //return bind(unit(unit("42")), x); // unit is an internal method
        return unit(unit("42")).flatMap(x);
    }

    @Test
    public void should_show_MoL_42() {
        final ContinuationMonad<ContinuationMonad<String>> meaning = meaning(x -> unit(bind(x, s -> unit("the meaning of life is " + s))));
        showshow(meaning);
    }

    @Test
    public void should_show_MoL_42_using_callcc() {
        showshow(callCC(this::meaning));
    }

}