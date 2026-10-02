#!/bin/bash
# ==============================================================================
# Script: populate_hbase.sh
# Purpose: Compiles and executes HBaseDataLoader to batch insert Chicago Crimes CSV
# ==============================================================================

set -e

echo "=================================================================="
echo " [1/3] Checking Environment Prerequisites"
echo "=================================================================="

# Auto-detect HBase and Hadoop locations in ~/BigData/ if not already in PATH
if ! command -v hbase &> /dev/null; then
    for candidate in "$HOME/BigData/HBase" "$HOME/HBase" "/usr/local/hbase" "/opt/hbase"; do
        if [ -d "$candidate" ] && [ -f "$candidate/bin/hbase" ]; then
            export HBASE_HOME="$candidate"
            export PATH="$PATH:$HBASE_HOME/bin"
            break
        fi
    done
fi

if ! command -v hbase &> /dev/null; then
    echo "[ERROR] 'hbase' command not found."
    echo "Please run: export HBASE_HOME=\$HOME/BigData/HBase && export PATH=\$PATH:\$HBASE_HOME/bin"
    exit 1
fi

DATASET_FILE="dataset/chicago_crimes_clean.csv"
if [ ! -f "$DATASET_FILE" ]; then
    echo "[ERROR] Clean dataset file not found at $DATASET_FILE"
    exit 1
fi

echo "[✓] HBase binary found: $(which hbase)"
echo "[✓] Dataset located: $DATASET_FILE ($(wc -l < "$DATASET_FILE") rows)"

echo ""
echo "=================================================================="
echo " [2/3] Compiling HBaseDataLoader.java"
echo "=================================================================="
mkdir -p classes
rm -rf classes/*
javac -source 1.8 -target 1.8 -cp "$(hbase classpath)" -d classes src/bigdata/HBaseDataLoader.java
echo "[✓] Compilation successful (Java 8 bytecode). Bytecode generated in classes/bigdata/"

echo ""
echo "=================================================================="
echo " [3/3] Ingesting CSV Records into HBase Table 'crime_records'"
echo "=================================================================="
HBASE_CLASSPATH=classes hbase bigdata.HBaseDataLoader "$DATASET_FILE" 10000

echo ""
echo "=================================================================="
echo " [SUCCESS] HBase Data Ingestion Complete!"
echo " Next step: Launch 'hbase shell' and run queries in hbase_commands.txt"
echo "=================================================================="
