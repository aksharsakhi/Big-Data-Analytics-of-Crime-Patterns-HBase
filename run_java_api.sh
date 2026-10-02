#!/bin/bash
# ==============================================================================
# Script: run_java_api.sh
# Purpose: Compiles and executes HBase Java API Operations (CRUD + Filters)
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

echo "[✓] HBase binary found: $(which hbase)"

echo ""
echo "=================================================================="
echo " [2/3] Compiling HBaseCrimeOperations.java"
echo "=================================================================="
mkdir -p classes
javac -cp "$(hbase classpath)" -d classes src/bigdata/HBaseCrimeOperations.java
echo "[✓] Compilation successful."

echo ""
echo "=================================================================="
echo " [3/3] Executing HBase Java API Program"
echo "=================================================================="
HBASE_CLASSPATH=classes hbase bigdata.HBaseCrimeOperations

echo ""
echo "=================================================================="
echo " [SUCCESS] HBase Java API Demonstration Finished!"
echo "=================================================================="
