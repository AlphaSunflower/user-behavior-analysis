@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0..\..\kafka-spark"
echo ============================================================
echo   Spark Streaming - Real-time ETL + Compute + MySQL Write
echo   Kafka: 192.168.100.140:9092
echo   MySQL: localhost:3306
echo ============================================================
if exist checkpoint (
    echo Cleaning old checkpoint...
    rmdir /s /q checkpoint
)
sbt "runMain streaming.UserBehaviorStreaming"
pause
