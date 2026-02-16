package nl.suriani.tiny.machine.util;

public interface SimpleOneOperandDecoder {
    static int decodeOp(int instr, int bitsOpcode, int bitsOperand) {
        int opMask = (int) ((1L << bitsOpcode) - 1);
        return (instr >>> bitsOperand) & opMask;
    }

    static int decodeImm(int instr, int bitsOperand) {
        if (bitsOperand == 0) return 0;
        int immMask = (int) ((1L << bitsOperand) - 1);
        return instr & immMask;
    }
}
