package sicxeassembler;

import java.util.*;
import sicxeassembler.errors.AssemblerException;
import sicxeassembler.output.OutputGenerator;

public class PassTwo {
  private final OpTable opTable;
  private final int startLocation;
  private final Map<String, SymbolData> symbolTable;
  private final Map<String, Literal> literalTable;
  private final List<Integer> blockAddresses;
  private final List<PassOneData> inputs;
  private final List<OutputGenerator> outputs = new ArrayList<>();

  int baseAddress;

  public PassTwo(
      OpTable opTable,
      int startLocation,
      Map<String, SymbolData> symbolTable,
      Map<String, Literal> literalTable,
      List<Integer> blockAddresses,
      List<PassOneData> inputs) {
    this.opTable = opTable;
    this.startLocation = startLocation;
    this.symbolTable = symbolTable;
    this.literalTable = literalTable;
    this.blockAddresses = blockAddresses;
    this.inputs = inputs;
  }

  public void addOutputGenerator(OutputGenerator generator) {
    outputs.add(generator);
  }

  public void process() {
    outputs.forEach(
        generator ->
            generator.initialize(
                inputs.get(0).line().getLabel(),
                startLocation,
                blockAddresses.get(blockAddresses.size() - 1)));
    for (var input : inputs) {
      if (input.line().isCommentOrBlank()) {
        outputCommentOnly(input);
      } else if (input.line().getOpCodePrefix().equals("=")) {
        outputLiteral(input);
      } else if (opTable.contains(input.line().getOpCode())) {
        outputInstruction(input);
      } else {
        handleDirective(input);
      }
    }
    outputs.forEach(OutputGenerator::finalizeOutput);
  }

  private void outputInstruction(PassOneData line) {
    var operation = opTable.get(line.line().getOpCode());
    int[] objectCode =
        switch (operation.format()) {
          case ONE -> assembleFormatOne(operation);
          case TWO -> assembleFormatTwo(
              operation, line.line().getArgOne(), line.line().getArgTwo());
          case THREE_FOUR -> assembleFormatThreeFour(operation, line);
        };
    appendOutput(new PassTwoData(line, convertAddress(line), objectCode));
  }

  private int[] assembleFormatOne(Operation op) {
    return new int[] {op.opcode()};
  }

  private int[] assembleFormatTwo(Operation op, String argOne, String argTwo) {

    int operands = (generateOperand(argOne) << 4) | generateOperand(argTwo);
    return new int[] {
      op.opcode(),
    };
  }

  private int[] assembleFormatThree(Operation op, PassOneData instruction) {
    var argFlag = instruction.line().getArgOnePrefix();
    var operand =
        generateOperand(instruction.line().getArgOnePrefix(), instruction.line().getArgOne());
    var address = convertAddress(instruction);
    var flags = 0;
    boolean n = false;
    boolean i = false;
    boolean x = false;
    boolean b = false;
    boolean p = false;
    boolean e = false;
    e = false;
    if (argFlag.equals("@")) {
      n = true;
      i = false;
    } else if (argFlag.equals("#")) {
      n = false;
      i = true;
    } else {
      n = true;
      i = true;
    }
    if (instruction.line().getArgTwo().equals("X")) {
      if (!n || !i) {
        throw new AssemblerException(
            "Indexed addressing cannot be used with immediate or indirect addressing.");
      }
      x = true;
    }
    if (argFlag.equals("#")) {
      if (operand > 4095) {
        throw new AssemblerException(
            "Direct operand "
                + "'"
                + instruction.line().getArgOne()
                + "' exceeds 4096! Try using extended format.");
      }
    }
    var disp = address + 3 - operand;
    // try pc relative
    if (disp > -2048 && disp < 2047) {
      p = true;
      b = false;
    } else if (disp > 0 && disp < 4095) {

    }

    var byteOne = op.opcode() | ((n ? 1 : 0) << 1) | (i ? 1 : 0);
    var byteTwo = 0;
    byteTwo |= (x ? 1 : 0) << 8;
    byteTwo |= (b ? 1 : 0) << 7;
    byteTwo |= (p ? 1 : 0) << 6;
    byteTwo |= (e ? 1 : 0) << 5;
    byteTwo |= ((operand & 0xf00) >> 8);
    var byteThree = (operand & 0xff);
    return new int[] {byteOne, byteTwo, byteThree};
  }

