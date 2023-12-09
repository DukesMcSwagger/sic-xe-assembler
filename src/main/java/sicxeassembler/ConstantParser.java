package sicxeassembler;

import java.util.HashMap;

public class ConstantParser {

  private static final HashMap<String, int[]> memoCache = new HashMap<>();

  /**
   * Parses a SIC/XE byte constant to a byte array. The constant should conform to the following
   * format:
   *
   * <p>{@code <type-id> "'" <data> "'" }
   *
   * <p>Valid type IDs:
   *
   * <ul>
   *   <li>{@code C}: constant will be interpreted as a string of ASCII characters
   *   <li>{@code X}: constant will be interpreted as a sequence of hex digits.
   * </ul>
   *
   * @param input Constant to be parsed
   * @return integer array containing the bytes of the input constant
   */
  public static int[] parseByteConstant(String input) {
    // clone so if caller modifies array, the original isn't changed.
    return memoCache.computeIfAbsent(input, ConstantParser::parseByteConstantInternal).clone();
  }

  private static int[] parseByteConstantInternal(String input) {
    if (input.length() < 4) {
      throw new AssemblerException("Invalid constant: " + input);
    }
    char typeID = input.charAt(0);
    if (input.charAt(1) != '\'') {
      throw new AssemblerException("Expected \"'\", got \"" + input.charAt(1) + "\"");
    }
    var secondQuote = input.indexOf('\'', 2);
    if (secondQuote < 0) {
      throw new AssemblerException("Unexpected end of constant");
    }
    var data = input.substring(2, secondQuote);
    return switch (input.charAt(0)) {
      case 'C' -> parseStringByteConstant(data);
      case 'X' -> parseHexByteConstant(data);
      default -> throw new AssemblerException("Unknown constant type: " + typeID);
    };
  }

  static int[] parseStringByteConstant(String data) throws AssemblerException {
    int[] bytes = new int[data.length()];
    for (int i = 0; i < data.length(); i++) {
      // Make sure string is all ASCII
      char c = data.charAt(i);
      if (c > 0x7f) {
        throw new AssemblerException("Invalid non-ASCII character in string constant: " + c);
      }
      bytes[i] = c;
    }
    return bytes;
  }

  static int[] parseHexByteConstant(String data) {
    // If there is an uneven number of digits, 0-pad the first byte.
    if (data.length() % 2 != 0) {
      data = "0" + data;
    }
    int[] bytes = new int[data.length() / 2];
    for (int i = 0; i < data.length(); i += 2) {
      bytes[i / 2] = Integer.parseInt(data.substring(i, i + 2), 16);
    }
    return bytes;
  }
}
