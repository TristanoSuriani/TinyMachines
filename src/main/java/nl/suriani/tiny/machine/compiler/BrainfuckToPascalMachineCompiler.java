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
        int i = 0;
        while (i < chars.length) {
            char ch = chars[i];

            int amount = 1;
            i++;
            while (i < chars.length && chars[i] == ch) {
                amount++;
                i++;
            }

            instructions.addAll(convert(ch, amount));
        }
    }

    private List<Integer> convert(char ch, int amount) {
        return switch (ch) {
            case '>' -> List.of(
                    encode(PUSH, amount),
                    encode(ADD)
            );

            case '<' -> List.of(
                    encode(PUSH, amount),
                    encode(SUB)
            );

            case '+' -> List.of(
                    encode(DUP),
                    encode(LDA),
                    encode(PUSH, amount),
                    encode(ADD),
                    encode(OVER),
                    encode(SWAP),
                    encode(STR)
            );

            case '-' -> List.of(
                    encode(DUP),
                    encode(LDA),
                    encode(PUSH, amount),
                    encode(SUB),
                    encode(OVER),
                    encode(SWAP),
                    encode(STR)
            );

            case '.' -> {
                List<Integer> instructions = new ArrayList<>();
                instructions.add(encode(DUP));
                instructions.add(encode(LDA));
                for (int k = 0; k < amount; k++) instructions.add(encode(SYS, 254));
                yield  instructions;
            }

            case ',' -> {
                    List<Integer> instructions = new ArrayList<>();
                instructions.add(encode(DUP));
                for (int k = 0; k < amount; k++) instructions.add(encode(SYS, 252));
                instructions.add(encode(STR));
                yield  instructions;
            }

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
