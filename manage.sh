#!/bin/bash

SRC_DIR="src/main/java"
RES_DIR="src/main/resources"
OUT_DIR="out"
MAIN_CLASS="com.apu.asc.Main"

case "$1" in
    build)
        echo "Building project..."
        mkdir -p $OUT_DIR
        find $SRC_DIR -name "*.java" > sources.txt
        javac -d $OUT_DIR @sources.txt
        BUILD_STATUS=$?
        rm sources.txt
        
        if [ $BUILD_STATUS -eq 0 ]; then
            echo "Copying resources..."
            if [ -d "$RES_DIR" ]; then
                cp -r $RES_DIR/* $OUT_DIR/ 2>/dev/null || true
            fi
            echo "Build complete."
        else
            echo "Build failed."
            exit 1
        fi
        ;;
    run)
        echo "Running project..."
        java -cp $OUT_DIR $MAIN_CLASS
        ;;
    clean)
        echo "Cleaning project..."
        rm -rf $OUT_DIR target
        echo "Clean complete."
        ;;
    verify)
        echo "Verifying project (compilation check)..."
        mkdir -p $OUT_DIR
        find $SRC_DIR -name "*.java" > sources.txt
        javac -d $OUT_DIR @sources.txt
        if [ $? -eq 0 ]; then
            echo "Verification passed."
        else
            echo "Verification failed."
            exit 1
        fi
        rm sources.txt
        ;;
    all)
        $0 clean
        $0 build
        $0 run
        ;;
    *)
        echo "Usage: ./manage.sh {build|run|clean|verify|all}"
        exit 1
        ;;
esac
