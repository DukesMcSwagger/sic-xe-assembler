package sicxeassembler;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssemblyParserTest {


  @Test
  void testEmptyString() throws ParseException {
    assertEquals(new SourceLine2(), SourceLine2.parseLine(""));
    assertEquals(new SourceLine2(), SourceLine2.parseLine("      \t   "));
  }

  @Test
  void testCommentOnly() throws ParseException {
    assertEquals(new SourceLine2().setComment("comment"), SourceLine2.parseLine("   .   comment"));
    assertEquals(new SourceLine2().setComment("comment"), SourceLine2.parseLine(". comment"));
    assertEquals(new SourceLine2().setComment("comment"), SourceLine2.parseLine(".comment"));
    assertEquals(new SourceLine2().setComment(""), SourceLine2.parseLine(" ."));
    assertEquals(new SourceLine2().setComment(""), SourceLine2.parseLine(". "));
  }

  @Test
  void testInstruction() throws ParseException {
    assertEquals(
        new SourceLine2().setLabel("LABEL").setOpCode("ADD").setArgOne("OP1"),
        SourceLine2.parseLine("LABEL ADD OP1"));
    assertEquals(new SourceLine2().setOpCode("CLEAR").setArgOne("A").setComment("comment"),
        SourceLine2.parseLine(" CLEAR A . comment"));
    assertEquals(
        new SourceLine2().setLabel("LABEL").setOpCode("+ADD").setArgOne("OTHER").setComment("comment"),
        SourceLine2.parseLine("LABEL +ADD OTHER .     comment"));
    assertEquals(
        new SourceLine2().setLabel("LABEL").setOpCode("+ADD").setArgOne("OTHER").setArgTwo("THING").setComment("comment"),
        SourceLine2.parseLine("LABEL +ADD OTHER,THING . comment"));
  }

  @Test
  void testBadParses() throws ParseException {
    assertParseFailure("LABEL ADD ,ARG2");
  }

  private void assertParseFailure(String source) {
    AtomicReference<SourceLine2> parsedAs = new AtomicReference<>();
    assertThrows(
        ParseException.class,
        () -> {
          parsedAs.set(SourceLine2.parseLine(source));
        },
        () -> "'" + source + "' parsed without error.\nParsed as: " + parsedAs.get().toString());
  }
}
