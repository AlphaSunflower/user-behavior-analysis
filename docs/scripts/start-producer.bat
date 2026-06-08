@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0..\..\kafka-producer"
echo ============================================================
echo   Kafka Producer - User Behavior Log Generator
echo   Target: 192.168.100.140:9092
echo ============================================================
sbt "runMain producer.KafkaProducerApp"
pause
