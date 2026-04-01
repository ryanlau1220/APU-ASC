#!/bin/bash

SRC_DIR="src/main/java"
RES_DIR="src/main/resources"
OUT_DIR="target"
MAIN_CLASS="com.apu.asc.Main"
SCRIPTS_DIR="scripts"

mkdir -p $SCRIPTS_DIR

case "$1" in
    build)
        echo "Building project..."
        mkdir -p $OUT_DIR
        find $SRC_DIR -name "*.java" > $SCRIPTS_DIR/sources.txt
        javac -d $OUT_DIR @$SCRIPTS_DIR/sources.txt > $SCRIPTS_DIR/compile_output.txt 2>&1
        BUILD_STATUS=$?
        
        if [ $BUILD_STATUS -eq 0 ]; then
            echo "Copying resources..."
            if [ -d "$RES_DIR" ]; then
                cp -r $RES_DIR/* $OUT_DIR/ 2>/dev/null || true
            fi
            echo "Build complete."
        else
            echo "Build failed. Check $SCRIPTS_DIR/compile_output.txt for details."
            cat $SCRIPTS_DIR/compile_output.txt
            exit 1
        fi
        ;;
    run)
        echo "Running project..."
        java -cp $OUT_DIR $MAIN_CLASS
        ;;
    clean)
        echo "Cleaning project..."
        rm -rf $OUT_DIR
        rm -f $SCRIPTS_DIR/sources.txt $SCRIPTS_DIR/compile_output.txt
        echo "Clean complete."
        ;;
    verify)
        echo "Verifying project (compilation check)..."
        mkdir -p $OUT_DIR
        find $SRC_DIR -name "*.java" > $SCRIPTS_DIR/sources.txt
        javac -d $OUT_DIR @$SCRIPTS_DIR/sources.txt > $SCRIPTS_DIR/compile_output.txt 2>&1
        if [ $? -eq 0 ]; then
            echo "Verification passed."
        else
            echo "Verification failed. Check $SCRIPTS_DIR/compile_output.txt for details."
            cat $SCRIPTS_DIR/compile_output.txt
            exit 1
        fi
        ;;
    format)
        echo "Removing unused imports..."
        if [ -f "$SCRIPTS_DIR/remove_unused_imports.py" ]; then
            python3 $SCRIPTS_DIR/remove_unused_imports.py
            echo "Unused imports removed."
        else
            echo "Format script not found."
        fi
        ;;
    all)
        $0 clean
        $0 build
        $0 run
        ;;
    *)
        echo "Usage: ./manage.sh {build|run|clean|verify|format|all}"
        exit 1
        ;;
esac
