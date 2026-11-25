# react-frontend

Web UI for the microservice project (customers, products, categories, orders).
It logs in with **Keycloak** (OAuth 2.0 Authorization Code + PKCE) and calls the services **through the API Gateway**.

**Stack:** React 19 · TypeScript · Vite · React Router · TanStack Query · react-hook-form · react-oidc-context (oidc-client-ts) · Vitest + Testing Library

---

## 1. Run it

### Prerequisites
- Node.js 20+ and npm
- The backend running (Keycloak `:8180`, Config Server, Eureka, the services, and the **API Gateway `:8080`**)
- The Keycloak realm `Learn_Microservices` with the roles and users from [docs/oauth-keycloak-guide.md](../docs/oauth-keycloak-guide.md)

### Step 1: create the Keycloak client (once)

The browser app is a **public client**: it has **no secret** (a browser cannot keep a secret). Security comes from PKCE and from the exact redirect URI.

Easiest way, with the Admin REST API (uses [keycloak/learn-microservices-web.json](keycloak/learn-microservices-web.json)):

```bash
KC=http://localhost:8180
TOKEN=$(curl -s -X POST $KC/realms/master/protocol/openid-connect/token \
  -d grant_type=password -d client_id=admin-cli -d username=admin -d password=admin \
  | sed -E 's/.*"access_token":"([^"]+)".*/\1/')

curl -i -X POST $KC/admin/realms/Learn_Microservices/clients \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d @keycloak/learn-microservices-web.json
```

Expected: `201 Created`. Or create it by hand in the admin console:

| Setting | Value |
|---|---|
| Client ID | `learn-microservices-web` |
| Client authentication | **Off** (public client) |
| Standard flow | On |
| Direct access grants | Off |
| Valid redirect URIs | `http://localhost:5173/*` |
| Web origins | `http://localhost:5173` |
| Valid post logout redirect URIs | `http://localhost:5173/*` |
| Proof Key for Code Exchange (Advanced tab) | `S256` |

### Step 2: install and start

```bash
cd react-frontend
npm install
npm run dev
```

Open <http://localhost:5173>. You see a landing page. Click **Log in**, you go to Keycloak, you log in (for example `admin.user` / `password123`), and you come back to the dashboard.

### Scripts

| Command | What it does |
|---|---|
| `npm run dev` | Dev server with hot reload on port 5173 |
| `npm run build` | Typecheck + production build into `dist/` |
| `npm run preview` | Serve the production build locally |
| `npm run typecheck` | TypeScript check only |
| `npm run lint` | Lint with oxlint |
| `npm test` | Run unit tests once (`npm run test:watch` to watch) |

### Environment variables

Copy [.env.example](.env.example). `.env.development` is already provided.

| Variable | Meaning |
|---|---|
| `VITE_KEYCLOAK_URL` | Keycloak base URL |
| `VITE_KEYCLOAK_REALM` | Realm name |
| `VITE_KEYCLOAK_CLIENT_ID` | The public client above |
| `VITE_API_BASE_URL` | Gateway URL. **Empty in development** (see the proxy below) |

> Anything starting with `VITE_` is visible in the browser. Never put secrets here.

---

## 2. How login works in this app

```
Browser                    Keycloak                  API Gateway -> services
   | 1. open /orders, click "Log in" |                       |
   | (not logged in)          |                              |
   |--- 2. redirect + PKCE -->|                              |
   |<-- 3. login page --------|                              |
   |--- 4. username/password ->                              |
   |<-- 5. redirect /auth/callback?code=... --               |
   |--- 6. code + PKCE verifier ->                           |
   |<-- 7. access token (JWT) + refresh token --             |
   |--- 8. GET /api/v1/orders  (Authorization: Bearer <token>) -->
   |<-- 9. data (or 401 / 403) ------------------------------|
```

