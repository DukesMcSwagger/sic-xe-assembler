package sicxeassembler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ConstantParserTest {
  @Test
  void testParseValidHexConstant() {
    assertArrayEquals(
        new int[] {0x01, 0x02, 0x03, 0x7f, 0xff},
        ConstantParser.parseHexByteConstant("0102037fff"));
    assertArrayEquals(new int[] {0x00, 0x01}, ConstantParser.parseHexByteConstant("001"));
    assertArrayEquals(new int[] {0x01}, ConstantParser.parseHexByteConstant("1"));
  }

  @Test
  void testParseInvalidHexConstant() {
    assertThrows(
        NumberFormatException.class,
        () -> {
          ConstantParser.parseHexByteConstant("asdf");
        });
  }

  @Test
  void testParseValidStringConstant() throws AssemblerException {
    assertArrayEquals(
        charsToInts('T', 'e', 's', 't'), ConstantParser.parseStringByteConstant("Test"));
  }

  @Test
  void testParseInvalidStringConstant() throws AssemblerException {
    assertThrows(
        AssemblerException.class, () -> ConstantParser.parseStringByteConstant("Invalid:¢"));
  }

  @Test
  void testParseWholeValidConstant() throws AssemblerException {
    assertArrayEquals(
        new int[] {0x05, 0x01, 0x71, 0x00, 0xc1},
        ConstantParser.parseByteConstant("X'05017100C1'"));
    assertArrayEquals(new int[] {0x05}, ConstantParser.parseByteConstant("X'5'"));

    assertArrayEquals(
        charsToInts('T', 'e', 's', 't', 'i', 'n', 'g'),
        ConstantParser.parseByteConstant("C'Testing'"));
  }

  @Test
  void testParseWholeInvalidConstant() throws AssemblerException {
    // Not a valid constant
    assertThrows(AssemblerException.class, () -> ConstantParser.parseByteConstant("garbage"));
    // Missing opening quote
    assertThrows(AssemblerException.class, () -> ConstantParser.parseByteConstant("XF1'"));
    // Missing closing quote
    assertThrows(AssemblerException.class, () -> ConstantParser.parseByteConstant("X'F1"));
    // Missing type identifier
    assertThrows(AssemblerException.class, () -> ConstantParser.parseByteConstant("'F1'"));
    // Invalid type identifier
    assertThrows(AssemblerException.class, () -> ConstantParser.parseByteConstant("J'whatever'"));
  }

  int[] charsToInts(char... chars) {
    int[] bytes = new int[chars.length];
    for (int i = 0; i < chars.length; i++) {
      bytes[i] = chars[i];
    }
    return bytes;
  }
}
