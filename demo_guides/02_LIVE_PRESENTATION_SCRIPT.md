# Live Presentation Commands

### 1. Check Daemons
```bash
jps
```
> Shows distributed cluster daemons: NameNode, DataNode, HMaster, HRegionServer, HQuorumPeer.

---

### 2. Run Java API
```bash
cd ~/BigData/Projects/Big-Data-Analytics-of-Crime-Patterns-HBase
./run_java_api.sh
```
> Executes Java client program demonstrating connection, Table admin, Put, Get, Scans, Filters, and Delete.

---

### 3. Open HBase Shell
```bash
hbase shell
```
> Launches interactive HBase command shell.

---

### 4. Table Schema
```ruby
describe 'crime_records'
```
> Displays table schema and the 3 column families: `incident`, `details`, and `geo`.

---

### 5. Point Retrieval (GET)
```ruby
get 'crime_records', '019#BURGLARY#14285893'
```
> Performs sub-second point lookup using compound row key (`District#CrimeType#ID`).

---

### 6. Filter 1: Suspect Arrests
```ruby
scan 'crime_records', {FILTER => "SingleColumnValueFilter('details', 'arrest', =, 'binary:true')", LIMIT => 3}
```
> Server-side filter returning incidents where `details:arrest = true`.

---

### 7. Filter 2: District 011 Patrol Scan
```ruby
scan 'crime_records', {FILTER => "PrefixFilter('011#')", LIMIT => 3}
```
> Prefix scan seeking straight to District 011 records in under 50ms using row key prefix.

---

### 8. Filter 3: Vehicle Crime Search
```ruby
scan 'crime_records', {FILTER => "ValueFilter(=, 'substring:VEHICLE')", LIMIT => 3}
```
> Substring value filter searching all columns across the table for keyword `VEHICLE`.

---

### 9. Count Records (10,000 Rows)
```ruby
count 'crime_records', INTERVAL => 1000
```
> Aggregates and confirms exactly 10,000 authentic records loaded with zero data loss.

---

### 10. Manual Insert (PUT)
```ruby
put 'crime_records', '099#DEMO#99999999', 'incident:case_number', 'DEMO101'
put 'crime_records', '099#DEMO#99999999', 'details:arrest', 'false'
get 'crime_records', '099#DEMO#99999999'
```
> Manually inserts sample test record across two column families and displays it.

---

### 11. Column Deletion (DELETE)
```ruby
delete 'crime_records', '099#DEMO#99999999', 'details:arrest'
get 'crime_records', '099#DEMO#99999999'
```
> Drops specific column cell (`details:arrest`) and verifies only incident remains.

---

### 12. Row Deletion (DELETEALL)
```ruby
deleteall 'crime_records', '099#DEMO#99999999'
get 'crime_records', '099#DEMO#99999999'
```
> Purges entire row with tombstone marker and verifies 0 rows returned.

---

### 13. Exit Shell
```ruby
exit
```
> Exits HBase shell back to Linux terminal.
