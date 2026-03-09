package nl.suriani.tiny.machine.accumulator.editor;

import lombok.SneakyThrows;

import static nl.suriani.tiny.machine.accumulator.editor.EdMachine.COLS;
import static nl.suriani.tiny.machine.accumulator.editor.EdMachine.SCREEN_SIZE;

public class EdMachineRevealOnExitExecutor {

    public static void main(String[] args) {
        EdMachineRevealOnExitExecutor.run();
    }

    static void run() {
        byte[] scriptedInput = new byte[] {
                'A',
                'B',
                3,      // left
                'C',
                2,      // down
                'D',
                1,      // up
                4,      // right
                'E',
                0       // exit → pres + halt
        };

        System.setIn(new java.io.ByteArrayInputStream(scriptedInput));


        int[] program = RevealOnExitProgram.supply();

        var inputDevice = new EdMachine.InputDevice() {
            @Override
            @SneakyThrows
            public int read() {
                int v = System.in.read();
                return (v == -1) ? 0 : v;   // EOF -> exit
            }
        };

        var outputDevice = new EdMachine.OutputDevice() {
            int[] buffer = new int[SCREEN_SIZE];

            @Override
            public void write(int value, int x, int y) {
                buffer[y * COLS + x] = value;
            }

            @Override
            public void present(int[] buffer, int offset, int length) {
                System.arraycopy(buffer, offset, this.buffer, 0, length);

                for (int row = 0; row < SCREEN_SIZE / COLS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        int v = this.buffer[row * COLS + col];

                        char c = (v == 0) ? ' ' : (char) (v & 0xFF);
                        System.out.print(c);
                    }
                    System.out.println();
                }

                System.out.println("----------------------------------------");
            }
        };

        var machine = new EdMachine(program, inputDevice, outputDevice);
        machine.run();
    }
}
