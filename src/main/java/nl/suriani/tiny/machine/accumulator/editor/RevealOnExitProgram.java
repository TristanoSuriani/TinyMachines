package nl.suriani.tiny.machine.accumulator.editor;

import static nl.suriani.tiny.machine.accumulator.editor.EdMachine.encode;

public class RevealOnExitProgram {

    static int[] supply() {
        int LOOP  = 0;
        int UP    = 20;
        int DOWN  = 22;
        int LEFT  = 24;
        int RIGHT = 26;
        int EXIT  = 28;

        int[] program = new int[] {
                // 0: loop
                encode("getc", 0),
                encode("stad", 0),     // ZP[0] = input

                encode("cmpi", 0),
                encode("jmp0", EXIT),  // if input == 0 -> exit

                encode("ldad", 0),
                encode("cmpi", 1),
                encode("jmp0", UP),

                encode("ldad", 0),
                encode("cmpi", 2),
                encode("jmp0", DOWN),

                encode("ldad", 0),
                encode("cmpi", 3),
                encode("jmp0", LEFT),

                encode("ldad", 0),
                encode("cmpi", 4),
                encode("jmp0", RIGHT),

                // default: write char and advance
                encode("ldad", 0),
                encode("setc", 0),
                encode("incc", 0),
                encode("jump", LOOP),

                // 20: up  (NOTE: "up" is previousRow -> decr in your VM)
                encode("decr", 0),
                encode("jump", LOOP),

                // 22: down (nextRow -> incr)
                encode("incr", 0),
                encode("jump", LOOP),

                // 24: left
                encode("decc", 0),
                encode("jump", LOOP),

                // 26: right
                encode("incc", 0),
                encode("jump", LOOP),

                // 28: exit
                encode("pres", 0),
                encode("sysc", 0)
        };

        return program;
    }
}
