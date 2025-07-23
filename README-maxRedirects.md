# What is maxRedirects?

> **maxRedirects is NOT equal to the number of hosts in clusters**

## What is maxRedirects?

Looking at the code and configuration:

```java
/**
 * Maximum number of redirections to follow when executing commands across the cluster
 */
@Min(1)
private int maxRedirects = 3;
```

```yaml
# application-redis-cloud.yaml
cluster:
  nodes: redis-cluster-1:6379,redis-cluster-2:6379,redis-cluster-3:6379  # 3 nodes
  max-redirects: 3  # NOT related to node count!
```

## How Redis Cluster Redirection Works

### 1. Redis Cluster Data Distribution

Redis Cluster distributes data across multiple nodes using hash slots:

- There are 16,384 hash slots total
- Each key is hashed to determine which slot it belongs to
- Each cluster node is responsible for a range of slots

### 2. Redirection Process

When your application sends a command:

- Step 1: Client connects to any cluster node

```text
Client → Node A: GET mykey
```

- Step 2: Node A checks if it owns the slot for mykey
    - ✅ If YES: Node A processes the request
    - ❌ If NO: Node A sends a MOVED redirect response

- Step 3: Client follows the redirect

```text
Node A → Client: MOVED 12345 redis-cluster-2:6379
Client → Node B: GET mykey
```

- Step 4: Process repeats if needed (rare, but possible during cluster reconfiguration)

### 3. Why maxRedirects = 3?

This means the client will follow at most 3 redirections before giving up:

```shell
# Example scenario (worst case):
Client → Node A: GET mykey
Node A → Client: MOVED 12345 Node-B:6379    # Redirect 1

Client → Node B: GET mykey  
Node B → Client: MOVED 12345 Node-C:6379    # Redirect 2 (during resharding)

Client → Node C: GET mykey
Node C → Client: MOVED 12345 Node-A:6379    # Redirect 3 (cluster reconfiguration)

Client → Node A: GET mykey
Node A: Returns the value ✅               # Success!

# If there was a 4th redirect needed, the client would give up and throw an error
```

## Cluster Size vs maxRedirects

| Cluster Configuration	 | Recommended maxRedirects | Reasoning                                                   |
|------------------------|--------------------------|-------------------------------------------------------------|
| **3 nodes**            | 3                        | Usually 1 redirect needed, 3 allows for cluster maintenance |
| **6 nodes**            | 3                        | Still usually 1 redirect, 3 provides safety margin          |
| **12 nodes**           | 3-5                      | Slightly higher due to more complex resharding              |
| **100 nodes**          | 5-10                     | More nodes = more potential redirections during maintenance |

## Real-World Examples

### Normal Operation (99% of cases):

```shell
Client → Node A: GET user:123
Node A: Returns value immediately ✅  # 0 redirects needed
```

### During Cluster Maintenance:

```shell
Client → Node A: GET user:456
Node A → Client: MOVED 8192 Node-B:6379  # 1 redirect
Client → Node B: Returns value ✅
```

### During Complex Resharding:

```shell
Client → Node A: GET order:789
Node A → Client: MOVED 4096 Node-B:6379  # Redirect 1
Client → Node B: GET order:789
Node B → Client: MOVED 4096 Node-C:6379  # Redirect 2 (slot being moved)
Client → Node C: Returns value ✅         # Success after 2 redirects
```

## Recommended Values

```yaml
# For most production clusters
cluster:
  max-redirects: 3  # Good default for most scenarios

# For large clusters with frequent maintenance
cluster:
  max-redirects: 5  # Higher tolerance for complex operations

# For development/testing
cluster:
  max-redirects: 1  # Fail fast to catch configuration issues
```

## Performance Impact

- Higher values: More fault tolerance but potentially slower failure detection
- Lower values: Faster error reporting but may fail during legitimate cluster operations

## Our Current Configuration

```yaml
cluster:
  nodes: ${REDIS_CLUSTER_NODES:redis-cluster-1:6379,redis-cluster-2:6379,redis-cluster-3:6379}
  max-redirects: 3  # Perfect for a 3-node cluster
```

This is optimal because:

- Allows for normal cluster operations
- Provides safety margin during maintenance
- Not unnecessarily high (which could mask real problems)
- Works well regardless of whether you have 3, 6, or more nodes

> **maxRedirects = 3** is a sweet spot that works well for most cluster sizes, not tied to the number of nodes!