- **PKCE** (steps 2 and 6) proves that the app that started the login is the one finishing it, so a stolen `code` is useless.
- The app **never sees the password**. Keycloak shows the login page.
- Tokens renew automatically before they expire (`automaticSilentRenew`).
- Tokens are kept in `sessionStorage` (cleared when the tab closes).
- If any API call returns **401**, the app clears the session and starts the login again.

### Roles in the UI

The access token contains `realm_access.roles`. The UI reads them (`src/auth/roles.ts`) to **show or hide** things:
menu items, "New", "Edit" and "Delete" buttons, and page routes (`RequirePermission`).

> **This is only for a nicer experience.** Anyone can change what the browser shows. The real protection is in the services (`@PreAuthorize`), which return **403** even if the UI is bypassed.

Creating an order also needs `CUSTOMER_VIEW` and `PRODUCT_VIEW`, because the order form loads customers and products, and the order service reads them on your behalf.

### Why the Vite proxy, and what about CORS?

In development `vite.config.ts` forwards `/api/*` to `http://localhost:8080`. The browser thinks it talks to its own origin, so **no CORS** is needed on the gateway.

For production, either serve the built files and the API from the **same host** (a reverse proxy such as nginx), or set `VITE_API_BASE_URL` to the gateway URL and **enable CORS on the gateway** for your site's origin.

---

## 3. Project structure

```
src/
  main.tsx                 entry point
  app/                     Providers (auth, query cache, toasts) and the router
  auth/                    Keycloak login: userManager, route guards, roles, permissions
  api/                     fetch wrapper (adds the token), ApiError, query client
  components/              reusable UI (Button, Pagination, ConfirmDialog, Toast ...)
  layouts/                 app shell (top bar + menu)
  pages/                   dashboard, not found, crash page
  features/                one folder per business area
    customers/             types · customerApi · customerHooks · pages
    products/              types · productApi · productHooks · pages
    categories/            ...
    orders/                ... + orderStatus (allowed status changes)
  lib/                     small helpers (formatting, form errors)
  styles/global.css        design tokens (light and dark) and styles
```

**Why this layout:** each feature owns its API calls, hooks and pages (easy to find and to delete). Pages never call `fetch` directly. They use hooks, and hooks use the `*Api` file, which uses the shared `http` client. So the token handling and error handling exist in exactly one place.

### Conventions
- Server data lives in **TanStack Query** (caching, loading and error states, refetch after changes). Forms use **react-hook-form**.
- Backend validation messages (`errors` map) are shown under the matching form field.
- Route pages are **lazy loaded** (smaller first download).
- `@/` is an alias for `src/`.
- Permission names in `src/auth/permissions.ts` mirror the backend `Permissions.java`.

---

## 4. Troubleshooting

| Problem | Cause / fix |
|---|---|
| Keycloak shows **"Invalid parameter: redirect_uri"** | The client's redirect URI list does not contain `http://localhost:5173/*`. Check Step 1 and the exact host and port. |
| Login works, then **CORS error** on the token request | Add `http://localhost:5173` to the client's **Web origins**. |
| Blank page and "Missing environment variable" | Create `.env.development` from `.env.example` and restart `npm run dev`. |
| Everything returns **401** | The token's issuer must equal the services' `issuer-uri` (use `localhost` in both). Restart the Config Server and services after changing it. |
| A page says **Access denied** | The user lacks that role. Add it in Keycloak and **log out and in again** (roles are inside the token). |
| `/api` calls return **502/504/404** from the dev server | The API Gateway is not running on port 8080, or it is an old build without the security changes. |
| Redirected to login again and again | The access token is rejected by the gateway (clock or issuer problem). Check the gateway log. |

---

## 5. What is not done yet

- Stock-only and status-only product updates (the product form edits both).
- Showing the customer's name on orders (the order API returns only the customer id).
- Component tests for the forms and a Playwright end-to-end test against a real Keycloak.
- A production Dockerfile / nginx config.
