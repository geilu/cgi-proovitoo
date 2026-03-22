# cgi-proovitoo

Restorani broneerimissüsteem Spring Boot backendi ja React + TypeScript frontendiga.

## Eeldused

- Java 25 JDK
- Node.js 18+ ja npm

---

## Käivitamine

### 1. Klooni repo ja liigu kausta

```bash
git clone https://github.com/geilu/cgi-proovitoo
cd cgi-proovitoo
```

### 2. Käivita backend

```bash
cd backend
./gradlew bootRun        # Linux / macOS
gradlew.bat bootRun      # Windows
```

Backend jookseb **http://localhost:8080** peal. Andmebaasi sisestatakse demo andmed automaatselt esimesel jooksutamisel.

### 3. Käivita frontend

**Teise terminali peal:**

```bash
cd frontend
npm install
npm run dev
```

### 4. Ava rakendus

Browseris mine lingile **http://localhost:5173**.

----
### API Dokumentatsioon

API dokumentatsiooniks on kasutatud **Swaggerit**. See on saadaval peale backendi käivitamist lingil **http://localhost:8080/swagger-ui/index.html#/**