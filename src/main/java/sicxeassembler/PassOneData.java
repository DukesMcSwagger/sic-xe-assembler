package sicxeassembler;

/**
 * Per-line data from first pass
 *
 * @param line the corresponding line
 * @param size the size in bytes of the resulting object code
 * @param block the block ID
 * @param addressInBlock the address relative to the start of the block
 */
public record PassOneData(
    SourceLine line, int size, int block, int addressInBlock, boolean isExecutableInstruction) {}
