package nl.ordina.reactive.monad;

import org.junit.Test;

import static org.junit.Assert.*;

public class TryReaderMonadTest {

    @Test
    public void shouldDeferSuccessfulComputation() {
        final TryMonad<String> outcome = monadicTryWithInput(16);
        assertEquals("42 16", outcome.apply());
        assertFalse(outcome.isFailure());
    }

    private TryMonad<String> monadicTryWithInput(int inputValue) {
        return new TryReaderMonad<Integer, Integer>(r -> r == 0 ? new Failure<>("broken") : new Success<>(r + 5))
                    .map(x -> x * 2)
                    .mapReader(x -> new EnvironmentMonad<Integer, String>(y -> new String(x + " " + y))) // brain hurts..
                    .apply(inputValue);
    }

    @Test
    public void shouldDeferFailingComputation() {
        final TryMonad<String> outcome = monadicTryWithInput(0);

        assertEquals("broken", ((Failure) outcome).getError());
        assertTrue(outcome.isFailure());
        assertNull(outcome.apply());
    }

}