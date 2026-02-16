package nl.suriani.tiny.machine.stack;

import lombok.SneakyThrows;
import nl.suriani.tiny.machine.util.SimpleOneOperandAssembler;
import nl.suriani.tiny.machine.util.SimpleOneOperandDecoder;

public class PascalMachine {
    private int[] stack = new int[STACK_SIZE];
    private int[] memory = new int[MEMORY_SIZE];
    private int s0;             // 0 - STACK_SIZE
    private int pc;             // 0-8
    private int state;          // 0 (ready), 1 (running), 2 (error)

    private int[] rom = new int[8];

    public static final int PUSH = 0;
    public static final int POP  = 1;
    public static final int SWAP = 2;
    public static final int DUP  = 3;
    public static final int OVER = 4;
    public static final int ADD  = 5;
    public static final int SUB  = 6;
    public static final int MOD  = 7;
    public static final int JMP  = 8;
    public static final int JNZ  = 9;
    public static final int JMZ  = 10;
    public static final int LDA  = 11;
    public static final int STR  = 12;
    public static final int LDX  = 13;
    public static final int STX  = 14;
    public static final int SYS  = 15;

    public static final int STACK_SIZE  = 256;
    public static final int MEMORY_SIZE  = 256;
    public static final int BITS_OPCODE  = 4;
    public static final int BITS_OPERAND  = 8;

    public PascalMachine(int[] rom) {
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

        if (s0 == 0) {
            return 0;
        }
        return peek();
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
            case PUSH -> {
                if (s0 >= STACK_SIZE) {
                    state = 2;
                    return state;
                }
                push(operand);
            }

            case POP -> {
                if (s0 < 1) {
                    state = 2;
                    return state;
                }
                pop();
            }

            case SWAP -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                int b = pop();
                int a = pop();
                push(b);
                push(a);
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
                push(peek());
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
                push(stack[s0 - 2]);
            }

            case ADD -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                int o2 = pop();
                int o1 = pop();
                push(o1 + o2);
            }

            case SUB -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                int o2 = pop();
                int o1 = pop();
                push(o1 - o2);
            }

            case MOD -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                int o2 = pop();
                if (o2 == 0) {
                    state = 2;
                    return state;
                }
                int o1 = pop();
                push(o1 % o2);
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
                if (pop() != 0) {
                    pc = operand;
                    return state;
                }
            }

            case JMZ -> {
                if (s0 == 0) {
                    state = 2;
                    return state;
                }
                if (pop() == 0) {
                    pc = operand;
                    return state;
                }
            }

            case LDA -> {
                if (s0 < 1) {
                    state = 2;
                    return state;
                }
                int addr = pop();
                if (addr < 0 || addr >= memory.length) {
                    state = 2;
                    return state;
                }
                push(memory[addr]);
            }

            case STR -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                int val = pop();
                int addr = pop();
                if (addr < 0 || addr >= memory.length) {
                    state = 2;
                    return state;
                }
                memory[addr] = val;
            }

            case STX -> {
                if (s0 < 2) {
                    state = 2;
                    return state;
                }
                int val = pop();
                int indirectAddress = pop();
                if (indirectAddress < 0 || indirectAddress >= memory.length) {
                    state = 2;
                    return state;
                }
                int addr = memory[indirectAddress];
                if (addr < 0 || addr >= memory.length) {
                    state = 2;
                    return state;
                }
                memory[addr] = val;
            }

            case SYS -> {
                switch (operand) {
                    case 252 -> push((char) System.in.read());
                    case 253 -> push(System.in.read());
                    case 254 -> System.out.println((char) pop());
                    case 255 -> System.out.println(pop());
                }
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

    private void push(int val) {
        stack[s0] = word(val);
        s0++;
    }

    private int pop() {
        s0--;
        int val = stack[s0];
        stack[s0] = 0;
        return val;
    }

    private int peek() {
        return stack[s0 - 1];
    }
    
    private int word(int val) {
        return val & 0xFF;
    }

    public static int opcodeOf(String instruction) {
        return switch (instruction.toUpperCase()) {
            case "PUSH" -> PUSH;
            case "POP" -> POP;
            case "SWAP" -> SWAP;
            case "DUP" -> DUP;
            case "OVER" -> OVER;
            case "ADD" -> ADD;
            case "SUB" -> SUB;
            case "MOD" -> MOD;
            case "JMP" -> JMP;
            case "JNZ" -> JNZ;
            case "JMZ" -> JMZ;
            case "LDA" -> LDA;
            case "STR" -> STR;
            case "LDX" -> LDX;
            case "STX" -> STX;
            case "SYS" -> SYS;
            default -> throw new IllegalArgumentException("Unknown instruction: " + instruction);
        };
    }

}
