# SET 2: Live Presentation Script & Demonstration Commands

**Course:** 23CSE352 - Big Data Analytics  
**Review:** Project Review 2 (10 Marks)  
**Project:** Big Data Analytics of Crime Patterns using Apache HBase  
**Team:** Sheela Akshar Sakhi & Nishanth S Gowda  

---

## Live Demonstration Flow Overview (3–4 Minutes Total)

```
DEMO SEQUENCE:
├── Part A: Distributed Architecture Verification (`jps`) [10 sec]
├── Part B: Standalone Java Client API (`./run_java_api.sh`) [30 sec] -> Rubric: 2 Marks
└── Part C: Interactive HBase Shell (`hbase shell`) [2.5 min] -> Rubric: 6 Marks
    ├── Command 1: Schema & Column Families (`describe`) -> Rubric: 1 Mark
    ├── Command 2: Fast Point Retrieval (`get`)
    ├── Command 3: Filter 1 - Arrests (`SingleColumnValueFilter`)
    ├── Command 4: Filter 2 - District 011 Patrol (`PrefixFilter`)
    ├── Command 5: Filter 3 - Vehicle Crimes (`ValueFilter`) -> Rubric: 4 Marks
    ├── Command 6: 10,000 Record Aggregation (`count`) -> Rubric: 1 Mark
    └── Command 7: DML Lifecycle (`put`, `delete`, `deleteall`) -> Rubric: 2 Marks
```

---

## Part A: Show Distributed Architecture (10 Seconds)

Open your VM terminal (`hadoop@...`) and run:
```bash
jps
```

### 🗣️ What to Say to Ma'am:
> *"Good morning Ma'am. Our project runs on a distributed Apache HBase cluster backed by Hadoop HDFS. As seen from `jps`, we have the HDFS storage layer running via `NameNode` and `DataNode`, coordinated with HBase `HMaster`, worker `HRegionServer`, and Apache ZooKeeper `HQuorumPeer`."*

---

## Part B: Programmatic Java Client API (30 Seconds) — [Rubric: 2 Marks]

In your project directory, execute:
```bash
cd ~/BigData/Projects/Big-Data-Analytics-of-Crime-Patterns-HBase
./run_java_api.sh
```

### 🗣️ What to Say to Ma'am:
> *"Before demonstrating the interactive shell, here is our native Java API implementation (`HBaseCrimeOperations.java`). It programmatically connects to the cluster using `ConnectionFactory`, verifies the table admin schema, inserts a real-time record via `PUT`, performs a point lookup with `GET`, executes server-side scans with `SingleColumnValueFilter` and `PrefixFilter`, and executes a transactional `DELETE` verified with an empty result."*

---

## Part C: Interactive HBase Shell Demonstration — [Rubric: 6 Marks]

Launch the interactive HBase shell:
```bash
hbase shell
```

---

### Command 1: Schema Inspection & Table Definition — [Rubric: 1 Mark]
```ruby
describe 'crime_records'
```

### 🗣️ What to Say to Ma'am:
> *"Here is our HBase table schema. We defined 3 domain-specific column families:  
> 1. `incident` for case number, date, and description.  
> 2. `details` for arrest status, domestic indicators, beat, and district.  
> 3. `geo` for spatial coordinates.  
> `BLOCKCACHE` and `BLOOMFILTER` are enabled to guarantee sub-millisecond lookups."*

---

### Command 2: Sub-Second Point Lookup (`GET`)
```ruby
get 'crime_records', '019#BURGLARY#14285893'
```

### 🗣️ What to Say to Ma'am:
> *"This demonstrates random point retrieval. Using our compound row key — `<District>#<CrimeType>#<IncidentID>` — HBase seeks directly to the target row without a full-table scan, returning all 14 column qualifiers in approximately 80 milliseconds."*

---

### Command 3: Analytical Filter 1 — Suspect Arrest Tracking (`SingleColumnValueFilter`)
```ruby
scan 'crime_records', {FILTER => "SingleColumnValueFilter('details', 'arrest', =, 'binary:true')", LIMIT => 3}
```

