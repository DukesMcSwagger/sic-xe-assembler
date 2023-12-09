package sicxeassembler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassOne {
  private List<SourceLine> lines;
  private final ProgramBlockTable programBlocks = new ProgramBlockTable();
  private final Map<String, SymbolData> symbolTable = new HashMap<>();

  /** The active program block. Initialized to default. */
  private String activeBlock = "";

  private int startAddress = -1;
  private int currentLineIndex = -1;
  private SourceLine currentLine;

  private final int WORD_SIZE = 3;
  private final int BYTE_SIZE = 1;

  public PassOne(List<SourceLine> lines) {
    this.lines = lines;
  }

  public String getActiveBlock() {
    return activeBlock;
  }

  private void setActiveBlock(String activeBlock) {
    this.activeBlock = activeBlock;
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
      if (currentLine instanceof SourceLine.Instruction instruction) {
        handleInstruction(instruction);
      } else if (currentLine instanceof SourceLine.Directive directive) {
        handleDirective(directive);
      }
      getNextLine();
    }
  }

  private boolean finished() {
    if (currentLine == null) {
      return true;
    }
    if (currentLine instanceof SourceLine.Directive directive
        && directive.directive().equals("END")) {
      return true;
    }
    return false;
  }

  private void initializeStartAddress() {
    if (currentLine instanceof SourceLine.Directive directive
        && directive.directive().equals("START")) {
      setStartAddress(Integer.parseInt(directive.argOne(), 16));
      getNextLine();
    } else {
      setStartAddress(0);
    }
  }

  private void handleDirective(SourceLine.Directive line) {
    switch (line.directive()) {
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
      default:
        throw new AssemblerException("Unknown directive: " + line.directive());
    }
  }

  private void handleInstruction(SourceLine.Instruction instruction) {
    int length =
        switch (instruction.op().format()) {
          case ONE -> 1;
          case TWO -> 2;
          case THREE_FOUR -> instruction.extFlag() ? 4 : 3;
        };
    int location = programBlocks.getAndAdd(getActiveBlock(), length);
    tryAddSymbol(instruction.label(), location);
  }

  private void handleUSE(SourceLine.Directive directive) {
    setActiveBlock(directive.argOne());
  }

  private void handleRESW(SourceLine.Directive directive) {
    if (directive.argOne().isEmpty()) {
      throw new AssemblerException("Size argument missing for RESW directive");
    }
    int count = Integer.parseInt(directive.argOne());
    if (count < 0) {
      throw new AssemblerException("Size argument for RESW directive cannot be negative");
    }
    int location = programBlocks.getAndAdd(getActiveBlock(), count * WORD_SIZE);
    tryAddSymbol(directive.label(), location);
  }

  private void handleRESB(SourceLine.Directive directive) {
    if (directive.argOne().isEmpty()) {
      throw new AssemblerException("Size argument missing for RESB directive");
    }
    int count = Integer.parseInt(directive.argOne());
    if (count < 0) {
      throw new AssemblerException("Size argument for RESB directive cannot be negative");
    }
    int location = programBlocks.getAndAdd(getActiveBlock(), count * BYTE_SIZE);
    tryAddSymbol(directive.label(), location);
  }

  private void handleWORD(SourceLine.Directive directive) {
    int location = programBlocks.getAndAdd(getActiveBlock(), WORD_SIZE);
    tryAddSymbol(directive.label(), location);
  }

  private void handleBYTE(SourceLine.Directive directive) {
    var length = ConstantParser.parseByteConstant(directive.argOne()).length;
    int location = programBlocks.getAndAdd(getActiveBlock(), length * BYTE_SIZE);
    tryAddSymbol(directive.label(), location);
  }

  /**
   * Attempts to add label to symbol table. If label is empty, nothing is added. If label already
   * exists in the symbol table, an error is thrown.
   *
   * @param label The label to add
   * @param location The location of the symbol in the current program block
   */
  private void tryAddSymbol(String label, int location) {
    if (!label.isEmpty()) {
      if (symbolTable.containsKey(label)) {
        throw new AssemblerException("Multiple definitions of symbol: " + label);
      }
      symbolTable.put(label, new SymbolData(location, getActiveBlock()));
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
}
