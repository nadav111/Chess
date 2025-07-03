# ♟️ Chess Engine with AI Bot

A **Java-based Chess game** with a graphical user interface and an intelligent AI opponent powered by a decision tree strategy.
Supports all standard chess rules: **castling, en passant, promotion, check, and checkmate**.

---

## 📂 Features

* ✅ **BitBoard-based representation** for efficient move calculation
* ✅ **Java Swing GUI**
* ✅ Supports all legal chess moves:

  * Castling (both sides)
  * En passant
  * Pawn promotion
  * Check and checkmate detection
* ✅ **AI Bot:**

  * Plays as white or black
  * Uses decision-tree strategy for opening, middlegame, and endgame
  * Avoids illegal or unsafe moves
* ✅ Move legality validation
* ✅ PGN-style move notation printed in the console for debugging

---

## 🖥️ Requirements

* **Java 8+** (tested on Java 17)
* **Git** (optional, for cloning)

---

## 🚀 Getting Started

### 📥 Clone the repository

```bash
git clone https://github.com/nadav111/Chess.git
cd Chess
```

### 🛠️ Build and Run

```bash
javac -d bin src/chess/*.java
java -cp bin chess.Main
```

Or open the project in **Visual Studio Code / IntelliJ IDEA / Eclipse**, then run `Main.java`.

---

## 🎮 How to Play

* White always starts first.
* You can play as **human vs. AI**, or adjust the bot to play as both sides.
* Make your move by clicking on a piece and then the destination square.
* The bot will automatically play its move after yours.

---

## 🧠 Bot Strategy

The AI bot follows a **three-phase decision tree**:

* **Opening:** control the center, develop pieces, castle early
* **Middlegame:** identify weak squares, activate pieces, look for tactics
* **Endgame:** push pawns, activate king, avoid stalemate

It prioritizes **king safety** and **material advantage**.

---

## 📷 Screenshots

![Screenshot](https://github.com/user-attachments/assets/58204574-8d09-473c-88b8-ff58efdaf82e)

---

## 📄 License

```text
Copyright © 2025 NadavB. All Rights Reserved.

This project and its source code are the intellectual property of NadavB.

Unauthorized copying, distribution, modification, or publication of any part of this project, in whole or in part, without explicit written permission from the copyright holder is strictly prohibited.
```

For inquiries about usage rights or licensing, please contact: [majorityrules60@gmail.com](mailto:majorityrules60@gmail.com)

---