### 🗣️ What to Say to Ma'am:
> *"This is our first analytical filter: `SingleColumnValueFilter`. It performs server-side filtering on `details:arrest = true` directly inside the RegionServer, transferring only matching rows across the network in ~70 ms."*

---

### Command 4: Analytical Filter 2 — District 011 Patrol Jurisdiction Scan (`PrefixFilter`)
```ruby
scan 'crime_records', {FILTER => "PrefixFilter('011#')", LIMIT => 3}
```

### 🗣️ What to Say to Ma'am:
> *"This is our second filter: `PrefixFilter`. It leverages our row key prefix to jump straight to District 011 records, bypassing 96% of irrelevant table data and completing in under 50 ms."*

---

### Command 5: Analytical Filter 3 — Vehicle Crime Forensic Search (`ValueFilter`) — [Rubric: 4 Marks]
```ruby
scan 'crime_records', {FILTER => "ValueFilter(=, 'substring:VEHICLE')", LIMIT => 3}
```

### 🗣️ What to Say to Ma'am:
> *"This is our third filter: `ValueFilter` with `SubstringComparator`. It searches all column values across the table for the keyword 'VEHICLE', matching both ride-share incidents and motor vehicle burglaries in 0.26 seconds. This satisfies the requirement for at least 3 distinct filters."*

---

### Command 6: Full-Table Aggregation Count (`COUNT`) — [Rubric: 1 Mark]
```ruby
count 'crime_records', INTERVAL => 1000
```

### 🗣️ What to Say to Ma'am:
> *"This audit command demonstrates high-speed sequential scanning with client-side caching (`INTERVAL => 1000`). It verifies that exactly 10,000 authentic City of Chicago crime records are stored in HBase with 100% data integrity in ~0.21 seconds."*

---

### Command 7: DML Lifecycle — Manual PUT, Column DELETE, Row DELETEALL — [Rubric: 2 Marks]

Copy and paste this entire block into the shell:
```ruby
put 'crime_records', '099#DEMO#99999999', 'incident:case_number', 'DEMO101'
put 'crime_records', '099#DEMO#99999999', 'details:arrest', 'false'
get 'crime_records', '099#DEMO#99999999'

delete 'crime_records', '099#DEMO#99999999', 'details:arrest'
get 'crime_records', '099#DEMO#99999999'

deleteall 'crime_records', '099#DEMO#99999999'
get 'crime_records', '099#DEMO#99999999'
```

### 🗣️ What to Say to Ma'am:
> *"Finally, we demonstrate the complete DML lifecycle:  
> 1. We manually insert a sample incident using `put`.  
> 2. We delete a specific column (`details:arrest`) using `delete`, and `get` verifies only `incident` remains.  
> 3. We delete the entire row using `deleteall` to simulate an expunged case, and `get` returns `0 row(s)` confirming the tombstone deletion."*

---

### Command 8: Exit the Shell
```ruby
exit
```

---

## 🎯 Evaluation Rubric Alignment Matrix

| Rubric Criteria | Marks | Exact Command Demonstrated Live |
| :--- | :---: | :--- |
| **Real-world problem & dataset** | **1** | `count 'crime_records', INTERVAL => 1000` (10,000 authentic records) |
| **HBase table design & schema** | **1** | `describe 'crime_records'` & `get 'crime_records', '019#BURGLARY#14285893'` |
| **HBase Shell implementation** | **2** | `put`, `get`, `delete`, `deleteall`, `count` |
| **HBase Filters ($\ge 3$ filters)** | **4** | `SingleColumnValueFilter`, `PrefixFilter`, `ValueFilter` |
| **Java API implementation** | **2** | `./run_java_api.sh` (`HBaseCrimeOperations.java`) |
| **TOTAL** | **10 / 10** | **All evaluation criteria covered live in under 4 minutes** |
