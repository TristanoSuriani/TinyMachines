package nl.suriani.tiny.machine.accumulator;

import nl.suriani.tiny.machine.util.SimpleOneOperandAssembler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static nl.suriani.tiny.machine.accumulator.BrainfuckMachine.*;
import static org.junit.jupiter.api.Assertions.*;

class BrainfuckMachineTest {
    
    @Test
    void test1() {
        var program = "++++++++++++++++++++++++++++++++++++++++++++++++++."; // 50+ -> prints 2
        var instructions = assemble(program);
        var bf = new BrainfuckMachine(instructions);
        bf.run();
    }

    @Test
    void test2() {
        var program = "++++++++++++++++++++++++++++++++++++++++++++++++++-."; // 50+, -, . -> prints 1
        var instructions = assemble(program);
        var bf = new BrainfuckMachine(instructions);
        bf.run();
    }

    @Test
    void test3() {
        var program = """
            ++++++++++++++++++++++++++++++++++++++++++++++++++-.
            >++++++++++++++++++++++++++++++++++++++++++++++++++.
            >+++++++++++++++++++++++++++++++++++++++++++++++++++.
            <.
            <.
            """;    // this should print 1 2 3 2 1
        var instructions = assemble(program);
        var bf = new BrainfuckMachine(instructions);
        bf.run();
    }

    @Test
    void test4() {
        var program = ",."; // echoes inserted character

        ByteArrayInputStream in = new ByteArrayInputStream("A".getBytes());
        System.setIn(in);

        var instructions = assemble(program);
        var bf = new BrainfuckMachine(instructions);
        bf.run();
    }

    @Test
    void test5() {
        var program = """
                ++++++++++++++++++++++++++++++++++++++++++++++++++[-]
                ++++++++++++++++++++++++++++++++++++++++++++++++++-.
                """;
        // this should print 1
        var instructions = assemble(program);
        var bf = new BrainfuckMachine(instructions);
        bf.run();
    }
    
    private static int[] assemble(String program) {
        return SimpleOneOperandAssembler.assemble(program, BITS_OPCODE, BITS_OPERAND, "", BrainfuckMachineTest::opcodeOf);
    }

    private static int opcodeOf(String instruction) {
        return switch (instruction.toUpperCase()) {
            case "+" -> INC;
            case "-" -> DEC;
            case "JMP" -> JMP;
            case "JMZ" -> JMZ;
            case "," -> TAKE;
            case "." -> GIVE;
            case "<" -> LEFT;
            case ">" -> RIGHT;
            default -> throw new IllegalArgumentException("Unknown instruction: " + instruction);
        };
    }
}