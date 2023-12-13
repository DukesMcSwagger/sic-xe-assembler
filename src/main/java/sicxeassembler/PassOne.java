package sicxeassembler;

import java.util.*;
import sicxeassembler.errors.AssemblerException;

public class PassOne {
  private final List<SourceLine> lines;
  private final List<PassOneData> output = new LinkedList<>();
  private final ProgramBlockTable programBlocks = new ProgramBlockTable();
  private final Map<String, SymbolData> symbolTable = new LinkedHashMap<>();
  private final OpTable opTable;

  /** The active program block. Initialized to default. */
  private ProgramBlock activeBlock;

  private int startAddress = -1;
  private int currentLineIndex = -1;
  private SourceLine currentLine;

  private final int WORD_SIZE = 3;
  private final int BYTE_SIZE = 1;

  public PassOne(OpTable opTable, List<SourceLine> lines) {
    this.opTable = opTable;
    this.lines = lines;
    activeBlock = programBlocks.getBlock("");
  }

  public String getActiveBlockName() {
    return getActiveBlock().getName();
  }

  public int getActiveBlockId() {
    return getActiveBlock().getId();
  }

  public ProgramBlock getActiveBlock() {
    return activeBlock;
  }

  public int getActiveBlockLocation() {
    return getActiveBlock().getId();
  }

  private void setActiveBlockName(String activeBlockName) {
    this.activeBlock = programBlocks.getBlock(activeBlockName);
  }

  public int getStartAddress() {
    return startAddress;
  }

  private void setStartAddress(int startAddress) {
    this.startAddress = startAddress;
  }

  public void process() {
    getNextLine();
    initializeStartAddress();
    while (!finished()) {
      try {
        if (currentLine.isCommentOrBlank()) {
          appendOutputData(
              new PassOneData(
                  currentLine, 0, getActiveBlock().getId(), getActiveBlock().get(), false));
        } else if (opTable.contains(currentLine.getOpCode())) {
          handleInstruction(currentLine);
        } else {
          handleDirective(currentLine);
        }
      } catch (AssemblerException e) {
        throw new AssemblerException(e, currentLine.getOriginalSource(), currentLineIndex + 1);
      }
      getNextLine();
    }
  }

  private boolean finished() {
    return currentLine == null || currentLine.getOpCode().equals("END");
  }

  private void initializeStartAddress() {
    if (currentLine.getOpCode().equals("START")) {
      setStartAddress(Integer.parseInt(currentLine.getArgOne(), 16));
      getNextLine();
    } else {
      setStartAddress(0);
    }
  }

  private void handleDirective(SourceLine line) {
    switch (line.getOpCode()) {
      case "WORD":
        handleWORD(line);
        break;
      case "BYTE":
        handleBYTE(line);
        break;
      case "RESW":
        handleRESW(line);
        break;
      case "RESB":
        handleRESB(line);
        break;
      case "USE":
        handleUSE(line);
        break;
      case "EQU":
        handleEQU(line);
        break;
      case "LTORG":
        handleLTORG(line);
      case "BASE":
        break;
      default:
        throw new AssemblerException("Unknown directive or instruction: " + line.getOpCode());
    }
  }

  private void handleInstruction(SourceLine instruction) {
    boolean extFlag = getInstructionExtFlag(instruction);
    int size =
        switch (opTable.get(instruction.getOpCode()).format()) {
          case ONE -> 1;
          case TWO -> 2;
          case THREE_FOUR -> extFlag ? 4 : 3;
        };
    int location = getAndAdd(size);
    tryAddSymbol(
        instruction.getLabel(),
        new SymbolData(location, getActiveBlock().getId(), SymbolData.Type.RELATIVE));
    appendOutputData(new PassOneData(instruction, size, getActiveBlockId(), location, true));
  }

  private boolean getInstructionExtFlag(SourceLine instruction) {
    var op = opTable.get(instruction.getOpCode());
    boolean extFlag = false;
    if (!instruction.getOpCodePrefix().isEmpty()) {
      if (instruction.getOpCodePrefix().equals("+")) {
        if (!op.format().equals(Operation.Format.THREE_FOUR)) {
          throw new AssemblerException(
              "Extended format flag not allowed on format "
                  + (op.format().ordinal() + 1)
                  + " instruction");
        } else {
          extFlag = true;
        }
      } else {
        throw new AssemblerException("Invalid opcode flag: " + instruction.getOpCodePrefix());
      }
    }
    return extFlag;
  }

