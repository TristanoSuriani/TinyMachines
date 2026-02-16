package nl.suriani.tiny.machine.accumulator;

public class SumMachine {
    private int acc;            // 0-15
    private int pc;             // 0, 1
    private int state;          // 0 (not running), 1 (running)

    private int[] rom = new int[2];

    public SumMachine(int[] rom) {
        this.rom = rom;
    }

    public static void main(String[] args) {
        SumMachine m = new SumMachine(new int[] {
                SumMachine.encode(0, 5),
                SumMachine.encode(1, 7)
        });
        m.step();
        m.step();

        System.out.println(m.acc);
    }

    public int step() {
        state = 1;
        if (pc > 1) {
            state = 0;
            return state;
        }
        int instruction = rom[pc];
        int operation = decodeOp(instruction);
        int operand = decodeImm(instruction);

        if (operation == 0) { // ldi
            acc = operand;
        }
        if (operation == 1) {
            acc = acc + operand;
        }
        pc = pc + 1;
        return state;
    }

    static int encode(int op, int imm) {
        int instr = ((op & 1) << 4) | (imm & 0xF);
        return instr & 0x1F; // keep only 5 bits (0..31)
    }

    static int decodeOp(int instr) {
        return (instr >> 4) & 1;
    }

    static int decodeImm(int instr) {
        return instr & 0xF;
    }
}
