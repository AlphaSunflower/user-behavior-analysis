@echo off
chcp 65001 >nul
title Spark Streaming - 实时计算引擎
echo ============================================================
echo   Spark Streaming - 实时消费 + 清洗 + 计算 + 写入 MySQL
echo   Kafka: 192.168.100.140:9092
echo   MySQL: localhost:3306
echo ============================================================

cd /d "%~dp0..\..\kafka-spark"

:: 清理上次 checkpoint（避免恢复冲突）
if exist checkpoint (
    echo 清理旧的 checkpoint 目录...
    rmdir /s /q checkpoint
)

sbt "runMain streaming.UserBehaviorStreaming"
pause
