package sicxeassembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sicxeassembler.errors.AssemblerException;

public class ExpressionParserTest {
  Map<String, SymbolData> symbolTable = new HashMap<>();
  ExpressionParser parser = new ExpressionParser(symbolTable);

  @BeforeEach
  void initSymbolTable() {
    symbolTable.put("ONE", new SymbolData(1, -1, SymbolData.Type.ABSOLUTE));
    symbolTable.put("TWO", new SymbolData(2, -1, SymbolData.Type.ABSOLUTE));
    symbolTable.put("DATA", new SymbolData(10, 1, SymbolData.Type.RELATIVE));
    symbolTable.put("BUF", new SymbolData(12, 1, SymbolData.Type.RELATIVE));
    symbolTable.put("OTHER", new SymbolData(16, 2, SymbolData.Type.RELATIVE));
  }

  @Test
  void testMultipleTerms() {
    assertThrows(AssemblerException.class, () -> parser.parse("ONE+TWO-THREE"));
  }

  @Test
  void testAdd() {
    var result = parser.parse("ONE+TWO");
    assertEquals(3, result.value());
    assertEquals(SymbolData.Type.ABSOLUTE, result.type());
    assertThrows(AssemblerException.class, () -> parser.parse("ONE+DATA"));
    assertThrows(AssemblerException.class, () -> parser.parse("DATA+BUF"));
  }

  @Test
  void testMinus() {
    var result = parser.parse("TWO-ONE");
    assertEquals(1, result.value());
    assertEquals(SymbolData.Type.ABSOLUTE, result.type());
    result = parser.parse("BUF-DATA");
    assertEquals(12 - 10, result.value());
    assertEquals(SymbolData.Type.ABSOLUTE, result.type());
    assertThrows(AssemblerException.class, () -> parser.parse("BUF-OTHER"));
  }

  @Test
  void testMultiply() {
    var result = parser.parse("ONE*TWO");
    assertEquals(2, result.value());
    assertEquals(SymbolData.Type.ABSOLUTE, result.type());
    assertThrows(AssemblerException.class, () -> parser.parse("ONE*DATA"));
    assertThrows(AssemblerException.class, () -> parser.parse("DATA*BUF"));
  }

  @Test
  void testDivide() {
    var result = parser.parse("ONE/TWO");
    assertEquals(0, result.value());
    assertEquals(SymbolData.Type.ABSOLUTE, result.type());
    assertThrows(AssemblerException.class, () -> parser.parse("ONE/DATA"));
    assertThrows(AssemblerException.class, () -> parser.parse("DATA/BUF"));
  }
}
