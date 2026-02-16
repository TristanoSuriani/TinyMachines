package nl.suriani.tiny.machine;

import java.util.function.BiFunction;

public class Fibonacci {

    public static void main(String[] args) {
        var result = fib(6);
        System.out.println(result);
    }

    private static int fib(int n) {
        if (n <= 1) return n;

        return fibIter(n, new FibState(2, 1, 1));
    }

    private static int fibIter(int n, FibState state) {
        if (state.i() == n) {
            return state.nMin1();
        }

        var next = evolve(state, state.nMin1() + state.nMin2(), Fibonacci::step);
        return fibIter(n, next);
    }

    private static FibState step(FibState s, int next) {
        return new FibState(
                s.i() + 1,
                next,
                s.nMin1()
        );
    }

    private static <S, Δ, S1> S1 evolve(S s, Δ δ, BiFunction<S, Δ, S1> f) {
        return f.apply(s, δ);
    }

    record FibState(int i, int nMin1, int nMin2) {}
}