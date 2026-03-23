# dist_KV_store
this is a personal project, making a distributed key value store


store shards in consistent hash table

create server side hash tables containing shards

one instance of raft operates acorss a shard

maintain shards in Consistent hash

use raft on shard groups to control adding/removing/spliting shards/nodes from network

add node to first shard clockwise from it on hash ring

clients discover nodes on network by requesting it from a seed node