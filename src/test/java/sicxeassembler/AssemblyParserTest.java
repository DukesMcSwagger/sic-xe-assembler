package sicxeassembler;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AssemblyParserTest {

  @Test
  void testEmptyString() throws ParseException {
    assertEquals(new SourceLine(), SourceLine.parseLine(""));
    assertEquals(new SourceLine(), SourceLine.parseLine("      \t   "));
  }

  @Test
  void testCommentOnly() throws ParseException {
    assertEquals(new SourceLine().setComment("comment"), SourceLine.parseLine("   .   comment"));
    assertEquals(new SourceLine().setComment("comment"), SourceLine.parseLine(". comment"));
    assertEquals(new SourceLine().setComment("comment"), SourceLine.parseLine(".comment"));
    assertEquals(new SourceLine().setComment(""), SourceLine.parseLine(" ."));
    assertEquals(new SourceLine().setComment(""), SourceLine.parseLine(". "));
  }

  @Test
  void testInstruction() throws ParseException {
    assertEquals(
        new SourceLine().setLabel("LABEL").setOpCode("ADD").setArgOne("OP1"),
        SourceLine.parseLine("LABEL ADD OP1"));
    assertEquals(
        new SourceLine().setOpCode("CLEAR").setArgOne("A").setComment("comment"),
        SourceLine.parseLine(" CLEAR A . comment"));
    assertEquals(
        new SourceLine()
            .setLabel("LABEL")
            .setOpCodePrefix("+")
            .setOpCode("ADD")
            .setArgOne("OTHER")
            .setComment("comment"),
        SourceLine.parseLine("LABEL +ADD OTHER .     comment"));
    assertEquals(
        new SourceLine()
            .setLabel("LABEL")
            .setOpCodePrefix("+")
            .setOpCode("ADD")
            .setArgOne("OTHER")
            .setArgTwo("THING")
            .setComment("comment"),
        SourceLine.parseLine("LABEL +ADD OTHER,THING . comment"));
    assertEquals(
        new SourceLine().setOpCode("ADD").setArgOnePrefix("@").setArgOne("THING"),
        SourceLine.parseLine(" ADD @THING"));
    assertEquals(
        new SourceLine()
            .setOpCode("ADD")
            .setArgOnePrefix("@")
            .setArgOne("THING")
            .setArgTwo("OTHER"),
        SourceLine.parseLine(" ADD @THING,OTHER"));
  }

  @Test
  void testBadParses() throws ParseException {
    assertParseFailure("LABEL ADD ,ARG2");
  }

  private void assertParseFailure(String source) {
    AtomicReference<SourceLine> parsedAs = new AtomicReference<>();
    assertThrows(
        ParseException.class,
        () -> {
          parsedAs.set(SourceLine.parseLine(source));
        },
        () -> "'" + source + "' parsed without error.\nParsed as: " + parsedAs.get().toString());
  }
}
