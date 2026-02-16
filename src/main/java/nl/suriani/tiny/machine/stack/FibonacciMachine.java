package nl.suriani.tiny.machine.stack;

public class FibonacciMachine {
    private int[] dStack = new int[STACK_SIZE];
    private int[] cStack = new int[STACK_SIZE];
    private int [] selStack = dStack;
    private SP sp = new SP();

    private int s0;             // 0 - STACK_SIZE
    private int s1;             // 0 - STACK_SIZE
    private int ws;             // 0 - 1
    private int pc;             // 0-8
    private int state;          // 0 (ready), 1 (running), 2 (error)

    private int[] rom = new int[8];

    private static final int PUSH = 0;
    private static final int POP  = 1;
    private static final int SWAP = 2;
    private static final int DUP =  3;
    private static final int CHS  = 4;
    private static final int OVER = 5;
    private static final int ADD =  6;
    private static final int DEC  = 7;
    private static final int JMZ  = 8;
    private static final int JMP  = 9;

    private static final int STACK_SIZE  = 3;

    public FibonacciMachine(int[] rom) {
        this.rom = rom;
    }

    public static void main(String[] args) {

        FibonacciMachine m = new FibonacciMachine(new int[] {
                /* 0 */ encode(PUSH, 0),    // a
                /* 1 */ encode(PUSH, 1),    // a b
                /* 2 */ encode(CHS, 1),     // select "c stack"
                /* 3 */ encode(PUSH, 10),   // n
                /* 4 */ encode(DEC),             // n-1
                /* 5 */ encode(JMZ, 13),    // if n==0 stop (after the DEC)
                /* 6 */ encode(CHS, 0),     // select "d stack"
                /* 7 */ encode(SWAP),            // b a
                /* 8 */ encode(OVER),            // b a b
                /* 9 */ encode(ADD),             // b a+b
                /*10 */ encode(CHS, 1),     // back to "n stack"
                /*11 */ encode(DEC),             // n-2
                /*12 */ encode(JMP, 5),     // loop check
                /*13 */ encode(CHS, 0)      // loop check
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

        if (sp.get() == 0) {
            return 0;
        }
        return selStack[sp.get() - 1];
    }

    public int step() {
        if (state != 1) {
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
                if (sp.get() >= STACK_SIZE) {
                    state = 2;
                    return state;
                }
                selStack[sp.get()] = operand & 0xFF;
                sp.set(sp.get() + 1);
            }

            case POP -> {
                if (sp.get() < 1) {
                    state = 2;
                    return state;
                }
                sp.set(sp.get() - 1);
                selStack[sp.get()] = 0;
            }

            case SWAP -> {
                if (sp.get() < 2) {
                    state = 2;
                    return state;
                }
                int temp = selStack[sp.get() - 1];
                selStack[sp.get() - 1] = selStack[sp.get() - 2];
                selStack[sp.get() - 2] = temp;
            }

            case DUP -> {
                if (sp.get() < 1) {
                    state = 2;
                    return state;
                }
                if (sp.get() >= STACK_SIZE) {
                    state = 2;
                    return state;
                }
                selStack[sp.get()] = selStack[sp.get() - 1];
                sp.set(sp.get() + 1);
            }

            case CHS -> {
                if (operand < 0 || operand > 1) {
                    state = 2;
                    return state;
                }
                sp.chs(operand);
                selStack = operand == 0 ? dStack : cStack;
            }

            case OVER -> {
                if (sp.get() < 2) {
                    state = 2;
                    return state;
                }
                if (sp.get() >= STACK_SIZE) {
                    state = 2;
                    return state;
                }
                selStack[sp.get()] = selStack[sp.get() - 2];
                sp.set(sp.get() + 1);
            }

            case ADD -> {
                if (sp.get() < 2) {
                    state = 2;
                    return state;
                }
                int o1 = selStack[sp.get() - 2];
                int o2 = selStack[sp.get() - 1];
                selStack[sp.get() - 2] = (o1 + o2) & 0xFF;
                sp.set(sp.get() - 1);
                selStack[sp.get()] = 0;

            }

            case DEC -> {
                if (sp.get() < 1) {
                    state = 2;
                    return state;
                }
                selStack[sp.get() - 1] = (selStack[sp.get() - 1] - 1) & 0xFF;
            }

            case JMP -> {
                pc = operand;
                return state;
            }

            case JMZ -> {
                if (sp.get() < 1) {
                    state = 2;
                    return state;
                }
                if (selStack[sp.get() - 1] == 0) {
                    pc = operand;
                    return state;
                }
            }
        }
        pc = pc + 1;
        return state;
    }

    static int encode(int op) {
        return encode(op, 0);
    }

    static int encode(int op, int imm) {
        int instr = ((op & 0xF) << 8) | (imm & 0xFF);
        return instr & 0xFFF;   // 12-bit word
    }

    static int decodeOp(int instr) {
        return (instr >> 8) & 0xF;
    }

    static int decodeImm(int instr) {
        return instr & 0xFF;
    }

    private class SP {
        public int get() {
            return ws == 0 ? s0 : s1;
        }

        public void chs(int value) {
            ws = value;
        }

        public void set(int value) {
            if (ws == 0) {
                s0 = value;
            } else {
                s1 = value;
            }
        }
    }
}
