package bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.CompareOperator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 23CSE352: Big Data Analytics - Project Review 2
 * HBase Java API Implementation for Chicago Crime Pattern Analytics
 * 
 * Demonstrates:
 * 1. Connecting to Apache HBase using Connection API
 * 2. Creating an HBase Table with Column Families ('incident', 'details', 'geo') using Admin API
 * 3. Inserting a Crime Record (Put API)
 * 4. Retrieving a Crime Record by Row Key (Get API)
 * 5. Scanning Records with Filters (SingleColumnValueFilter, PrefixFilter) using Scan API
 * 6. Deleting a Record (Delete API)
 * 
 * Admin API
 * Put API
 * Get API
 * Scan API
 * Delete API
 * Connection API
 *
 * 
 */
public class HBaseCrimeOperations {

    private static final String TABLE_NAME = "crime_records";
    private static final byte[] CF_INCIDENT = Bytes.toBytes("incident");
    private static final byte[] CF_DETAILS = Bytes.toBytes("details");
    private static final byte[] CF_GEO = Bytes.toBytes("geo");

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println(" 23CSE352: Big Data Analytics - Project Review 2");
        System.out.println(" Apache HBase Java API Implementation - Crime Pattern Analytics");
        System.out.println("================================================================\n");

        Configuration config = HBaseConfiguration.create();

