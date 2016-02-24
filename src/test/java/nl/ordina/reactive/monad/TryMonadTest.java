package nl.ordina.reactive.monad;

import org.junit.Test;

import static org.junit.Assert.*;

public class TryMonadTest {

    public TryMonad<Integer> div(int x, int y) {
        if (y == 0) {
            return new Failure<>("cannot div by 0");
        }
        return new Success<>(x/y);
    }

    @Test
    public void shouldDivByNonZero(){
        final TryMonad tryMonad = div(5, 2)
                .map(x -> x + 2)
                .flatMap(x -> new Success(x));
        assertFalse(tryMonad.isFailure());
    }

    @Test
    public void shouldNotDivByZero(){
        final TryMonad tryMonad = div(5, 0)
                .map(x -> x + 2) // nothing executed
                .flatMap(x -> new Success(x)); // idem
        assertTrue(tryMonad.isFailure());
    }

}