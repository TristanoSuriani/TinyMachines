package nl.suriani.tiny.machine.accumulator;

import lombok.SneakyThrows;
import nl.suriani.tiny.machine.util.SimpleOneOperandDecoder;

public class BrainfuckMachine {
    private final int[] rom;
    private final int[] memory = new int[MEMORY_SIZE];
    private int m;
    private int d;
    private int state;
    private int pc;

    public static final int INC  = 1;
    public static final int DEC  = 2;
    public static final int JMP  = 3;
    public static final int JMZ  = 4;
    public static final int TAKE  = 5;
    public static final int GIVE  = 6;
    public static final int LEFT  = 7;
    public static final int RIGHT  = 8;

    public static final int MEMORY_SIZE  = 256;
    public static final int BITS_OPCODE  = 4;
    public static final int BITS_OPERAND  = 8;

    public BrainfuckMachine(int[] rom) {
        this.rom = rom;
    }

    public int run() {
        state = 1;
        while (state == 1) {
            state = step();
        }

        if (state == 2) {
            return -1;
        }

        return d;
    }

    @SneakyThrows
    public int step() {
        if (state == 0) {
            return state;
        }
        if (pc >= rom.length) {
            state = 0;
            return state;
        }
        int instruction = rom[pc];
        int operation = SimpleOneOperandDecoder.decodeOp(instruction, BITS_OPCODE, BITS_OPERAND);
        int operand = SimpleOneOperandDecoder.decodeImm(instruction, BITS_OPERAND);

        switch (operation) {
            case INC -> {
                d = word(d + 1);
                memory[m] = d;
            }

            case DEC -> {
                d = word(d - 1);
                memory[m] = d;
            }

            case JMP -> {
                if (operand < 0) {
                    state = 2;
                    return state;
                }

                pc = operand;
                return state;
            }

            case JMZ -> {
                if (operand < 0) {
                    state = 2;
                    return state;
                }

                if (d == 0) {
                    pc = operand;
                    return state;
                }
            }

            case TAKE -> {
                d = (char) System.in.read();
                memory[m] = word(d);
            }

            case GIVE -> {
                System.out.print((char) d);
            }

            case RIGHT -> {
                m = word(m + 1);
                d = memory[m];
            }

            case LEFT -> {
                m = word(m - 1);
                d = memory[m];
            }

            default -> {
                System.out.println("Unsupported opcode: " + operation);
                state = 2;
                return state;
            }
        }
        pc = pc + 1;
        return state;
    }

    private int word(int v) {
        return SimpleOneOperandDecoder.decodeImm(v, BITS_OPERAND);
    }
}
