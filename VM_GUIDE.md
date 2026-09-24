# UTM Ubuntu Virtual Machine (VM) Execution & Screenshot Guide

This guide provides a comprehensive walkthrough for running the **Project Review 2 (Apache HBase)** pipeline on your Ubuntu UTM Virtual Machine and capturing all the screenshots required for your presentation slides and final LaTeX report.

---

## 📋 Pre-Flight Checklist
Before beginning, ensure your VM has internet connectivity (or local access) and terminal access.

---

## Step 1: Clone Repository on Your VM
Open your Ubuntu terminal on the UTM VM and clone the repository:
```bash
git clone https://github.com/aksharsakhi/Big-Data-Analytics-of-Crime-Patterns-HBase.git
cd Big-Data-Analytics-of-Crime-Patterns-HBase
```

---

## Step 2: Start Hadoop & HBase Daemons
Start HDFS (HBase relies on HDFS in distributed/pseudo-distributed mode) and Apache HBase:
```bash
# Start HDFS
start-dfs.sh

# Start Apache HBase
start-hbase.sh

# Verify all daemons are running
jps
```
> **What you should see in `jps`:**
> - `HMaster`
> - `HRegionServer`
> - `HQuorumPeer` (ZooKeeper)
> - `NameNode`
> - `DataNode`
> - `SecondaryNameNode`

📸 **SCREENSHOT 1: HBase Daemons Verification**
- **Action:** Take a screenshot of the `jps` output showing `HMaster`, `HRegionServer`, and `HQuorumPeer`.
- **Save as:** `presentation/new_img/screenshot1_daemons.png`

---

## Step 3: Ingest Dataset into HBase
Run the high-speed data ingestion utility:
```bash
chmod +x populate_hbase.sh run_java_api.sh run_hbase_shell.sh
./populate_hbase.sh
```
> **What happens:** The script compiles `HBaseDataLoader.java` and streams the 10,000 real-world crime records into the `crime_records` table in batches of 500 records.

📸 **SCREENSHOT 2: Batch Data Ingestion**
- **Action:** Take a screenshot showing the compilation and the output `[SUCCESS] Completed loading 10000 records in ... ms`.
- **Save as:** `presentation/new_img/screenshot2_ingestion.png`

---

## Step 4: HBase Shell - Table Verification & Point Retrieval (GET)
Launch the HBase interactive shell:
```bash
hbase shell
```
Inside the HBase shell, run:
```ruby
# Verify table schema
describe 'crime_records'

# Point Lookup (GET)
get 'crime_records', '019#BURGLARY#14285893'
```

📸 **SCREENSHOT 3: Table Schema & GET Operation**
- **Action:** Take a screenshot showing `describe 'crime_records'` and the returned cells for the `get` command.
- **Save as:** `presentation/new_img/screenshot3_get.png`

---

## Step 5: HBase Shell - Filter Operations (Rubric Requirement)
Inside the HBase shell, execute the analytical filters:

### Filter 1: Arrest Status Filter
```ruby
scan 'crime_records', {FILTER => "SingleColumnValueFilter('details', 'arrest', =, 'binary:true')", LIMIT => 5}
```
📸 **SCREENSHOT 4: SingleColumnValueFilter (Arrests)**
- **Save as:** `presentation/new_img/screenshot4_filter_arrest.png`

### Filter 2: Domestic Abuse Incident Filter
```ruby
scan 'crime_records', {FILTER => "SingleColumnValueFilter('details', 'domestic', =, 'binary:true')", LIMIT => 5}
```
📸 **SCREENSHOT 5: SingleColumnValueFilter (Domestic)**
- **Save as:** `presentation/new_img/screenshot5_filter_domestic.png`

### Filter 3: Jurisdiction Prefix Scan (District 011)
```ruby
scan 'crime_records', {FILTER => "PrefixFilter('011#')", LIMIT => 5}
```
📸 **SCREENSHOT 6: PrefixFilter (District Jurisdiction)**
- **Save as:** `presentation/new_img/screenshot6_prefix_filter.png`

### Filter 4 & 5: Regex RowFilter & Value Substring Filter
```ruby
# Narcotics across all districts
scan 'crime_records', {FILTER => "RowFilter(=, 'regexstring:^.*#NARCOTICS#.*')", LIMIT => 5}

# Keyword search in description
scan 'crime_records', {FILTER => "ValueFilter(=, 'substring:VEHICLE')", LIMIT => 5}
```
📸 **SCREENSHOT 7: Advanced Regex & Substring Filters**
- **Save as:** `presentation/new_img/screenshot7_regex_filter.png`

---

## Step 6: Count & Deletion Operations
Inside the HBase shell, run the count and deletion commands:
```ruby
# Count total records in table
count 'crime_records', INTERVAL => 1000, CACHE => 1000

# Delete specific cell
delete 'crime_records', '019#BURGLARY#14285893', 'geo:x_coordinate'

# Delete entire row
deleteall 'crime_records', '019#BURGLARY#14285893'

# Verify row is purged
get 'crime_records', '019#BURGLARY#14285893'
```
Exit the shell when done:
```ruby
exit
```

📸 **SCREENSHOT 8: Count, Delete, and DeleteAll Operations**
- **Action:** Take a screenshot of the `count` output and the `deleteall` execution.
- **Save as:** `presentation/new_img/screenshot8_count_delete.png`

---

## Step 7: Java API Demonstration
Run the standalone Java API runner script:
```bash
./run_java_api.sh
```
> **What happens:** The script compiles and executes `HBaseCrimeOperations.java`, demonstrating:
> 1. Table status verification
> 2. Real-time `Put` of incident `011#NARCOTICS#14289999`
> 3. Point lookup `Get`
> 4. `SingleColumnValueFilter` scan in Java
> 5. `PrefixFilter` scan in Java
> 6. Record `Delete`
> 7. Verified empty retrieval

📸 **SCREENSHOT 9: HBase Java API Execution**
- **Action:** Take a screenshot showing the clean terminal banners and results from Step 1 through Step 7.
- **Save as:** `presentation/new_img/screenshot9_java_api.png`

---

## Step 8: Sync Screenshots to Presentation & Report
1. Copy the captured screenshots from your VM into your laptop repo folder under `presentation/new_img/`.
2. Commit and push back to git:
   ```bash
   git add presentation/new_img/
   git commit -m "Added live execution screenshots from Ubuntu VM"
   git push origin main
   ```
3. Recompile the LaTeX report and presentation using `tectonic`:
   ```bash
   tectonic presentation/report.tex
   tectonic presentation/presentation.tex
   ```
You are 100% prepared for your project review!
