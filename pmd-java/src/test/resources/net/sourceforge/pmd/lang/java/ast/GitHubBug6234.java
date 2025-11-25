import java.util.concurrent.Callable;

class A {
  A(Callable<Long> x) {}
}

class B extends A {
  B() {
    super(
        () -> switch (X()) {
            case 1 -> 1L;
            default -> 0;
        }
    );
  }
    void m(Callable<Boolean> x) {
         m(() -> switch (x) {
            case true -> 1;
            default -> 2;
         });
        m(switch (x) {
            case true -> () -> 1;
            default -> () -> 2;
        });
    }


    static int X() {
    return 6;
  }
}
