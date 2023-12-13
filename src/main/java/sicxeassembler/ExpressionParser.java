package sicxeassembler;

import java.util.Map;
import java.util.regex.Pattern;
import sicxeassembler.errors.AssemblerException;

public class ExpressionParser {
  record Result(int value, SymbolData.Type type) {}

  private final Map<String, SymbolData> symbolTable;

  public ExpressionParser(Map<String, SymbolData> symbolTable) {
    this.symbolTable = symbolTable;
  }

  public SymbolData parse(String expression) {
    var operatorLocation = findOperator(expression);
    // No operator, either predefined symbol or decimal literal
    if (operatorLocation == -1) {
      return parseSymbol(expression);
    }
    var operator = expression.charAt(operatorLocation);
    var values = expression.split(Pattern.quote(String.valueOf(operator)));

    SymbolData left, right;
    try {
      left = parseSymbol(values[0]);
      right = parseSymbol(values[1]);
    } catch (Exception e) {
      throw new AssemblerException("Invalid expression: " + expression + ", " + e.getMessage());
    }
    switch (operator) {
      case '+' -> {
        // Both need to be absolute
        checkType(left, SymbolData.Type.ABSOLUTE);
        checkType(right, SymbolData.Type.ABSOLUTE);
        return new SymbolData(left.value() + right.value(), -1, SymbolData.Type.ABSOLUTE);
      }
      case '-' -> {
        // Cannot have Absolute - Relative or Relative - Absolute
        checkType(left, right.type());
        // Cannot have 2 arguments from different blocks
        checkSameBlock(left, right);
        return new SymbolData(left.value() - right.value(), -1, SymbolData.Type.ABSOLUTE);
      }
      case '*' -> {
        checkType(left, SymbolData.Type.ABSOLUTE);
        checkType(right, SymbolData.Type.ABSOLUTE);
        return new SymbolData(left.value() * right.value(), -1, SymbolData.Type.ABSOLUTE);
      }
      case '/' -> {
        checkType(left, SymbolData.Type.ABSOLUTE);
        checkType(right, SymbolData.Type.ABSOLUTE);
        return new SymbolData(left.value() / right.value(), -1, SymbolData.Type.ABSOLUTE);
      }
      default -> throw new AssemblerException("This shouldn't be reachable");
    }
  }

  private int findOperator(String expression) {
    Pattern pattern = Pattern.compile("[+\\-*/]");
    var matcher = pattern.matcher(expression);
    if (matcher.find()) {
      int result = matcher.start();
      if (matcher.find()) {
        throw new AssemblerException("Expressions may only have 2 terms.");
      }
      return result;
    } else {
      return -1;
    }
  }

  private void checkSameBlock(SymbolData left, SymbolData right) {
    if (left.programBlockId() != right.programBlockId()) {
      throw new AssemblerException("Expression arguments must be from the same block.");
    }
  }

  private void checkType(SymbolData data, SymbolData.Type expectedType) {
    if (!data.type().equals(expectedType)) {
      throw new AssemblerException("Invalid expression.");
    }
  }

  private SymbolData parseSymbol(String data) {
    if (symbolTable.containsKey(data)) {
      return symbolTable.get(data);
    }
    // Otherwise it has to be a literal. Try parsing as a decimal number. If that fails, bail.
    try {
      int value = Integer.parseInt(data);
      return new SymbolData(value, -1, SymbolData.Type.ABSOLUTE);
    } catch (Exception e) {
      throw new AssemblerException("Invalid symbol: " + data);
    }
  }
}
