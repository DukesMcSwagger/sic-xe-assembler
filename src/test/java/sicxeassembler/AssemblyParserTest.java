package sicxeassembler;

import static org.junit.jupiter.api.Assertions.*;
import static sicxeassembler.Operation.op;

import java.text.ParseException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssemblyParserTest {
  AssemblyParser parser;

  @BeforeEach
  void createParser() {
    OpTable opTable =
        new OpTable(
            Set.of(
                op("ADD", 0x18, Operation.Format.THREE_FOUR),
                op("SUB", 0x1C, Operation.Format.THREE_FOUR),
                op("CLEAR", 0xB4, Operation.Format.TWO)));
    Set<String> directives = Set.of("USE");
    parser = new AssemblyParser(opTable, directives);
  }

  @Test
  void testEmptyString() throws ParseException {
    assertEquals(new SourceLine.Empty(), parser.parse(""));
    assertEquals(new SourceLine.Empty(), parser.parse("      \t   "));
  }

  @Test
  void testCommentOnly() throws ParseException {
    assertEquals(new SourceLine.Comment("comment"), parser.parse("   .   comment"));
    assertEquals(new SourceLine.Comment("comment"), parser.parse(". comment"));
    assertEquals(new SourceLine.Comment(""), parser.parse(" ."));
    assertEquals(new SourceLine.Comment(""), parser.parse(". "));
    assertEquals(new SourceLine.Comment(""), parser.parse(" . "));
    assertParseFailure(".comment");
    assertParseFailure("LABEL ADD .comment");
  }

  @Test
  void testInstruction() throws ParseException {
    assertEquals(
        new SourceLine.Instruction("LABEL", false, "ADD", "", "OP1", "", ""),
        parser.parse("LABEL ADD OP1"));
    assertEquals(
        new SourceLine.Instruction("", false, "CLEAR", "", "A", "", "comment"),
        parser.parse(" CLEAR A . comment"));
    assertEquals(
        new SourceLine.Instruction("LABEL", true, "ADD", "", "OTHER", "", "comment"),
        parser.parse("LABEL +ADD OTHER .     comment"));
    assertEquals(
        new SourceLine.Instruction("LABEL", true, "ADD", "", "OTHER", "THING", "comment"),
        parser.parse("LABEL +ADD OTHER,THING . comment"));

    assertParseFailure("garbage garbage");
    assertParseFailure("LABEL GARBO");
    assertParseFailure("LABEL +CLEAR A");
  }

  @Test
  void testDirective() throws ParseException {
    assertEquals(new SourceLine.Directive("", "USE", "CDATA", ""), parser.parse(" USE CDATA . "));
    assertEquals(
        new SourceLine.Directive("LABEL", "USE", "ARG1", ""), parser.parse("LABEL USE ARG1"));

    assertParseFailure(" USE ARG1,ARG2");
    assertParseFailure(" +USE");
    assertParseFailure(" USE @ARG");
  }

  @Test
  void testBadParses() throws ParseException {
    assertParseFailure(" ,");
    assertParseFailure(" NOTANOP");
    assertParseFailure("LABEL ADD ,ARG2");
  }

  private void assertParseFailure(String source) {
    assertThrows(
        ParseException.class,
        () -> {
          parser.parse(source);
        },
        () -> "'" + source + "' parsed without error");
  }
}
