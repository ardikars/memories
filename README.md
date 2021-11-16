<!--
SPDX-FileCopyrightText: 2020 Memories
SPDX-License-Identifier: Apache-2.0
-->

# Memories Project

*) NOT READY FOR PRODUCTION YET

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=bugs)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=ardikars_memories&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=ardikars_memories)


## About this project

Off-heap (Native) memory access for Java. Compatible with the old version of Java and not using `sun.misc.Unsafe`.


### Consideration

* netty-buffer API.

* foreign-memory-access by Project Panama.


### Usage

```
Note: Musl is required on Linux (malloc with musl is slow); consider to use mimaloc/jemalloc/others.
```


* Allocate a block of memory

```java
final MemoryAllocator allocator = new MemoryAllocatorApi(); // instantiate allocator.
final Memory memory = allocator.allocate(4); // Allocate 4 bytes block of memory.
```

* Accessing memory

```java
assert 0 == memory.writerIndex(); // initial writer index.
assert 0 == memory.readerIndex(); // initial reader index.
memory.writeInt(10); // write integer value to first 4 bytes will increase writer index.
assert 4 == memory.writerIndex(); // writer index after write 4 bytes data.
assert 0 == memory.readerIndex(); // writing data will not change reader index.
assert 10 == memory.readInt(); // read 4 bytes data based on current reader index.
assert 4 == memory.writerIndex(); // reading data will not change writer index.
assert 4 == memory.readerIndex(); // reader index after read 4 bytes data.
```

* Convert to direct ByteBuffer

```java
ByteBuffer buffer = (ByteBuffer) memory.as(ByteBuffer.class); // convert Memory to direct ByteBuffer without copying the buffer.
assert buffer.isDirect(); // should be direct buffer.
assert !buffer.isReadOnly(); // should be able to read or write.
```

* Releasing memory

```java
assert memory.release(); // Release 'memory' immediately without waiting 'memory' object GC'ed.
```

* Wrap direct ByteBuffer into Memory

```java
final ByteBuffer buffer = ByteBuffer.allocateDirect(4); // allocate direct ByteBuffer.
buffer.putInt(0, 10); // set integer value to first 4 bytes.
final Memory memory = allocator.wrap(buffer); // wrap direct ByteBuffer into Memory.
assert memory.capacity() == buffer.capacity(); // 'buffer' and 'memory' capacity should be same in size.
assert 10 == memory.getInt(0); // shared buffer between 'buffer' and 'memory'.
assert memory.release(); // release the buffer immediately without waiting both 'buffer' and 'memory' GC'ed.
```

### License

The Memories Project is licensed under Apache-2.0.

```
SPDX-License-Identifier: Apache-2.0
```
