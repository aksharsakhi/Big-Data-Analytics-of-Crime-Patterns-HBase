# Commands Executed (100% Raw Manual Commands Log)

This document provides a chronological, step-by-step log of every single raw terminal command and HBase Shell command required to build, load, query, and demonstrate the HBase Big Data Crime Patterns pipeline.

---

## 📌 Phase 1: Host Workspace Setup & Git Synchronization

Executed on Host Machine (Mac Terminal):

```bash
# Navigate to workspace
cd /Users/aksharsakhi/Documents/Files/Code/Amrita/Big_Data

# Initialize Git repository
cd Big-Data-Analytics-of-Crime-Patterns-HBase
git init
git add .
git commit -m "Initial commit for Project Review 2 - Apache HBase"
git branch -M main
git remote add origin https://github.com/aksharsakhi/Big-Data-Analytics-of-Crime-Patterns-HBase.git
git push -u origin main
```

---

## 📌 Phase 2: Virtual Machine Environment & Daemon Startup

Executed on UTM Ubuntu Virtual Machine (`hadoop@aksharsakhi-QEMU-Virtual-Machine`):

```bash
# Clone the repository
git clone https://github.com/aksharsakhi/Big-Data-Analytics-of-Crime-Patterns-HBase.git
cd Big-Data-Analytics-of-Crime-Patterns-HBase

# Start HDFS Distributed Storage Daemons
start-dfs.sh

# Start Apache HBase Distributed Master & RegionServer Daemons
start-hbase.sh

# Verify all daemons are active
jps
```
Expected output:
```
NameNode
DataNode
SecondaryNameNode
HMaster
HRegionServer
HQuorumPeer
```

---

## 📌 Phase 3: Raw Java Compilation & Batch Dataset Ingestion

Executed on Virtual Machine Terminal:

```bash
# Clean and create bytecode output directory
rm -rf classes && mkdir -p classes

# Compile HBase DataLoader with HBase classpath
javac -cp $(hbase classpath) -d classes src/bigdata/HBaseDataLoader.java

# Execute DataLoader to ingest 10,000 real-world records into HBase
HBASE_CLASSPATH=classes hbase bigdata.HBaseDataLoader dataset/chicago_crimes_clean.csv 10000
```

---

## 📌 Phase 4: HBase Interactive Shell Execution

Executed inside `hbase shell`:

```bash
# Start HBase interactive shell
hbase shell
```

### 1. Verification of Table Schema
```ruby
status
list
describe 'crime_records'
```

### 2. Manual Sample Insertion (PUT)
```ruby
put 'crime_records', '019#BURGLARY#14285893', 'incident:case_number', 'JK363045'
put 'crime_records', '019#BURGLARY#14285893', 'incident:date', '2026-08-04T00:00:00.000'
put 'crime_records', '019#BURGLARY#14285893', 'incident:block', '046XX N HAMILTON AVE'
put 'crime_records', '019#BURGLARY#14285893', 'incident:primary_type', 'BURGLARY'
put 'crime_records', '019#BURGLARY#14285893', 'incident:description', 'BURGLARY FROM MOTOR VEHICLE'
put 'crime_records', '019#BURGLARY#14285893', 'details:location_desc', 'STREET'
put 'crime_records', '019#BURGLARY#14285893', 'details:arrest', 'false'
put 'crime_records', '019#BURGLARY#14285893', 'details:domestic', 'false'
put 'crime_records', '019#BURGLARY#14285893', 'details:district', '019'
```

### 3. Point Retrieval (GET)
```ruby
# Retrieve entire row
get 'crime_records', '019#BURGLARY#14285893'

# Retrieve specific column family
get 'crime_records', '019#BURGLARY#14285893', 'incident'

# Retrieve specific qualifier
get 'crime_records', '019#BURGLARY#14285893', 'details:arrest'
```

### 4. Basic Scans & Range Queries
```ruby
# Sample table preview
scan 'crime_records', {LIMIT => 5}

# District 011 Range Scan
scan 'crime_records', {STARTROW => '011#', STOPROW => '012#', LIMIT => 5}
```

### 5. Advanced Analytical Filters (Rubric Requirement: 4 Marks)
```ruby
# Filter 1: SingleColumnValueFilter (Suspect Arrests)
scan 'crime_records', {FILTER => "SingleColumnValueFilter('details', 'arrest', =, 'binary:true')", LIMIT => 5}

# Filter 2: SingleColumnValueFilter (Domestic Violence Flag)
scan 'crime_records', {FILTER => "SingleColumnValueFilter('details', 'domestic', =, 'binary:true')", LIMIT => 5}

# Filter 3: PrefixFilter (District 019 Fast Retrieval)
scan 'crime_records', {FILTER => "PrefixFilter('019#')", LIMIT => 5}

# Filter 4: RowFilter with RegexStringComparator (Narcotics Category Match)
scan 'crime_records', {FILTER => "RowFilter(=, 'regexstring:^.*#NARCOTICS#.*')", LIMIT => 5}

# Filter 5: ValueFilter with SubstringComparator (Keyword "VEHICLE")
scan 'crime_records', {FILTER => "ValueFilter(=, 'substring:VEHICLE')", LIMIT => 5}

# Filter 6: Compound FilterList (District 011 AND Arrest = true)
scan 'crime_records', {FILTER => "(PrefixFilter('011#')) AND (SingleColumnValueFilter('details', 'arrest', =, 'binary:true'))", LIMIT => 5}

# Filter 7: ColumnPaginationFilter
scan 'crime_records', {FILTER => "ColumnPaginationFilter(4, 0)", LIMIT => 5}
```

### 6. Count and Data Deletion Operations
```ruby
# Row Count
count 'crime_records', INTERVAL => 1000, CACHE => 1000

# Delete specific cell
delete 'crime_records', '019#BURGLARY#14285893', 'geo:x_coordinate'

# Delete entire row
deleteall 'crime_records', '019#BURGLARY#14285893'

# Verify deletion
get 'crime_records', '019#BURGLARY#14285893'
```

---

## 📌 Phase 5: Raw Java API Compilation & Execution

Executed on Virtual Machine Terminal:

```bash
# Compile HBase Java API client
javac -cp $(hbase classpath) -d classes src/bigdata/HBaseCrimeOperations.java

# Run Java application
HBASE_CLASSPATH=classes hbase bigdata.HBaseCrimeOperations
```
