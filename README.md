# Totem Shop

A high-performance client-side Fabric mod for Minecraft 26.x designed for fast, single-key automated totem purchases on economy and SMP servers.

---

### Direct Download

* **Latest Release**: [Download `totemshop-1.0.0.jar`](https://github.com/bruhslow/totemshop/releases/latest)
* **Repository Build**: [`build/libs/totemshop-1.0.0.jar`](build/libs/totemshop-1.0.0.jar)

---

### Overview

| Detail | Specification |
| :--- | :--- |
| **Platform** | Fabric (Client-Side) |
| **Minecraft Version** | 26.2 (26.x) |
| **Java Version** | 25 |
| **Dependencies** | Fabric Loader (>= 0.15.0), Fabric API |
| **License** | MIT |

---

### Key Features

* **Single-Key Automated Purchase**: Runs your server's shop purchase command and confirms the transaction with one hotkey press.
* **Anti-Cheat Safe Delay**: Configurable click delay (0 to 20 ticks) ensures smooth operation without triggering server anti-cheat or rate limits.
* **Automated Menu Close**: Closes shop menus automatically once the transaction is finished.
* **Visual Settings GUI**: Intuitive configuration screen with live status, delay sliders, and keybind settings.
* **Audio & Chat Alerts**: Distinct audio cues and chat notifications when purchases succeed.

---

### Commands

| Command | Arguments | Description |
| :--- | :--- | :--- |
| `/totemshop` | None | Opens the graphical configuration menu |
| `/totemshop buy` | None | Triggers the automated buy routine manually |
| `/totemshop status` | None | Displays current configuration and purchase counters |
| `/totemshop command` | `<cmd>` | Sets the shop purchase command |
| `/totemshop delay` | `<ticks>` | Adjusts click delay between 0 and 20 ticks |
| `/totemshop key` | `<combo>` | Rebinds the shortcut hotkey (default: `V`) |

---

### Quick Start

1. Download **`totemshop-1.0.0.jar`** and place it in `.minecraft/mods` along with Fabric Loader and Fabric API.
2. Launch Minecraft and press **`V`** or run `/totemshop` in chat to open configuration.
3. Configure the purchase command for your server.
4. Press **`V`** whenever you need to purchase totems instantly.

---

### Building from Source

Prerequisites: JDK 25.

```bash
# Clone the repository
git clone https://github.com/bruhslow/totemshop.git

# Build the mod archive
./gradlew build
```

Compiled JARs will be generated in `build/libs/`.

---

### License

This project is licensed under the MIT License.
