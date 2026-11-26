# Анализатор репозитория

## Коммиты и CI

- **Монолит:** https://github.com/GorbatovNikita/kfd-gorbatov/commit/4fba2bb024ddc79878b966c54985edc54de96cc2
- **Модули:** https://github.com/GorbatovNikita/kfd-gorbatov/commit/4fba2bb024ddc79878b966c54985edc54de96cc2
- **Успешный прогон CI:** https://github.com/GorbatovNikita/kfd-gorbatov/actions/runs/45802065384

## Запуск анализатора

Команда запуска:

```bash
./gradlew :cli:run --args="/tmp/repo-analyzer-example"
```

Пример выполняется на каталоге из четырёх файлов:

- `Alpha.kt`
- `Beta.kt`
- `build.gradle.kts`
- `README.md`

Результат:

```text
Статистика по расширениям:
kt: файлов=2, строк=5
kts: файлов=1, строк=4
md: файлов=1, строк=5

Три самых длинных файла:
README.md: 5 строк
build.gradle.kts: 4 строк
Alpha.kt: 3 строк
```

## Эксперимент со сборкой

Сначала была выполнена первоначальная сборка:

```bash
./gradlew build --console=plain
```

Затем в файле `core/src/main/kotlin/kfd/RepositoryAnalyzer.kt` была изменена реализация функции `countLines`.

Вместо подсчёта строк через `useLines` использована операция `readLines().size`:

```kotlin
private fun countLines(file: File): Int =
    file.readLines().size
```

Изменение сохраняет публичные сигнатуры и результат работы программы.

После изменения повторно выполнена сборка без очистки:

```bash
./gradlew build --console=plain
```

Результаты повторной сборки:

| Задача | Результат |
|---|---|
| `:core:compileKotlin` | Выполнена повторно |
| `:core:jar` | Пересобран |
| `:cli:compileKotlin` | UP-TO-DATE |
| `:core:compileTestKotlin` | UP-TO-DATE |
| `:core:test` | Все 5 тестов PASSED |

Итог: `BUILD SUCCESSFUL in 1s`, 9 задач: 6 выполнено, 3 актуальны.

**Вывод:** Gradle обнаружил изменение реализации в модуле `core` и повторно выполнил его компиляцию. Компиляция `cli` не потребовалась, поскольку публичные сигнатуры `core` не изменились. Компиляция тестов также не потребовалась, а сами тесты успешно выполнились.