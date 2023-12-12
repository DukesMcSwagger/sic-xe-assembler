package sicxeassembler;

import java.text.ParseException;
import java.util.Objects;
import java.util.Set;

public class SourceLine {
  private String originalSource;
  private String label = "";
  private String opCodePrefix = "";
  private String opCode = "";
  private String argOnePrefix = "";
  private String argOne = "";
  private String argTwo = "";
  private String comment = "";

  private static final Set<Character> OPCODE_PREFIXES = Set.of('+');
  private static final Set<Character> ARG_PREFIXES = Set.of('=', '#', '@');

  public SourceLine() throws ParseException {}

  public static SourceLine parseLine(String source) throws ParseException {
    SourceLine line = new SourceLine();
    line.setOriginalSource(source);
    // First, lets get rid of any blank lines
    if (source.isBlank()) {
      return line;
    }

    // split around the first occurrence of ".", trim whitespace
    var codeAndComment = source.split("\\.", 2);
    var code = codeAndComment[0];
    if (codeAndComment.length > 1) {
      line.setComment(codeAndComment[1].trim());
    }
    // Handle comment-only lines
    if (code.isBlank()) {
      return line;
    }

    // code[0]: label
    // code[1]: mnemonic or directive
    // code[2]: args

    // Split code around whitespace
    var fields = code.split("[\t ]+");

    // this check should be redundant since we know the line is not blank
    if (fields.length > 0) {
      line.setLabel(fields[0]);
    }
    if (fields.length > 1) {
      line.setOpCode(fields[1]);
      if (!line.getOpCode().isBlank() && OPCODE_PREFIXES.contains(line.getOpCode().charAt(0))) {
        line.setOpCodePrefix(Character.toString(line.getOpCode().charAt(0)));
        line.setOpCode(line.getOpCode().substring(1));
      }
    }
    if (fields.length > 2) {
      var args = fields[2];

      // Now parse the args
      if (!args.isEmpty()) {
        var splitArgs = args.split(",");
        line.setArgOne(splitArgs[0]);
        if (splitArgs.length > 1) {
          // Make sure argOne actually has something (in the case of args = ",argTwo")
          if (line.getArgOne().isEmpty()) {
            throw new ParseException("Missing argument before ','", source.indexOf(args));
          }
          line.setArgTwo(splitArgs[1]);
        }
        if (!line.getArgOne().isBlank() && ARG_PREFIXES.contains(line.getArgOne().charAt(0))) {
          line.setArgOnePrefix(Character.toString(line.getArgOne().charAt(0)));
          line.setArgOne(line.getArgOne().substring(1));
        }
      }
    }
    return line;
  }

  private void setOriginalSource(String originalSource) {
    this.originalSource = originalSource;
  }

  public String getOriginalSource() {
    return originalSource;
  }

  public String getLabel() {
    return label;
  }

  public SourceLine setLabel(String label) {
    this.label = label;
    return this;
  }

  public String getOpCodePrefix() {
    return opCodePrefix;
  }

  public SourceLine setOpCodePrefix(String opCodePrefix) {
    this.opCodePrefix = opCodePrefix;
    return this;
  }

  public String getOpCode() {
    return opCode;
  }

  public SourceLine setOpCode(String opCode) {
    this.opCode = opCode;
    return this;
  }

  public String getArgOnePrefix() {
    return argOnePrefix;
  }

  public SourceLine setArgOnePrefix(String argOnePrefix) {
    this.argOnePrefix = argOnePrefix;
    return this;
  }

  public String getArgOne() {
    return argOne;
  }

  public SourceLine setArgOne(String argOne) {
    this.argOne = argOne;
    return this;
  }

  public String getArgTwo() {
    return argTwo;
  }

  public SourceLine setArgTwo(String argTwo) {
    this.argTwo = argTwo;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public SourceLine setComment(String comment) {
    this.comment = comment;
    return this;
  }

  public boolean isCommentOrBlank() {
    return label.isBlank() && opCode.isBlank() && argOne.isBlank() && argTwo.isBlank();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SourceLine that = (SourceLine) o;
    return Objects.equals(label, that.label)
        && Objects.equals(opCode, that.opCode)
        && Objects.equals(argOne, that.argOne)
        && Objects.equals(argTwo, that.argTwo)
        && Objects.equals(comment, that.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, opCode, argOne, argTwo, comment);
  }

  @Override
  public String toString() {
    return "SourceLine2{"
        + "label='"
        + label
        + '\''
        + ", opCodePrefix='"
        + opCodePrefix
        + '\''
        + ", opCode='"
        + opCode
        + '\''
        + ", argOnePrefix='"
        + argOnePrefix
        + '\''
        + ", argOne='"
        + argOne
        + '\''
        + ", argTwo='"
        + argTwo
        + '\''
        + ", comment='"
        + comment
        + '\''
        + '}';
  }
}