  private int[] assembleFormatFour(Operation op, PassOneData instruction) {
    var argFlag = instruction.line().getArgOnePrefix();
    var operand =
        generateOperand(instruction.line().getArgOnePrefix(), instruction.line().getArgOne());
    var address = convertAddress(instruction);
    boolean n = false;
    boolean i = false;
    boolean x = false;
    boolean b = false;
    boolean p = false;
    boolean e = true;
    if (argFlag.equals("@")) {
      n = true;
    } else if (argFlag.equals("#")) {
      i = true;
    } else {
      n = true;
      i = true;
    }
    if (instruction.line().getArgTwo().equals("X")) {
      if (!n || !i) {
        throw new AssemblerException(
            "Indexed addressing cannot be used with immediate or indirect addressing.");
      }
      x = true;
    }
    var byteOne = op.opcode() | ((n ? 1 : 0) << 1) | (i ? 1 : 0);
    var byteTwo = 0;
    byteTwo |= (x ? 1 : 0) << 8;
    byteTwo |= (b ? 1 : 0) << 7;
    byteTwo |= (p ? 1 : 0) << 6;
    byteTwo |= (e ? 1 : 0) << 5;
    byteTwo |= ((operand & 0xf0000) >> 16);
    var byteThree = (operand & 0xff00) >> 8;
    var byteFour = (operand & 0xff);
    return new int[] {byteOne, byteTwo, byteThree};
  }

  private int[] assembleFormatThreeFour(Operation op, PassOneData instruction) {
    if (instruction.line().getOpCodePrefix().equals("+")) {
      return assembleFormatFour(op, instruction);
    } else {
      return assembleFormatThree(op, instruction);
    }
  }

  private int getLiteralAddress(String literal) {
    var lit = literalTable.get(literal);
    return convertAddress(lit.getBlock(), lit.getAddress());
  }

  private int generateOperand(String argFlag, String arg) {
    if (argFlag.equals("=")) {
      return getLiteralAddress(arg);
    }
    return generateOperand(arg);
  }

  private int generateOperand(String arg) {
    if (arg.isEmpty()) return 0;

    if (symbolTable.containsKey(arg)) {
      var symbol = symbolTable.get(arg);
      return convertAddress(symbol.programBlockId(), symbol.value());
    } else {
      try {
        return Integer.parseInt(arg);
      } catch (Exception e) {
        throw new AssemblerException("Undefined symbol: " + arg);
      }
    }
  }

  private void handleDirective(PassOneData line) {
    switch (line.line().getOpCode()) {
      case "BASE":
        handleBASE(line);
        break;
      case "WORD":
        handleWORD(line);
        break;
      case "BYTE":
        handleBYTE(line);
        break;
      case "RESW":
      case "RESB":
        appendOutput(new PassTwoData(line, convertAddress(line), null));
      case "START":
        break;
    }
  }

  private void handleWORD(PassOneData line) {
    var value = Integer.parseInt(line.line().getArgOne());
    var bytes = new int[3];
    bytes[0] = (value & 0xff0000) >> 4;
    bytes[1] = (value & 0xff00) >> 2;
    bytes[2] = (value & 0xff);
    appendOutput(new PassTwoData(line, convertAddress(line), bytes));
  }

  private void handleBYTE(PassOneData line) {
    appendOutput(
        new PassTwoData(
            line, convertAddress(line), ConstantParser.parseByteConstant(line.line().getArgOne())));
  }

  private void handleBASE(PassOneData line) {
    if (line.line().getArgOne().equals("*")) {
      baseAddress = convertAddress(line);
    } else {
      baseAddress = generateOperand(line.line().getArgOne());
    }
  }

  private void outputLiteral(PassOneData line) {
    var literal = literalTable.get(line.line().getOpCode());
    appendOutput(
        new PassTwoData(
            line, convertAddress(literal.getBlock(), literal.getAddress()), literal.getData()));
  }

  private void outputCommentOnly(PassOneData line) {
    appendOutput(new PassTwoData(line, convertAddress(line.block(), line.addressInBlock()), null));
  }

  private int convertAddress(int blockId, int location) {
    if (blockId < 1) {
      return location;
    }
    return blockAddresses.get(blockId) + location;
  }

  private int convertAddress(PassOneData line) {
    return convertAddress(line.block(), line.addressInBlock());
  }

  private void appendOutput(PassTwoData out) {
    outputs.forEach((generator) -> generator.accept(out));
  }
}
