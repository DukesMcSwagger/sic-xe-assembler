package sicxeassembler;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
