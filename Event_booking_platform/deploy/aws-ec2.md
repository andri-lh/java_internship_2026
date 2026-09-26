# Deploying on AWS EC2 (Docker Compose)

Runs the whole app on one small Linux server: MySQL, the API, the web frontend, and Caddy (which serves
HTTPS automatically). Only ports 80 and 443 are exposed.

## 1. Create the instance

In the EC2 console choose **Launch instance**:

| Setting | Value |
|---|---|
| Name | `eventbooking` |
| AMI | **Ubuntu Server 24.04 LTS** (64-bit x86) |
| Instance type | **t3.small** (2 GB RAM, comfortable) or **t3.micro** (1 GB, works because of the swap file the setup script adds) |
| Key pair | Create one and download the `.pem` file (you need it to log in) |
| Storage | 20 GB gp3 |
| Network / security group | Allow **SSH (22) from My IP**, **HTTP (80)** and **HTTPS (443)** from anywhere |

Then allocate an **Elastic IP** (EC2 > Elastic IPs > Allocate) and associate it with the instance, so the address
does not change when the instance restarts. Note the IP, for example `3.120.4.5`.

Before anything else, create a **budget alert**: Billing > Budgets > Create budget > Zero spend or a small monthly
amount, with your email.

## 2. Prepare the server (once)

```bash
ssh -i eventbooking.pem ubuntu@3.120.4.5
git clone https://github.com/<you>/<repo>.git          # private repo: see "Private repository" below
cd <repo>/Event_booking_platform
sudo bash deploy/ec2-setup.sh                           # installs Docker and adds swap
exit                                                    # log out and back in so the docker group applies
```

### Private repository

Create a fine-grained personal access token on GitHub (Settings > Developer settings) with **read-only Contents**
access to this repository only, and clone with it:

```bash
git clone https://<username>:<token>@github.com/<you>/<repo>.git
```

## 3. Configure

```bash
cd <repo>/Event_booking_platform
cp .env.prod.example .env.prod
nano .env.prod
```

Fill in at least these:

- `SITE_ADDRESS` — your IP with dashes plus `.sslip.io`, e.g. `3-120-4-5.sslip.io`. Caddy then gets a free HTTPS
  certificate automatically. (Use `:80` to test over plain HTTP first.)
- `APP_FRONTEND_BASE_URL` — `https://3-120-4-5.sslip.io` (exactly what people type in the browser).
- `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` — long random values.
- `JWT_SECRET` — `openssl rand -base64 48`.
- `BOOTSTRAP_ADMIN_*` — your first admin (the password needs upper/lower case, a number and a special character;
  avoid `#`, which some tools treat as a comment).
- `DEMO_DATA_ENABLED=true` and `DEMO_DATA_PASSWORD` for demo accounts (switch back to `false` afterwards).

`.env.prod` holds secrets and is ignored by git. Keep it that way.

## 4. Start

```bash
docker compose -f compose.prod.yaml --env-file .env.prod up -d --build
```

The first build takes 5 to 10 minutes (Maven and npm). Watch progress with:

```bash
docker compose -f compose.prod.yaml --env-file .env.prod logs -f api
```

You are ready when the log shows `Started EventBookingPlatformApplication` (and, on first run,
`Bootstrap admin created` and `Demo data created`).

## 5. Check

- `https://3-120-4-5.sslip.io` — the site loads and shows the demo events
- `https://3-120-4-5.sslip.io/swagger-ui.html` — API documentation
- Sign in as your admin, and as a demo user

The first request after startup can be slow while the certificate is issued.

## Operating it

| Task | Command |
|---|---|
| Update after `git push` | `git pull && docker compose -f compose.prod.yaml --env-file .env.prod up -d --build` |
| Status | `docker compose -f compose.prod.yaml --env-file .env.prod ps` |
| Logs | `docker compose -f compose.prod.yaml --env-file .env.prod logs -f api` |
| Restart | `docker compose -f compose.prod.yaml --env-file .env.prod restart` |
| Database backup | `docker compose -f compose.prod.yaml --env-file .env.prod exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" event_booking_db' > backup.sql` |
| Stop, keep data | `docker compose -f compose.prod.yaml --env-file .env.prod down` |

Containers restart automatically after a reboot.

## Troubleshooting

- **Site not reachable** — check the security group allows 80 and 443, and that the Elastic IP is associated.
- **HTTPS certificate error** — `SITE_ADDRESS` must exactly match the hostname you open, and ports 80 and 443 must
  be open to the internet (Let's Encrypt validates over them). Check `docker compose ... logs caddy`.
- **Login says "Invalid CORS request"** — `APP_FRONTEND_BASE_URL` must equal the URL in the browser, including
  `https://` and with no trailing slash. Edit `.env.prod`, then `up -d` again.
- **Build is killed / out of memory** — confirm swap is active (`free -h`) or use a t3.small.
- **API keeps restarting** — read `logs api`; the usual cause is a missing or invalid value in `.env.prod`.
- **Changed a `BOOTSTRAP_ADMIN_*` value but the old password still works** — the admin is created only once. Delete
  the admin row (or wipe the database volume) and restart the API.

## Cost and shutting down

A t3.small runs about $0.02 per hour plus the public IPv4 address, roughly $0.005 per hour, so a week or two costs a
few dollars and usually falls inside a new account's credits. Check the current pricing page and your Billing
dashboard. When the review is over:

1. **Terminate** the instance (EC2 > Instances > Instance state > Terminate).
2. **Release** the Elastic IP (EC2 > Elastic IPs > Release). An unattached Elastic IP is billed.
3. Delete the key pair and any leftover volumes or snapshots.
