package dk.ngr.step.engine.common;

public interface Mapper<K,V> {
  V map(K k);
}