package nl.suriani.tiny.machine.compiler;

import nl.suriani.tiny.machine.stack.PascalMachine;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class BrainfuckToPascalMachineCompilerTest {

    private BrainfuckToPascalMachineCompiler compiler = new BrainfuckToPascalMachineCompiler();

    @Test
    void test1() {
        var program = "++++++++++++++++++++++++++++++++++++++++++++++++++."; // 50+ -> prints 2
        var pm = new PascalMachine(compiler.compile(program));
        pm.run();
    }

    @Test
    void test2() {
        var program = "++++++++++++++++++++++++++++++++++++++++++++++++++-."; // 50+, -, . -> prints 1
        var pm = new PascalMachine(compiler.compile(program));
        pm.run();
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
        var instructions = compiler.compile(program);
        var pm = new PascalMachine(instructions);
        pm.run();
    }

    @Test
    void test4() {
        var program = ",."; // echoes inserted character

        ByteArrayInputStream in = new ByteArrayInputStream("A".getBytes());
        System.setIn(in);

        var pm = new PascalMachine(compiler.compile(program));
        pm.run();
    }

    @Test
    void test5() {
        var program = """
                ++++++++++++++++++++++++++++++++++++++++++++++++++[-]
                ++++++++++++++++++++++++++++++++++++++++++++++++++-.
                """;
        // this should print 1
        var pm = new PascalMachine(compiler.compile(program));
        pm.run();
    }
}