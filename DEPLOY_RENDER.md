# Deploy Smart Hostel on Render

This project is split into a React static site and a Spring Boot web service. Deploy the backend first, then the frontend.

## 1. Push this folder to a GitHub repository

Upload the contents of `OOM_p` to a GitHub repository. Do not upload `node_modules`, `target`, or local database files.

## 2. Create the backend service

In Render, choose **New + → Web Service** and connect the repository. Set:

- **Root Directory:** `smart-hostel-backend`
- **Runtime:** Java (if offered) or Docker not required; use the commands below
- **Build Command:** `mvn clean package -DskipTests`
- **Start Command:** `java -jar target/smarthostel-1.0.0.jar`

Add a persistent disk to the backend service:

- **Mount Path:** `/var/data`
- Choose a disk size suitable for your demo.

Add these environment variables to the backend service:

| Key | Value |
|---|---|
| `DB_FILE` | `/var/data/hosteldb` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:4173` initially |

Render supplies `PORT` automatically; the Spring Boot configuration reads it. After deployment, copy the backend's public `https://...onrender.com` URL.

## 3. Create the frontend static site

Choose **New + → Static Site** and connect the same repository. Set:

- **Root Directory:** `smart-hostel-frontend`
- **Build Command:** `npm ci && npm run build`
- **Publish Directory:** `dist`

Add this environment variable before the first build:

| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | Your backend URL, e.g. `https://your-backend.onrender.com` |

Do not add `/api` unless you want to; the frontend adds it automatically.

For client-side routing, add a Static Site rewrite if needed:

- **Source:** `/*`
- **Destination:** `/index.html`
- **Action:** `Rewrite`

After the frontend is deployed, copy its public URL.

## 4. Allow the frontend origin in the backend

Return to the backend service's environment variables and set `CORS_ALLOWED_ORIGINS` to the exact frontend origin, for example:

`https://your-frontend.onrender.com`

You can include multiple comma-separated origins if required. Save and redeploy the backend.

## 5. Test

1. Open the frontend public URL.
2. Sign in using demo accounts already present in the project or create accounts through the app's supported flow.
3. Test room listing/allocation, visitor records, notices and mess features.
4. Restart/redeploy the backend and verify that demo data remains on the persistent disk.

## Important limitations and safety

- H2 is retained for this academic demo. `DB_FILE` points it at Render's persistent disk. Do not run multiple backend instances against this single H2 file.
- Render's free web services may sleep when idle and can take time to wake. Persistent disks and service pricing/availability depend on the current Render plan.
- This code's current role check uses the `X-User-Id` request header. That is not production-grade authentication. Anyone able to alter requests may impersonate a user. Use only fictional demo data; do not expose real student, visitor, or payment information.
- Keep mail and Razorpay credentials out of GitHub. Configure `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `RAZORPAY_KEY_ID`, and `RAZORPAY_KEY_SECRET` only as private service environment variables if you need those optional integrations.

## Local development remains unchanged

The frontend defaults to `/api`, and `vite.config.js` continues proxying `/api` to `http://localhost:8080`. Run Spring Boot locally as before, then run `npm run dev` in the frontend folder.
