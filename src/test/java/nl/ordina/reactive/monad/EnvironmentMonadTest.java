package nl.ordina.reactive.monad;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

public class EnvironmentMonadTest {

    @Test
    // m map id === m
    public void testFunctorIdentityLaw() {
        EnvironmentMonad<Map<Integer, String>, String> fortyTwoReader = new EnvironmentMonad<>(_env -> _env.get(42));

        Function<String, String> id = a -> a;
        final EnvironmentMonad<Map<Integer, String>, String> sameEnvReader = fortyTwoReader.map(id);

        Map<Integer, String> e = getEnvironmentMappingI42ToString("foo");
        final String result1 = fortyTwoReader.apply(e);
        final String result = sameEnvReader.apply(e);

        assertEquals("foo", result);
        assertEquals("foo", result1);
    }

    @Test
    // m flatMap unit === m
    public void testMonadIdentityLaw() {
        EnvironmentMonad<Map<Integer, String>, String> fortyTwoReader = new EnvironmentMonad<>(_env -> _env.get(42));

        final EnvironmentMonad<Map<Integer, String>, String> sameEnvReader = fortyTwoReader.flatMap(t -> new EnvironmentMonad<>(e -> t));

        Map<Integer, String> e = getEnvironmentMappingI42ToString("foo");
        final String result1 = fortyTwoReader.apply(e);
        final String result = sameEnvReader.apply(e);

        assertEquals("foo", result);
        assertEquals("foo", result1);
    }

    @Test
    // (m map g) map f === m map (x -> f(g(x)))
    public void testFunctorCompositionLaw() {
        EnvironmentMonad<Map<Integer, String>, String> fortyTwoReader = new EnvironmentMonad<>(_env -> _env.get(42));

        Function<String, Integer> parseInt = Integer::parseInt;
        Function<Integer, String> toString = i -> i.toString();

        final EnvironmentMonad<Map<Integer, String>, String> mappedReader = fortyTwoReader.map(parseInt).map(toString);
        final EnvironmentMonad<Map<Integer, String>, String> otherMappedReader = fortyTwoReader.map(x -> toString.apply(parseInt.apply(x)));

        Map<Integer, String> e = getEnvironmentMappingI42ToString("666");
        final String result1 = mappedReader.apply(e);
        final String result2 = otherMappedReader.apply(e);

        assertEquals("666", result1);
        assertEquals("666", result2);
    }

    @Test
    // (m flatMap g) flatMap f === m flatMap (x -> g(x) flatMap f)
    public void testMonadCompositionLaw() {
        EnvironmentMonad<Map<Integer, String>, String> fortyTwoReader = new EnvironmentMonad<>(_env -> _env.get(42));

        Function<String, EnvironmentMonad<Map<Integer, String>, Integer>> parseInt = s -> new EnvironmentMonad<>(e -> Integer.parseInt(s));
        Function<Integer, EnvironmentMonad<Map<Integer, String>, String>> toString = i -> new EnvironmentMonad<>(e -> i.toString());

        final EnvironmentMonad<Map<Integer, String>, String> flatMappedReader = fortyTwoReader.flatMap(parseInt).flatMap(toString);
        final EnvironmentMonad<Map<Integer, String>, String> otherFlatMappedReader = fortyTwoReader.flatMap(x -> parseInt.apply(x).flatMap(toString));

        Map<Integer, String> e = getEnvironmentMappingI42ToString("666");
        final String result1 = flatMappedReader.apply(e);
        final String result2 = otherFlatMappedReader.apply(e);

        assertEquals("666", result1);
        assertEquals("666", result2);
    }

    @Test
    // unit(x) flatMap f === f(x)
    public void testMonadUnitLaw() {
        EnvironmentMonad<Map<Integer, String>, String> fixedFortyTwo = new EnvironmentMonad<>(env -> "42");
        Function<String, EnvironmentMonad<Map<Integer, String>, String>> parseIntAndGet = s -> new EnvironmentMonad<>(e -> e.get(Integer.parseInt(s)));
        final EnvironmentMonad<Map<Integer, String>, String> stringEnv = fixedFortyTwo.flatMap(parseIntAndGet);
        Map<Integer, String> e = getEnvironmentMappingI42ToString("666");

        final String result1 = stringEnv.apply(e);
        final String fX = parseIntAndGet.apply("42").apply(e);

        assertEquals("666", result1);
        assertEquals("666", fX);
    }

    @Test
    public void testAsk() {
        EnvironmentMonad<Map<Integer, String>, String> fortyTwoReader = new EnvironmentMonad<>(_env -> _env.get(42));

        Map<Integer, String> e = getEnvironmentMappingI42ToString("666");
        final Map<Integer, String> context = fortyTwoReader.ask().apply(e);

        assertEquals(e, context);
    }

    @Test
    public void testLocal() {
        EnvironmentMonad<Map<Integer, String>, String> fortyTwoReader = new EnvironmentMonad<>(_env -> _env.get(42));

        Map<Integer, String> e = getEnvironmentMappingI42ToString("666");
        final EnvironmentMonad<Map<Integer, String>, String> localizedEnv = fortyTwoReader.local(_e -> {
            Map<Integer, String> e1 = new HashMap<>(_e);
            e1.put(42, "foo");
            return Collections.unmodifiableMap(e1);
        });
        final String localResult = localizedEnv.apply(e);
        final String globalResult = fortyTwoReader.apply(e);

        assertEquals("foo", localResult);
        assertEquals("666", globalResult);
    }

    @Test
    public void shouldDeferComputation() {
        final int inputValue = 16;
        final String outcome = new EnvironmentMonad<Integer, Integer>(r -> r + 5)
                .map(x -> x * 2)
                .flatMap(x -> new EnvironmentMonad<Integer, String>(y -> new String(x + " " + y)))
                .apply(inputValue);
        assertEquals("42 16", outcome);
    }

    private Map<Integer, String> getEnvironmentMappingI42ToString(String result) {
        Map<Integer, String> e = new HashMap<>();
        e.put(42, result);
        return Collections.unmodifiableMap(e);
    }
}
