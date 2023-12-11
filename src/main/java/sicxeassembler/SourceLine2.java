package sicxeassembler;

import java.text.ParseException;
import java.util.Objects;

public class SourceLine2 {
  private String originalSource;
  private String label = "";
  private String opCode = "";
  private String argOne = "";
  private String argTwo = "";
  private String comment = "";

  public SourceLine2() throws ParseException {}

  public static SourceLine2 parseLine(String source) throws ParseException {
    SourceLine2 line = new SourceLine2();
    // First, lets get rid of any blank lines
    if (source.isBlank()) {
      return line;
    }

    // split around the first occurrence of "." surrounded by any non-digit characters
    var codeAndComment = source.split("(\\h+|^)\\.\\h*", 2);
    var code = codeAndComment[0];
    if (codeAndComment.length > 1) {
      line.setComment(codeAndComment[1]);
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
    }
    if (fields.length > 2) {
      var args = fields[2];

      // Now parse the args
      String argOne = "";
      String argTwo = "";
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

  public SourceLine2 setLabel(String label) {
    this.label = label;
    return this;
  }

  public String getOpCode() {
    return opCode;
  }

  public SourceLine2 setOpCode(String opCode) {
    this.opCode = opCode;
    return this;
  }

  public String getArgOne() {
    return argOne;
  }

  public SourceLine2 setArgOne(String argOne) {
    this.argOne = argOne;
    return this;
  }

  public String getArgTwo() {
    return argTwo;
  }

  public SourceLine2 setArgTwo(String argTwo) {
    this.argTwo = argTwo;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public SourceLine2 setComment(String comment) {
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
    SourceLine2 that = (SourceLine2) o;
    return Objects.equals(label, that.label) && Objects.equals(opCode, that.opCode) && Objects.equals(argOne, that.argOne) && Objects.equals(argTwo, that.argTwo) && Objects.equals(comment, that.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, opCode, argOne, argTwo, comment);
  }

  @Override
  public String toString() {
    return "SourceLine2{" +
        "label='" + label + '\'' +
        ", opCode='" + opCode + '\'' +
        ", argOne='" + argOne + '\'' +
        ", argTwo='" + argTwo + '\'' +
        ", comment='" + comment + '\'' +
        '}';
  }
}