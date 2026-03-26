#include <stdio.h>

#define PUSH 0
#define ADD 1
#define HALT 255

#define IDLE 0
#define RUNNING 1
#define HALTED 2
#define ERROR 3

#define PROGRAM_CAPACITY 256
#define STACK_SIZE 256

typedef struct {
    int stack[STACK_SIZE];
    int sp;
    int pc;
    unsigned char state;
} VM;

void step(VM* vm, const unsigned char* program) {
    if (vm->pc >= PROGRAM_CAPACITY) {
        vm->state = ERROR;
        return;
    }

    const unsigned char op = program[vm->pc++];

    switch (op) {
        case PUSH:
            if (vm->pc >= PROGRAM_CAPACITY || vm->sp >= STACK_SIZE) {
                vm->state = ERROR;
                break;
            }
            vm->stack[vm->sp++] = program[vm->pc++];
            break;

        case ADD:
            if (vm->sp < 2) {
                vm->state = ERROR;
                break;
            }
            vm->sp--;
            vm->stack[vm->sp - 1] += vm->stack[vm->sp];
            vm->stack[vm->sp] = 0;
            break;

        case HALT:
            vm->state = HALTED;
            break;

        default:
            vm->state = ERROR;
            break;
    }
}

int main(void) {

    VM vm = {0};

    unsigned char program[PROGRAM_CAPACITY] =
    {
        PUSH, 2,
        PUSH, 3,
        ADD,
        HALT
    };

    vm.state = RUNNING;
    while(vm.state == RUNNING) {
        step(&vm, program);
    }

    if (vm.state == HALTED) {
        printf("tiny machine -> %d\n", vm.stack[vm.sp - 1]);
    } else {
        printf("FATAL ERROR, exit code -> %d\n", vm.state);
    }
    return 0;
}