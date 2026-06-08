@echo off
chcp 65001 >nul
title Kafka Producer - 用户行为日志生成器
echo ============================================================
echo   Kafka Producer - 模拟用户行为日志生成
echo   目标: 192.168.100.140:9092
echo ============================================================
cd /d "%~dp0..\..\kafka-producer"
sbt "runMain producer.KafkaProducerApp"
pause
