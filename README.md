# SIC/XE Assembler

## Building/Running

Requirements:
- JDK 17

To build:
```
./gradlew build
```

To run:
```
java -jar build/libs/sic-xe-assembler-1.0-SNAPSHOT.jar INPUT_FILE_NAME
```

After building there will be an executable jar file in `build/libs/`

## Formatting

Formatting is provided by Spotless. To run the formatting task:
```
./gradlew spotlessApply
```