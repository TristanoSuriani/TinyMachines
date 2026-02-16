package nl.suriani.tiny.machine.compiler;

import nl.suriani.tiny.machine.util.SimpleOneOperandEncoder;

import java.util.ArrayList;
import java.util.List;

import static nl.suriani.tiny.machine.stack.PascalMachine.*;

public class BrainfuckToPascalMachineCompiler {
    public int[] compile(String program) {
        List<Integer> instructions = new ArrayList<>();

        char[] chars = program.toCharArray();

        instructions.add(encode(PUSH, 0));
        instructions.add(encode(DUP));
        instructions.add(encode(PUSH, 0));
        instructions.add(encode(STR));

        convertToPascalMachineInstructions(chars, instructions);

        return instructions.stream().mapToInt(i -> i).toArray();
    }

    private void convertToPascalMachineInstructions(char[] chars, List<Integer> instructions) {
        for (int i = 0; i < chars.length; i++) {
            var ops = convert(chars[i]);
            instructions.addAll(ops);
        }
    }

    private List<Integer> convert(char ch) {
        return switch (ch) {
            case '>' -> List.of(
                    encode(PUSH, 1),
                    encode(ADD)
            );

            case '<' -> List.of(
                    encode(PUSH, 1),
                    encode(SUB)
            );

            case '+' -> List.of(
                    encode(DUP),
                    encode(LDA),
                    encode(PUSH, 1),
                    encode(ADD),
                    encode(OVER),
                    encode(SWAP),
                    encode(STR)
            );

            case '-' -> List.of(
                    encode(DUP),
                    encode(LDA),
                    encode(PUSH, 1),
                    encode(SUB),
                    encode(OVER),
                    encode(SWAP),
                    encode(STR)
            );

            case '.' -> List.of(
                    encode(DUP),
                    encode(LDA),
                    encode(SYS, 254)
            );

            case ',' -> List.of(
                    encode(DUP),
                    encode(SYS, 252),
                    encode(STR)
            );

            default -> List.of();
        };
    }

    private int encode(int opcode, int operand) {
        return SimpleOneOperandEncoder.encode(opcode, operand, BITS_OPCODE, BITS_OPERAND);
    }

    private int encode(int opcode) {
        return encode(opcode, 0);
    }
}
