package nl.ordina.reactive.monad;

import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.*;

class Memo extends HashMap<BigInteger, BigInteger> {
    public Optional<BigInteger> retrieve(BigInteger key) {
        return Optional.ofNullable(super.get(key));
    }
    public Memo addEntry(BigInteger key, BigInteger value) {
        super.put(key, value);
        return this;
    }
}

public class StateMonadFibTest {
    BigInteger fibMemo2(final BigInteger n) {
        return fibMemo(n).eval(new Memo().addEntry(BigInteger.ZERO, BigInteger.ZERO).addEntry(BigInteger.ONE, BigInteger.ONE));
    }

    StateMonad<Memo, BigInteger> fibMemo(final BigInteger n) {
        return StateMonad.getState((Memo m) -> m.retrieve(n))
                .flatMap(u -> u.map(StateMonad::<Memo, BigInteger>unit)
                        .orElse(fibMemo(n.subtract(BigInteger.ONE))
                                .flatMap(x -> fibMemo(n.subtract(BigInteger.ONE).subtract(BigInteger.ONE))
                                        .map(x::add)
                                        .flatMap(z -> StateMonad.transition((Memo m) -> m.addEntry(n, z), z)))));
    }

    @Test
    public void calcFib3() {
        assertEquals(2, fibMemo2(new BigInteger("3")).intValue());
        assertEquals(5, fibMemo2(new BigInteger("5")).intValue());
        assertEquals(8, fibMemo2(new BigInteger("6")).intValue());
        assertEquals(55, fibMemo2(new BigInteger("10")).intValue());
        assertEquals(610, fibMemo2(new BigInteger("15")).intValue());
        assertEquals(832040, fibMemo2(new BigInteger("30")).intValue());
        assertEquals(102334155, fibMemo2(new BigInteger("40")).intValue());
        // naive fib(50) takes 44 minutes! total test done in under 200ms
        assertEquals(3736710778780434371L, fibMemo2(new BigInteger("100")).longValue());
        assertEquals(new BigInteger("43466557686937456435688527675040625802564660517371780402481729089536555417949051890403879840079255169295922593080322634775209689623239873322471161642996440906533187938298969649928516003704476137795166849228875"),
                fibMemo2(new BigInteger("1000")));
    }

}