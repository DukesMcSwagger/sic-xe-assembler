package sicxeassembler;

import static org.junit.jupiter.api.Assertions.*;
import static sicxeassembler.Operation.op;

import java.util.List;
import org.junit.jupiter.api.Test;

class OpTableTest {
  OpTable makeTestTable() {
    return new OpTable(
        List.of(
            op("OP1", 1, Operation.Format.ONE),
            op("OP2", 2, Operation.Format.TWO),
            op("OP3", 3, Operation.Format.THREE_FOUR)));
  }

  @Test
  void testExists() {
    var table = makeTestTable();
    assertTrue(table.contains("OP1"));
    assertFalse(table.contains("NOPE"));
  }

  @Test
  void testGet() {
    var table = makeTestTable();
    var result = table.get("OP2");
    assertTrue(result.isPresent());
    assertEquals(op("OP2", 2, Operation.Format.TWO), result.get());
  }
}
