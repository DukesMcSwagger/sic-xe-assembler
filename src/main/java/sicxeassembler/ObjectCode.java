package sicxeassembler;

public record ObjectCode(int address, byte[] data, int programBlockId, boolean executable) {}
