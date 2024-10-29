package org.step.engine.common;

public interface Mapper<K,V> {
  V map(K k);
}