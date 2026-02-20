package nl.suriani.tiny.machine.stack;

import nl.suriani.tiny.machine.util.SimpleOneOperandAssembler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PascalMachineTest {

    @Test
    void sum() {
        var program = """
                PUSH 5
                PUSH 7
                ADD""";

        var instructions = SimpleOneOperandAssembler.assemble(program, 4, 8, PascalMachine::opcodeOf);
        var pm = new PascalMachine(instructions);

        var result = pm.run();
        System.out.println(result);
    }

    @Test
    void gcd() {
        var program = """
                PUSH 252
                PUSH 198
                loop:
                    SWAP
                    OVER
                    MOD
                    DUP
                    JNZ loop
                    POP
        """;

        var instructions = SimpleOneOperandAssembler.assemble(program, 4, 8, PascalMachine::opcodeOf);
        var pm = new PascalMachine(instructions);

        var result = pm.run();
        System.out.println(result);
    }

    @Test
    void fibonacci() {
        var program = """
                PUSH 0      ; push(a)                   a
                PUSH 1      ; push(b)                   a b
                PUSH 0      ; push(0)                   a b 0
                PUSH 10     ; push(n)                   a b 0 n
                STR         ; store n to ram[0]         a b     mem[n]
                PUSH 1      ; push(1)                   a b 1   mem[n]
                PUSH 1      ; push(i)                   a b 1 i mem[n]
                STR         ; store i to ram[1]         a b     mem[n, i]
                
                loop:
                    PUSH 0  ; push(0)                   a b 0   mem[n, i]
                    LDA     ; push(mem[0])              a b n   mem[n, i]
                    PUSH 1  ; push(1)                   a b n 1 mem[n, i]
                    LDA     ; push(mem[1])              a b n i mem[n, i]
                    SUB     ; push(n-i)                 a b n-i mem[n, i]
                    JMZ end ; if n == i end
                    PUSH 1  ; push(1)                   a b 1   mem[n, i]
                    LDA     ; push(mem[1])              a b i   mem[n, i]
                    PUSH 1  ; push(1)                   a b i 1 mem[n, i]
                    ADD     ; push(i+1)                 a b i+1 mem[n, i]
                    PUSH 1  ; push(1)                   a b i+1 1 mem[n, i]
                    SWAP    ;                           a b 1 i+1 mem[n, i]
                    STR     ;                           a b     mem[n, i+1]
                    SWAP    ;                           b a
                    OVER    ;                           b a b
                    ADD     ;                           b a+b
                    JMP loop
                end:
                
                
        """;

        var instructions = SimpleOneOperandAssembler.assemble(program, 4, 8, PascalMachine::opcodeOf);
        var pm = new PascalMachine(instructions);

        var result = pm.run();
        System.out.println(result);
    }

    @Test
    void sherezade() {
        var program = """
                    PUSH 0
                    PUSH 4      ; thieves
                    STR
                    
                    PUSH 1
                    PUSH 1      ; diamonds
                    STR
                    
                    PUSH 2
                    PUSH 2      ; x in 2 * (n + x)
                    STR
                    
                    loop:
                        PUSH 0
                        LDA     ; thieves
                        PUSH 0
                        SUB     
                        JMZ end    ; if thieves == 0 go to end;
                        
                        ; else
                        PUSH 0
                        LDA
                        PUSH 1
                        SUB         ; thieves - 1
                        PUSH 0
                        SWAP
                        STR
                        
                        PUSH 1
                        LDA
                        PUSH 2
                        LDA
                        ADD
                        DUP
                        ADD         ; 2 * (nDiamonds + 1)
                        PUSH 1
                        SWAP
                        STR
                        JMP loop
                        
                    end:
                        PUSH 1
                        LDA
                        SYS 255
                           
                    """;

        var instructions = SimpleOneOperandAssembler.assemble(program, 4, 8, PascalMachine::opcodeOf);
        var pm = new PascalMachine(instructions);

        var result = pm.run();
        System.out.println(result);
    }
}