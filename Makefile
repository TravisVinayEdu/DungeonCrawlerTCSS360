JAVAC ?= javac
JAVA ?= java

JAVA_RELEASE := 21
MAIN_CLASS := dungeoncrawler.controller.DungeonCrawler
BUILD_DIR := out/production/DungeonCrawlerTCSS360
SQLITE_JAR := lib/sqlite-jdbc-3.53.1.0.jar
CLASSPATH := $(BUILD_DIR):$(SQLITE_JAR)
SOURCES := $(shell find src -name '*.java')

.PHONY: all compile run clean

all: compile

compile:
	mkdir -p $(BUILD_DIR)
	$(JAVAC) --release $(JAVA_RELEASE) -cp $(SQLITE_JAR) -d $(BUILD_DIR) $(SOURCES)

run: compile
	$(JAVA) --enable-native-access=ALL-UNNAMED -cp $(CLASSPATH) $(MAIN_CLASS)

clean:
	rm -rf out
