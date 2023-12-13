package sicxeassembler;

public record PassTwoData(
    SourceLine line,
    int size,
    int block,
    int address,
    boolean isExecutableInstruction,
    int[] objectCode) {
  public PassTwoData(PassOneData passOne, int address, int[] objectCode) {
    this(
        passOne.line(),
        passOne.size(),
        passOne.block(),
        address,
        passOne.isExecutableInstruction(),
        objectCode);
  }
}
