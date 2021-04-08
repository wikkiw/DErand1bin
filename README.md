# DErand1bin - CEC2020

### Tohle je testovací projekt pro DP Bc. Babáka.

Stačí celý repozitář stáhnout, rozbalit, případně (nemělo by to být potřeba) zkompilovat ze složky src/main/java:

`javac cz/viktorin/derand1bin/DErand1bin.java`

A pak spouštět ze složky src/main/java:

`java cz/viktorin/derand1bin/DErand1bin [prefix] [f] [dim] [fes] [runID]`

Kde platí následující nastavení:
- `[prefix]` je textový prefix vzniklého souboru, např. název algoritmu
- `[f]` je id volané funkce, povolený rozsah 1-10
- `[dim]` je dimenze problému, povolené hodnoty 5, 10, 15 a 20
- `[fes]` je počet ohodnocení účelové funkce (pro dim 5 - 50 000, 10 - 1 000 000, 15 - 3 000 000 a 20 - 10 000 000)
- `[runID]` je ID běhu, které slouží k unikátní identifikaci výsledku

Příklad volání:

`java cz/viktorin/derand1bin/DErand1bin DErand 3 10 10000 1`

zavolá optimalizaci 3. funkce z benchmarku CEC2020 v 10 dimenzích s ukončovacím kritériem 10000 ohodnocení účelové funkce. Vytvoří v kořenovém adresáři soubor s výsledkem:
> DErandCEC2020_f3_d10_run-1.txt

## Java RMI

> Metoda getResult
