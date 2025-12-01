# Arrowhead Backend

The Arrowhead Backend provides data for the Arrowhead web application, including energy mix forecasts and optimal charging time calculations. It fetches data from a public API and exposes it through two REST endpoints.

## Features

- **Forecast Endpoint:** Returns real-time and forecasted energy mix data for the UK (up to 2 days ahead).
- **Optimal Charging Endpoint:** Calculates the best 1–6 hour window for charging devices or EVs when clean energy production is highest.
- Fetches and processes data from a public energy API.

## Technologies Used

- **Language:** Java
- **Framework:** Spring Boot
- **Build Tool:** Gradle

## Endpoints

### `GET /api/prognosis`

Returns energy mix forecast data.

**Response Example:**

```json
{
  "date": "2025-11-30",
  "averages": [
    {
      "fuel": "biomass",
      "perc": 9.433333333333334
    },
    {
      "fuel": "coal",
      "perc": 0.0
    },
    {
      "fuel": "gas",
      "perc": 18.033333333333335
    },
    {
      "fuel": "hydro",
      "perc": 0.0
    },
    {
      "fuel": "imports",
      "perc": 12.666666666666666
    },
    {
      "fuel": "nuclear",
      "perc": 13.5
    },
    {
      "fuel": "other",
      "perc": 0.0
    },
    {
      "fuel": "solar",
      "perc": 0.0
    },
    {
      "fuel": "wind",
      "perc": 46.36666666666667
    }
  ]
}
```

### `GET /api/chargingTime?hours={hours}`

Returns energy mix forecast data.

**Response Example:**

```json
{
  "from": "2025-12-01T00:30:00Z",
  "to": "2025-12-01T06:30:00Z",
  "perc": 77.64999999999999
}
```
