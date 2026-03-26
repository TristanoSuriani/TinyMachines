package nl.suriani.tiny.machine.symbolic;

import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class RockingJackMachine {
    /*
    * This is glorious nonsense, and it is so wrong at so many levels that it makes it right.
    * */

    private Stack<Symbol> s0 = new Stack<>();
    private Symbol sx = new Symbol("0");
    private Symbol dr = new Symbol("0");
    private Symbol rr = new Symbol("0");
    private Symbol rjs = new Symbol("");
    private Symbol msg = new Symbol("");
    private Symbol mhs = new Symbol(MachineStatus.IDLE.name());

    public static void main(String[] args) {
        List<Instruction> program = List.of(
                new Instruction(Operation.HAVE_SEX, new Symbol("20"), null, null),
                new Instruction(Operation.HAVE_SEX, new Symbol("20"), null, null),
                new Instruction(Operation.HAVE_SEX, new Symbol("20"), null, null),
                new Instruction(Operation.HAVE_SEX, new Symbol("20"), null, null),
                new Instruction(Operation.HAVE_SEX, new Symbol("20"), null, null),

                new Instruction(Operation.GET_ROCK, new Symbol("20"), null, null),
                new Instruction(Operation.GET_ROCK, new Symbol("20"), null, null),
                new Instruction(Operation.GET_ROCK, new Symbol("20"), null, null),
                new Instruction(Operation.GET_ROCK, new Symbol("20"), null, null),
                new Instruction(Operation.GET_ROCK, new Symbol("20"), null, null),

                new Instruction(Operation.TAKE_DRUGS, new Symbol("20"), null, null),
                new Instruction(Operation.TAKE_DRUGS, new Symbol("20"), null, null),
                new Instruction(Operation.TAKE_DRUGS, new Symbol("20"), null, null),
                new Instruction(Operation.TAKE_DRUGS, new Symbol("20"), null, null),
                new Instruction(Operation.TAKE_DRUGS, new Symbol("20"), null, null),

                new Instruction(Operation.DIE_WHEN, new Symbol("rjs"), new Symbol("NIRVANA"), null)
        );
        var rjm = new RockingJackMachine();
        System.out.println(rjm.run(program));
    }

    public String run(List<Instruction> instructions) {
        mhs = new Symbol(MachineStatus.RUNNING.name());
        int iterations = 0;

        while (!mhs.value().equals(MachineStatus.ERROR.name()) &&
                !mhs.value().equals(MachineStatus.HALTED.name())) {

            for (var instruction : instructions) {
                floorNeeds();
                checkMachineState();
                if (!mhs.value().equals(MachineStatus.RUNNING.name())) {
                    break;
                }
                step(instruction);
                updateJackStatus();
            }

            if (mhs.value().equals(MachineStatus.RESTARTING.name())) {
                mhs = new Symbol(MachineStatus.RUNNING.name());
            }

            iterations++;
            if (iterations > 69) {
                mhs = new Symbol(MachineStatus.HALTED.name());
                checkMachineState();
            }
        }

        if (rjs.value().equals("NIRVANA")) {
            msg = new Symbol("Rocking Jack reached the Nirvana!!! :-)");
            return  msg.value();
        }

        throw new RuntimeException(msg.value());
    }

    void step(Instruction instruction) {
        if (!mhs.value().equals(MachineStatus.RUNNING.name())) {
            return;
        }

        switch (instruction.operation()) {

            case TAKE_DRUGS -> {
                int amount = Integer.parseInt(instruction.param1().value());
                checkDelta(amount);
                dr = deltaNeed(dr, amount);
                rr = deltaNeed(rr, -5);
            }

            case HAVE_SEX -> {
                int amount = Integer.parseInt(instruction.param1().value());
                checkDelta(amount);
                sx = deltaNeed(sx, amount);
                dr = deltaNeed(dr, -5);
                rr = deltaNeed(rr, -5);
            }

            case GET_ROCK -> {
                int amount = Integer.parseInt(instruction.param1().value());
                checkDelta(amount);
                rr = deltaNeed(rr, amount);
                dr = deltaNeed(dr, -5);
            }

            case SLEEP -> {
                int iterations = Integer.parseInt(instruction.param1().value());
                if (iterations < 1) {
                    msg = new Symbol("Iterations must be at least 1");
                    mhs = new Symbol(MachineStatus.ERROR.name());
                    return;
                }
                rr = deltaNeed(rr, -5 * iterations);
                dr = deltaNeed(dr, -5 * iterations);
            }

            case SET_STATE -> {
                if ("NIRVANA".equals(instruction.param1().value())) {
                    msg = new Symbol("NIRVANA cannot be set directly");
                    mhs = new Symbol(MachineStatus.ERROR.name());
                } else {
                    rjs = instruction.param1();
                }
            }

            case RESET -> {
                sx = new Symbol("0");
                dr = new Symbol("0");
                rr = new Symbol("0");
            }

            case ADD -> msg = new Symbol("We are not going to do sums");

            case SUB -> msg = new Symbol("We are not going to do subtractions");

            case MOD3 -> msg = new Symbol("We are not going to do mod3");

            case MANTRA -> {
                var mantra = "Better dying like a lion than living like a sheep";
                msg = new Symbol(mantra);
                System.out.println(mantra);
            }

            case ECHO -> {
                msg = new Symbol(instruction.param1().value());
                System.out.println(instruction.param1().value());
            }

            case DIE_UNCONDITIONALLY -> mhs = new Symbol(MachineStatus.HALTED.name());

            case DIE_WHEN -> {
                boolean dead = switch (instruction.param1().value()) {
                    case "sx" -> sx.equals(instruction.param2());
                    case "dr" -> dr.equals(instruction.param2());
                    case "rr" -> rr.equals(instruction.param2());
                    case "rjs" -> rjs.equals(instruction.param2());
                    case "msg" -> msg.equals(instruction.param2());
                    case "mhs" -> mhs.equals(instruction.param2());
                    default -> false;
                };
                if (dead) {
                    mhs = new Symbol(MachineStatus.HALTED.name());
                }
            }

            case REPEAT -> mhs = new Symbol(MachineStatus.RESTARTING.name());
        }
    }

    private void updateJackStatus() {
        if (sx.value().equals("100") &&
                dr.value().equals("100") &&
                rr.value().equals("100")) {
            rjs = new Symbol("NIRVANA");
        }
    }

    private void checkMachineState() {
        checkNeeds();
        if (mhs.value().equals(MachineStatus.ERROR.name())) {
            return;
        }

        if (mhs.value().equals(MachineStatus.HALTED.name()) &&
                !rjs.equals(new Symbol("NIRVANA"))) {
            msg = new Symbol("Jack died without reaching Nirvana :(");
            mhs = new Symbol(MachineStatus.ERROR.name());
        }
    }

    private void checkNeeds() {
        checkNeed(sx);
        checkNeed(dr);
        checkNeed(rr);
    }

    private void floorNeeds() {
        sx = correctNeeds(sx);
        dr = correctNeeds(dr);
        rr = correctNeeds(rr);
    }

    private Symbol correctNeeds(Symbol need) {
        int needAmount = Integer.parseInt(need.value());
        if (needAmount < 0) return new Symbol("0");
        if (needAmount > 100) return new Symbol("100");
        return need;
    }

    private void checkNeed(Symbol need) {
        int needAmount = Integer.parseInt(need.value());
        if (needAmount < 0 || needAmount > 100) {
            msg = new Symbol("Needs should be between 0 and 100");
            mhs = new Symbol(MachineStatus.ERROR.name());
        }
    }

    private Symbol deltaNeed(Symbol need, int delta) {
        int current = Integer.parseInt(need.value());
        if (current == 100) {
            return new Symbol("100");
        }
        int next = current + delta;
        next = Math.max(0, Math.min(100, next));

        return new Symbol(String.valueOf(next));
    }

    private void checkDelta(int amount) {
        if (amount < 5 || amount > 20) {
            msg = new Symbol("Delta needs should be between 5 and 20");
            mhs = new Symbol(MachineStatus.ERROR.name());
        }
    }

    private enum MachineStatus {
        IDLE,
        RUNNING,
        HALTED,
        RESTARTING,
        ERROR
    }

    private enum Operation {
        TAKE_DRUGS, HAVE_SEX, GET_ROCK, SLEEP, SET_STATE, RESET, ADD, SUB, MOD3, MANTRA, ECHO,
        DIE_UNCONDITIONALLY, DIE_WHEN, REPEAT
    }

    private record Instruction(Operation operation, Symbol param1, Symbol param2, Symbol param3) {}

    private record Symbol(String value) {
        private Symbol {
            Objects.requireNonNull(value);
        }
    }
}