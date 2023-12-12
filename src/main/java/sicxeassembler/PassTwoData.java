package sicxeassembler;

public record PassTwoData(
    SourceLine line,
    int size,
    int block,
    int addressInBlock,
    boolean isExecutableInstruction,
    int[] objectCode) {
  public PassTwoData(PassOneData passOne, int[] objectCode) {
    this(
        passOne.line(),
        passOne.size(),
        passOne.block(),
        passOne.addressInBlock(),
        passOne.isExecutableInstruction(),
        objectCode);
  }
}
