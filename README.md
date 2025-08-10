# 🏦 BMA BANK

**BMA BANK** is a desktop banking management application built using **Java**, **JavaFX** for the user interface, and **SQLite** as the database.  
The system provides essential banking operations in a secure and user-friendly way.

---

## 👥 Team Members

- **Mazen Ahmed**
- **Mahmoud Nasser**
- **Ahmed Hussein**
- **Mohamed Amgad**
- **Ibrahim Abdel Mohsen**

---

## 📌 Features

- Create and manage customer accounts.
- Secure deposit and withdrawal transactions.
- Transfer money between accounts.
- View transaction history.
- User-friendly GUI built with JavaFX.
- Local database using SQLite.
- **Integrated Chatbot** to answer customer queries automatically.

---

## 🛠️ Technologies Used

- **Programming Language:** Java
- **GUI Framework:** JavaFX
- **Database:** SQLite
- **Tools:** Git, GitHub, Scene Builder

---

## 🏗️ Architecture

The project is built with a **3-layer architecture**:

1. **Presentation Layer (JavaFX UI)** – Handles user interface and user interactions.
2. **Business Logic Layer (Java Classes)** – Processes requests, applies rules, and manages the flow of data.
3. **Data Access Layer (SQLite)** – Handles database operations such as insert, update, delete, and queries.

### ERD (Entity Relationship Diagram)

## 🚀 How to Run the Project

1. *Clone the Repository*
   ```bash
   git clone https://github.com/username/BMA-BANK.git
   cd BMA-BANK
   
2. Open the project in IntelliJ IDEA or Eclipse.
3. Download and add the SQLite JDBC Driver (sqlite-jdbc.jar) to the project classpath.
4. Download and add JavaFX SDK (all JAR files from the lib folder) to the project classpath.
5. Configure VM Options:--module-path "path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml  

