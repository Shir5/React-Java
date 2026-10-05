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

Показатель `allocated` — приблизительное число байтов, выделенных текущим потоком во время расчёта. Он измеряется через `ThreadMXBean` отдельно от интервала времени и не означает, что эти байты остались заняты в куче после расчёта. Если JVM не поддерживает такое измерение или запрещает его, вместо числа выводится `n/a`.

Пример одного запуска:

```text
Productivity activity statistics benchmark

Collection size: 5,000
Sessions: 5,000; minutes: 807,712; earned coins: 1,853,906
for loop                           start=32948806774583 ns, finish=32948807336916 ns, elapsed=0.562 ms, allocated=72 bytes
Stream API, standard collectors    start=32948807938416 ns, finish=32948808399875 ns, elapsed=0.461 ms, allocated=784 bytes
Stream API, custom collector       start=32948808608666 ns, finish=32948808881500 ns, elapsed=0.273 ms, allocated=248 bytes
Stream API, teeing collector       start=32948809038833 ns, finish=32948809363875 ns, elapsed=0.325 ms, allocated=784 bytes

Collection size: 50,000
Sessions: 50,000; minutes: 8,037,332; earned coins: 18,363,067
for loop                           start=32948909033416 ns, finish=32948911959333 ns, elapsed=2.926 ms, allocated=72 bytes
Stream API, standard collectors    start=32948911982916 ns, finish=32948916313833 ns, elapsed=4.331 ms, allocated=784 bytes
Stream API, custom collector       start=32948916337375 ns, finish=32948918843583 ns, elapsed=2.506 ms, allocated=248 bytes
Stream API, teeing collector       start=32948918848416 ns, finish=32948921811250 ns, elapsed=2.963 ms, allocated=784 bytes

Collection size: 250,000
Sessions: 250,000; minutes: 40,322,527; earned coins: 92,438,182
for loop                           start=32949186874916 ns, finish=32949200066583 ns, elapsed=13.192 ms, allocated=40 bytes
Stream API, standard collectors    start=32949200078708 ns, finish=32949221015500 ns, elapsed=20.937 ms, allocated=784 bytes
Stream API, custom collector       start=32949221024458 ns, finish=32949234124125 ns, elapsed=13.100 ms, allocated=248 bytes
Stream API, teeing collector       start=32949234135708 ns, finish=32949248736291 ns, elapsed=14.601 ms, allocated=784 bytes
```

Это единичный учебный замер, а не точное сравнение производительности. Его значения меняются при повторных запусках.
