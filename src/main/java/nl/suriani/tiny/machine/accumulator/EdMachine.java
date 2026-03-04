package nl.suriani.tiny.machine.accumulator;

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

    private final int[] program;
    private int acc = 0;
    private int m = 0;
    private int pc = 0;
    private boolean halted = false;
    private final int[] ram = new int[4096];

    public EdMachine(int[] program) {
        this.program = program;
    }

    public void run() {
        while (!halted) {
            step();
        }
    }

    public void step() {
        if (pc < 0 || pc >= program.length) { halted = true; return; }
        int instruction = program[pc];
        int[] parts = decode(instruction);
        int opcode = parts[0];
        int operand = parts[1];

        switch (opcode) {
            case 0 -> {
                nextRow();
            }
            case 1 -> {
                previousRow();
            }
            case 2 -> {
                nextColumn();
            }
            case 3 -> {
                previousColumn();
            }
            case 4 -> {
                ram[m] = acc;
            }
            case 5 -> {
                // todo input device
            }
            case 6 -> {
                // todo output device
            }
            case 7 -> {
                // todo output device
            }
            case 8 -> {
                pc = operand;
                return;
            }
            case 9 -> {
                if (acc == 0) {
                    pc = operand;
                    return;
                }
            }
            case 10 -> {
                setAcc(operand);
            }
            case 11 -> {
                setAcc(ram[m]);
            }
            case 12 -> {
                setAcc(ram[operand]);
            }
            case 13 -> {
                ram[operand] = acc;
            }
            case 14 -> {
                setAcc(acc - operand);
            }
            case 15 -> {
                if (operand == 0) {
                    halted = true;
                }
            }
        }
        pc = pc + 1;
    }

    private static int mod(int x, int n) {
        int r = x % n;
        return r < 0 ? r + n : r;
    }

    void previousRow()      { m = mod(m - 80, 3200); }
    void previousColumn()   { m = mod(m - 1, 3200); }
    void nextRow()          { m = mod(m + 80, 3200); }
    void nextColumn()       { m = mod(m + 1, 3200); }

    void setAcc(int acc) { this.acc = acc & 0xFFF; }

    static int encode(String mnemonic, int operand) {
        var opcode = Optional.ofNullable(instructions.get(mnemonic))
                .orElseThrow(() -> new IllegalArgumentException("Unknown mnemonic: " + mnemonic));
        return SimpleOneOperandEncoder.encode(opcode, operand, 4, 8);
    }

    static int[] decode(int instruction) {
        int[] out = new int[2];
        out[0] = SimpleOneOperandDecoder.decodeOp(instruction, 4, 8);
        out[1] = SimpleOneOperandDecoder.decodeImm(instruction, 4);
        return out;
    }
}
