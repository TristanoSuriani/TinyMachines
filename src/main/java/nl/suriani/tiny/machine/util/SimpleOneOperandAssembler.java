package nl.suriani.tiny.machine.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface SimpleOneOperandAssembler {
    static int[] assemble(String program, int bitsOpcode, int bitsOperand, Function<String, Integer> translateOpcode) {
        int bOpcode = bitsOpcode;
        int bOperand = bitsOperand;

        if (bOpcode < 1) throw new IllegalArgumentException("bitsOpcode must be >= 1");
        if (bOperand < 0) throw new IllegalArgumentException("bitsOperand must be >= 0");
        int w = bOpcode + bOperand;
        if (w <= 0 || w > 31) throw new IllegalArgumentException("word bits must be 1..31");

        List<String> lines = new ArrayList<>(Arrays.stream(program.split("\n"))
                .map(String::trim)
                .map(line -> line.split(";")[0].trim())    // strip comments
                .filter(line -> !line.isEmpty())
                .toList());

        List<Label> labels = Stream.iterate(0, i -> i < lines.size(), i -> i + 1)
                .filter(i -> lines.get(i).endsWith(":"))
                .map(i -> new Label(lines.get(i), i))
                .toList();

        int[] instructions = new int[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.endsWith(":")) {
                line = "SYS 0";
            }
            List<String> tokens = Arrays.stream(line.split(" "))
                    .map(String::trim)
                    .filter(token -> !token.isEmpty())
                    .toList();

            int opcode = translateOpcode.apply(tokens.get(0));
            int operand = tokens.size() == 2
                    ? normaliseOperand(tokens.get(1), labels)
                    : 0;

            instructions[i] = SimpleOneOperandEncoder.encode(opcode, operand, bOpcode, bOperand);
        }
        return instructions;
    }

    private static int normaliseOperand(String operand, List<Label> labels) {
        return labels.stream()
                .filter(label -> label.value().equals(operand))
                .map(Label::lineNumber)
                .findFirst()
                .orElse(Integer.parseInt(operand));
    }

    record Label(String value, int lineNumber) {
        public Label(String value, int lineNumber) {
            this.value = value.split(":")[0];
            this.lineNumber = lineNumber;
        }
    }
}