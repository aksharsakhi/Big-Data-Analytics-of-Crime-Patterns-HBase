# SET 1: Pre-Flight Testing Guide (Run Before Presentation)

**Course:** 23CSE352 - Big Data Analytics  
**Project:** Big Data Analytics of Crime Patterns using Apache HBase  
**Purpose:** Run these commands inside your VM tonight / prior to the review to verify that Hadoop HDFS, HBase, the dataset, and the Java API client are 100% operational.

---

## Step 1: Update Repository in VM
Ensure your VM has the latest presentation, report, and script files:

```bash
cd ~/BigData/Projects/Big-Data-Analytics-of-Crime-Patterns-HBase
git pull
```

---

## Step 2: Check & Start Cluster Daemons (HDFS & HBase)

Check currently running Java processes:
```bash
jps
```

### Expected Processes (All 5 must be present):
- `NameNode` (HDFS Master)
- `DataNode` (HDFS Storage)
- `HMaster` (HBase Cluster Master)
- `HRegionServer` (HBase Storage Worker)
- `HQuorumPeer` (Apache ZooKeeper Coordination)

---

### If Any Daemon Is Missing, Run This Startup Sequence:

```bash
# 1. Clear any conflicting Hadoop environment variables
unset HADOOP_COMMON_HOME HADOOP_HDFS_HOME HADOOP_YARN_HOME HADOOP_MAPRED_HOME

# 2. Export active Hadoop and HBase environment paths
export HADOOP_HOME=/home/hadoop/BigData/Hadoop/hadoop-3.4.1
export HBASE_HOME=$HOME/BigData/HBase/hbase
export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$HBASE_HOME/bin:$PATH

# 3. Start HDFS NameNode and DataNode
hdfs --daemon start namenode
hdfs --daemon start datanode

# 4. Start Apache HBase Daemons
start-hbase.sh

# 5. Verify all 5 daemons are running
jps
```

---

## Step 3: Test Programmatic Java API Client

Test compile and execute the native Java client:
```bash
cd ~/BigData/Projects/Big-Data-Analytics-of-Crime-Patterns-HBase
./run_java_api.sh
```

### Expected Output:
- Bytecode compiled into `classes/` with Java 8 target.
- Output lines:
  - `[STEP 1] Checking HBase Table Existence`
  - `[STEP 2] Inserting Real-World Incident via PUT`
  - `[STEP 3] Random Point Lookup via GET`
  - `[STEP 4] Filtered Scan 1: SingleColumnValueFilter (Suspect Arrests)`
  - `[STEP 5] Filtered Scan 2: PrefixFilter (District 011 Patrol Scans)`
  - `[STEP 6] Transactional Deletion via DELETE`
  - `[STEP 7] Verifying Deletion with GET -> [VERIFIED] Record no longer exists in HBase (Empty Result).`
  - `[SUCCESS] All HBase Java API Operations Completed Successfully!`

---

## Step 4: Test Interactive HBase Shell & Dataset Integrity

Launch HBase Shell:
```bash
hbase shell
```

Inside the shell (`hbase:001:0>`), run:

### 4.1 Verify Table Schema:
```ruby
describe 'crime_records'
```
*Expected: Table is `ENABLED` with column families `incident`, `details`, and `geo`.*

### 4.2 Verify Record Count (10,000 Records):
```ruby
count 'crime_records', INTERVAL => 1000
```
*Expected: Increments every 1,000 rows and finishes with `10000 row(s)` in ~0.21s.*

### 4.3 Test Filter:
```ruby
scan 'crime_records', {FILTER => "ValueFilter(=, 'substring:VEHICLE')", LIMIT => 3}
```
*Expected: Returns 3 rows in ~0.26s.*

### 4.4 Exit Shell:
```ruby
exit
```

---

## Pre-Flight Checklist Summary
- [ ] `git pull` successful.
- [ ] `jps` shows `NameNode`, `DataNode`, `HMaster`, `HRegionServer`, `HQuorumPeer`.
- [ ] `./run_java_api.sh` runs cleanly with 0 errors.
- [ ] `count 'crime_records'` confirms exactly 10,000 rows in HBase.
- [ ] System is 100% ready for the live demonstration.
