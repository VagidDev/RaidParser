#!/usr/bin/env bash

# Настройки приложения
APPLICATION="raid_parser-5.0.jar"
CONFIG_FILE=".config.yaml"

# Переход в директорию скрипта
cd "$(dirname "$0")" || exit 1

# Проверка наличия JAR-файла
if [ ! -f "$APPLICATION" ]; then
    echo "Ошибка: Файл $APPLICATION не найден!"
    exit 1
fi

# Проверка наличия конфигурационного файла
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Ошибка: Конфигурационный файл $CONFIG_FILE не найден!"
    exit 1
fi

echo "Запуск интерактивного приложения $APPLICATION..."
echo "Для остановки нажмите Ctrl + C"
echo "----------------------------------------"

# Запуск Java в интерактивном режиме
java -jar "$APPLICATION" --spring.config.location="$CONFIG_FILE"
