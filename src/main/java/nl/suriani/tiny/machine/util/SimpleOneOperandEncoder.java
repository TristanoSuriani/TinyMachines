package nl.suriani.tiny.machine.util;

public interface SimpleOneOperandEncoder {
    static int encode(int op, int imm, int bitsOpcode, int bitsOperand) {
        int w = bitsOpcode + bitsOperand;
        if (w <= 0 || w > 31) throw new IllegalArgumentException("word bits must be 1..31");
        if (bitsOpcode < 1) throw new IllegalArgumentException("bitsOpcode must be >= 1");
        if (bitsOperand < 0) throw new IllegalArgumentException("bitsOperand must be >= 0");

        int opMask = (int) ((1L << bitsOpcode) - 1);
        int immMask = bitsOperand == 0 ? 0 : (int) ((1L << bitsOperand) - 1);
        int wordMask = (int) ((1L << w) - 1);

        int instr = ((op & opMask) << bitsOperand) | (bitsOperand == 0 ? 0 : (imm & immMask));
        return instr & wordMask;
    }
}
