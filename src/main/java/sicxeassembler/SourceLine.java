package sicxeassembler;

public sealed interface SourceLine {
  /** Represents an empty line */
  record Empty() implements SourceLine {}

  /**
   * Represents a line with only a comment.
   *
   * @param comment the comment. May be empty.
   */
  record Comment(String comment) implements SourceLine {}

  /**
   * Represents a line that invokes an assembler directive.
   *
   * @param label the label. may be empty.
   * @param directive the assembler directive
   * @param argOne the argument, if applicable. may be empty.
   * @param comment the comment, if it exists. may be empty.
   */
  record Directive(String label, String directive, String argOne, String comment)
      implements SourceLine {}

  /**
   * Represents a line that will generate a machine instruction
   *
   * @param label The label for the instruction. may be empty.
   * @param extFlag Whether the extended format flag was set.
   * @param op the instruction operation
   * @param argFlag The operand flag, if set. may be empty.
   * @param argOne The first operand. May be empty.
   * @param argTwo The second operand. May be empty.
   * @param comment the comment, if it exists. May be empty.
   */
  record Instruction(
      String label,
      boolean extFlag,
      Operation op,
      String argFlag,
      String argOne,
      String argTwo,
      String comment)
      implements SourceLine {}
}