  private void handleRESW(SourceLine directive) {
    if (directive.getArgOne().isEmpty()) {
      throw new AssemblerException("Size argument missing for RESW directive");
    }
    int count = Integer.parseInt(directive.getArgOne());
    if (count < 0) {
      throw new AssemblerException("Size argument for RESW directive cannot be negative");
    }
    int size = count * WORD_SIZE;
    int location = getAndAdd(size);
    tryAddSymbol(
        directive.getLabel(),
        new SymbolData(location, getActiveBlock().getId(), SymbolData.Type.RELATIVE));
    appendOutputData(new PassOneData(directive, size, getActiveBlock().getId(), location, false));
  }

  private void handleRESB(SourceLine directive) {
    if (directive.getArgOne().isEmpty()) {
      throw new AssemblerException("Size argument missing for RESB directive");
    }
    int size = Integer.parseInt(directive.getArgOne()) * BYTE_SIZE;
    if (size < 0) {
      throw new AssemblerException("Size argument for RESB directive cannot be negative");
    }
    int location = getAndAdd(size);
    tryAddSymbol(
        directive.getLabel(),
        new SymbolData(location, getActiveBlock().getId(), SymbolData.Type.RELATIVE));
    appendOutputData(new PassOneData(directive, size, getActiveBlockId(), location, false));
  }

  private void handleWORD(SourceLine directive) {
    int location = getAndAdd(WORD_SIZE);
    tryAddSymbol(
        directive.getLabel(),
        new SymbolData(location, getActiveBlock().getId(), SymbolData.Type.RELATIVE));
    appendOutputData(new PassOneData(directive, WORD_SIZE, getActiveBlockId(), location, false));
  }

  private void handleBYTE(SourceLine directive) {
    var size = ConstantParser.parseByteConstant(directive.getArgOne()).length * BYTE_SIZE;
    int location = getAndAdd(size);
    tryAddSymbol(
        directive.getLabel(),
        new SymbolData(location, getActiveBlock().getId(), SymbolData.Type.RELATIVE));
    appendOutputData(new PassOneData(directive, size, getActiveBlockId(), location, false));
  }

  private void handleUSE(SourceLine directive) {
    setActiveBlockName(directive.getArgOne());
    appendOutputData(
        new PassOneData(directive, 0, getActiveBlockId(), getActiveBlock().get(), false));
  }

  private void handleEQU(SourceLine directive) {
    if (directive.getArgOne().isBlank()) {
      throw new AssemblerException("Missing value for EQU directive.");
    }
    SymbolData data;
    if (directive.getArgOne().equals("*")) {
      data =
          new SymbolData(
              getActiveBlock().get(), getActiveBlock().getId(), SymbolData.Type.RELATIVE);
    } else {
      ExpressionParser parser = new ExpressionParser(symbolTable);
      data = parser.parse(directive.getArgOne());
    }
    tryAddSymbol(directive.getLabel(), data);
    appendOutputData(new PassOneData(directive, 0, data.programBlockId(), data.value(), false));
  }

  private void handleLTORG(SourceLine directive) {
    System.out.println("LTORG not implemented yet");
  }

  /**
   * Adds count to the counter for the active block, and returns the previously stored value.
   *
   * @param count Count to add to counter for the active block
   * @return Counter value before adding count
   */
  private int getAndAdd(int count) {
    return getActiveBlock().getAndAdd(count);
  }

  /**
   * Adds the data from the output. If the data has an associated symbol, adds the label to the
   * symbol table via {@link #tryAddSymbol(String, int, int)}
   */
  private void appendOutputData(PassOneData data) {
    output.add(data);
  }

  /**
   * Attempts to add label to symbol table. If label is empty, nothing is added. If label already
   * exists in the symbol table, an error is thrown.
   *
   * @param label The label to add
   * @param symbolData The symbol data
   */
  private void tryAddSymbol(String label, SymbolData symbolData) {
    if (!label.isEmpty()) {
      if (symbolTable.containsKey(label)) {
        throw new AssemblerException("Multiple definitions of symbol: " + label);
      }
      symbolTable.put(label, symbolData);
    }
  }

  /**
   * Sets currentLine to the next line, and returns it.
   *
   * @return the next line
   */
  private SourceLine getNextLine() {
    currentLineIndex++;
    if (lines.size() <= currentLineIndex) {
      currentLine = null;
    } else {
      currentLine = lines.get(currentLineIndex);
    }
    return currentLine;
  }

  public ProgramBlockTable getProgramBlocks() {
    return programBlocks;
  }

  public Map<String, SymbolData> getSymbolTable() {
    return symbolTable;
  }

  public List<PassOneData> getOutput() {
    return output;
  }
}
