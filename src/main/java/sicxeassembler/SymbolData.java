package sicxeassembler;

public record SymbolData(int value, int programBlockId, Type type) {
  enum Type {
    ABSOLUTE,
    RELATIVE
  }

  @Override
  public String toString() {
    return "SymbolData["
        + "value="
        + Integer.toString(value, 16)
        + ", programBlockId="
        + programBlockId
        + ']';
  }
}
