JAVAC ?= javac
JAVA ?= java

JAVA_RELEASE := 21
MAIN_CLASS := dungeoncrawler.controller.DungeonCrawler
BUILD_DIR := out/production/DungeonCrawlerTCSS360
TEST_BUILD_DIR := out/test/DungeonCrawlerTCSS360
SQLITE_JAR := lib/sqlite-jdbc-3.53.1.0.jar
CLASSPATH := $(BUILD_DIR):$(SQLITE_JAR)
SOURCES := $(shell find src -name '*.java')
TEST_SOURCES := $(shell find test -name '*.java')

# Test-only jars (JUnit 5 console launcher + Mockito and its runtime deps).
JUNIT_JAR := lib/junit-platform-console-standalone-1.14.0.jar
MOCKITO_JARS := lib/mockito-core-5.20.0.jar:lib/byte-buddy-1.17.8.jar:lib/byte-buddy-agent-1.17.8.jar:lib/objenesis-3.3.jar
TEST_CLASSPATH := $(BUILD_DIR):$(SQLITE_JAR):$(JUNIT_JAR):$(MOCKITO_JARS)

.PHONY: all compile run test clean

all: compile

compile:
	mkdir -p $(BUILD_DIR)
	$(JAVAC) --release $(JAVA_RELEASE) -cp $(SQLITE_JAR) -d $(BUILD_DIR) $(SOURCES)

run: compile
	$(JAVA) --enable-native-access=ALL-UNNAMED -cp $(CLASSPATH) $(MAIN_CLASS)

# Compiles the production code, then the tests, then runs the whole suite.
test: compile
	mkdir -p $(TEST_BUILD_DIR)
	$(JAVAC) --release $(JAVA_RELEASE) -cp $(TEST_CLASSPATH) -d $(TEST_BUILD_DIR) $(TEST_SOURCES)
	$(JAVA) -jar $(JUNIT_JAR) execute \
		--class-path $(TEST_BUILD_DIR):$(TEST_CLASSPATH) \
		--scan-class-path $(TEST_BUILD_DIR) \
		--details=tree --disable-banner

clean:
	rm -rf out
