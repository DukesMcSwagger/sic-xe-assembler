package sicxeassembler;

public record Operation(String mnemonic, int opcode, Format format) {
  static Operation op(String mnemonic, int opcode, Format format) {
    return new Operation(mnemonic, opcode, format);
  }

  public enum Format {
    ONE,
    TWO,
    THREE_FOUR;

    public static Format fromValue(int format) {
      return switch (format) {
        case 1 -> ONE;
        case 2 -> TWO;
        case 3, 4 -> THREE_FOUR;
        default -> throw new IndexOutOfBoundsException("Format code " + format + " out of bounds!");
      };
    }
  }
}
