package sicxeassembler;

public class AssemblerException extends RuntimeException {
  private String causeLine;
  private int lineNumber = -1;

  public AssemblerException(String message) {
    super(message);
  }

  public AssemblerException(AssemblerException cause, String causeLine, int lineNumber) {
    this(cause.getMessage());
    this.causeLine = causeLine;
    this.lineNumber = lineNumber;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getCauseLine() {
    return causeLine;
  }
}
