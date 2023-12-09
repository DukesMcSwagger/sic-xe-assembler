package sicxeassembler;

public class AssemblerException extends RuntimeException {
  public AssemblerException(String message) {
    super(message);
  }

  public AssemblerException(String message, Throwable cause) {
    super(message, cause);
  }
}
