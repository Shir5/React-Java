Запуск из каталога `demo`:

```sh
./mvnw clean -DskipTests package
java -cp target/classes com.reactjava.shir.ProductivityLabApplication
```

В программе сравниваются четыре варианта расчёта одинаковой статистики:

- цикл `for` — один проход по коллекции;
- стандартные коллекторы `counting()` и `summingLong()` — три прохода, по одному на показатель;
- собственный коллектор — один проход;
- стандартные коллекторы, объединённые через `Collectors.teeing` — один проход.

`teeing` передаёт каждую сессию трём стандартным коллекторам в одном конвейере. Он считает количество, оплачиваемые минуты и монеты, после чего объединяет суммы в `ActivityStatistics`. Один проход сам по себе не гарантирует самое короткое время: на замер влияют накладные расходы Stream API и JVM.

Перед измерениями выполняются пять прогревочных раундов. Затем каждый способ запускается 30 раз на том же списке, а порядок методов перемешивается в каждом раунде. Вывод содержит медиану, минимум, максимум и стандартное отклонение времени, а также медианы CPU-времени и выделенной памяти.

Показатель `allocated` — приблизительное число байтов, выделенных текущим потоком за один расчёт. Он измеряется через `ThreadMXBean` отдельно от времени и не означает, что эти байты остались заняты в куче после расчёта. Если JVM не поддерживает измерение или запрещает его, выводится `n/a`.

Пример одного запуска:

```text
Productivity activity statistics benchmark

Collection size: 5,000
Sessions: 5,000; minutes: 807,712; earned coins: 1,853,906
for loop                           runs=30, median=0.255 ms, min=0.186 ms, max=0.563 ms, sd=0.134 ms, CPU=0.259 ms, allocated=72 bytes
Stream API, standard collectors    runs=30, median=0.304 ms, min=0.298 ms, max=0.447 ms, sd=0.040 ms, CPU=0.306 ms, allocated=784 bytes
Stream API, custom collector       runs=30, median=0.197 ms, min=0.194 ms, max=0.297 ms, sd=0.033 ms, CPU=0.199 ms, allocated=248 bytes
Stream API, teeing collector       runs=30, median=0.244 ms, min=0.237 ms, max=0.343 ms, sd=0.023 ms, CPU=0.246 ms, allocated=784 bytes

Collection size: 50,000
Sessions: 50,000; minutes: 8,037,332; earned coins: 18,363,067
for loop                           runs=30, median=2.298 ms, min=2.198 ms, max=2.586 ms, sd=0.106 ms, CPU=2.301 ms, allocated=40 bytes
Stream API, standard collectors    runs=30, median=3.799 ms, min=3.512 ms, max=4.644 ms, sd=0.217 ms, CPU=3.789 ms, allocated=784 bytes
Stream API, custom collector       runs=30, median=2.357 ms, min=2.214 ms, max=2.639 ms, sd=0.119 ms, CPU=2.349 ms, allocated=248 bytes
Stream API, teeing collector       runs=30, median=2.737 ms, min=2.589 ms, max=3.121 ms, sd=0.132 ms, CPU=2.728 ms, allocated=784 bytes

Collection size: 250,000
Sessions: 250,000; minutes: 40,322,527; earned coins: 92,438,182
for loop                           runs=30, median=9.920 ms, min=9.813 ms, max=10.115 ms, sd=0.069 ms, CPU=9.904 ms, allocated=40 bytes
Stream API, standard collectors    runs=30, median=15.720 ms, min=15.519 ms, max=15.969 ms, sd=0.125 ms, CPU=15.678 ms, allocated=784 bytes
Stream API, custom collector       runs=30, median=10.099 ms, min=9.943 ms, max=10.463 ms, sd=0.131 ms, CPU=10.081 ms, allocated=248 bytes
Stream API, teeing collector       runs=30, median=12.022 ms, min=11.824 ms, max=12.659 ms, sd=0.198 ms, CPU=11.999 ms, allocated=784 bytes
```
