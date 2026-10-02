#!/bin/bash
# ==============================================================================
# Script: run_hbase_shell.sh
# Purpose: Helper script to launch interactive HBase shell or run automated test puts
# ==============================================================================

# Auto-detect HBase location in ~/BigData/ if not already in PATH
if ! command -v hbase &> /dev/null; then
    for candidate in "$HOME/BigData/HBase" "$HOME/HBase" "/usr/local/hbase" "/opt/hbase"; do
        if [ -d "$candidate" ] && [ -f "$candidate/bin/hbase" ]; then
            export HBASE_HOME="$candidate"
            export PATH="$PATH:$HBASE_HOME/bin"
            break
        fi
    done
fi

if [ "$1" == "--batch-sample" ]; then
    echo "[*] Executing sample puts from dataset/sample_puts.hbase into HBase Shell..."
    hbase shell dataset/sample_puts.hbase
elif [ "$1" == "--all" ]; then
    echo "[*] Executing full commands script hbase_commands.txt into HBase Shell..."
    hbase shell hbase_commands.txt
else
    echo "=================================================================="
    echo " Launching Interactive Apache HBase Shell"
    echo " Type 'help' for syntax or paste commands from hbase_commands.txt"
    echo " Type 'exit' to quit the shell"
    echo "=================================================================="
    hbase shell
fi
