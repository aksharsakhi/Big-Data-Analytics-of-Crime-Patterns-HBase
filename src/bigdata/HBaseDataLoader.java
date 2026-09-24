package bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 23CSE352: Big Data Analytics - Project Review 2
 * Fast Batch CSV Data Loader for Chicago Crime Patterns in HBase
 * 
 * Ingests cleaned real-world CSV records into HBase with batching (500 records/batch).
 */
public class HBaseDataLoader {

    private static final String TABLE_NAME = "crime_records";
    private static final byte[] CF_INCIDENT = Bytes.toBytes("incident");
    private static final byte[] CF_DETAILS = Bytes.toBytes("details");
    private static final byte[] CF_GEO = Bytes.toBytes("geo");
    private static final int BATCH_SIZE = 500;

    public static void main(String[] args) {
        String csvFilePath = (args.length > 0) ? args[0] : "dataset/chicago_crimes_clean.csv";
        int maxRecords = (args.length > 1) ? Integer.parseInt(args[1]) : 10000;

        System.out.println("================================================================");
        System.out.println(" HBase Real-World CSV Ingestion Utility");
        System.out.println(" Source File: " + csvFilePath);
        System.out.println(" Target Table: " + TABLE_NAME);
        System.out.println(" Max Ingest Limit: " + maxRecords);
        System.out.println("================================================================\n");

        Configuration config = HBaseConfiguration.create();

        try (Connection connection = ConnectionFactory.createConnection(config);
             Admin admin = connection.getAdmin()) {

            TableName tableName = TableName.valueOf(TABLE_NAME);

            // Create table if missing
            if (!admin.tableExists(tableName)) {
                System.out.println("[-] Table '" + TABLE_NAME + "' does not exist. Creating schema...");
                try {
                    TableDescriptor desc = TableDescriptorBuilder.newBuilder(tableName)
                            .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(CF_INCIDENT).build())
                            .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(CF_DETAILS).build())
                            .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(CF_GEO).build())
                            .build();
                    admin.createTable(desc);
                } catch (Throwable t) {
                    org.apache.hadoop.hbase.HTableDescriptor htd = new org.apache.hadoop.hbase.HTableDescriptor(tableName);
                    htd.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor(CF_INCIDENT));
                    htd.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor(CF_DETAILS));
                    htd.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor(CF_GEO));
                    admin.createTable(htd);
                }
                System.out.println("[+] Schema created successfully with families: incident, details, geo.\n");
            }

            try (Table table = connection.getTable(tableName);
                 BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

                String headerLine = br.readLine();
                if (headerLine == null) {
                    System.err.println("[ERROR] Empty CSV file: " + csvFilePath);
                    return;
                }

                String line;
                int totalLoaded = 0;
                List<Put> batchList = new ArrayList<>(BATCH_SIZE);
                long startTime = System.currentTimeMillis();

                System.out.println("[*] Streaming and batching records into HBase...");

                while ((line = br.readLine()) != null && totalLoaded < maxRecords) {
                    String[] tokens = parseCsvLine(line);
                    if (tokens.length < 15) {
                        continue;
                    }

                    // Field mapping:
                    // 0: id, 1: case_number, 2: date, 3: block, 4: iucr, 5: primary_type,
                    // 6: description, 7: location_description, 8: arrest, 9: domestic,
                    // 10: beat, 11: district, 12: ward, 13: community_area, 14: fbi_code,
                    // 19: latitude, 20: longitude
                    String id = tokens[0].trim();
                    String caseNumber = tokens.length > 1 ? tokens[1].trim() : "";
                    String date = tokens.length > 2 ? tokens[2].trim() : "";
                    String block = tokens.length > 3 ? tokens[3].trim() : "";
                    String iucr = tokens.length > 4 ? tokens[4].trim() : "";
                    String primaryType = tokens.length > 5 ? tokens[5].trim() : "UNKNOWN";
                    String description = tokens.length > 6 ? tokens[6].trim() : "";
                    String locDesc = tokens.length > 7 ? tokens[7].trim() : "";
                    String arrest = tokens.length > 8 ? tokens[8].trim() : "false";
                    String domestic = tokens.length > 9 ? tokens[9].trim() : "false";
                    String beat = tokens.length > 10 ? tokens[10].trim() : "";
                    String districtRaw = tokens.length > 11 ? tokens[11].trim() : "000";
                    String ward = tokens.length > 12 ? tokens[12].trim() : "";
                    String commArea = tokens.length > 13 ? tokens[13].trim() : "";
                    String lat = tokens.length > 19 ? tokens[19].trim() : "";
                    String lon = tokens.length > 20 ? tokens[20].trim() : "";

                    // Pad district to 3 digits (e.g., "19" -> "019")
                    String district = padDistrict(districtRaw);
                    String cleanPrimaryType = primaryType.replace(" ", "_");

                    // Composite Row Key: District#PrimaryType#ID
                    String rowKey = district + "#" + cleanPrimaryType + "#" + id;
                    Put put = new Put(Bytes.toBytes(rowKey));

                    // Family: incident
                    addCell(put, CF_INCIDENT, "case_number", caseNumber);
                    addCell(put, CF_INCIDENT, "date", date);
                    addCell(put, CF_INCIDENT, "block", block);
                    addCell(put, CF_INCIDENT, "iucr", iucr);
                    addCell(put, CF_INCIDENT, "primary_type", primaryType);
                    addCell(put, CF_INCIDENT, "description", description);

                    // Family: details
                    addCell(put, CF_DETAILS, "location_desc", locDesc);
                    addCell(put, CF_DETAILS, "arrest", arrest);
                    addCell(put, CF_DETAILS, "domestic", domestic);
                    addCell(put, CF_DETAILS, "beat", beat);
                    addCell(put, CF_DETAILS, "district", district);
                    addCell(put, CF_DETAILS, "ward", ward);
                    addCell(put, CF_DETAILS, "community_area", commArea);

                    // Family: geo
                    if (!lat.isEmpty() && !lon.isEmpty()) {
                        addCell(put, CF_GEO, "latitude", lat);
                        addCell(put, CF_GEO, "longitude", lon);
                    }

                    batchList.add(put);
                    totalLoaded++;

                    if (batchList.size() >= BATCH_SIZE) {
                        table.put(batchList);
                        batchList.clear();
                        System.out.printf("    Loaded %6d records...%n", totalLoaded);
                    }
                }

                if (!batchList.isEmpty()) {
                    table.put(batchList);
                    batchList.clear();
                }

                long duration = System.currentTimeMillis() - startTime;
                System.out.println("\n[SUCCESS] Completed loading " + totalLoaded + " records in " + duration + " ms (" + (duration / 1000.0) + " seconds).");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load data into HBase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addCell(Put put, byte[] family, String qualifier, String value) {
        if (value != null && !value.isEmpty()) {
            put.addColumn(family, Bytes.toBytes(qualifier), Bytes.toBytes(value));
        }
    }

    private static String padDistrict(String dist) {
        if (dist == null || dist.isEmpty()) return "000";
        while (dist.length() < 3) {
            dist = "0" + dist;
        }
        return dist;
    }

    private static String[] parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list.toArray(new String[0]);
    }
}
