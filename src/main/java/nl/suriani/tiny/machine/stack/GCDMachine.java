package nl.suriani.tiny.machine.stack;

public class GCDMachine {
    private int[] stack = new int[STACK_SIZE];
    private int s0;             // 0 - STACK_SIZE
    private int pc;             // 0-8
    private int state;          // 0 (ready), 1 (running), 2 (error)

    private int[] rom = new int[8];

    private static final int PUSH = 0;
    private static final int POP  = 1;
    private static final int SWAP = 2;
    private static final int DUP  = 3;
    private static final int OVER = 4;
    private static final int MOD  = 5;
    private static final int JMP  = 6;
    private static final int JNZ  = 7;

    private static final int STACK_SIZE  = 3;

    public GCDMachine(int[] rom) {
        this.rom = rom;
    }

    public static void main(String[] args) {

        GCDMachine m = new GCDMachine(new int[] {
                /* 0 */ encode(PUSH, 252),    // m
                /* 1 */ encode(PUSH, 198),    // m n
                /* 2 */ encode(SWAP),            // n m
                /* 3 */ encode(OVER),            // n m n
                /* 4 */ encode(MOD),             // n m%n
                /* 5 */ encode(JNZ, 2),
                /* 6 */ encode(POP)
        });

        int result = m.run();
        System.out.println(result);
    }

    public int run() {
        state = 1;
        while (state == 1) {
            state = step();
        }

        if (state == 2) {
            return -1;
        }

        if (s0 == 0) {
            return 0;
        }
        return stack[s0 - 1];
    }

    public int step() {
        if (state == 0) {
            return state;
        }
        if (pc >= rom.length) {
            state = 0;
            return state;
        }
        int instruction = rom[pc];
        int operation = decodeOp(instruction);
        int operand = decodeImm(instruction);

        switch (operation) {
            case PUSH -> {
                if (s0 >= STACK_SIZE) {
                    state = 2;
                    return state;
                }
                stack[s0] = operand & 0xFF;
                s0 = s0 + 1;
            }

            case POP -> {
                if (s0 < 1) {
                    state = 2;
                    return state;
                }
                s0 = s0 - 1;
                stack[s0] = 0;
            }

            case SWAP -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                int temp = stack[s0 - 1];
                stack[s0 - 1] = stack[s0 - 2];
                stack[s0 - 2] = temp;
            }

            case DUP -> {
                if (s0 < 1) {
                    state = 2;
                    return state;
                }
                if (s0 >= STACK_SIZE) {
                    state = 2;
                    return state;
                }
                stack[s0] = stack[s0 - 1];
                s0 = s0 + 1;
            }

            case OVER -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                if (s0 >= STACK_SIZE) {
                    state = 2;
                    return state;
                }
                stack[s0] = stack[s0 - 2];
                s0 = s0 + 1;
            }

            case MOD -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                if (stack[s0 - 1] == 0) {
                    state = 2;
                    return state;
                }
                int o1 = stack[s0 - 2];
                int o2 = stack[s0 - 1];
                stack[s0 - 2] = (o1 % o2) & 0xFF;
                s0 = s0 - 1;
                stack[s0] = 0;
            }

            case JMP -> {
                pc = operand;
                return state;
            }

            case JNZ -> {
                if (s0 < 1) {
                    state = 2;
                    return state;
                }
                if (stack[s0 - 1] != 0) {
                    pc = operand;
                    return state;
                }
            }
        }
        pc = pc + 1;
        return state;
    }

    static int encode(int op) {
        return encode(op, 0, 0);
    }

    static int encode(int op, int imm) {
        return encode(op, 0, imm);
    }

    static int encode(int op, int r, int imm) {
        int instr = ((op & 0b111) << 9) | ((r & 0b1) << 8) | (imm & 0xFF);
        return instr & 0xFFF;
    }

    static int decodeOp(int instr) {
        return (instr >> 9) & 0b111;
    }

    static int decodeR(int instr) {
        return (instr >> 8) & 0b1;
    }

    static int decodeImm(int instr) {
        return instr & 0xFF;
    }
}
