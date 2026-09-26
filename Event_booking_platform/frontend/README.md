# Event Booking Frontend

React, TypeScript, and Vite frontend for EventBooking. The home page and public event browser are implemented. Authentication, event details, and role-specific workspaces are upcoming steps.

## Requirements

- Node.js 20.19 or newer
- npm
- Backend running at http://localhost:8080 for live event data

## Run locally

From the frontend directory:

    npm install
    npm run dev

Vite prints the local development URL, normally http://localhost:5173. Start the backend from the repository root with docker compose up --build. During development, Vite proxies /api requests to the backend on port 8080.

## Scripts

- npm run dev: start the development server.
- npm run build: type-check and create a production build in dist/.
- npm run preview: serve the production build locally.

The API base path defaults to /api/v1. Copy .env.example to .env.local to override VITE_API_BASE_URL when needed. A separately hosted production frontend also needs an API routing or CORS configuration.
