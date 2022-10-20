package memories.spi;

public interface MappedMemory extends Memory {

  MappedMemory sync();

  @Override
  boolean release();
}
