package sicxeassembler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProgramBlockTableTest {
  final String kTestBlock = "test";

  @Test
  void testGetNonexistantBlock() {
    ProgramBlockTable table = new ProgramBlockTable();
    int count = table.get(kTestBlock);
    assertEquals(0, count);
  }

  @Test
  void testAddAndGet() {
    ProgramBlockTable table = new ProgramBlockTable();
    assertEquals(0, table.get(kTestBlock));
    assertEquals(0, table.getAndAdd(kTestBlock, 3));
    assertEquals(3, table.getAndAdd(kTestBlock, 2));
    assertEquals(5, table.get(kTestBlock));
  }

  @Test
  void testGetBlockId() {
    ProgramBlockTable table = new ProgramBlockTable();
    table.getAndAdd("ZERO", 0);
    int idOne = table.getBlockId("ONE");
    int idTwo = table.getBlockId("TWO");
    int idZero = table.getBlockId("ZERO");
    assertEquals(idZero, 0);
    assertEquals(idOne, 1);
    assertEquals(idTwo, 2);
  }

  @Test
  void testMakeAbsolutePositions() {
    ProgramBlockTable table = new ProgramBlockTable();
    table.getAndAdd("ZERO", 10);
    table.getAndAdd("ONE", 20);
    table.getAndAdd("TWO", 30);
    table.getAndAdd("EMPTY", 0);
    table.getAndAdd("_", 0);
    var out = table.makeAbsolutePositions();
    assertEquals(out.get(0), 0);
    assertEquals(out.get(1), 10);
    assertEquals(out.get(2), 30);
    assertEquals(out.get(3), 60);
    assertEquals(out.get(4), 60);
    assertEquals(out.size(), 5);
  }
}