        try (Connection connection = ConnectionFactory.createConnection(config);
             Admin admin = connection.getAdmin()) {

            TableName tableName = TableName.valueOf(TABLE_NAME);

            // Step 1: Create Table if it does not exist
            createTableIfNotExists(admin, tableName);

            try (Table table = connection.getTable(tableName)) {

                // Step 2: Insert a new crime incident record
                String demoRowKey = "011#NARCOTICS#14289999";
                insertCrimeRecord(table, demoRowKey);

                // Step 3: Retrieve and display the inserted record (GET)
                getCrimeRecord(table, demoRowKey);

                // Step 4: Scan records with SingleColumnValueFilter (Arrest = true)
                scanArrestedCrimes(table);

                // Step 5: Scan records with PrefixFilter (District 011)
                scanDistrictCrimes(table, "011#");

                // Step 6: Delete the demonstrated record
                deleteCrimeRecord(table, demoRowKey);

                // Step 7: Verify deletion
                verifyDeletion(table, demoRowKey);

            }

            System.out.println("\n================================================================");
            System.out.println(" [SUCCESS] All HBase Java API Operations Completed Successfully!");
            System.out.println("================================================================");

        } catch (IOException e) {
            System.err.println("[ERROR] Exception occurred during HBase operation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 1. Creates HBase Table with Column Families if it doesn't already exist.
     * Uses reflection/compatible descriptor builder to work cleanly on HBase 2.x and 1.x.
     */
    private static void createTableIfNotExists(Admin admin, TableName tableName) throws IOException {
        System.out.println("[STEP 1] Checking HBase Table: " + tableName.getNameAsString());
        if (admin.tableExists(tableName)) {
            System.out.println("         -> Table '" + tableName.getNameAsString() + "' already exists. Ready for operations.\n");
            return;
        }

        System.out.println("         -> Table does not exist. Creating table with column families: incident, details, geo...");
        try {
            // HBase 2.x API via TableDescriptorBuilder
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(tableName)
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(CF_INCIDENT).build())
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(CF_DETAILS).build())
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(CF_GEO).build())
                    .build();
            admin.createTable(tableDescriptor);
            System.out.println("         -> Table '" + tableName.getNameAsString() + "' created successfully.\n");
        } catch (NoClassDefFoundError | NoSuchMethodError legacyFallback) {
            // Legacy HBase 1.x fallback
            org.apache.hadoop.hbase.HTableDescriptor htd = new org.apache.hadoop.hbase.HTableDescriptor(tableName);
            htd.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor(CF_INCIDENT));
            htd.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor(CF_DETAILS));
            htd.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor(CF_GEO));
            admin.createTable(htd);
            System.out.println("         -> Table created using legacy descriptor.\n");
        }
    }

    /**
     * 2. Inserts a realistic Crime Incident Record into HBase.
     * Row Key Pattern: District#CrimeType#IncidentID
     */
    private static void insertCrimeRecord(Table table, String rowKey) throws IOException {
        System.out.println("[STEP 2] Inserting Crime Incident Record (PUT)");
        System.out.println("         -> Row Key: " + rowKey);

        Put put = new Put(Bytes.toBytes(rowKey));

        // Column Family: incident
        put.addColumn(CF_INCIDENT, Bytes.toBytes("case_number"), Bytes.toBytes("JK369999"));
        put.addColumn(CF_INCIDENT, Bytes.toBytes("date"), Bytes.toBytes("2026-08-04T12:30:00.000"));
        put.addColumn(CF_INCIDENT, Bytes.toBytes("block"), Bytes.toBytes("038XX W MADISON ST"));
        put.addColumn(CF_INCIDENT, Bytes.toBytes("iucr"), Bytes.toBytes("1811"));
        put.addColumn(CF_INCIDENT, Bytes.toBytes("primary_type"), Bytes.toBytes("NARCOTICS"));
        put.addColumn(CF_INCIDENT, Bytes.toBytes("description"), Bytes.toBytes("POSS: CANNABIS 30G OR LESS"));

        // Column Family: details
        put.addColumn(CF_DETAILS, Bytes.toBytes("location_desc"), Bytes.toBytes("SIDEWALK"));
        put.addColumn(CF_DETAILS, Bytes.toBytes("arrest"), Bytes.toBytes("true"));
        put.addColumn(CF_DETAILS, Bytes.toBytes("domestic"), Bytes.toBytes("false"));
        put.addColumn(CF_DETAILS, Bytes.toBytes("beat"), Bytes.toBytes("1123"));
        put.addColumn(CF_DETAILS, Bytes.toBytes("district"), Bytes.toBytes("011"));
        put.addColumn(CF_DETAILS, Bytes.toBytes("ward"), Bytes.toBytes("28"));
        put.addColumn(CF_DETAILS, Bytes.toBytes("community_area"), Bytes.toBytes("26"));

        // Column Family: geo
        put.addColumn(CF_GEO, Bytes.toBytes("latitude"), Bytes.toBytes("41.880912345"));
        put.addColumn(CF_GEO, Bytes.toBytes("longitude"), Bytes.toBytes("-87.721498765"));

        table.put(put);
        System.out.println("         -> Record inserted into table '" + TABLE_NAME + "' across 3 column families.\n");
    }

    /**
     * 3. Retrieves a specific incident by Row Key (GET).
     */
    private static void getCrimeRecord(Table table, String rowKey) throws IOException {
        System.out.println("[STEP 3] Fetching Incident Record by Row Key (GET)");
        System.out.println("         -> Target Row Key: " + rowKey);

        Get get = new Get(Bytes.toBytes(rowKey));
        Result result = table.get(get);

        if (result.isEmpty()) {
            System.out.println("         -> [WARN] No record found for row key: " + rowKey + "\n");
            return;
        }

        System.out.println("         ---------------- Incident Details ----------------");
        printCell(result, CF_INCIDENT, "case_number");
        printCell(result, CF_INCIDENT, "date");
        printCell(result, CF_INCIDENT, "block");
        printCell(result, CF_INCIDENT, "primary_type");
        printCell(result, CF_INCIDENT, "description");
        printCell(result, CF_DETAILS, "location_desc");
        printCell(result, CF_DETAILS, "arrest");
        printCell(result, CF_DETAILS, "domestic");
        printCell(result, CF_DETAILS, "district");
        printCell(result, CF_GEO, "latitude");
        printCell(result, CF_GEO, "longitude");
        System.out.println("         --------------------------------------------------\n");
    }

    /**
     * 4. Scans table applying SingleColumnValueFilter (crimes where arrest == 'true').
     */
    private static void scanArrestedCrimes(Table table) throws IOException {
        System.out.println("[STEP 4] Scanning Records with SingleColumnValueFilter (arrest = 'true')");
        Scan scan = new Scan();
        scan.addFamily(CF_INCIDENT);
        scan.addFamily(CF_DETAILS);

        // Filter: details:arrest EQUAL 'true'
        SingleColumnValueFilter filter = new SingleColumnValueFilter(
                CF_DETAILS,
                Bytes.toBytes("arrest"),
                CompareOperator.EQUAL,
                new BinaryComparator(Bytes.toBytes("true"))
        );
        filter.setFilterIfMissing(true);
        scan.setFilter(filter);
        scan.setLimit(5); // Show first 5 matching incidents

        int matchCount = 0;
        try (ResultScanner scanner = table.getScanner(scan)) {
            for (Result res : scanner) {
                matchCount++;
                String row = Bytes.toString(res.getRow());
                String pType = Bytes.toString(res.getValue(CF_INCIDENT, Bytes.toBytes("primary_type")));
                String arrest = Bytes.toString(res.getValue(CF_DETAILS, Bytes.toBytes("arrest")));
                String block = Bytes.toString(res.getValue(CF_INCIDENT, Bytes.toBytes("block")));
                System.out.println("         [Result " + matchCount + "] RowKey: " + row +
                        " | Type: " + pType + " | Arrest: " + arrest + " | Block: " + block);
            }
        }
        System.out.println("         -> Total sampled arrested incidents displayed: " + matchCount + "\n");
    }

    /**
     * 5. Scans table applying PrefixFilter (crimes in District 011).
     */
    private static void scanDistrictCrimes(Table table, String districtPrefix) throws IOException {
        System.out.println("[STEP 5] Scanning Records with PrefixFilter (District Prefix: " + districtPrefix + ")");
        Scan scan = new Scan();
        scan.addFamily(CF_INCIDENT);
        scan.addFamily(CF_DETAILS);

        PrefixFilter prefixFilter = new PrefixFilter(Bytes.toBytes(districtPrefix));
        scan.setFilter(prefixFilter);
        scan.setLimit(5);

        int count = 0;
        try (ResultScanner scanner = table.getScanner(scan)) {
            for (Result res : scanner) {
                count++;
                String row = Bytes.toString(res.getRow());
                String pType = Bytes.toString(res.getValue(CF_INCIDENT, Bytes.toBytes("primary_type")));
                String desc = Bytes.toString(res.getValue(CF_INCIDENT, Bytes.toBytes("description")));
                System.out.println("         [District 011 Match " + count + "] RowKey: " + row +
                        " | Crime: " + pType + " (" + desc + ")");
            }
        }
        System.out.println("         -> District incidents previewed: " + count + "\n");
    }

    /**
     * 6. Deletes a record from HBase (DELETE).
     */
    private static void deleteCrimeRecord(Table table, String rowKey) throws IOException {
        System.out.println("[STEP 6] Deleting Record from HBase (DELETE)");
        System.out.println("         -> Target Row Key: " + rowKey);

        Delete delete = new Delete(Bytes.toBytes(rowKey));
        table.delete(delete);
        System.out.println("         -> Delete executed successfully.\n");
    }

    /**
     * 7. Verifies that the record has indeed been purged.
     */
    private static void verifyDeletion(Table table, String rowKey) throws IOException {
        System.out.println("[STEP 7] Verifying Deletion with GET");
        Get get = new Get(Bytes.toBytes(rowKey));
        Result result = table.get(get);
        if (result.isEmpty()) {
            System.out.println("         -> [VERIFIED] Record no longer exists in HBase (Empty Result).\n");
        } else {
            System.out.println("         -> [WARNING] Record was still retrieved.\n");
        }
    }

    private static void printCell(Result result, byte[] family, String qualifier) {
        byte[] value = result.getValue(family, Bytes.toBytes(qualifier));
        String valStr = (value != null) ? Bytes.toString(value) : "NULL";
        System.out.printf("         %-18s: %s%n", Bytes.toString(family) + ":" + qualifier, valStr);
    }
}
