package nl.suriani.tiny.machine.accumulator.editor;

import lombok.Getter;
import lombok.Setter;
import nl.suriani.tiny.machine.util.SimpleOneOperandDecoder;
import nl.suriani.tiny.machine.util.SimpleOneOperandEncoder;

import java.util.Map;
import java.util.Optional;

public class EdMachine {
    private static final Map<String, Integer> instructions = Map.ofEntries(
            Map.entry("incr", 0),
            Map.entry("decr", 1),
            Map.entry("incc", 2),
            Map.entry("decc", 3),
            Map.entry("setc", 4),
            Map.entry("getc", 5),
            Map.entry("pres", 6),
            Map.entry("pred", 7),
            Map.entry("jump", 8),
            Map.entry("jmp0", 9),
            Map.entry("ldim", 10),
            Map.entry("ldmc", 11),
            Map.entry("ldad", 12),
            Map.entry("stad", 13),
            Map.entry("cmpi", 14),
            Map.entry("sysc", 15)
    );

    static final int COLS = 80;
    static final int SCREEN_SIZE = 3200;
    static final int ZP_SIZE = 256;
    static final int SCREEN_BASE = ZP_SIZE;
    static final int RAM_SIZE = 4096;

    private final int[] program;
    private final InputDevice in;
    private final OutputDevice out;

    private final EdMachineState state = new EdMachineState();

    public EdMachine(int[] program, InputDevice in, OutputDevice out) {
        this.program = program;
        this.in = in;
        this.out = out;
    }

    public void run() {
        while (!state.isHalted()) {
            step();
        }
    }

    public void step() {
        if (state.getPc() < 0 || state.getPc() >= program.length) { state.setHalted(true); return; }
        int instruction = program[state.getPc()];
        int[] parts = decode(instruction);
        int opcode = parts[0];
        int operand = parts[1];

        switch (opcode) {
            case 0 -> {
                incr(state);
            }
            case 1 -> {
                decr(state);
            }
            case 2 -> {
                incc(state);
            }
            case 3 -> {
                decc(state);
            }
            case 4 -> {
                setc(state);
            }
            case 5 -> {
                getc(state);
            }
            case 6 -> {
                pres(state);
            }
            case 7 -> {
                pred(state);
            }
            case 8 -> {
                jump(state, operand);
                return;
            }
            case 9 -> {
                boolean jumped = jmp0(state, operand);
                if (jumped) {
                    return;
                }
            }
            case 10 -> {
                ldim(state, operand);
            }
            case 11 -> {
                ldmc(state);
            }
            case 12 -> {
                ldad(state, operand);
            }
            case 13 -> {
                stad(state, operand);
            }
            case 14 -> {
                cmpi(state, operand);
            }
            case 15 -> {
                sys(state, operand);
            }
        }
        state.setPc(state.getPc() + 1);
    }

    void sys(EdMachineState state, int operand) {
        if (operand == 0) {
            state.setHalted(true);
        }
    }

    void cmpi(EdMachineState state, int operand) {
        state.setAcc(state.getAcc() - operand);
    }

    void stad(EdMachineState state, int operand) {
        state.getRam()[operand] = state.getAcc();
    }

    void ldad(EdMachineState state, int operand) {
        state.setAcc(state.getRam()[operand]);
    }

    void ldmc(EdMachineState state) {
        state.setAcc(state.getRam()[SCREEN_BASE + state.getM()]);
    }

    void ldim(EdMachineState state, int operand) {
        state.setAcc(operand);
    }

    boolean jmp0(EdMachineState state, int operand) {
        if (state.getAcc() == 0) {
            state.setPc(operand);
            return true;
        }
        return false;
    }

    void jump(EdMachineState state, int operand) {
        state.setPc(operand);
    }

    void pred(EdMachineState state) {
        out.write(state.getAcc(), state.getM() % COLS, state.getM() / COLS);
    }

    void pres(EdMachineState state) {
        out.present(state.getRam(), SCREEN_BASE, SCREEN_SIZE);
    }

    void getc(EdMachineState state) {
        state.setAcc(in.read());
    }

    void setc(EdMachineState state) {
        state.getRam()[SCREEN_BASE + state.getM()] = state.getAcc();
    }

    void decc(EdMachineState state) {
        previousColumn(state);
    }

    void incc(EdMachineState state) {
        nextColumn(state);
    }

    void decr(EdMachineState state) {
        previousRow(state);
    }

    void incr(EdMachineState state) {
        nextRow(state);
    }

    private static int mod(int x, int n) {
        int r = x % n;
        return r < 0 ? r + n : r;
    }

    void previousRow(EdMachineState state)      { state.setM(mod(state.getM() - COLS, SCREEN_SIZE)); }
    void previousColumn(EdMachineState state)   { state.setM(mod(state.getM() - 1, SCREEN_SIZE)); }
    void nextRow(EdMachineState state)          { state.setM(mod(state.getM() + COLS, SCREEN_SIZE)); }
    void nextColumn(EdMachineState state)       { state.setM(mod(state.getM() + 1, SCREEN_SIZE)); }

    static int encode(String mnemonic, int operand) {
        var opcode = Optional.ofNullable(instructions.get(mnemonic))
                .orElseThrow(() -> new IllegalArgumentException("Unknown mnemonic: " + mnemonic));
        return SimpleOneOperandEncoder.encode(opcode, operand, 4, 8);
    }

    static int[] decode(int instruction) {
        int[] out = new int[2];
        out[0] = SimpleOneOperandDecoder.decodeOp(instruction, 4, 8);
        out[1] = SimpleOneOperandDecoder.decodeImm(instruction, 8);
        return out;
    }

    public boolean isHalted() {
        return state.isHalted();
    }

    int acc() {
        return state.getAcc();
    }

    @Getter
    @Setter
    public static class EdMachineState {
        private int acc = 0;
        private int m = 0;
        private int pc = 0;
        private boolean halted = false;
        private final int[] ram = new int[RAM_SIZE];

        void setAcc(int acc) { this.acc = acc & 0xFFF; }
    }

    public interface InputDevice {
        int read();
    }

    public interface OutputDevice {
        void write(int value, int x, int y);
        void present(int[] buffer, int offset, int length);
    }
}
