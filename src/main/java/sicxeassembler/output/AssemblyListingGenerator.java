package sicxeassembler.output;

import java.io.*;
import sicxeassembler.PassTwoData;
import sicxeassembler.SourceLine;

public class AssemblyListingGenerator implements OutputGenerator {
  PrintWriter writer;

  public AssemblyListingGenerator(OutputStream outputStream) {
    writer = new PrintWriter(outputStream);
  }

  public AssemblyListingGenerator(File output) throws FileNotFoundException {
    writer = new PrintWriter(output);
  }

  @Override
  public void initialize(String name, int start, int length) {
    output(
        0,
        0,
        new SourceLine().setLabel(name).setOpCode("START").setArgOne(String.valueOf(start)),
        null);
  }

  @Override
  public void accept(PassTwoData output) {
    output(output.address(), output.block(), output.line(), output.objectCode());
  }

  @Override
  public void addModificationRecord(int address, int size) {
    // pass
  }

  @Override
  public void finalizeOutput() {
    writer.flush();
  }

  private void output(int address, int block, SourceLine statement, int[] objectCode) {
    writer.println(formatColumns(address, block, statement, objectCode));
  }

  public static String formatColumns(int address, int block, SourceLine source, int[] objectCode) {
    if (source.isCommentOrBlank()) {
      return formatCommentOnlyLine(source.getComment());
    }
    String blockString = block < 0 ? " " : String.valueOf(block);
    return formatColumns(
        String.format("%04X", address),
        blockString,
        source.getLabel(),
        source.getOpCodePrefix(),
        source.getOpCode(),
        source.getArgOnePrefix(),
        formatArgs(source.getArgOne(), source.getArgTwo()),
        OutputUtils.bytesToHexString(objectCode),
        source.getComment());
  }

  private static String formatCommentOnlyLine(String comment) {
    return formatColumns("", "", ".", "", comment, "", "", "", "");
  }

  public static String formatColumns(
      String address,
      String block,
      String label,
      String opcodePrefix,
      String opcode,
      String argOnePrefix,
      String args,
      String objectCode,
      String comment) {
    if (!comment.isEmpty()) {
      comment = ". " + comment;
    }
    /*
     * Location: 0000
     * two spaces
     * block or " ": 5
     * Statement string (already formatted)
     * object code
     * . comment
     */
    return String.format(
        "%-4s  %-5s%s%-8s%s",
        address,
        block,
        formatSourceStatement(label, opcodePrefix, opcode, argOnePrefix, args),
        objectCode,
        comment);
  }

  public static String formatArgs(String argOne, String argTwo) {
    if (!argTwo.isEmpty()) {
      return argOne + "," + argTwo;
    } else {
      return argOne;
    }
  }

  public static String formatSourceStatement(
      String label, String opCodePrefix, String opcode, String argOnePrefix, String args) {
    /*
     * Label: 15
     * opcode prefix: 1
     * opcode: 7
     * arg prefix: 1
     * args: 20
     */
    return String.format("%-15s%-1s%-7s%-1s%-20s", label, opCodePrefix, opcode, argOnePrefix, args);
  }
}
