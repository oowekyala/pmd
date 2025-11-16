import java.util.concurrent.Callable;

class A {
  A(Callable<Boolean> x) {}
}

class B extends A {
  B() {
    super(
        () -> switch (x) {
            case true -> true;
            default -> false;
        }
    );
  }

  static boolean X() {
    return true;
  }
}
