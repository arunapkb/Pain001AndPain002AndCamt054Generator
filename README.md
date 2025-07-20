# 💳 Pain001AndPain002AndCamt054Generator

A Java-based tool to generate and validate SEPA-compliant XML files — specifically **Pain.001**, **Pain.002**, and **Camt.054** formats. Designed for financial institutions and payment processors who require batch-ready XML generation with validation support.

---

## ✨ Features

- ✅ Generate `Pain.001`, `Pain.002`, `Camt.054` payment XMLs
- ✅ XML Schema validation included
- 🔁 Batch processing support
- 📂 Easy configuration of inputs/outputs
- 📦 Lightweight and deployable JAR

---

## 📦 Build & Run

### Requirements

- Java 17+
- Maven

### Steps

```bash
git clone https://github.com/arunktietoevry/Pain001AndPain002AndCamt054Generator.git
cd Pain001AndPain002AndCamt054Generator
mvn clean package
java -jar target/pain-xml-generator.jar



