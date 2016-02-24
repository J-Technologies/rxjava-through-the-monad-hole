package nl.ordina.reactive.monad;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

interface Input {
    boolean isDeposit();
    boolean isWithdraw();
    int getAmount();
}
class Deposit implements Input {
    private final int amount;

    public Deposit(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean isDeposit() {
        return true;
    }

    @Override
    public boolean isWithdraw() {
        return false;
    }

    @Override
    public int getAmount() {
        return this.amount;
    }
}
class Withdraw implements Input {
    private final int amount;

    public Withdraw(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean isDeposit() {
        return false;
    }

    @Override
    public boolean isWithdraw() {
        return true;
    }
    @Override
    public int getAmount() {
        return this.amount;
    }
}

// TODO Either Monad!!
// has no flatMap now. Cf. Optional
interface Either<A, B> {
    boolean isLeft();
    boolean isRight();

    A getLeft();
    B getRight();

    static <A, B> Either<A, B> right(B value) {
        return new Right<>(value);
    }

    static <A, B> Either<A, B> left(A value) {
        return new Left<>(value);
    }

    public class Left<A, B> implements Either<A, B> {
        private final A left;

        private Left(A left) {
            super();
            this.left = left;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public A getLeft() {
            return this.left;
        }

        @Override
        public B getRight() {
            throw new IllegalStateException("getRight() called on Left value");
        }

        @Override
        public String toString() {
            return left.toString();
        }
    }

    public class Right<A, B> implements Either<A, B> {
        private final B right;

        private Right(B right) {
            super();
            this.right = right;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public A getLeft() {
            throw new IllegalStateException("getLeft() called on Right value");
        }

        @Override
        public B getRight() {
            return this.right;
        }

        @Override
        public String toString() {
            return right.toString();
        }
    }
}

class Outcome {
    public final Integer account;
    public final FunctionalList<Either<Exception, Integer>> operations;

    public Outcome(Integer account, FunctionalList<Either<Exception, Integer>> operations) {
        this.account = account;
        this.operations = operations;
    }

    public String toString() {
        return "(" + account.toString() + "," + operations.toString() + ")";
    }
}

interface Condition<I, S> extends Predicate<StateTuple<I, S>> {}
interface Transition<I, S> extends Function<StateTuple<I, S>, S> {}

class FunctionalList<T> {
    private java.util.List<T> list = new ArrayList<>();

    public static <T> FunctionalList<T> empty() {
        return new FunctionalList<T>();
    }

    @SafeVarargs
    public static <T> FunctionalList<T> apply(T... ta) {
        FunctionalList<T> result = new FunctionalList<>();
        for (T t : ta)
            result.list.add(t);
        return result;
    }

    public FunctionalList<T> cons(T t) {
        FunctionalList<T> result = new FunctionalList<>();
        result.list.add(t);
        result.list.addAll(list);
        return result;
    }

    public <U> U foldRight(U seed, Function<T, Function<U, U>> f) {
        U result = seed;
        for (int i = list.size() - 1; i >= 0; i--) {
            result = f.apply(list.get(i)).apply(result);
        }
        return result;
    }

    public <U> FunctionalList<U> map(Function<T, U> f) {
        FunctionalList<U> result = new FunctionalList<>();
        for (T t : list) {
            result.list.add(f.apply(t));
        }
        return result;
    }

    public FunctionalList<T> filter(Function<T, Boolean> f) {
        FunctionalList<T> result = new FunctionalList<>();
        for (T t : list) {
            if (f.apply(t)) {
                result.list.add(t);
            }
        }
        return result;
    }

    public Optional<T> findFirst() {
        return list.size() == 0
                ? Optional.empty()
                : Optional.of(list.get(0));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for (T t : list) {
            s.append(t).append(", ");
        }
        return s.append("NIL]").toString();
    }

    public static <S, A> StateMonad<S, FunctionalList<A>> compose(final FunctionalList<StateMonad<S, A>> fs) {
        return fs.foldRight(StateMonad.unit(FunctionalList.<A>empty()), f -> acc -> f.map2(acc, a -> b -> b.cons(a)));
    }
}

class StateMachine<I, S> {
    Function<I, StateMonad<S, Nothing>> function;

    public StateMachine(FunctionalList<StateTuple<Condition<I, S>, Transition<I, S>>> transitions) {
        function = i -> StateMonad.transition(m ->
                Optional.of(new StateTuple<>(i, m)).flatMap((StateTuple<I, S> t) ->
                        transitions.filter((StateTuple<Condition<I, S>, Transition<I, S>> x) ->
                                x.value.test(t)).findFirst().map((StateTuple<Condition<I, S>, Transition<I, S>> y) ->
                                y.state.apply(t))).get());
    }

    public StateMonad<S, S> process(FunctionalList<I> inputs) {
        FunctionalList<StateMonad<S, Nothing>> a = inputs.map(function);
        StateMonad<S, FunctionalList<Nothing>> b = FunctionalList.compose(a);
        return b.flatMap(x -> StateMonad.get());
    }
}

class Account {
    public static StateMachine<Input, Outcome> createMachine() {
        Condition<Input, Outcome> predicate1 = t -> t.value.isDeposit();
        Transition<Input, Outcome> transition1 = t -> new Outcome(t.state.account + t.value.getAmount(), t.state.operations.cons(Either.right(t.value.getAmount())));

        Condition<Input, Outcome> predicate2 = t -> t.value.isWithdraw() && t.state.account >= t.value.getAmount();
        Transition<Input, Outcome> transition2 = t -> new Outcome(t.state.account - t.value.getAmount(), t.state.operations.cons(Either.right(- t.value.getAmount())));

        Condition<Input, Outcome> predicate3 = t -> true;
        Transition<Input, Outcome> transition3 = t -> new Outcome(t.state.account, t.state.operations.cons(Either.left(new IllegalStateException(String.format("Can't withdraw %s because balance is only %s", t.value.getAmount(), t.state.account)))));

        FunctionalList<StateTuple<Condition<Input, Outcome>, Transition<Input, Outcome>>> transitions = FunctionalList.apply(
                new StateTuple<>(predicate1, transition1),
                new StateTuple<>(predicate2, transition2),
                new StateTuple<>(predicate3, transition3));

        return new StateMachine<>(transitions);
    }
}

public class StateMonadAccountStateMachineTest {
    @Test
    public void testAccountStateMachine() {
        FunctionalList<Input> inputs = FunctionalList.apply(
                new Deposit(100),
                new Withdraw(50),
                new Withdraw(150),
                new Deposit(200),
                new Withdraw(150));
        StateMonad<Outcome, Outcome> state = Account.createMachine().process(inputs);
        Outcome outcome = state.eval(new Outcome(0, FunctionalList.empty()));
        System.out.println(outcome.toString());
    }
}
