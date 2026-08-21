#!/usr/bin/env bash

# Настройки приложения
APPLICATION="raid_parser-5.0.jar"
CONFIG_FILE=".config.yaml"

# Режим по умолчанию: интерактивная консоль.
# Серверный режим с REST API: ./run.sh -d
MODE="${1:--i}"

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

case "$MODE" in
    -d|--daemon)
        echo "Запуск $APPLICATION в серверном режиме (REST API под /api/v1)..."
        ;;
    *)
        echo "Запуск интерактивного приложения $APPLICATION..."
        echo "Для остановки нажмите Ctrl + C"
        ;;
esac
echo "----------------------------------------"

# Остальные аргументы (например --server.port=9090) передаются как есть
shift 2>/dev/null
java -jar "$APPLICATION" "$MODE" "$@" --spring.config.location="$CONFIG_FILE"
