package sicxeassembler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProgramBlockTableTest {
  final String kTestBlock = "test";

  @Test
  void testGetNonexistantBlock() {
    ProgramBlockTable table = new ProgramBlockTable();
    int count = table.getBlock(kTestBlock).get();
    assertEquals(0, count);
  }

  @Test
  void testAddAndGet() {
    ProgramBlockTable table = new ProgramBlockTable();
    assertEquals(0, table.getBlock(kTestBlock).get());
    assertEquals(0, table.getBlock(kTestBlock).getAndAdd(3));
    assertEquals(3, table.getBlock(kTestBlock).getAndAdd(2));
    assertEquals(5, table.getBlock(kTestBlock).get());
  }

  @Test
  void testGetBlockId() {
    ProgramBlockTable table = new ProgramBlockTable();
    table.getBlock("ZERO").getAndAdd(0);
    int idOne = table.getBlock("ONE").getId();
    int idTwo = table.getBlock("TWO").getId();
    int idZero = table.getBlock("ZERO").getId();
    assertEquals(0, idZero);
    assertEquals(1, idOne);
    assertEquals(2, idTwo);
  }

  @Test
  void testMakeAbsolutePositions() {
    ProgramBlockTable table = new ProgramBlockTable();
    table.getBlock("ZERO").getAndAdd(10);
    table.getBlock("ONE").getAndAdd(20);
    table.getBlock("TWO").getAndAdd(30);
    table.getBlock("EMPTY").getAndAdd(0);
    table.getBlock("_").getAndAdd(0);
    var out = table.makeAbsolutePositions();
    assertEquals(0, out.get(0));
    assertEquals(10, out.get(1));
    assertEquals(30, out.get(2));
    assertEquals(60, out.get(3));
    assertEquals(60, out.get(4));
    assertEquals(5, out.size());
  }
}
